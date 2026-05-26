package pl.error_handling_app.user.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import pl.error_handling_app.company.entity.Company;
import pl.error_handling_app.user.entity.User;
import pl.error_handling_app.user.entity.VerificationToken;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class VerificationTokenRepositoryTest {

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldFindVerificationTokenByTokenString() {
        //given
        Company company = createDefaultCompany();
        User user = createValidUser("user@fixaro.pl", company);
        entityManager.persist(user);

        VerificationToken token = new VerificationToken();
        token.setToken("token123");
        token.setUser(user);
        entityManager.persistAndFlush(token);

        //when
        Optional<VerificationToken> found = tokenRepository.findByToken("token123");

        //then
        assertThat(found).isPresent();
        assertThat(found.get().getUser().getEmail()).isEqualTo("user@fixaro.pl");
    }

    @Test
    void shouldDeleteTokensExpiredBeforeGivenTime() {
        //given
        LocalDateTime now = LocalDateTime.now();
        Company company = createDefaultCompany();
        User user = createValidUser("user2@fixaro.pl", company);
        entityManager.persist(user);

        VerificationToken expiredToken = new VerificationToken();
        expiredToken.setToken("expired");
        expiredToken.setExpirationTime(now.minusDays(2));
        expiredToken.setUser(user);
        entityManager.persist(expiredToken);

        VerificationToken validToken = new VerificationToken();
        validToken.setToken("valid");
        validToken.setExpirationTime(now.plusDays(1));
        validToken.setUser(user);
        entityManager.persistAndFlush(validToken);

        //when
        tokenRepository.deleteAllByExpirationTimeBefore(now);
        entityManager.flush();
        entityManager.clear();

        //then
        Iterable<VerificationToken> remainingTokens = tokenRepository.findAll();
        assertThat(remainingTokens).hasSize(1);
        assertThat(remainingTokens.iterator().next().getToken()).isEqualTo("valid");
    }
    
    private Company createDefaultCompany() {
        Company company = new Company();
        company.setName("Firma testowa");
        company.setTimeToFirstRespond(2);
        company.setTimeToResolve(24);
        return entityManager.persist(company);
    }

    private User createValidUser(String email, Company company) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName("Jan");
        user.setLastName("Kowalski");
        user.setPassword("password123");
        user.setActive(true);
        user.setCompany(company);
        return user;
    }
}