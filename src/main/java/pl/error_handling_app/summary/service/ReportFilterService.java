package pl.error_handling_app.summary.service;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import pl.error_handling_app.report.ReportSpecification;
import pl.error_handling_app.report.dto.ReportDto;
import pl.error_handling_app.report.entity.Report;
import pl.error_handling_app.report.entity.ReportCategory;
import pl.error_handling_app.report.dto.ReportDtoMapper;
import pl.error_handling_app.report.repository.ReportRepository;
import pl.error_handling_app.summary.dto.SummaryFormRequest;
import pl.error_handling_app.user.entity.User;
import pl.error_handling_app.user.service.UserService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReportFilterService {

    private final ReportRepository reportRepository;
    private final ReportDtoMapper reportMapper;
    private final UserService userService;

    public ReportFilterService(ReportRepository reportRepository, ReportDtoMapper reportMapper, UserService userService) {
        this.reportRepository = reportRepository;
        this.reportMapper = reportMapper;
        this.userService = userService;
    }

    public List<ReportDto> prepareFilteredReports(SummaryFormRequest request, ReportCategory category,
                                                  LocalDate dateFrom, LocalDate dateTo) {
        Specification<Report> spec = Specification.where(null);
        spec = spec.and((root, query, cb) -> cb.between(root.get("datedAdded"),
                dateFrom.atStartOfDay(),
                dateTo.atTime(LocalTime.MAX)));
        if (category != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category"), category));
        }
        if (request.status() != null) {
            spec = spec.and(ReportSpecification.filterBy(null, request.status()));
        }
        if (request.user() != null && !request.user().isBlank()) {
            Optional<User> userOpt = userService.findUserByEmail(request.user());
            if (userOpt.isPresent()) {
                spec = spec.and(ReportSpecification.filterByAssignedEmployee(userOpt.get()));
            }
        }
        return reportRepository.findAll(spec).stream()
                .map(reportMapper::mapToDto)
                .collect(Collectors.toList());
    }
}