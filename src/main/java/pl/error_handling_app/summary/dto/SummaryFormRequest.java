package pl.error_handling_app.summary.dto;

import pl.error_handling_app.report.ReportStatus;
import java.time.LocalDate;

public record SummaryFormRequest(
        LocalDate dateFrom,
        LocalDate dateTo,
        String categoryName,
        ReportStatus status,
        String user,
        String sort,
        Boolean showReportsTable,
        Boolean showCharts
) {
    public SummaryFormRequest {
        if (user == null) {
            user = "all";
        }
        if (showReportsTable == null) {
            showReportsTable = false;
        }
        if (showCharts == null) {
            showCharts = false;
        }
    }
}