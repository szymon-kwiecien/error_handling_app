package pl.error_handling_app.report;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class ReportService {

    private final static String PENDING_STATUS_POLISH_NAME = "Oczekujące";

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public Page<ReportDto> findReports(String titelFragment, ReportStatus status, Pageable pageable) {
        Specification<Report> reportSpecification = ReportSpecification.filterBy(titelFragment, status);
        return reportRepository.findAll(reportSpecification, pageable).map(this::mapToDto);
    }

    public int calculateTimeLeftPercentage(ReportDto report) {
        Instant now = Instant.now();
        Instant dateAdded = report.getDateAdded().atZone(ZoneId.systemDefault()).toInstant();
        Instant toFirstRespondDate = report.getToFirstRespondDate().atZone(ZoneId.systemDefault()).toInstant(); //gdy zgloszenie ma status PENDING(oczekujace)
        Instant dueDate = report.getDueDate().atZone(ZoneId.systemDefault()).toInstant(); // gdy zgłoszenie ma status różny od PENDING

        long totalDuration = report.getStatusName()
                .equalsIgnoreCase(PENDING_STATUS_POLISH_NAME) ? Duration.between(dateAdded, toFirstRespondDate).toSeconds() : Duration.between(dateAdded, dueDate).toSeconds();
        long remainingDuration = report.getStatusName()
                .equalsIgnoreCase(PENDING_STATUS_POLISH_NAME) ? Duration.between(now, toFirstRespondDate).toSeconds() : Duration.between(now, dueDate).toSeconds();

        if (remainingDuration <= 0) return 0; //gdy minął termin to zwracamy 0
        if (remainingDuration >= totalDuration) return 100; //gdy pozostały czas jest wiekszy lub równy całkowitemu to zwracam 100

        return (int) ((remainingDuration * 100) / totalDuration); //ilosc pozostalego czasu wzgledem calkowitego czasu (w procentach)
    }

    private ReportDto mapToDto(Report report) {
        ReportDto reportDto = new ReportDto();
        reportDto.setId(report.getId());
        reportDto.setTitle(report.getTitle());
        reportDto.setDescription(report.getDescription());
        reportDto.setDateAdded(report.getDatedAdded());
        reportDto.setDueDate(report.getDueDate());
        reportDto.setToFirstRespondDate(report.getTimeToRespond());
        String categoryName = report.getCategory() != null ? report.getCategory().getName() : "-";
        reportDto.setCategoryName(categoryName);
        String statusName = report.getStatus() != null ? report.getStatus().description : "-";
        reportDto.setStatusName(statusName);
        String assignedEmployee = report.getAssignedEmployee() != null ? report.getAssignedEmployee().getEmail() : "-";
        reportDto.setAssignedEmployee(assignedEmployee);
        String reportingUser = report.getReportingUser() != null ? report.getReportingUser().getEmail() : "-";
        reportDto.setReportingUser(reportingUser);
        reportDto.setLastMessageTime(LocalDateTime.MAX); //tymczasowo (póki nie wprowadziłem modułu czatu)
        return reportDto;
    }


}
