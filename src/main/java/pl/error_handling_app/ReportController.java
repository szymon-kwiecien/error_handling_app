package pl.error_handling_app;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.error_handling_app.report.*;

import java.awt.*;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;
    private final ReportCategoryService reportCategoryService;

    public ReportController(ReportService reportService, ReportCategoryService reportCategoryService) {
        this.reportService = reportService;
        this.reportCategoryService = reportCategoryService;
    }


    @GetMapping()
    public String listReports(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "5") int size,
                              @RequestParam(defaultValue = "all") String status,
                              @RequestParam(defaultValue = "") String search,
                              @RequestParam(defaultValue = "addedDateDesc")String sort,Model model) {

        Sort sorting = getSort(sort);
        Pageable pageable = PageRequest.of(--page, size, sorting);
        ReportStatus reportStatus = status.equals("all") ? null : ReportStatus.valueOf(status);
        Page<ReportDto> reports = reportService.findReports(search, reportStatus, pageable);
        reports.forEach(report -> report.setLeftTimePercentage(reportService.calculateTimeLeftPercentage(report)));
        model.addAttribute("reports", reports);
        model.addAttribute("currentPage", ++page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", reports.getTotalPages());
        model.addAttribute("status", status);
        model.addAttribute("search",search);
        model.addAttribute("sort", sort);
        return "report-listing";
    }

    @GetMapping("/add")
    public String addReportForm(Model model) {
        model.addAttribute("report", new NewReportDto());
        model.addAttribute("categories", reportCategoryService.getAllCategories());
        return "add-new-report";
    }

    @PostMapping(value = "/add", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public String addReport(NewReportDto report, BindingResult bindingResult, Model model) {
        if(bindingResult.hasErrors()) {
            return "add-new-report";        // wkrótce dodam walidację
        }
        try {
            reportService.addNewReport(report);
        } catch(UsernameNotFoundException | IllegalArgumentException e) {
            model.addAttribute("error", "Wystąpił błąd podczas dodawania zgłoszenia. Spróbuj ponownie.");
            return "add-new-report";
        }
            return "redirect:/reports/add?success";
    }


    private Sort getSort(String sort) {
        return switch (sort) {
            case "addedDateAsc" -> Sort.by("datedAdded").ascending();
            case "remainingTimeAsc" -> Sort.by("dueDate").ascending();
            case "remainingTimeDesc" -> Sort.by("dueDate").descending();
            default -> Sort.by("datedAdded").descending(); //domyslnie sortuje wg daty dodania (od najnowszych)
        };

}
}
