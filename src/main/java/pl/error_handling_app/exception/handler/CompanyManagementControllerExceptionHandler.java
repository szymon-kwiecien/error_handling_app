package pl.error_handling_app.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.error_handling_app.controller.CompanyManagementController;
import pl.error_handling_app.exception.CompanyAlreadyExistsException;
import pl.error_handling_app.exception.CompanyNotFoundException;

@ControllerAdvice(assignableTypes = CompanyManagementController.class)
public class CompanyManagementControllerExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(CompanyManagementControllerExceptionHandler.class);

    @ExceptionHandler({CompanyAlreadyExistsException.class, CompanyNotFoundException.class})
    public String handleCompanyManagementExceptions(RuntimeException e, RedirectAttributes redirectAttributes,
                                                    HttpServletRequest request) {
        String uri = request.getRequestURI();

        String typeOfAction = getTypeOfAction(uri);

        logger.error("Wystąpił wyjątek podczas obsługi żądania: {}, typ wyjątku: {}, treść: {}",
                uri, e.getClass().getSimpleName(), e.getMessage());

        redirectAttributes.addFlashAttribute("error",
                String.format("Podczas %s firmy wystąpił błąd: %s", typeOfAction, e.getMessage()));

        return "redirect:/admin/manage-companies";

    }

    private String getTypeOfAction(String uri) {
        if (uri.contains("/add-company")) {
            return "dodawania";
        } else if (uri.contains("/edit-company")) {
            return "edycji";
        } else if (uri.contains("/delete-company")) {
            return "usuwania";
        }
        return "wybranej operacji";
    }
}
