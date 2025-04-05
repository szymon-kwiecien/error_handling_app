package pl.error_handling_app.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import pl.error_handling_app.attachment.Attachment;
import pl.error_handling_app.attachment.AttachmentDto;
import pl.error_handling_app.exception.UserNotFoundException;
import pl.error_handling_app.file.FileService;
import pl.error_handling_app.user.User;
import pl.error_handling_app.user.UserRepository;
import pl.error_handling_app.user.UserRole;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReportService {

    private final static String PENDING_STATUS_POLISH_NAME = "Oczekujące";

    private final ReportRepository reportRepository;
    private final ReportCategoryService reportCategoryService;
    private final UserRepository userRepository;
    private final FileService fileService;
    Logger logger = LoggerFactory.getLogger(getClass());

    public ReportService(ReportRepository reportRepository, ReportCategoryService reportCategoryService, UserRepository userRepository, FileService fileService) {
        this.reportRepository = reportRepository;
        this.reportCategoryService = reportCategoryService;
        this.userRepository = userRepository;
        this.fileService = fileService;
    }

    public Optional<ReportDetailsDto> findReportById(Long reportId) {
        return reportRepository.findById(reportId).map(this::mapToReportDetailsDto);
    }

    public Page<ReportDto> findReports(String titelFragment, ReportStatus status, Pageable pageable) {
        User user = getCurrentUser();
        boolean isAdmin = user.getRoles().stream().anyMatch(role -> role.getName().equals("ADMINISTRATOR"));
        boolean isEmployee = user.getRoles().stream().anyMatch(role -> role.getName().equals("EMPLOYEE"));
        Specification<Report> reportSpecification = ReportSpecification.filterBy(titelFragment, status);
        if (isAdmin) {
            return reportRepository.findAll(reportSpecification, pageable).map(this::mapToDto);
        } else if (isEmployee) {
            return reportRepository.findAll(Specification.where(reportSpecification).and(ReportSpecification.filterByAssignedEmployee(user)),pageable).map(this::mapToDto);
        } else {
            return reportRepository.findAll(Specification.where(reportSpecification).and(ReportSpecification.filterByReportingUser(user)),pageable).map(this::mapToDto);
        }
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

    @Transactional
    public void addNewReport(NewReportDto newReportDto) {

        ReportCategory category = getCategory(newReportDto.getCategoryId());
        User user = getCurrentUser();
        Report report = createReport(newReportDto, user, category);
        boolean reportHasValidFiles = checkReportHasValidFiles(newReportDto);
        //zapis dodanych zalaczników
        if (reportHasValidFiles) {
            List<Attachment> attachments = saveAttachments(newReportDto.getFile(), user);
            report.setAttachments(attachments);
        }
        reportRepository.save(report);
    }

    private boolean checkReportHasValidFiles(NewReportDto newReportDto) {
        return newReportDto.getFile().stream()
                .anyMatch(file ->
                        file != null && file.getOriginalFilename() != null && !file.getOriginalFilename().isBlank() && file.getSize() > 0);
    }

    private ReportCategory getCategory(Long categoryId) {
        return reportCategoryService.getCategoryById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono kategorii o ID: " + categoryId));
    }

    private User getCurrentUser() {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UserNotFoundException("Użytkownik %s nie został znaleziony.".formatted(currentUserEmail)));
    }


    private Report createReport(NewReportDto newReportDto, User user, ReportCategory category) {
        LocalDateTime now = LocalDateTime.now();

        Report report = new Report();
        report.setTitle(newReportDto.getTitle());
        report.setDescription(newReportDto.getDescription());
        report.setDatedAdded(now);
        report.setDueDate(now.plusDays(user.getCompany().getTimeToResolve()));
        report.setTimeToRespond(now.plusHours(user.getCompany().getTimeToFirstRespond()));
        report.setCategory(category);
        report.setStatus(ReportStatus.PENDING);
        report.setReportingUser(user);
        return report;
    }

    private List<Attachment> saveAttachments(List<MultipartFile> files, User user) {
        List<Attachment> attachments = new ArrayList<>();
        fileService.createDirectoryForAttachmentsIfNotExists();
        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            Path filePath = Paths.get("uploads/" + fileName);
            int fileIndex = 1;

            while (fileService.filenameAlreadyExists(filePath.getFileName().toString())) {
                filePath = fileService.createPathWithUniqueFilename(fileName, fileIndex, "uploads/");
                fileIndex++;
            }

            try {
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                Attachment attachment = new Attachment();
                attachment.setFilePath("/uploads/" + filePath.getFileName().toString());
                attachment.setTimestamp(LocalDateTime.now());
                attachment.setAddingUser(user.getEmail());
                attachment.setFileName(file.getOriginalFilename());
                attachment.setFileSize(file.getSize());
                attachments.add(attachment);

            } catch (IOException e) {
                logger.error("Wystąpil błąd podczas zapisywania pliku {}: {}", filePath, e.getMessage(), e);
                throw new RuntimeException("Wystąpił błąd podczas zapisywania plików. Spróbuj ponownie.");
            }
        }
        return attachments;
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

    private ReportDetailsDto mapToReportDetailsDto(Report report) {
        String categoryName = report.getCategory() != null ? report.getCategory().getName() : "-";
        User reportingUser = report.getReportingUser();
        String reportingUserEmail = reportingUser != null ? reportingUser.getEmail() : "brak";
        String reportingUserCompanyName = reportingUser != null &&
                reportingUser.getCompany() != null ? reportingUser.getCompany().getName() : "brak";
        User assignedEmployee = report.getAssignedEmployee();
        Long assignedEmployeeId = assignedEmployee != null ? assignedEmployee.getId() : null;
        String assignedEmployeeEmail = assignedEmployee != null ? assignedEmployee.getEmail() : "brak";
        List<AttachmentDto> attachments = report.getAttachments()
                .stream().map(attachment -> new AttachmentDto(attachment.getFilePath(), attachment.getAddingUser(),
                        attachment.getTimestamp(), attachment.getFileName(), attachment.getFileSize(), attachment.getFileIconClass())).toList();
        return new ReportDetailsDto(report.getTitle(), report.getDescription(), report.getDatedAdded(),
                report.getDueDate(), report.getTimeToRespond(), categoryName, report.getStatus(), reportingUserEmail,
                reportingUserCompanyName, assignedEmployeeId, assignedEmployeeEmail, attachments);
    }

    @Transactional
    public void deleteReport(Long reportId, String currentUserName) {
        Report report = getAuthorizedReport(reportId, currentUserName);
        reportRepository.delete(report);
    }

    @Transactional
    public void closeReport(Long reportId, String currentUserName) {
        Report report = getAuthorizedReport(reportId, currentUserName);
        report.setStatus(ReportStatus.COMPLETED);
        report.setAddedToCompleteDuration(); //po zamknięciu zgłoszenia ustawiam ilość czasu między dodaniem zgłoszenia a jego zamknięciem, która będzie
        //wykorzystana do utworzenia statystyk w raportach
    }

    private Report getAuthorizedReport(Long reportId, String currentUserName) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Zgłoszenie nie zostało znalezione."));

        User user = userRepository.findByEmail(currentUserName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Błąd uwierzytelnienia."));

        if (!hasPermissionToCloseOrDeleteReport(report, user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Brak uprawnień do wykonania tej operacji.");
        }
        return report;
    }

    private boolean hasPermissionToCloseOrDeleteReport(Report report, User user) {
        return (report.getAssignedEmployee() != null && report.getAssignedEmployee().equals(user)) ||
                user.getRoles().stream()
                        .map(UserRole::getName)
                        .anyMatch("ADMINISTRATOR"::equals);
    }

    @Transactional
    public void assignEmployeeToReport(Long reportId, Long employeeId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Zgłoszenie nie zostało znalezione"));
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Wybrany pracownik nie został znaleziony"));

        if (employee.getRoles().stream().noneMatch(role -> role.getName().equals("EMPLOYEE"))) {
            throw new IllegalArgumentException("Wybrany użytkownik nie jest pracownikiem!");
        }
        if(report.getAssignedEmployee() == null) {
            report.setAddedToFirstReactionDuration(); //przy pierwszym przypisaniu pracownika do zgłoszenia zmienia się status na "UNDER_REVIEW"
            // a więc czas między dodaniem zgłoszenia a pierwszym przypisaniem pracownika do zgłoszenia to czas pierwszej reakcji na zgłoszenie
        }
        report.setAssignedEmployee(employee);
        report.setStatus(ReportStatus.UNDER_REVIEW);
    }
}
