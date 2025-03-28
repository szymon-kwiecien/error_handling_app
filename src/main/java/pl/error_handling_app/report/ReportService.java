package pl.error_handling_app.report;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportService {

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public Page<ReportDto> findAllReports(Pageable pageable) {
        return reportRepository.findAll(pageable).map(this::mapToDto);
    }

    public Page<ReportDto> findReportsByStatus(ReportStatus status, Pageable pageable) {
        return reportRepository.findAllByStatus(status, pageable).map(this::mapToDto);
    }

    private ReportDto mapToDto(Report report) {
        ReportDto reportDto = new ReportDto();
        reportDto.setId(report.getId());
        reportDto.setTitle(report.getTitle());
        reportDto.setDescription(report.getDescription());
        reportDto.setDateAdded(report.getDatedAdded());
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
