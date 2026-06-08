package pl.error_handling_app.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.error_handling_app.exception.ReportNotFoundException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportControllerExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private ReportControllerExceptionHandler exceptionHandler;

    private final String exceptionMessage = "Testowy komunikat błędu";
    private final RuntimeException exception = new ReportNotFoundException(exceptionMessage);

    @ParameterizedTest(name = "Dla uri ''{0}'' przekierowanie to ''{2}''")
    @CsvSource({
            "/reports/add, 'Podczas dodawania zgłoszenia wystąpił błąd: ', /reports/add",
            "/reports/delete/5, 'Podczas usuwania zgłoszenia wystąpił błąd: ', /reports",
            "/reports/zzzz, 'Wystąpił błąd: ', /reports" //Domyslna sciezka
    })
    void shouldHandleStaticRedirects(String uri, String messagePrefix, String expectedRedirect) {
        //given
        when(request.getRequestURI()).thenReturn(uri);
        //jako reportId symuluje null poniewaz nie ma on znaczenia w tym przypadku
        when(request.getParameter("reportId")).thenReturn(null);

        //when
        String redirectUrl = exceptionHandler.handleReportExceptions(exception, redirectAttributes, request);

        //then
        assertThat(redirectUrl).isEqualTo("redirect:" + expectedRedirect);
        verify(redirectAttributes).addFlashAttribute("errorMessage", messagePrefix + exceptionMessage);
    }

    @ParameterizedTest(name = "z ID Dla uri ''{0}'' przekierowanie to szczegóły zgłoszenia")
    @CsvSource({
            "/reports/close, 'Podczas zamykania zgłoszenia wystąpił błąd: '",
            "/reports/assign, 'Nie udało się przypisać pracownika do zgłoszenia: '",
            "/reports/attachment/upload, 'Wystąpił błąd podczas dodawania załączników: '"
    })
    void shouldHandleDynamicRedirectsWhenReportIdIsPresent(String uri, String messagePrefix) {
        //given
        String reportId = "123";
        when(request.getRequestURI()).thenReturn(uri);
        when(request.getParameter("reportId")).thenReturn(reportId);

        //when
        String redirectUrl = exceptionHandler.handleReportExceptions(exception, redirectAttributes, request);

        //then
        //prawidłowe zachowanie to powrót do strony szczegółów zgłoszenia
        assertThat(redirectUrl).isEqualTo("redirect:/report?id=" + reportId);
        verify(redirectAttributes).addFlashAttribute("errorMessage", messagePrefix + exceptionMessage);
    }

    @ParameterizedTest(name = "Bez ID (null): Dla uri ''{0}'' przekierowanie to lista zgłoszeń")
    @CsvSource({
            "/reports/close, 'Podczas zamykania zgłoszenia wystąpił błąd: '",
            "/reports/assign, 'Nie udało się przypisać pracownika do zgłoszenia: '",
            "/reports/attachment/upload, 'Wystąpił błąd podczas dodawania załączników: '"
    })
    void shouldHandleDynamicRedirectsWhenReportIdIsNull(String uri, String messagePrefix) {
        //given
        when(request.getRequestURI()).thenReturn(uri);
        when(request.getParameter("reportId")).thenReturn(null);

        //when
        String redirectUrl = exceptionHandler.handleReportExceptions(exception, redirectAttributes, request);

        //then
        //w przypadku braku ID prawidłowe zachowanie to przekierowanie do listy zgłoszęń
        assertThat(redirectUrl).isEqualTo("redirect:/reports");
        verify(redirectAttributes).addFlashAttribute("errorMessage", messagePrefix + exceptionMessage);
    }
}