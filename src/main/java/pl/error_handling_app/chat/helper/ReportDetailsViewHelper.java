package pl.error_handling_app.chat.helper;

import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import pl.error_handling_app.report.ReportStatus;
import pl.error_handling_app.report.dto.ReportDetailsDto;
import pl.error_handling_app.report.dto.ReportDto;
import pl.error_handling_app.report.service.ReportService;
import java.util.List;


@Component
public class ReportDetailsViewHelper {

    private final ReportService reportService;

    public ReportDetailsViewHelper(ReportService reportService) {
        this.reportService = reportService;
    }

    public void prepareReportViewModel(Model model, ReportDetailsDto report) {
        model.addAttribute("statusColor", getStatusColor(report.getStatus()));

        if (report.getStatus() == ReportStatus.PENDING) {
            model.addAttribute("remainingTimeToFirstRespond", calculateRemainingTime(report, true));
            model.addAttribute("remainingTimeToComplete", calculateRemainingTime(report, false));
        } else if (report.getStatus() == ReportStatus.UNDER_REVIEW) {
            model.addAttribute("remainingTimeToComplete", calculateRemainingTime(report, false));
        }

        int respondProgress = calculateLeftTimePercentage(report, true);
        int resolveProgress = calculateLeftTimePercentage(report, false);

        model.addAttribute("timeToRespondProgress", respondProgress);
        model.addAttribute("timeToResolveProgress", resolveProgress);
        model.addAttribute("timeToRespondColor", getProgressColor(respondProgress));
        model.addAttribute("timeToResolveColor", getProgressColor(resolveProgress));
    }

    String getStatusColor(ReportStatus status) {
        return switch (status) {
            case PENDING -> "orange";
            case UNDER_REVIEW -> "yellow";
            case COMPLETED -> "green";
            case OVERDUE -> "red";
        };
    }

    String getProgressColor(int progress) {
        if (progress >= 70) return "green";
        if (progress >= 25) return "yellow";
        return "red";
    }

    private int calculateLeftTimePercentage(ReportDetailsDto report, boolean forFirstRespond) {
        return reportService.calculateTimeLeftPercentage(mapToReportDto(report, forFirstRespond));
    }

    private String calculateRemainingTime(ReportDetailsDto report, boolean forFirstRespond) {
        return reportService.calculateRemainingTime(List.of(mapToReportDto(report, forFirstRespond)))[0];
    }

    private ReportDto mapToReportDto(ReportDetailsDto report, boolean forFirstRespond) {
        ReportDto dto = new ReportDto();
        dto.setDateAdded(report.getDateAdded());
        dto.setToRespondDate(report.getTimeToRespond());
        dto.setDueDate(report.getDueDate());
        dto.setStatusName(forFirstRespond ? report.getStatus().description : "W trakcie");
        return dto;
    }
}