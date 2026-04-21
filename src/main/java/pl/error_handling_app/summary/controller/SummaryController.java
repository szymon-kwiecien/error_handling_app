package pl.error_handling_app.summary.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.thymeleaf.context.Context;
import pl.error_handling_app.report.ReportStatus;
import pl.error_handling_app.report.dto.ReportCategoryDto;
import pl.error_handling_app.report.service.ReportCategoryService;
import pl.error_handling_app.summary.dto.SummaryFormRequest;
import pl.error_handling_app.summary.service.SummaryDataService;
import pl.error_handling_app.summary.service.SummaryPdfGenerator;
import pl.error_handling_app.user.repository.UserRepository;

import java.util.List;

@Controller
public class SummaryController {

    private static final String ROLE_ADMIN = "ROLE_ADMINISTRATOR";
    private final UserRepository userRepository;
    private final ReportCategoryService reportCategoryService;
    private final SummaryDataService summaryDataService;
    private final SummaryPdfGenerator pdfGenerator;

    public SummaryController(UserRepository userRepository,
                             ReportCategoryService reportCategoryService,
                             SummaryDataService summaryDataService,
                             SummaryPdfGenerator pdfGenerator) {
        this.userRepository = userRepository;
        this.reportCategoryService = reportCategoryService;
        this.summaryDataService = summaryDataService;
        this.pdfGenerator = pdfGenerator;
    }

    @GetMapping("/summaries")
    public String getSummariesPanel(Model model) {
        List<String> categories = reportCategoryService.getAllCategories().stream()
                .map(ReportCategoryDto::name).toList();

        model.addAttribute("categories", categories);
        model.addAttribute("statusList", ReportStatus.values());

        if (currentUserHasRole(ROLE_ADMIN)) {
            model.addAttribute("employees", userRepository.findALlByRoles_Name("EMPLOYEE"));
        } else {
            String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            model.addAttribute("currentEmployee", currentUserEmail);
        }
        return "summary/summaries-panel";
    }

    @PostMapping("/generate-summary")
    public void generateSummary(SummaryFormRequest formRequest, HttpServletResponse response) {
        Context context = summaryDataService.prepareSummaryContext(formRequest);
        pdfGenerator.generatePdf("summary/summary-template", context, response);
    }

    private boolean currentUserHasRole(String role) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role::equals);
    }
}