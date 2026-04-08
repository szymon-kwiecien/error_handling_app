package pl.error_handling_app.report.controller;

import jakarta.validation.Valid;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;
import pl.error_handling_app.company.dto.CompanyDto;
import pl.error_handling_app.report.dto.ReportCategoryDto;
import pl.error_handling_app.report.service.ReportCategoryService;
import pl.error_handling_app.utils.PaginationUtils;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class ReportCategoryManagementController {

    private final ReportCategoryService reportCategoryService;

    public ReportCategoryManagementController(ReportCategoryService reportCategoryService) {
        this.reportCategoryService = reportCategoryService;
    }

    @GetMapping("/manage-categories")
    public String manageCategories(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size, Model model) {
        prepareCategoryManagementModel(model, page, size);
        return "report/manage-categories";
    }

    @PostMapping("/add-category")
    public String addCategory(@Valid @ModelAttribute("newCategory") ReportCategoryDto newCategory, BindingResult bindingResult,
                              @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size,
                              Model model, RedirectAttributes redirectAttributes) {
        if(bindingResult.hasErrors()) {
            prepareCategoryManagementModel(model, page, size);
            return "report/manage-categories";
        }

        reportCategoryService.addCategory(newCategory);
        redirectAttributes.addFlashAttribute("success", "Kategoria została dodana.");
        return redirectToCategoryManagementPage(page, size);
    }

    @PostMapping("/edit-category/{id}")
    public String editCategory(@PathVariable Long id, @Valid @ModelAttribute("category") ReportCategoryDto category,
                               BindingResult bindingResult,RedirectAttributes redirectAttributes,
                               @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            redirectAttributes.addFlashAttribute("editErrors", errors);
            return redirectToCategoryManagementPage(page, size);
        }

        reportCategoryService.editCategory(id, category);
        redirectAttributes.addFlashAttribute("success", "Edycja kategorii nastąpiła pomyślnie.");
        return redirectToCategoryManagementPage(page, size);
    }

    @PostMapping("/delete-category/{id}")
    public String deleteCategory(@PathVariable Long id, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size,
                                 RedirectAttributes redirectAttributes) {

        reportCategoryService.deleteCategory(id);
        redirectAttributes.addFlashAttribute("success", "Kategoria została usunięta.");
        return redirectToCategoryManagementPage(page, size);
    }

    private void prepareCategoryManagementModel(Model model, int page, int size) {
        Pageable pageable = PaginationUtils.createPageable(page, size, Sort.unsorted());
        Page<ReportCategoryDto> categoriesPage = reportCategoryService.getPagedCategories(pageable);

        if(!model.containsAttribute("newCategory")) {
            model.addAttribute("newCategory", CompanyDto.empty());
        }
        model.addAttribute("categories", categoriesPage);
        model.addAttribute("currentPage", pageable.getPageNumber() + 1);
        model.addAttribute("pageSize", pageable.getPageSize());
        model.addAttribute("totalPages", categoriesPage.getTotalPages());
    }

    private String redirectToCategoryManagementPage(int page, int size) {
        return UriComponentsBuilder.fromPath("redirect:/admin/manage-categories")
                .queryParam("page", page)
                .queryParam("size", size)
                .toUriString();
    }

}
