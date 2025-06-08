package pl.error_handling_app.controller;

import jakarta.validation.Valid;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.error_handling_app.company.CompanyDto;
import pl.error_handling_app.company.CompanyService;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class CompanyManagementController {

    private final CompanyService companyService;

    public CompanyManagementController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping("/manage-companies")
    public String manageCompanies(Model model) {
        model.addAttribute("companies", companyService.findALlCompanies());
        model.addAttribute("newCompany", new CompanyDto());
        return "manage-companies";
    }

    @PostMapping("/add-company")
    public String addCompany(@Valid @ModelAttribute("newCompany") CompanyDto newCompany, BindingResult bindingResult,
                             Model model, RedirectAttributes redirectAttributes) {
        if(bindingResult.hasErrors()) {
            model.addAttribute("companies", companyService.findALlCompanies());
            return "manage-companies";
        }
        companyService.saveCompany(newCompany);
        redirectAttributes.addFlashAttribute("success", "Firma %s została utworzona.".formatted(newCompany.getName()));
        return "redirect:/admin/manage-companies";
    }

    @PostMapping("/edit-company/{id}")
    public String editCompany(@PathVariable Long id, @Valid @ModelAttribute("company") CompanyDto companyDto, BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            redirectAttributes.addFlashAttribute("editErrors", errors);
            return "redirect:/admin/manage-companies";
        }

        companyService.updateCompany(companyDto, id);
        redirectAttributes.addFlashAttribute("success", "Pomyślnie zaaktualizowano dane firmy " + companyDto.getName());
        return "redirect:/admin/manage-companies";
    }

    @PostMapping("/delete-company/{id}")
    public String deleteCompany(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        companyService.deleteCompany(id);
        redirectAttributes.addFlashAttribute("success", "Firma została usunięta.");
        return "redirect:/admin/manage-companies";
    }
}
