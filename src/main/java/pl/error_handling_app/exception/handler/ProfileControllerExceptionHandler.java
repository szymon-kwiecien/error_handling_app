package pl.error_handling_app.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.error_handling_app.controller.ProfileController;
import pl.error_handling_app.exception.InvalidEmailException;
import pl.error_handling_app.exception.InvalidPasswordException;
import pl.error_handling_app.exception.UnauthorizedOperationException;

@ControllerAdvice(assignableTypes = ProfileController.class)
public class ProfileControllerExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ProfileControllerExceptionHandler.class);

    @ExceptionHandler({InvalidEmailException.class, InvalidPasswordException.class, UnauthorizedOperationException.class})
    public String handleProfileUpdateExceptions(RuntimeException e, RedirectAttributes redirectAttributes, HttpServletRequest request) {

        String uri = request.getRequestURI();
        String message = "";

        logger.error("Wystąpił wyjatek podczas obsługi żądania: {}, typ wyjątku: {}, treść: {}",
                uri, e.getClass().getSimpleName(), e.getMessage());

        if(uri.contains("/change-email")) {
            message = "Podczas zmiany adresu e-mail wystąpił błąd: ";
        } else if(uri.contains("/change-password")) {
            message = "Podczas zmiany hasła wystąpił błąd: ";
        }

        redirectAttributes.addFlashAttribute("error", message + e.getMessage());

        return "redirect:/profile";

    }
}
