package pl.error_handling_app;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.error_handling_app.company.Company;
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
        List<CompanyDto> companies = companyService.findALlCompanies();
        model.addAttribute("companies", companies);
        return "manage-companies";
    }

    @PostMapping("/add-company")
    public String addCompany(CompanyDto newCompany, RedirectAttributes redirectAttributes) {
        try {
            companyService.saveCompany(newCompany);
            redirectAttributes.addFlashAttribute("success", "Firma została utworzona.");
        } catch(IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Podczas dodawania firmy wystąpił błąd: " + e.getMessage());
        }
        return "redirect:/admin/manage-companies";
    }
}
