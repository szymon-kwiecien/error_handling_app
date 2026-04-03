package pl.error_handling_app.user.repository;

import org.springframework.data.repository.CrudRepository;
import pl.error_handling_app.user.entity.User;
import pl.error_handling_app.user.entity.VerificationToken;

import java.time.LocalDateTime;
import java.util.Optional;

public interface VerificationTokenRepository extends CrudRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
    Optional<VerificationToken> findByUser(User user);
    void deleteAllByExpirationTimeBefore(LocalDateTime dateTime);
}
