package pl.error_handling_app;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.error_handling_app.report.*;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }


    @GetMapping()
    public String listReports(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "5") int size,
                              @RequestParam(defaultValue = "all") String status, Model model) {

        Pageable pageable = PageRequest.of(--page, size);
        Page<ReportDto> reports;
        if(status.equals("all")) {
            reports = reportService.findAllReports(pageable);
        } else {
            ReportStatus reportStatus = ReportStatus.valueOf(status);
            reports = reportService.findReportsByStatus(reportStatus, pageable);
        }
        model.addAttribute("reports", reports);
        model.addAttribute("currentPage", ++page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", reports.getTotalPages());
        model.addAttribute("status", status);
        return "report-listing";
    }

}
