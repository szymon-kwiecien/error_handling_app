package pl.error_handling_app.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.error_handling_app.exception.UserNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserManagementControllerExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private UserManagementControllerExceptionHandler exceptionHandler;

    @ParameterizedTest(name = "Dla uri ''{0}'' komunikat powinien zawierać slowo ''{1}''")
    @CsvSource({
            "/admin/add-user, dodawania",
            "/admin/edit-user/15, edycji",
            "/admin/delete-user/2, usuwania",
            "/admin/other, wybranej operacji"//inne żądanie
    })
    void shouldRedirectAndSetCorrectFlashAttributeBasedOnUri(String uri, String actionWord) {
        //given
        when(request.getRequestURI()).thenReturn(uri);

        String exceptionMessage = "Podany adres e-mail jest już zajęty";
        RuntimeException exception = new UserNotFoundException(exceptionMessage);

        //when
        String redirectUrl = exceptionHandler.handleUserManagementExceptions(exception, redirectAttributes, request);

        //then
        assertThat(redirectUrl).isEqualTo("redirect:/admin/manage-users");
        String expectedFlashMessage = String.format("Podczas %s użytkownika wystąpił błąd: %s", actionWord, exceptionMessage);
        verify(redirectAttributes).addFlashAttribute("error", expectedFlashMessage);
    }
}