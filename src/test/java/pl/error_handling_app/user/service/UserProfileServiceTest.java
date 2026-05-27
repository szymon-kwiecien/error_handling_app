package pl.error_handling_app.user.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.error_handling_app.company.entity.Company;
import pl.error_handling_app.exception.InvalidEmailException;
import pl.error_handling_app.exception.InvalidPasswordException;
import pl.error_handling_app.exception.UnauthorizedOperationException;
import pl.error_handling_app.user.dto.ChangeEmailDto;
import pl.error_handling_app.user.dto.ChangePasswordDto;
import pl.error_handling_app.user.dto.UserProfileDetailsDto;
import pl.error_handling_app.user.entity.User;
import pl.error_handling_app.user.entity.UserRole;
import pl.error_handling_app.user.repository.UserRepository;
import pl.error_handling_app.utils.SecurityUtils;
import java.util.Optional;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserService userService;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserProfileService userProfileService;

    private SecurityContext originalSecurityContext;

    @BeforeEach
    void setUp() {
        originalSecurityContext = SecurityContextHolder.getContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.setContext(originalSecurityContext);
    }

    @Test
    void shouldFindUserProfileDetailsSuccessfully() {
        //given
        Company company = new Company();
        company.setName("TechCorp");
        company.setTimeToFirstRespond(2);
        company.setTimeToResolve(48);

        UserRole role = new UserRole();
        role.setName("ROLE_ADMIN");

        User user = new User();
        user.setId(10L);
        user.setFirstName("Jan");
        user.setLastName("Kowalski");
        user.setEmail("jan@fixaro.pl");
        user.setCompany(company);
        user.setRoles(Set.of(role));

        given(userRepository.findByEmail("jan@fixaro.pl")).willReturn(Optional.of(user));

        //when
        UserProfileDetailsDto dto = userProfileService.findUserProfileDetailsByEmail("jan@fixaro.pl");

        //then
        assertThat(dto.firstName()).isEqualTo("Jan");
        assertThat(dto.companyName()).isEqualTo("TechCorp");
        assertThat(dto.role()).isEqualTo("ROLE_ADMIN");
        assertThat(dto.companyTimeToFirstRespond()).isEqualTo("2");
    }

    @Test
    void shouldFindUserProfileDetailsWithNullCompanyGracefully() {
        //given
        User user = new User();
        user.setEmail("bezfirmy@fixaro.pl");

        given(userRepository.findByEmail("bezfirmy@fixaro.pl")).willReturn(Optional.of(user));

        //when
        UserProfileDetailsDto dto = userProfileService.findUserProfileDetailsByEmail("bezfirmy@fixaro.pl");

        //then
        assertThat(dto.companyName()).isEqualTo("-");
        assertThat(dto.role()).isEqualTo("-");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundByEmail() {
        //given
        given(userRepository.findByEmail("brak@fixaro.pl")).willReturn(Optional.empty());

        //when, then
        assertThrows(UsernameNotFoundException.class, () ->
                userProfileService.findUserProfileDetailsByEmail("brak@fixaro.pl"));
    }

    @Test
    void shouldChangeEmailSuccessfully() {
        //given
        String currentUserEmail = "stary@fixaro.pl";
        ChangeEmailDto dto = new ChangeEmailDto(1L, "nowy@fixaro.pl", "MojeHaslo123");

        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setEmail(currentUserEmail);
        currentUser.setPassword("encoded-pass");

        given(userRepository.findByEmail("nowy@fixaro.pl")).willReturn(Optional.empty());
        given(userRepository.findByEmail(currentUserEmail)).willReturn(Optional.of(currentUser));
        given(userService.isPasswordInvalid("MojeHaslo123", "encoded-pass")).willReturn(false);

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getCredentials()).willReturn("cred");
        SecurityContextHolder.setContext(securityContext);

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserEmail).thenReturn(currentUserEmail);

            userProfileService.changeEmail(dto);
        }

        //then
        assertThat(currentUser.getEmail()).isEqualTo("nowy@fixaro.pl");
    }

    @Test
    void shouldThrowExceptionWhenChangingToSameEmail() {
        //given
        String currentUserEmail = "taki-sam@fixaro.pl";
        ChangeEmailDto dto = new ChangeEmailDto(1L, currentUserEmail, "MojeHaslo123");

        //when ,then
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserEmail).thenReturn(currentUserEmail);

            assertThrows(InvalidEmailException.class, () -> userProfileService.changeEmail(dto));
        }
    }

    @Test
    void shouldThrowExceptionWhenNewEmailAlreadyTakenBySomeoneElse() {
        //given
        String currentUserEmail = "moj@fixaro.pl";
        ChangeEmailDto dto = new ChangeEmailDto(1L, "zajety@fixaro.pl", "MojeHaslo123");

        User otherUser = new User();
        otherUser.setId(99L); // takie id posiada już inny użytkownik

        given(userRepository.findByEmail("zajety@fixaro.pl")).willReturn(Optional.of(otherUser));

        //when, then
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserEmail).thenReturn(currentUserEmail);

            assertThrows(InvalidEmailException.class, () -> userProfileService.changeEmail(dto));
        }
    }

    @Test
    void shouldThrowExceptionWhenCurrentPasswordIsWrongForEmailChange() {
        //given
        String currentUserEmail = "moj@fixaro.pl";
        ChangeEmailDto dto = new ChangeEmailDto(1L, "nowy@fixaro.pl", "ZleHaslo");

        User currentUser = new User();
        currentUser.setPassword("encoded-pass");

        given(userRepository.findByEmail("nowy@fixaro.pl")).willReturn(Optional.empty());
        given(userRepository.findByEmail(currentUserEmail)).willReturn(Optional.of(currentUser));
        given(userService.isPasswordInvalid("ZleHaslo", "encoded-pass")).willReturn(true);

        //when, then
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserEmail).thenReturn(currentUserEmail);

            assertThrows(InvalidEmailException.class, () -> userProfileService.changeEmail(dto));
        }
    }

    @Test
    void shouldChangePasswordSuccessfully() {
        //given
        String currentUserEmail = "ja@fixaro.pl";
        ChangePasswordDto dto = new ChangePasswordDto(1L, "Nowe123!", "Nowe123!", "Stare123");

        User currentUser = new User();
        currentUser.setPassword("encoded-old");

        given(userRepository.findByEmail(currentUserEmail)).willReturn(Optional.of(currentUser));
        given(userService.isPasswordInvalid("Stare123", "encoded-old")).willReturn(false); //Stare haslo prawidlowe
        given(userService.isPasswordInvalid("Nowe123!", "encoded-old")).willReturn(true);
        given(passwordEncoder.encode("Nowe123!")).willReturn("encoded-new");

        //when
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserEmail).thenReturn(currentUserEmail);

            userProfileService.changePassword(dto);
        }

        //then
        assertThat(currentUser.getPassword()).isEqualTo("encoded-new");
    }

    @Test
    void shouldThrowExceptionWhenNewPasswordsDoNotMatch() {
        //given
        ChangePasswordDto dto = new ChangePasswordDto(1L, "Nowe123", "InneNowe123", "Stare");

        //when, then
        assertThrows(InvalidPasswordException.class, () -> userProfileService.changePassword(dto));
    }

    @Test
    void shouldThrowExceptionWhenCurrentPasswordIsWrongForPasswordChange() {
        //given
        String currentUserEmail = "ja@fixaro.pl";
        ChangePasswordDto dto = new ChangePasswordDto(1L, "Nowe123!", "Nowe123!", "ZleStare");

        User currentUser = new User();
        currentUser.setPassword("encoded-old");

        given(userRepository.findByEmail(currentUserEmail)).willReturn(Optional.of(currentUser));
        given(userService.isPasswordInvalid("ZleStare", "encoded-old")).willReturn(true); // Hasło się nie zgadza

        //when,then
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserEmail).thenReturn(currentUserEmail);

            assertThrows(InvalidPasswordException.class, () -> userProfileService.changePassword(dto));
        }
    }

    @Test
    void shouldThrowExceptionWhenNewPasswordIsSameAsOld() {
        //given
        String currentUserEmail = "ja@fixaro.pl";
        ChangePasswordDto dto = new ChangePasswordDto(1L, "Stare123", "Stare123", "Stare123");

        User currentUser = new User();
        currentUser.setPassword("encoded-old");

        given(userRepository.findByEmail(currentUserEmail)).willReturn(Optional.of(currentUser));
        given(userService.isPasswordInvalid("Stare123", "encoded-old")).willReturn(false); // Stare hasło jest poprawne
        given(userService.isPasswordInvalid("Stare123", "encoded-old")).willReturn(false);

        //when,then
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserEmail).thenReturn(currentUserEmail);
            assertThrows(InvalidPasswordException.class, () -> userProfileService.changePassword(dto));
        }
    }
}