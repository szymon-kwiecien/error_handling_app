package pl.error_handling_app.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.error_handling_app.exception.CompanyNotFoundException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyManagementControllerExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private CompanyManagementControllerExceptionHandler exceptionHandler;

    @ParameterizedTest(name = "Dla uri ''{0}'' komunikat powinien zawierać slowo ''{1}''")
    @CsvSource({
            "/admin/add-company, dodawania",
            "/admin/edit-company/5, edycji",
            "/admin/delete-company/12, usuwania",
            "/admin/other-path, wybranej operacji" //Przypadek domyslny
    })
    void shouldRedirectAndSetCorrectFlashAttributeBasedOnUri(String uri, String actionWord) {
        //given
        when(request.getRequestURI()).thenReturn(uri);
        String exceptionMessage = "Brak firmy w bazie";
        RuntimeException exception = new CompanyNotFoundException(exceptionMessage);

        //when
        String redirectUrl = exceptionHandler.handleCompanyManagementExceptions(exception, redirectAttributes, request);

        //then
        assertThat(redirectUrl).isEqualTo("redirect:/admin/manage-companies");
        String expectedFlashMessage = String.format("Podczas %s firmy wystąpił błąd: %s", actionWord, exceptionMessage);
        verify(redirectAttributes).addFlashAttribute("error", expectedFlashMessage);
    }
}