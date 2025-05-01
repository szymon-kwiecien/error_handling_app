package pl.error_handling_app.summary;

import org.springframework.stereotype.Service;
import pl.error_handling_app.report.ReportCategory;
import pl.error_handling_app.report.ReportService;
import pl.error_handling_app.report.dto.ReportDto;
import pl.error_handling_app.user.User;
import pl.error_handling_app.user.UserService;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReportFilterService {

    private final ReportService reportService;
    private final UserService userService;

    public ReportFilterService(ReportService reportService, UserService userService) {
        this.reportService = reportService;
        this.userService = userService;
    }

    public List<ReportDto> prepareFilteredReports(SummaryFormRequest request, ReportCategory category) {
        List<ReportDto> reports = new ArrayList<>(reportService.getReportsByDateRange(
                request.getDateFrom().atStartOfDay(),
                request.getDateTo().atTime(LocalTime.MAX))
        );

        if (category != null || request.getStatus() != null) {
            if (category != null && request.getStatus() != null) {
                reports.retainAll(reportService.getReportsByCategoryAndStatus(category, request.getStatus()));
            } else if (category != null) {
                reports.retainAll(reportService.filterReportsByCategory(category));
            } else {
                reports.retainAll(reportService.filterReportsByStatus(request.getStatus()));
            }
        }

        Optional<User> userOpt = userService.findUserByEmail(request.getUser());
        userOpt.ifPresent(user -> reports.retainAll(reportService.filterReportsByAssignedEmployee(reports, user)));

        return reports;
    }
}

