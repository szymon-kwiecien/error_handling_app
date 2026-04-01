package pl.error_handling_app.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.error_handling_app.exception.TokenNotFoundException;
import pl.error_handling_app.mail.MailService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserPasswordChangeOrActiveService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final VerificationTokenRepository tokenRepository;
    private final MailService mailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger log = LoggerFactory.getLogger(UserPasswordChangeOrActiveService.class);

    @Value("${app.token.verification-expiry-minutes:10080}") // 7 dni
    private int verificationExpiry;

    @Value("${app.token.reset-expiry-minutes:60}")
    private int resetExpiry;

    public UserPasswordChangeOrActiveService(VerificationTokenRepository tokenRepository,
                                             MailService mailService,
                                             UserRepository userRepository,
                                             PasswordEncoder passwordEncoder) {
        this.tokenRepository = tokenRepository;
        this.mailService = mailService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void createVerificationToken(User user) {
        VerificationToken token = new VerificationToken(UUID.randomUUID().toString(), verificationExpiry, user);
        tokenRepository.save(token);

        String url = buildUrl("account/verification/", token.getToken());
        mailService.newUserWelcomeMessage(user.getEmail(), user.getFirstName(), url, token.getExpirationTime().format(FORMATTER));
    }

    @Transactional
    public boolean sendResetPasswordMail(String email) {
        return userRepository.findByEmailAndIsActiveIsTrue(email).map(user -> {
            VerificationToken token = getOrCreateResetToken(user);
            tokenRepository.save(token);

            String url = buildUrl("account/forgot-password/", token.getToken());
            mailService.forgotPasswordMessage(user.getEmail(), user.getFirstName(), url, token.getExpirationTime().format(FORMATTER));
            return true;
        }).orElse(false);
    }

    public TokenStatus validateToken(String token, boolean userIsAlreadyActive) {
        return tokenRepository.findByToken(token)
                .map(t -> {
                    if (!userIsAlreadyActive && t.getUser().isActive()) {
                        return TokenStatus.USER_ALREADY_ACTIVE;
                    }
                    if (t.getExpirationTime().isBefore(LocalDateTime.now())) {
                        return TokenStatus.EXPIRED;
                    }
                    return TokenStatus.VALID;
                })
                .orElse(TokenStatus.INVALID);
    }

    @Transactional
    public void setNewPassword(String token, String password) {
        VerificationToken vToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenNotFoundException("Token nie został znaleziony"));

        User user = vToken.getUser();
        user.setPassword(passwordEncoder.encode(password));
        user.setActive(true);
        userRepository.save(user);
        vToken.setExpirationTime(LocalDateTime.now().minusSeconds(1));
        tokenRepository.save(vToken);
    }

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupOldTokens() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);

        tokenRepository.deleteAllByExpirationTimeBefore(threshold);

        log.info("Scheduled - Usunięto tokeny wygasłe przed {}", threshold);
    }

    private VerificationToken getOrCreateResetToken(User user) {
        return tokenRepository.findByUser(user)
                .map(existingToken -> {
                    existingToken.setToken(UUID.randomUUID().toString());
                    existingToken.setExpirationTime(LocalDateTime.now().plusMinutes(resetExpiry));
                    return existingToken;
                })
                .orElseGet(() -> new VerificationToken(UUID.randomUUID().toString(), resetExpiry, user));
    }

    private String buildUrl(String path, String token) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(path + token)
                .build()
                .toUriString();
    }
}