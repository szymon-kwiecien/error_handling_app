package pl.error_handling_app.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.error_handling_app.controller.ReportCategoryManagementController;
import pl.error_handling_app.exception.CategoryAlreadyExistsException;
import pl.error_handling_app.exception.CategoryNotFoundException;


@ControllerAdvice(assignableTypes = ReportCategoryManagementController.class)
public class ReportCategoryManagementControllerExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ReportCategoryManagementControllerExceptionHandler.class);

    @ExceptionHandler({CategoryAlreadyExistsException.class, CategoryNotFoundException.class})
    public String handleReportCategoryManagementExceptions(RuntimeException e, RedirectAttributes redirectAttributes,
                                                    HttpServletRequest request) {
        String uri = request.getRequestURI();

        String typeOfAction = getTypeOfAction(uri);

        logger.error("Wystąpił wyjątek podczas obsługi żądania: {}, typ wyjątku: {}, treść: {}",
                uri, e.getClass().getSimpleName(), e.getMessage());

        redirectAttributes.addFlashAttribute("error",
                String.format("Podczas %s kategorii wystąpił błąd: %s", typeOfAction, e.getMessage()));

        return "redirect:/admin/manage-categories";

    }

    private String getTypeOfAction(String uri) {
        if (uri.contains("/add-category")) {
            return "dodawania";
        } else if (uri.contains("/edit-category")) {
            return "edycji";
        } else if (uri.contains("/delete-category")) {
            return "usuwania";
        }
        return "wybranej operacji";
    }

}
