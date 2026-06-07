package pl.error_handling_app.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.error_handling_app.exception.CategoryNotFoundException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportCategoryManagementControllerExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private ReportCategoryManagementControllerExceptionHandler exceptionHandler;

    @ParameterizedTest(name = "Dla uri ''{0}'' komunikat powinien zawierac słowo''{1}''")
    @CsvSource({
            "/admin/add-category, dodawania",
            "/admin/edit-category/3, edycji",
            "/admin/delete-category/8, usuwania",
            "/admin/other-path, wybranej operacji"//przypadek domyślny
    })
    void shouldRedirectAndSetCorrectFlashAttributeBasedOnUri(String uri, String actionWord) {
        //given
        when(request.getRequestURI()).thenReturn(uri);
        String exceptionMessage = "Kategoria o podanej nazwie nie istnieje";
        RuntimeException exception = new CategoryNotFoundException(exceptionMessage);

        //when
        String redirectUrl = exceptionHandler.handleReportCategoryManagementExceptions(exception, redirectAttributes, request);

        //then
        assertThat(redirectUrl).isEqualTo("redirect:/admin/manage-categories");

        String expectedFlashMessage = String.format("Podczas %s kategorii wystąpił błąd: %s", actionWord, exceptionMessage);
        verify(redirectAttributes).addFlashAttribute("error", expectedFlashMessage);
    }
}