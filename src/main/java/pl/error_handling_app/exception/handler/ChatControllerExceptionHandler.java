package pl.error_handling_app.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.error_handling_app.chat.controller.ChatReportDetailsController;
import pl.error_handling_app.chat.controller.ChatWebSocketController;
import pl.error_handling_app.exception.*;

import java.security.Principal;
import java.util.Map;

@ControllerAdvice(assignableTypes = {ChatReportDetailsController.class, ChatWebSocketController.class})
public class ChatControllerExceptionHandler {

    private final SimpMessagingTemplate messagingTemplate;
    private static final Logger logger = LoggerFactory.getLogger(ChatControllerExceptionHandler.class);

    public ChatControllerExceptionHandler(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @ExceptionHandler({
            ReportNotFoundException.class,
            UnauthorizedOperationException.class,
            UserNotFoundException.class,
            IllegalArgumentException.class,
            ResponseStatusException.class
    })
    public Object handleChatExceptions(Exception e, RedirectAttributes redirectAttributes, HttpServletRequest request) {

        String uri = request.getRequestURI();
        String reportId = request.getParameter("id");
        if (reportId == null && uri.contains("/")) {
            String[] parts = uri.split("/");
            reportId = parts[parts.length - 1];
        }

        logger.error("Wystapił błąd czatu: URI: {}, dla zgłoszenia o ID: {}, typ: {}, wiadomość: {}",
                uri, reportId, e.getClass().getSimpleName(), e.getMessage());

        if (uri.contains("/api/chat")) {
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            if (e instanceof UnauthorizedOperationException || e instanceof UserNotFoundException) status = HttpStatus.FORBIDDEN;
            if (e instanceof ReportNotFoundException) status = HttpStatus.NOT_FOUND;
            if (e instanceof IllegalArgumentException) status = HttpStatus.BAD_REQUEST;
            if (e instanceof ResponseStatusException rse) status = HttpStatus.valueOf(rse.getStatusCode().value());

            return ResponseEntity.status(status).body(Map.of("Wystapił błąd:", e.getMessage()));
        }

        String message = "Wystąpił błąd podczas obsługi zgłoszenia: ";
        String redirect = "/reports";

        if (e instanceof UnauthorizedOperationException) {
            message = "Brak uprawnień: ";
        } else if (e instanceof ReportNotFoundException) {
            message = "Nie znaleziono zgłoszenia: ";
        }

        redirectAttributes.addFlashAttribute("errorMessage", message + e.getMessage());
        return "redirect:" + redirect;
    }

    @MessageExceptionHandler({UnauthorizedOperationException.class, ReportNotFoundException.class, UserNotFoundException.class})
    public void handleWebSocketExceptions(Exception e, Principal principal) {
        logger.error("Wystapił błąd Websocket dla uzytkownika {}: {}", principal.getName(), e.getMessage());

        messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/errors",
                Map.of("errorMessage", e.getMessage())
        );
    }
}