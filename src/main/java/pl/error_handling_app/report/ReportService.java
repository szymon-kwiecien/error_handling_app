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
import pl.error_handling_app.chat.ChatService;
import pl.error_handling_app.exception.UserNotFoundException;
import pl.error_handling_app.file.FileService;
import pl.error_handling_app.report.dto.NewReportDto;
import pl.error_handling_app.report.dto.ReportDetailsDto;
import pl.error_handling_app.report.dto.ReportDto;
import pl.error_handling_app.user.User;
import pl.error_handling_app.user.UserRepository;
import pl.error_handling_app.user.UserRole;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class ReportService {

    public final static String PENDING_STATUS_POLISH_NAME = "Oczekujące";
    public final static String COMPLETED_STATUS_POLISH_NAME = "Zakończone";

    private final ReportRepository reportRepository;
    private final ReportCategoryService reportCategoryService;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final ChatService chatService;
    Logger logger = LoggerFactory.getLogger(getClass());

    public ReportService(ReportRepository reportRepository, ReportCategoryService reportCategoryService, UserRepository userRepository, FileService fileService, ChatService chatService) {
        this.reportRepository = reportRepository;
        this.reportCategoryService = reportCategoryService;
        this.userRepository = userRepository;
        this.fileService = fileService;
        this.chatService = chatService;
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
        Instant toFirstRespondDate = report.getToRespondDate().atZone(ZoneId.systemDefault()).toInstant(); //gdy zgloszenie ma status PENDING(oczekujace)
        Instant dueDate = report.getDueDate().atZone(ZoneId.systemDefault()).toInstant(); // gdy zgłoszenie ma status różny od PENDING

        long totalDuration = report.getStatusName()
                .equalsIgnoreCase(PENDING_STATUS_POLISH_NAME) ?
                Duration.between(dateAdded, toFirstRespondDate).toSeconds() : Duration.between(dateAdded, dueDate).toSeconds();
        long remainingDuration = report.getStatusName()
                .equalsIgnoreCase(PENDING_STATUS_POLISH_NAME) ?
                Duration.between(now, toFirstRespondDate).toSeconds() : Duration.between(now, dueDate).toSeconds();

        if (remainingDuration <= 0) return 0; //gdy minął termin to zwracamy 0
        if (remainingDuration >= totalDuration) return 100; //gdy pozostały czas jest wiekszy lub równy całkowitemu to zwracam 100

        return (int) ((remainingDuration * 100) / totalDuration); //ilosc pozostalego czasu wzgledem calkowitego czasu (w procentach)
    }

    @Transactional
    public void addNewReport(NewReportDto newReportDto) {

        ReportCategory category = getCategory(newReportDto.getCategoryId());
        User user = getCurrentUser();
        Report report = createReport(newReportDto, user, category);
        boolean reportHasValidFiles = areFilesValid(newReportDto.getFile());
        //zapis dodanych zalaczników
        if (reportHasValidFiles) {
            List<Attachment> attachments = saveAttachments(newReportDto.getFile(), user);
            report.setAttachments(attachments);
        }
        reportRepository.save(report);
    }

    private boolean areFilesValid(List<MultipartFile> files) {
        return files.stream()
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
        reportDto.setToRespondDate(report.getTimeToRespond());
        String categoryName = report.getCategory() != null ? report.getCategory().getName() : "-";
        reportDto.setCategoryName(categoryName);
        String statusName = report.getStatus() != null ? report.getStatus().description : "-";
        reportDto.setStatusName(statusName);
        String assignedEmployee = report.getAssignedEmployee() != null ? report.getAssignedEmployee().getEmail() : "-";
        reportDto.setAssignedEmployee(assignedEmployee);
        String reportingUser = report.getReportingUser() != null ? report.getReportingUser().getEmail() : "-";
        reportDto.setReportingUser(reportingUser);
        reportDto.setLastMessageTime(chatService.getLastMessageTimeByReportId(report.getId()));
        reportDto.setAddedToFirstReactionDuration(report.getAddedToFirstReactionDuration());
        reportDto.setAddedToCompleteDuration(report.getAddedToCompleteDuration());
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

    @Transactional
    public void addAttachmentsToExistingReport(Long reportId, List<MultipartFile> files) {

        if (!areFilesValid(files)) {
            throw new IllegalArgumentException("Pliki są niepoprawne. Spróbuj jeszcze raz");
        }

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Zgłoszenie nie istnieje"));

        User currentUser = getCurrentUser();

        if (!hasPermissionToAddAttachments(report, currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Nie masz uprawnień aby dodać załączniki do tego zgłoszenia");
        }
        List<Attachment> newAttachments = saveAttachments(files, currentUser);
        if (report.getAttachments() == null) {
            report.setAttachments(new ArrayList<>());
        }
        report.getAttachments().addAll(newAttachments);
    }

    public List<ReportDto> getReportsByDateRange(LocalDateTime dateFrom, LocalDateTime dateTo) {
        return reportRepository.findAllByDatedAddedIsBetween(dateFrom, dateTo).stream()
                .map(this::mapToDto).toList();
    }

    public List<ReportDto> getReportsByCategoryAndStatus(ReportCategory category, ReportStatus status) {
        List<ReportDto> reportsByCategory = filterReportsByCategory(category);
        List<ReportDto> reportsByStatus = filterReportsByStatus(status);
        reportsByCategory.retainAll(reportsByStatus);
        return reportsByCategory;
    }

    public List<ReportDto> filterReportsByCategory(ReportCategory category) {
        return filterReports(report -> report.getCategory() != null &&
                report.getCategory().getId().equals(category.getId()));
    }

    public List<ReportDto> filterReportsByStatus(ReportStatus status) {
        return filterReports(report -> status.equals(report.getStatus()));
    }

    private List<ReportDto> filterReports(Predicate<Report> predicate) {
        return reportRepository.findAll().stream()
                .filter(predicate)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<ReportDto> filterReportsByAssignedEmployee(List<ReportDto> reports, User employee) {
        return reports.stream()
                .filter(report -> report.getAssignedEmployee() != null && report.getAssignedEmployee().equals(employee.getEmail()))
                .collect(Collectors.toList());
    }

    public String sortReports(List<ReportDto> reports, String sort) {
        return switch (sort) {
            case "remainingTimeAsc" -> {
                reports.sort(Comparator.<ReportDto, Boolean>comparing(
                        report -> report.getRemainingTime(checkIsForFirstResponse(report)).isExpired() || report.getStatusName().equals(COMPLETED_STATUS_POLISH_NAME)
                ).thenComparing(
                        report -> report.getRemainingTime(checkIsForFirstResponse(report)),
                        Comparator.comparing(RemainingTime::isExpired)
                                .thenComparing(RemainingTime::getDays)
                                .thenComparing(RemainingTime::getHours)
                                .thenComparing(RemainingTime::getMinutes)
                ));
                yield "Pozostały czas do końca (rosnąco)";
            }
            case "remainingTimeDesc" -> {
                reports.sort(Comparator.<ReportDto, Boolean>comparing(
                        report -> report.getRemainingTime(checkIsForFirstResponse(report)).isExpired() || report.getStatusName().equals(COMPLETED_STATUS_POLISH_NAME)
                ).thenComparing(
                        report -> report.getRemainingTime(checkIsForFirstResponse(report)),
                        Comparator.comparing(RemainingTime::isExpired).reversed()
                                .thenComparing(RemainingTime::getDays, Comparator.reverseOrder())
                                .thenComparing(RemainingTime::getHours, Comparator.reverseOrder())
                                .thenComparing(RemainingTime::getMinutes, Comparator.reverseOrder())
                ));
                yield "Pozostały czas do końca (malejąco)";
            }
            case "addedDateAsc" -> {
                reports.sort(Comparator.comparing(ReportDto::getDateAdded));
                yield "Data zgłoszenia (od najstarszych)";
            }
            case "addedDateDesc" -> {
                reports.sort(Comparator.comparing(ReportDto::getDateAdded).reversed());
                yield "Data zgłoszenia (od najnowszych)";
            }
            default -> "Brak";
        };
    }

    public String[] calculateRemainingTime(List<ReportDto> reports) {
        String[] remainingTime = new String[reports.size()];
        for (int i = 0; i < reports.size(); i++) {
            boolean forFirstRespond = reports.get(i).getStatusName().equals(ReportService.PENDING_STATUS_POLISH_NAME);
            String remainingTimeTemplate = reports.get(i).getRemainingTime(forFirstRespond).getDays() > 0 ? (reports.get(i).getRemainingTime(forFirstRespond).getDays() + "d ") : "";
            remainingTimeTemplate += reports.get(i).getRemainingTime(forFirstRespond).getHours() > 0 ? (reports.get(i).getRemainingTime(forFirstRespond).getHours() + "godz. ") : "";
            remainingTimeTemplate += reports.get(i).getRemainingTime(forFirstRespond).getMinutes() > 0 ? (reports.get(i).getRemainingTime(forFirstRespond).getMinutes() + "min.") : "";
            remainingTime[i] = (reports.get(i).getStatusName().equals(ReportService.COMPLETED_STATUS_POLISH_NAME)|| (reports.get(i).getRemainingTime(forFirstRespond).getDays() <= 0  &&
                    reports.get(i).getRemainingTime(forFirstRespond).getHours() <= 0 && reports.get(i).getRemainingTime(forFirstRespond).getMinutes() <= 0))? "-": remainingTimeTemplate;
        }
        return remainingTime;
    }

    public Map<LocalDate, Double> getAverageFirstReactionTimesForReports(List<ReportDto> reports, LocalDate startDate, LocalDate endDate) {
        return calculateAverageTimes(reports, startDate, endDate, ReportDto::getAddedToFirstReactionDuration);
    }

    public Map<LocalDate, Double> getAverageCompletionTimesForReports(List<ReportDto> reports, LocalDate startDate, LocalDate endDate) {
        return calculateAverageTimes(reports, startDate, endDate, ReportDto::getAddedToCompleteDuration);
    }


    private Map<LocalDate, Double> calculateAverageTimes(List<ReportDto> reports, LocalDate startDate, LocalDate endDate, Function<ReportDto, Double> timeExtractor) {
        Map<LocalDate, List<Double>> groupedTimes = startDate.withDayOfMonth(1)
                .datesUntil(endDate.withDayOfMonth(1).plusMonths(1), Period.ofMonths(1))
                .collect(Collectors.toMap(
                        date -> date,
                        date -> new ArrayList<>()
                ));

        //pogrupowanie czasow wg miesiecy
        reports.stream()
                .filter(report -> !report.getDateAdded().toLocalDate().isBefore(startDate) &&
                        !report.getDateAdded().toLocalDate().isAfter(endDate))
                .forEach(report -> {
                    LocalDate month = report.getDateAdded().toLocalDate().with(TemporalAdjusters.firstDayOfMonth());
                    Double duration = timeExtractor.apply(report);

                    if (duration != null) {
                        groupedTimes.get(month).add(duration);
                    }
                });

        //obliczenie srednich dla każdego miesiaca
        return groupedTimes.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0)
                ));
    }

    private boolean checkIsForFirstResponse(ReportDto report) {
        return report.getStatusName().equals(PENDING_STATUS_POLISH_NAME);
    }

    private boolean hasPermissionToAddAttachments(Report report, User user) {
        boolean isAdmin = user.getRoles().stream()
                .map(UserRole::getName).anyMatch("ADMINISTRATOR"::equals);
        boolean isReportingUser = report.getReportingUser().equals(user);
        boolean isAssignedEmployee = report.getAssignedEmployee() != null && report.getAssignedEmployee().equals(user);
        return isReportingUser || isAssignedEmployee || isAdmin;
    }
}
