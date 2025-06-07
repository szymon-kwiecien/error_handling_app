package pl.error_handling_app.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.error_handling_app.controller.UserManagementController;
import pl.error_handling_app.exception.*;

@ControllerAdvice(assignableTypes = UserManagementController.class)
public class UserManagementControllerExceptionHandler {


    private static final Logger logger = LoggerFactory.getLogger(UserManagementControllerExceptionHandler.class);


    @ExceptionHandler({UserAlreadyExistsException.class, UserNotFoundException.class, InvalidEmailException.class,
            CompanyNotFoundException.class, RoleNotFoundException.class})
    public String handleUserManagementExceptions(RuntimeException e, RedirectAttributes redirectAttributes,
                                                 HttpServletRequest request) {
        String uri = request.getRequestURI();

        String typeOfAction = getTypeOfAction(uri);

        logger.error("Wystąpił wyjątek podczas obsługi żądania: {}, typ wyjątku: {}, treść: {}",
                uri, e.getClass().getSimpleName(), e.getMessage());

        redirectAttributes.addFlashAttribute("error",
                String.format("Podczas %s użytkownika wystąpił błąd: %s", typeOfAction, e.getMessage()));

        return "redirect:/admin/manage-users";
    }

    private String getTypeOfAction(String uri) {
        if (uri.contains("/add-user")) {
            return "dodawania";
        } else if (uri.contains("/edit-user")) {
            return "edycji";
        } else if (uri.contains("/delete-user")) {
            return "usuwania";
        }
        return "wybranej operacji";
    }

}
