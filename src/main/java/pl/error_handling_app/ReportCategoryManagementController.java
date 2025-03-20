package pl.error_handling_app;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.error_handling_app.report.ReportCategoryDto;
import pl.error_handling_app.report.ReportCategoryService;

import java.util.NoSuchElementException;

@Controller
@RequestMapping("/admin")
public class ReportCategoryManagementController {

    private final ReportCategoryService reportCategoryService;

    public ReportCategoryManagementController(ReportCategoryService reportCategoryService) {
        this.reportCategoryService = reportCategoryService;
    }

    @GetMapping("/manage-categories")
    public String manageCategories(Model model) {
        model.addAttribute("categories", reportCategoryService.findAllCategories());
        return "manage-categories";
    }

    @PostMapping("/add-category")
    public String addCategory(ReportCategoryDto newCategory, RedirectAttributes redirectAttributes) {
        try {
            reportCategoryService.addCategory(newCategory);
            redirectAttributes.addFlashAttribute("success", "Kategoria została dodana.");
        } catch(IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Podczas dodawania kategorii wystąpił błąd: " + e.getMessage());
        }
        return "redirect:/admin/manage-categories";
    }

    @PostMapping("/edit-category/{id}")
    public String editCategory(@PathVariable Long id, @RequestParam("name") String newCategoryName, RedirectAttributes redirectAttributes) {
        try {
            reportCategoryService.editCategory(id, newCategoryName);
            redirectAttributes.addFlashAttribute("success", "Edycja kategorii nastąpiła pomyślnie.");
        } catch(IllegalArgumentException | NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("error", "Podczas edycji kategorii wystąpił błąd: " + e.getMessage());
        }
        return "redirect:/admin/manage-categories";
    }
}
