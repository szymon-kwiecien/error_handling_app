package pl.error_handling_app.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.error_handling_app.exception.InvalidPasswordException;
import pl.error_handling_app.exception.UnauthorizedOperationException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileControllerExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private ProfileControllerExceptionHandler exceptionHandler;

    @ParameterizedTest(name = "Dla uri ''{0}'' prefiks komunikatu to ''{1}''")
    @CsvSource({
            "/profile/change-email, 'Podczas zmiany adresu e-mail wystąpił błąd: '",
            "/profile/change-password, 'Podczas zmiany hasła wystąpił błąd: '"
    })
    void shouldRedirectAndSetCorrectPrefixBasedOnUri(String uri, String messagePrefix) {
        //given
        when(request.getRequestURI()).thenReturn(uri);
        String exceptionMessage = "Nieprawidłowe dane";
        RuntimeException exception = new InvalidPasswordException(exceptionMessage);

        //when
        String redirectUrl = exceptionHandler.handleProfileUpdateExceptions(exception, redirectAttributes, request);

        //then
        assertThat(redirectUrl).isEqualTo("redirect:/profile");
        String expectedFlashMessage = messagePrefix + exceptionMessage;
        verify(redirectAttributes).addFlashAttribute("error", expectedFlashMessage);
    }

    @Test
    void shouldSetOnlyExceptionMessageWhenUriDoesNotMatchKnownPaths() {
        //given
        when(request.getRequestURI()).thenReturn("/profile/test");

        String exceptionMessage = "Brak uprawnień do wykonania tej akcji";
        RuntimeException exception = new UnauthorizedOperationException(exceptionMessage);

        //when
        String redirectUrl = exceptionHandler.handleProfileUpdateExceptions(exception, redirectAttributes, request);

        //then
        assertThat(redirectUrl).isEqualTo("redirect:/profile");
        verify(redirectAttributes).addFlashAttribute("error", exceptionMessage);
    }
}