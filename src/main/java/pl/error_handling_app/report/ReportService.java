package pl.error_handling_app.report;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.error_handling_app.attachment.Attachment;
import pl.error_handling_app.file.FileService;
import pl.error_handling_app.user.User;
import pl.error_handling_app.user.UserRepository;

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

@Service
public class ReportService {

    private final static String PENDING_STATUS_POLISH_NAME = "Oczekujące";

    private final ReportRepository reportRepository;
    private final ReportCategoryService reportCategoryService;
    private final UserRepository userRepository;
    private final FileService fileService;

    public ReportService(ReportRepository reportRepository, ReportCategoryService reportCategoryService, UserRepository userRepository, FileService fileService) {
        this.reportRepository = reportRepository;
        this.reportCategoryService = reportCategoryService;
        this.userRepository = userRepository;
        this.fileService = fileService;
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

    public void addNewReport(NewReportDto newReportDto) {

        ReportCategory category = getCategory(newReportDto.getCategoryId());
        User user = getCurrentUser();
        Report report = createReport(newReportDto, user, category);

        //zapis dodanych zalaczników
        if (!newReportDto.getFile().isEmpty()) {
            List<Attachment> attachments = saveAttachments(newReportDto.getFile(), user);
            report.setAttachments(attachments);
        }

        reportRepository.save(report);
    }

    private ReportCategory getCategory(Long categoryId) {
        return reportCategoryService.getCategoryById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono kategorii o ID: " + categoryId));
    }

    private User getCurrentUser() {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Użytkownik %s nie został znaleziony.".formatted(currentUserEmail)));
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
                System.err.println("Błąd podczas zapisywania pliku: " + e.getMessage());
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


}
