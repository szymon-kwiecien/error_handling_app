package pl.error_handling_app.summary;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.thymeleaf.context.Context;
import pl.error_handling_app.report.*;
import pl.error_handling_app.report.dto.ReportCategoryDto;
import pl.error_handling_app.report.dto.ReportDto;
import pl.error_handling_app.user.UserRepository;

import java.time.LocalDate;
import java.util.*;


@Controller
public class SummaryController {

    private final ReportService reportService;
    private final UserRepository userRepository;
    private final ReportFilterService reportFilterService;
    private final ChartDataPreparer chartDataPreparer;
    private final ReportCategoryService reportCategoryService;
    private final SummaryPdfGenerator pdfGenerator;
    private final EmailUtils emailUtils;

    public SummaryController(ReportService reportService, UserRepository userRepository, ReportFilterService reportFilterService, ChartDataPreparer chartDataPreparer,
                             ReportCategoryService reportCategoryService, SummaryPdfGenerator pdfGenerator, EmailUtils emailUtils) {
        this.reportService = reportService;
        this.userRepository = userRepository;
        this.reportFilterService = reportFilterService;
        this.chartDataPreparer = chartDataPreparer;
        this.reportCategoryService = reportCategoryService;
        this.pdfGenerator = pdfGenerator;
        this.emailUtils = emailUtils;
    }

    @GetMapping("/summaries")
    public String getSummariesPanel(Model model) {
        List<String> categories = reportCategoryService.getAllCategories().stream()
                .map(ReportCategoryDto::getName).toList();
        model.addAttribute("categories", categories);
        model.addAttribute("statusList", ReportStatus.values());
        if (currentUserIsAdmin()) {
            model.addAttribute("employees", userRepository.findALlByRoles_Name("EMPLOYEE"));
        } else {
            String currentUser = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow().getEmail();
            model.addAttribute("currentEmployee", currentUser);
        }
        return "summaries-panel";
    }

    private boolean currentUserIsAdmin() {
        List<String> currentUserRoles = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().
                map(GrantedAuthority::getAuthority).toList();
        return currentUserRoles.contains("ROLE_ADMINISTRATOR");
    }

    @PostMapping("/generate-summary")
    public void generateSummary(SummaryFormRequest formRequest, HttpServletResponse response) {

        LocalDate[] localDates = DateUtils.checkAnyDateIsNull(formRequest.getDateFrom(), formRequest.getDateTo());
        formRequest.setDateFrom(localDates[0]);
        formRequest.setDateTo(localDates[1]);

        ReportCategory category = reportCategoryService.getCategoryByName(formRequest.getCategoryName()).orElse(null);
        List<ReportDto> reports = reportFilterService.prepareFilteredReports(formRequest, category);
        List<ReportDto> allFoundReports = new ArrayList<>(reports);

        String sortedBy = reportService.sortReports(reports, formRequest.getSort());

        //Rozdzielenie adresow email potrzebne do poprawengo wyswietlania ich w raporcie pdf
        EmailUtils.EmailPartsMap emailParts = emailUtils.extractEmailParts(reports);

        Context context = SummaryContextBuilder.build(formRequest.getDateFrom(), formRequest.getDateTo(),
                formRequest.getStatus(), formRequest.getUser(), reports, category, sortedBy);
        if(formRequest.isShowReportsTable()) {
            String[] remainingTime = reportService.calculateRemainingTime(reports);
            context.setVariable("remainingTime", remainingTime);
            context.setVariable("reportingUserEmailLocalPart", emailParts.reportingLocalPart());
            context.setVariable("reportingUserEmailDomain", emailParts.reportingDomain());
            context.setVariable("assignedUserEmailLocalPart", emailParts.assignedLocalPart());
            context.setVariable("assignedUserEmailDomain", emailParts.assignedDomain());
            context.setVariable("showReportsTable", formRequest.isShowReportsTable());
        }
        if (formRequest.isShowCharts()) {
            context.setVariable("showCharts", true);
            boolean singleEmployee = !formRequest.getUser().equals("all");

            Map<String, Object> chartData = chartDataPreparer.prepareCharts(
                    reports,
                    allFoundReports,
                    formRequest.getDateFrom(),
                    formRequest.getDateTo(),
                    singleEmployee
            );
            context.setVariable("categoryReportChart", chartData.get("categoryChart"));
            context.setVariable("statusReportChart", chartData.get("statusChart"));
            context.setVariable("averageCompletionTimeChart", chartData.get("averageCompletionTimeChart"));
        }

        pdfGenerator.generatePdf("summary-template", context, response);
    }
}
