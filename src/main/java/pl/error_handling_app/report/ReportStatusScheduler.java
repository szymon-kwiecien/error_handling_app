package pl.error_handling_app.report;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportStatusScheduler {

    private final ReportRepository reportRepository;

    public ReportStatusScheduler(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Scheduled(fixedRate = 60000) //co 1 minutę
    public void updateOverdueReports() {
        LocalDateTime now = LocalDateTime.now();

        List<Report> overduePendingReports = reportRepository
                .findByStatusAndTimeToRespondLessThanEqual(ReportStatus.PENDING, now);

        List<Report> overdueUnderReviewReports = reportRepository
                .findByStatusAndDueDateLessThanEqual(ReportStatus.UNDER_REVIEW, now);

        List<Report> overdueReports = new ArrayList<>();
        overdueReports.addAll(overduePendingReports);
        overdueReports.addAll(overdueUnderReviewReports);

        for (Report report : overdueReports) {
            report.setStatus(ReportStatus.OVERDUE);
        }

        reportRepository.saveAll(overdueReports);
    }
}
