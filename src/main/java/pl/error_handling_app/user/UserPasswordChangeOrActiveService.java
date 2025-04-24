package pl.error_handling_app.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.error_handling_app.mail.MailService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserPasswordChangeOrActiveService {

    private final VeryficationTokenRepository veryficationTokenRepository;
    private final MailService mailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Autowired
    public UserPasswordChangeOrActiveService(VeryficationTokenRepository veryficationTokenRepository, MailService mailService
            , UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.veryficationTokenRepository = veryficationTokenRepository;
        this.mailService = mailService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private final int VERIFICATION_ACCOUNT_EXPIRATION_TIME = 60 * 24 * 7; // 7 dni
    private final int PASSWORD_RESET_EXPIRATION_TIME = 60; //60 min

    @Transactional
    public void NewVerification(User user) {
        VeryficationToken veryficationToken = new VeryficationToken(UUID.randomUUID().toString(), VERIFICATION_ACCOUNT_EXPIRATION_TIME, user);
        veryficationTokenRepository.save(veryficationToken);
        String verifUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path("account/verification/" + veryficationToken.getToken()).build().toUriString();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        mailService.NewUserWelcomeMessage(user.getEmail(), user.getFirstName(), verifUrl, veryficationToken.getExpirationTime().format(formatter));

    }

    public Boolean NewResetPasswordMail(String username) {
        Optional<User> OptionalUser = userRepository.findByEmailAndIsActiveIsTrue(username);
        if (OptionalUser.isEmpty()) {
            return false;
        }
        User user = OptionalUser.get();
        VeryficationToken veryficationToken = getOrCreateVeryficationTokenByToken(user);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        veryficationTokenRepository.save(veryficationToken);

        String verifUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("account/forgot-password/" + veryficationToken.getToken()).build().toUriString();
        mailService.ForgotPasswordMessage(user.getEmail(), user.getFirstName()
                , verifUrl, veryficationToken.getExpirationTime().format(formatter));

        return true;
    }

    public String validateToken(String token, boolean cheackIfActive) {

        Optional<VeryficationToken> byToken = veryficationTokenRepository.findByToken(token);
        if (byToken.isPresent()) {
            if (byToken.get().getExpirationTime().isAfter(LocalDateTime.now())) {
                if (!cheackIfActive) {
                    return "OK";
                } else if (!byToken.get().getUser().isActive()) {
                    return "OK";
                } else {
                    return "alreadyAactive";
                }
            } else {
                return "tokenExpired";
            }
        } else {
            return "error";
        }

    }

    public void SetNewPassword(String token, String password) {
        Optional<VeryficationToken> byToken = veryficationTokenRepository.findByToken(token);
        User user = byToken.get().getUser();
        user.setPassword(passwordEncoder.encode(password));
        user.setActive(true);
        userRepository.save(user);

        byToken.get().setExpirationTime(LocalDateTime.now().minusMinutes(1));
        veryficationTokenRepository.save(byToken.get());
    }

    private VeryficationToken getOrCreateVeryficationTokenByToken(User user) {
        Optional<VeryficationToken> byToken = veryficationTokenRepository.findByUser(user);
        if (byToken.isPresent()) {
            byToken.get().setExpirationTime(byToken.get().getTokenExpirationTime(PASSWORD_RESET_EXPIRATION_TIME));
            byToken.get().setToken(UUID.randomUUID().toString());
            return byToken.get();
        } else {
            return new VeryficationToken(UUID.randomUUID().toString(), PASSWORD_RESET_EXPIRATION_TIME, user);
        }
    }
}

