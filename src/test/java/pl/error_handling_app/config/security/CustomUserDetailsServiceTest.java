package pl.error_handling_app.config.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import pl.error_handling_app.user.dto.UserCredentialsDto;
import pl.error_handling_app.user.service.UserService;
import java.util.Optional;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void shouldReturnUserDetailsWhenUserExistsAndPasswordIsNotNull() {
        //given
        String email = "test@test.pl";
        String encodedPassword = "haslo123";
        UserCredentialsDto credentialsDto = new UserCredentialsDto(email, encodedPassword, Set.of("EMPLOYEE", "ADMINISTRATOR"));

        when(userService.findCredentialsByEmail(email)).thenReturn(Optional.of(credentialsDto));

        //when
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        //then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(email);
        assertThat(userDetails.getPassword()).isEqualTo(encodedPassword);

        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_EMPLOYEE", "ROLE_ADMINISTRATOR");
    }

    @Test
    void shouldThrowDisabledExceptionWhenUserExistsButPasswordIsNull() {
        //given
        String email = "test@test.pl";
        UserCredentialsDto credentialsDto = new UserCredentialsDto(email, null, Set.of("EMPLOYEE"));

        when(userService.findCredentialsByEmail(email)).thenReturn(Optional.of(credentialsDto));

        //when, then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(email))
                .isInstanceOf(DisabledException.class)
                .hasMessage("Konto nie zostało jeszcze aktywowane. Najpierw aktywuj konto.");
    }

    @Test
    void shouldThrowUsernameNotFoundExceptionWhenUserDoesNotExist() {
        //given
        String email = "test@test.pl";
        when(userService.findCredentialsByEmail(email)).thenReturn(Optional.empty());

        //when, then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Użytkownik test@test.pl nie został znaleziony.");
    }
}