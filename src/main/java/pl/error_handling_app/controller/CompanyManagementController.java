package pl.error_handling_app.controller;

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
import pl.error_handling_app.company.CompanyDto;
import pl.error_handling_app.company.CompanyService;
import pl.error_handling_app.utils.PaginationUtils;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class CompanyManagementController {

    private final CompanyService companyService;

    public CompanyManagementController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping("/manage-companies")
    public String manageCompanies(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size, Model model) {
        prepareCompanyManagementModel(model, page, size);
        return "manage-companies";
    }

    @PostMapping("/add-company")
    public String addCompany(@Valid @ModelAttribute("newCompany") CompanyDto newCompany, BindingResult bindingResult,
                             @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size,
                             Model model, RedirectAttributes redirectAttributes) {
        if(bindingResult.hasErrors()) {
            prepareCompanyManagementModel(model, page, size);
            return "manage-companies";
        }
        companyService.saveCompany(newCompany);
        redirectAttributes.addFlashAttribute("success", "Firma %s została utworzona.".formatted(newCompany.getName()));
        return redirectToCompanyManagementPage(page,size);
    }

    @PostMapping("/edit-company/{id}")
    public String editCompany(@PathVariable Long id, @Valid @ModelAttribute("company") CompanyDto companyDto, BindingResult bindingResult,
                              @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            redirectAttributes.addFlashAttribute("editErrors", errors);
            return redirectToCompanyManagementPage(page,size);
        }

        companyService.updateCompany(companyDto, id);
        redirectAttributes.addFlashAttribute("success", "Pomyślnie zaaktualizowano dane firmy " + companyDto.getName());
        return redirectToCompanyManagementPage(page,size);
    }

    @PostMapping("/delete-company/{id}")
    public String deleteCompany(@PathVariable Long id, RedirectAttributes redirectAttributes,
                                @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {

        companyService.deleteCompany(id);
        redirectAttributes.addFlashAttribute("success", "Firma została usunięta.");
        return redirectToCompanyManagementPage(page,size);
    }

    private void prepareCompanyManagementModel(Model model, int page, int size) {
        Pageable pageable = PaginationUtils.createPageable(page, size, Sort.unsorted());
        Page<CompanyDto> companiesPage = companyService.findPagedCompanies(pageable);

        if(!model.containsAttribute("newCompany")) {
            model.addAttribute("newCompany", new CompanyDto());
        }
        model.addAttribute("companies", companiesPage);
        model.addAttribute("currentPage", pageable.getPageNumber() + 1);
        model.addAttribute("pageSize", pageable.getPageSize());
        model.addAttribute("totalPages", companiesPage.getTotalPages());
    }

    private String redirectToCompanyManagementPage(int page, int size) {
        return UriComponentsBuilder.fromPath("redirect:/admin/manage-companies")
                .queryParam("page", page)
                .queryParam("size", size)
                .toUriString();
    }
}
