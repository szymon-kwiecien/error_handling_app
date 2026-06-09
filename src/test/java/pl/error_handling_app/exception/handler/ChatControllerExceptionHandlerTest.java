package pl.error_handling_app.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.error_handling_app.exception.ReportNotFoundException;
import pl.error_handling_app.exception.UnauthorizedOperationException;
import pl.error_handling_app.exception.UserNotFoundException;
import java.security.Principal;
import java.util.Map;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ChatControllerExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ChatControllerExceptionHandler exceptionHandler;

    //Testy API (json)

    @ParameterizedTest(name = "Wyjątek {0} powinien zwrócić status HTTP {1}")
    @MethodSource("provideExceptionsForApi")
    void shouldReturnResponseEntityWithCorrectStatusForApiRequests(Exception exception, HttpStatus expectedStatus) {
        //given
        when(request.getRequestURI()).thenReturn("/api/chat/messages/12");
        when(request.getParameter("id")).thenReturn(null);

        //when
        ResponseEntity<?> response = (ResponseEntity<?>) exceptionHandler.handleChatExceptions(exception, redirectAttributes, request);

        //then
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body).containsEntry("Wystapił błąd:", exception.getMessage());
    }

    //pomocnicza metoda dla powyzszego testu
    static Stream<Arguments> provideExceptionsForApi() {
        return Stream.of(
                Arguments.of(new UnauthorizedOperationException("Brak uprawnień"), HttpStatus.FORBIDDEN),
                Arguments.of(new UserNotFoundException("Nie ma usera"), HttpStatus.FORBIDDEN),
                Arguments.of(new ReportNotFoundException("Nie ma raportu"), HttpStatus.NOT_FOUND),
                Arguments.of(new IllegalArgumentException("Zły argument"), HttpStatus.BAD_REQUEST),
                Arguments.of(new ResponseStatusException(HttpStatus.CONFLICT, "Konflikt"), HttpStatus.CONFLICT),
                Arguments.of(new RuntimeException("Inny nieznany błąd"), HttpStatus.INTERNAL_SERVER_ERROR)
        );
    }

    //testy widoków HTML

    @Test
    void shouldRedirectToReportsWithCorrectMessageForReportNotFound() {
        //given
        //ścieżka która nie zawiera /api/chat
        when(request.getRequestURI()).thenReturn("/chat/view");
        when(request.getParameter("id")).thenReturn("5");
        Exception exception = new ReportNotFoundException("Zgłoszenie zostało usunięte");

        //when
        String redirectUrl = (String) exceptionHandler.handleChatExceptions(exception, redirectAttributes, request);

        //then
        assertThat(redirectUrl).isEqualTo("redirect:/reports");
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Nie znaleziono zgłoszenia: Zgłoszenie zostało usunięte");
    }

    @Test
    void shouldRedirectToReportsWithCorrectMessageForUnauthorizedOperation() {
        //given
        when(request.getRequestURI()).thenReturn("/chat/details");
        Exception exception = new UnauthorizedOperationException("To nie twoje zgłoszenie");

        //when
        String redirectUrl = (String) exceptionHandler.handleChatExceptions(exception, redirectAttributes, request);

        //then
        assertThat(redirectUrl).isEqualTo("redirect:/reports");
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Brak uprawnień: To nie twoje zgłoszenie");
    }

    @Test
    void shouldRedirectToReportsWithGenericMessageForUnknownException() {
        //given
        when(request.getRequestURI()).thenReturn("/chat/upload");
        Exception exception = new NullPointerException("Null w kodzie");

        //when
        String redirectUrl = (String) exceptionHandler.handleChatExceptions(exception, redirectAttributes, request);

        //then
        assertThat(redirectUrl).isEqualTo("redirect:/reports");
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Wystąpił błąd podczas obsługi zgłoszenia: Null w kodzie");
    }

    //testy WebSocket
    @Test
    void shouldSendErrorMessageToSpecificUserViaWebSocket() {
        //given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("jankowalski@test.pl");
        Exception exception = new UnauthorizedOperationException("Zostałeś zablokowany na czacie");

        //when
        exceptionHandler.handleWebSocketExceptions(exception, principal);

        //then
        verify(messagingTemplate).convertAndSendToUser(
                eq("jankowalski@test.pl"),
                eq("/queue/errors"),
                eq(Map.of("errorMessage", "Zostałeś zablokowany na czacie"))
        );
    }
}