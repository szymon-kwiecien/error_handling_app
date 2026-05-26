package pl.error_handling_app.user.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import pl.error_handling_app.company.entity.Company;
import pl.error_handling_app.user.entity.User;
import pl.error_handling_app.user.entity.UserRole;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldFindUserByEmail() {
        //given
        Company company = createDefaultCompany();
        User user = createValidUser("test@fixaro.pl", company);
        entityManager.persistAndFlush(user);

        //when
        Optional<User> found = userRepository.findByEmail("test@fixaro.pl");

        //then
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Jan");
    }

    @Test
    void shouldFindAllUsersByRoleName() {
        //given
        Company company = createDefaultCompany();

        UserRole adminRole = new UserRole();
        adminRole.setName("ROLE_ADMINISTRATOR");
        adminRole.setDescription("Administrator systemu");
        entityManager.persist(adminRole);

        User admin = createValidUser("admin@fixaro.pl", company);
        admin.setRoles(Set.of(adminRole));
        entityManager.persist(admin);

        User employee = createValidUser("employee@fixaro.pl", company);
        entityManager.persistAndFlush(employee);

        //when
        List<User> foundAdmins = userRepository.findALlByRoles_Name("ROLE_ADMINISTRATOR");

        //then
        assertThat(foundAdmins).hasSize(1);
        assertThat(foundAdmins.get(0).getEmail()).isEqualTo("admin@fixaro.pl");
    }

    @Test
    void shouldFindUserByEmailAndIsActive() {
        //given
        Company company = createDefaultCompany();

        User activeUser = createValidUser("active@fixaro.pl", company);
        activeUser.setActive(true);
        entityManager.persist(activeUser);

        User inactiveUser = createValidUser("inactive@fixaro.pl", company);
        inactiveUser.setActive(false);
        entityManager.persistAndFlush(inactiveUser);

        //when
        Optional<User> foundActive = userRepository.findByEmailAndIsActiveIsTrue("active@fixaro.pl");
        Optional<User> foundInactive = userRepository.findByEmailAndIsActiveIsTrue("inactive@fixaro.pl");

        //then
        assertThat(foundActive).isPresent();
        assertThat(foundInactive).isEmpty();
    }

    @Test
    void shouldFindFirstNameByEmailUsingCustomQuery() {
        //given
        Company company = createDefaultCompany();
        User user = createValidUser("imie@fixaro.pl", company);
        user.setFirstName("Krzysztof");
        entityManager.persistAndFlush(user);

        //when
        Optional<String> firstName = userRepository.findFirstNameByEmail("imie@fixaro.pl");

        //then
        assertThat(firstName).isPresent();
        assertThat(firstName.get()).isEqualTo("Krzysztof");
    }

    private Company createDefaultCompany() {
        Company company = new Company();
        company.setName("Testowa Firma IT");
        company.setTimeToFirstRespond(2);
        company.setTimeToResolve(24);
        return entityManager.persist(company);
    }

    private User createValidUser(String email, Company company) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName("Jan");
        user.setLastName("Kowalski");
        user.setPassword("secretPassword");
        user.setActive(true);
        user.setCompany(company);
        return user;
    }
}