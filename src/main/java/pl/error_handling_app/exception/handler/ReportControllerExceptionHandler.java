package pl.error_handling_app.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.error_handling_app.controller.ReportController;
import pl.error_handling_app.exception.*;

@ControllerAdvice(assignableTypes = ReportController.class)
public class ReportControllerExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ReportControllerExceptionHandler.class);

    @ExceptionHandler({
            ReportNotFoundException.class,
            UnauthorizedOperationException.class,
            ReportAlreadyCompletedException.class,
            UserLacksRequiredRoleException.class,
            UserNotFoundException.class,
            CategoryNotFoundException.class,
            FileStorageException.class,
            InvalidAttachmentException.class
    })
    public String handleReportExceptions(RuntimeException e,
                                         RedirectAttributes redirectAttributes,
                                         HttpServletRequest request) {
        String uri = request.getRequestURI();
        String reportId = request.getParameter("reportId");

        logger.error("Wystąpił wyjatek podczas obsługi żądania: {}, dla zgłoszenia o id {},typ wyjątku: {}, treść: {}",
                uri, reportId, e.getClass().getSimpleName(), e.getMessage(), e);

        String message;
        String redirect;


        if(uri.contains("/add")) {
            message = "Podczas dodawania zgłoszenia wystąpił błąd: ";
            redirect = "/reports/add";
        }
        else if (uri.contains("/delete")) {
            message = "Podczas usuwania zgłoszenia wystąpił błąd: ";
            redirect = "/reports";
        } else if (uri.contains("/close")) {
            message = "Podczas zamykania zgłoszenia wystąpił błąd: ";
            redirect = (reportId != null) ? "/report?id=" + reportId : "/reports";
        } else if (uri.contains("/assign")) {
            message = "Nie udało się przypisać pracownika do zgłoszenia: ";
            redirect = (reportId != null) ? "/report?id=" + reportId : "/reports";
        } else if (uri.contains("/attachment/upload")) {
            message = "Wystąpił błąd podczas dodawania załączników: ";
            redirect = (reportId != null) ? "/report?id=" + reportId : "/reports";
        } else {
            message = "Wystąpił błąd: ";
            redirect = "/reports";
        }

        redirectAttributes.addFlashAttribute("errorMessage", message + e.getMessage());
        return "redirect:" + redirect;
    }
}
