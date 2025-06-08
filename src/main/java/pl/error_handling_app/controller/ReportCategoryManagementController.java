package pl.error_handling_app.controller;

import jakarta.validation.Valid;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.error_handling_app.report.dto.ReportCategoryDto;
import pl.error_handling_app.report.ReportCategoryService;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class ReportCategoryManagementController {

    private final ReportCategoryService reportCategoryService;

    public ReportCategoryManagementController(ReportCategoryService reportCategoryService) {
        this.reportCategoryService = reportCategoryService;
    }

    @GetMapping("/manage-categories")
    public String manageCategories(Model model) {
        model.addAttribute("categories", reportCategoryService.getAllCategories());
        model.addAttribute("newCategory", new ReportCategoryDto());
        return "manage-categories";
    }

    @PostMapping("/add-category")
    public String addCategory(@Valid @ModelAttribute("newCategory") ReportCategoryDto newCategory, BindingResult bindingResult,
                              Model model, RedirectAttributes redirectAttributes) {
        if(bindingResult.hasErrors()) {
            model.addAttribute("categories", reportCategoryService.getAllCategories());
            return "manage-categories";
        }

        reportCategoryService.addCategory(newCategory);
        redirectAttributes.addFlashAttribute("success", "Kategoria została dodana.");
        return "redirect:/admin/manage-categories";
    }

    @PostMapping("/edit-category/{id}")
    public String editCategory(@PathVariable Long id, @Valid @ModelAttribute("category") ReportCategoryDto category,
                               BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            redirectAttributes.addFlashAttribute("editErrors", errors);
            return "redirect:/admin/manage-categories";
        }

        reportCategoryService.editCategory(id, category);
        redirectAttributes.addFlashAttribute("success", "Edycja kategorii nastąpiła pomyślnie.");
        return "redirect:/admin/manage-categories";
    }

    @PostMapping("/delete-category/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        reportCategoryService.deleteCategory(id);
        redirectAttributes.addFlashAttribute("success", "Kategoria została usunięta.");
        return "redirect:/admin/manage-categories";
    }
}
