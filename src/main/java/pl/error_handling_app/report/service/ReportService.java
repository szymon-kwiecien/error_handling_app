package pl.error_handling_app.report.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pl.error_handling_app.attachment.entity.Attachment;
import pl.error_handling_app.exception.*;
import pl.error_handling_app.file.FileService;
import pl.error_handling_app.report.ReportSpecification;
import pl.error_handling_app.report.ReportStatus;
import pl.error_handling_app.report.dto.*;
import pl.error_handling_app.report.entity.Report;
import pl.error_handling_app.report.entity.ReportCategory;
import pl.error_handling_app.report.dto.ReportDtoMapper;
import pl.error_handling_app.report.repository.ReportRepository;
import pl.error_handling_app.user.entity.User;
import pl.error_handling_app.user.repository.UserRepository;
import java.time.*;
import java.util.*;

@Service
public class ReportService {

    public final static String PENDING_STATUS_POLISH_NAME = "Oczekujące";
    public final static String COMPLETED_STATUS_POLISH_NAME = "Zakończone";
    public final static String OVERDUE_STATUS_POLISH_NAME = "Nieobsłużone w terminie";

    private final ReportRepository reportRepository;
    private final ReportCategoryService reportCategoryService;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final ReportDtoMapper reportMapper;
    private final ReportStatisticsService statisticsService;

    public ReportService(ReportRepository reportRepository,
                         ReportCategoryService reportCategoryService,
                         UserRepository userRepository,
                         FileService fileService,
                         ReportDtoMapper reportMapper,
                         ReportStatisticsService statisticsService) {
        this.reportRepository = reportRepository;
        this.reportCategoryService = reportCategoryService;
        this.userRepository = userRepository;
        this.fileService = fileService;
        this.reportMapper = reportMapper;
        this.statisticsService = statisticsService;
    }

    public Page<ReportDto> findReports(String titleFragment, ReportStatus status, Pageable pageable) {
        User user = getCurrentUser();
        boolean isAdmin = user.getRoles().stream().anyMatch(role -> role.getName().equals("ADMINISTRATOR"));
        boolean isEmployee = user.getRoles().stream().anyMatch(role -> role.getName().equals("EMPLOYEE"));

        Specification<Report> spec = ReportSpecification.filterBy(titleFragment, status);

        if (isAdmin) {
            return reportRepository.findAll(spec, pageable).map(reportMapper::mapToDto);
        } else if (isEmployee) {
            spec = spec.and(ReportSpecification.filterByAssignedEmployee(user));
        } else {
            spec = spec.and(ReportSpecification.filterByReportingUser(user));
        }
        return reportRepository.findAll(spec, pageable).map(reportMapper::mapToDto);
    }

    public int calculateTimeLeftPercentage(ReportDto report) {
        if (OVERDUE_STATUS_POLISH_NAME.equals(report.getStatusName()) || COMPLETED_STATUS_POLISH_NAME.equals(report.getStatusName())) {
            return 0;
        }
        LocalDateTime targetDate = resolveDeadline(report);
        long totalDuration = Duration.between(report.getDateAdded(), targetDate).toSeconds();
        long remainingDuration = Duration.between(LocalDateTime.now(), targetDate).toSeconds();
        if (remainingDuration <= 0) return 0;
        return remainingDuration >= totalDuration ? 100 : (int) Math.ceil((remainingDuration * 100.0) / totalDuration);
    }

    public String[] calculateRemainingTime(List<ReportDto> reports) {
        return reports.stream()
                .map(report -> {
                    if (COMPLETED_STATUS_POLISH_NAME.equals(report.getStatusName()) ||
                            OVERDUE_STATUS_POLISH_NAME.equals(report.getStatusName())) return "-";
                    return RemainingTime.calculate(resolveDeadline(report)).format();
                })
                .toArray(String[]::new);
    }

    @Transactional
    public void addNewReport(NewReportDto newReportDto) {
        ReportCategory category = reportCategoryService.getCategoryById(newReportDto.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Nie znaleziono kategorii"));
        User user = getCurrentUser();

        Report report = createReportEntity(newReportDto, user, category);
        if (fileService.areFilesValid(newReportDto.getFile())) {
            List<Attachment> attachments = fileService.storeFiles(newReportDto.getFile(), user.getEmail());
            report.setAttachments(attachments);
        }
        reportRepository.save(report);
    }

    @Transactional
    public void closeReport(Long reportId, String currentUserName) {
        Report report = getAuthorizedReport(reportId, currentUserName);
        if (report.getStatus() == ReportStatus.COMPLETED || report.getStatus() == ReportStatus.OVERDUE) {
            throw new ReportAlreadyCompletedException("Zgłoszenie jest już zakończone.");
        }
        report.setStatus(ReportStatus.COMPLETED);
        report.setAddedToCompleteDuration();
    }

    @Transactional
    public void deleteReport(Long reportId, String currentUserName) {
        Report report = getAuthorizedReport(reportId, currentUserName);
        reportRepository.delete(report);
    }

    @Transactional
    public void assignEmployeeToReport(Long reportId, Long employeeId) {
        Report report = reportRepository.findById(reportId).orElseThrow(() -> new ReportNotFoundException("Zgłoszenie nie istnieje"));
        User employee = userRepository.findById(employeeId).orElseThrow(() -> new UserNotFoundException("Pracownik nie istnieje"));

        if (employee.getRoles().stream().noneMatch(role -> role.getName().equals("EMPLOYEE"))) {
            throw new UserLacksRequiredRoleException("Wybrany użytkownik nie jest pracownikiem!");
        }

        if (report.getAssignedEmployee() == null) report.setAddedToFirstReactionDuration();
        report.setAssignedEmployee(employee);
        report.setStatus(ReportStatus.UNDER_REVIEW);
    }

    @Transactional
    public void addAttachmentsToExistingReport(Long reportId, List<MultipartFile> files) {

        if (!fileService.areFilesValid(files)) {
            throw new InvalidAttachmentException("Pliki są niepoprawne. Spróbuj jeszcze raz");
        }
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException("Zgłoszenie nie istnieje"));
        User currentUser = getCurrentUser();
        if (!isUserAuthorizedForReport(report, currentUser)) {
            throw new UnauthorizedOperationException("Nie masz uprawnień do tego zgłoszenia");
        }
        List<Attachment> newAttachments = fileService.storeFiles(files, currentUser.getEmail());
        if (report.getAttachments() == null) {
            report.setAttachments(new ArrayList<>());
        }
        report.getAttachments().addAll(newAttachments);
    }

    public String sortReports(List<ReportDto> reports, String sort) {
        LocalDateTime now = LocalDateTime.now();
        return switch (sort) {
            case "remainingTimeAsc" -> {
                reports.sort(Comparator.comparing((ReportDto r) -> {
                    LocalDateTime deadline = resolveDeadline(r);
                    return deadline.isBefore(now) || COMPLETED_STATUS_POLISH_NAME.equals(r.getStatusName());
                }).thenComparing(this::resolveDeadline));
                yield "Pozostały czas do końca (rosnąco)";
            }
            case "remainingTimeDesc" -> {
                reports.sort(Comparator.comparing((ReportDto r) -> {
                    LocalDateTime deadline = resolveDeadline(r);
                    return deadline.isBefore(now) || COMPLETED_STATUS_POLISH_NAME.equals(r.getStatusName());
                }).reversed().thenComparing(this::resolveDeadline, Comparator.reverseOrder()));
                yield "Pozostały czas do końca (malejąco)";
            }
            case "addedDateAsc" -> {
                reports.sort(Comparator.comparing(ReportDto::getDateAdded));
                yield "Data zgłoszenia (od najstarszych)";
            }
            case "addedDateDesc" -> {
                reports.sort(Comparator.comparing(ReportDto::getDateAdded).reversed());
                yield "Data zgłoszenia (od najnowszzych)";
            }
            default -> "Brak";
        };
    }

    public Map<LocalDate, Double> getAverageFirstReactionTimesForReports(List<ReportDto> reports, LocalDate start, LocalDate end) {
        return statisticsService.calculateAverageTimes(reports, start, end, ReportDto::getAddedToFirstReactionDuration);
    }

    public Map<LocalDate, Double> getAverageCompletionTimesForReports(List<ReportDto> reports, LocalDate start, LocalDate end) {
        return statisticsService.calculateAverageTimes(reports, start, end, ReportDto::getAddedToCompleteDuration);
    }

    private Report createReportEntity(NewReportDto dto, User user, ReportCategory category) {
        LocalDateTime now = LocalDateTime.now();
        Report report = new Report();
        report.setTitle(dto.getTitle());
        report.setDescription(dto.getDescription());
        report.setDatedAdded(now);
        report.setDueDate(now.plusHours(user.getCompany().getTimeToResolve()));
        report.setTimeToRespond(now.plusHours(user.getCompany().getTimeToFirstRespond()));
        report.setCategory(category);
        report.setStatus(ReportStatus.PENDING);
        report.setReportingUser(user);
        return report;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("Użytkownik nie istnieje"));
    }

    private Report getAuthorizedReport(Long reportId, String userEmail) {
        Report report = reportRepository.findById(reportId).orElseThrow(() ->
                new ReportNotFoundException("Zgłoszenie nie zostało znalezione"));
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new UnauthorizedOperationException("Błąd autoryzacji"));
        if (!isUserAuthorizedForReport(report, user)) throw new UnauthorizedOperationException("Brak uprawnień");
        return report;
    }

    public boolean isUserAuthorizedForReport(Report report, User user) {
        boolean isAdmin = user.getRoles().stream().anyMatch(r -> "ADMINISTRATOR".equals(r.getName()));
        boolean isOwner = report.getReportingUser().equals(user);
        boolean isAssigned = report.getAssignedEmployee() != null && report.getAssignedEmployee().equals(user);
        return isOwner || isAssigned || isAdmin;
    }

    private LocalDateTime resolveDeadline(ReportDto report) {
        return PENDING_STATUS_POLISH_NAME.equals(report.getStatusName())
                ? report.getToRespondDate()
                : report.getDueDate();
    }

    @Transactional(readOnly = true)
    public ReportDetailsDto getReportForChat(Long reportId, String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new UserNotFoundException("Użytkownik nie istnieje"));
        Report report = reportRepository.findById(reportId).orElseThrow(() ->
                new ReportNotFoundException("Zgłoszenie nie zostało znalezione"));
        if (!isUserAuthorizedForReport(report, user)) throw new UnauthorizedOperationException("Brak uprawnień");
        return reportMapper.mapToReportDetailsDto(report);
    }
}