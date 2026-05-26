package pl.error_handling_app.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pl.error_handling_app.exception.TokenNotFoundException;
import pl.error_handling_app.mail.MailService;
import pl.error_handling_app.user.entity.TokenStatus;
import pl.error_handling_app.user.entity.User;
import pl.error_handling_app.user.entity.VerificationToken;
import pl.error_handling_app.user.repository.UserRepository;
import pl.error_handling_app.user.repository.VerificationTokenRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class UserPasswordChangeOrActiveServiceTest {

    @Mock
    private VerificationTokenRepository tokenRepository;
    @Mock
    private MailService mailService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserPasswordChangeOrActiveService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "verificationExpiry", 10080);
        ReflectionTestUtils.setField(service, "resetExpiry", 60);
        ReflectionTestUtils.setField(service, "cleanupDays", 14);

        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void shouldCreateVerificationTokenAndSendMail() {
        //given
        User user = new User();
        user.setEmail("nowy@fixaro.pl");
        user.setFirstName("Jan");

        //when
        service.createVerificationToken(user);

        //then
        ArgumentCaptor<VerificationToken> tokenCaptor = ArgumentCaptor.forClass(VerificationToken.class);
        then(tokenRepository).should().save(tokenCaptor.capture());

        VerificationToken savedToken = tokenCaptor.getValue();
        assertThat(savedToken.getUser()).isEqualTo(user);
        assertThat(savedToken.getToken()).isNotNull();

        then(mailService).should().newUserWelcomeMessage(
                eq("nowy@fixaro.pl"),
                eq("Jan"),
                anyString(),
                anyString()
        );
    }

    @Test
    void shouldSendResetPasswordMailWhenUserIsActiveAndExists() {
        //given
        User activeUser = new User();
        activeUser.setEmail("aktywny@fixaro.pl");
        activeUser.setFirstName("Piotr");

        given(userRepository.findByEmailAndIsActiveIsTrue("aktywny@fixaro.pl"))
                .willReturn(Optional.of(activeUser));

        //when
        boolean result = service.sendResetPasswordMail("aktywny@fixaro.pl");

        //then
        assertThat(result).isTrue();
        then(tokenRepository).should().save(any(VerificationToken.class));
        then(mailService).should().forgotPasswordMessage(eq("aktywny@fixaro.pl"), eq("Piotr"), anyString(), anyString());
    }

    @Test
    void shouldNotSendResetPasswordMailWhenUserDoesNotExistOrIsInactive() {
        //given
        given(userRepository.findByEmailAndIsActiveIsTrue("nieznany@fixaro.pl"))
                .willReturn(Optional.empty());

        //when
        boolean result = service.sendResetPasswordMail("nieznany@fixaro.pl");

        //then
        assertThat(result).isFalse();
        then(tokenRepository).should(never()).save(any());
        then(mailService).should(never()).forgotPasswordMessage(any(), any(), any(), any());
    }

    @Test
    void validateToken_shouldReturnInvalidWhenTokenDoesNotExist() {
        //given
        given(tokenRepository.findByToken("fake-token")).willReturn(Optional.empty());

        //when
        TokenStatus status = service.validateToken("fake-token", false);

        //then
        assertThat(status).isEqualTo(TokenStatus.INVALID);
    }

    @Test
    void validateToken_shouldReturnExpiredWhenExpirationTimePassed() {
        //given
        VerificationToken expiredToken = new VerificationToken();
        expiredToken.setExpirationTime(LocalDateTime.now().minusMinutes(5));
        expiredToken.setUser(new User());

        given(tokenRepository.findByToken("expired-token")).willReturn(Optional.of(expiredToken));

        //when
        TokenStatus status = service.validateToken("expired-token", true);

        //then
        assertThat(status).isEqualTo(TokenStatus.EXPIRED);
    }

    @Test
    void validateToken_shouldReturnValidWhenTokenIsOk() {
        //given
        VerificationToken validToken = new VerificationToken();
        validToken.setExpirationTime(LocalDateTime.now().plusHours(1));
        validToken.setUser(new User());

        given(tokenRepository.findByToken("valid-token")).willReturn(Optional.of(validToken));

        //when
        TokenStatus status = service.validateToken("valid-token", true);

        //then
        assertThat(status).isEqualTo(TokenStatus.VALID);
    }

    @Test
    void shouldSetNewPasswordAndActivateUser() {
        //given
        User user = new User();
        user.setActive(false);

        VerificationToken token = new VerificationToken();
        token.setToken("valid-token");
        token.setExpirationTime(LocalDateTime.now().plusHours(1));
        token.setUser(user);

        given(tokenRepository.findByToken("valid-token")).willReturn(Optional.of(token));
        given(passwordEncoder.encode("noweHaslo123!")).willReturn("encoded-password");

        //when
        service.setNewPassword("valid-token", "noweHaslo123!");

        //then
        assertThat(user.getPassword()).isEqualTo("encoded-password");
        assertThat(user.isActive()).isTrue();

        then(userRepository).should().save(user);
        then(tokenRepository).should().save(token);
        assertThat(token.getExpirationTime()).isBefore(LocalDateTime.now());
    }

    @Test
    void shouldThrowExceptionWhenSettingPasswordWithInvalidToken() {
        //given
        given(tokenRepository.findByToken("invalid-token")).willReturn(Optional.empty());

        //when,then
        assertThrows(TokenNotFoundException.class, () ->
                service.setNewPassword("invalid-token", "noweHaslo123!")
        );
    }

    @Test
    void shouldCleanupOldTokens() {
        //when
        service.cleanupOldTokens();

        //then
        ArgumentCaptor<LocalDateTime> dateCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        then(tokenRepository).should().deleteAllByExpirationTimeBefore(dateCaptor.capture());
        LocalDateTime capturedDate = dateCaptor.getValue();
        assertThat(capturedDate).isBefore(LocalDateTime.now().minusDays(13));
    }
}