package pl.error_handling_app.report.dto;

import org.springframework.stereotype.Component;
import pl.error_handling_app.attachment.dto.AttachmentDto;
import pl.error_handling_app.chat.service.ChatService;
import pl.error_handling_app.report.ReportStatus;
import pl.error_handling_app.report.dto.ReportDetailsDto;
import pl.error_handling_app.report.dto.ReportDto;
import pl.error_handling_app.report.entity.Report;
import pl.error_handling_app.user.entity.User;

import java.util.List;

@Component
public class ReportDtoMapper {

    private final ChatService chatService;

    public ReportDtoMapper(ChatService chatService) {
        this.chatService = chatService;
    }

    public ReportDto mapToDto(Report report) {
        ReportDto dto = new ReportDto();
        dto.setId(report.getId());
        dto.setTitle(report.getTitle());
        dto.setDescription(report.getDescription());
        dto.setDateAdded(report.getDatedAdded());
        dto.setDueDate(report.getDueDate());
        dto.setToRespondDate(report.getTimeToRespond());
        dto.setCategoryName(report.getCategory() != null ? report.getCategory().getName() : "-");
        dto.setStatusName(report.getStatus() != null ? report.getStatus().description : "-");
        dto.setAssignedEmployee(report.getAssignedEmployee() != null ? report.getAssignedEmployee().getEmail() : "-");
        dto.setReportingUser(report.getReportingUser() != null ? report.getReportingUser().getEmail() : "-");
        dto.setLastMessageTime(chatService.getLastMessageTimeByReportId(report.getId()));
        dto.setAddedToFirstReactionDuration(report.getAddedToFirstReactionDuration());
        dto.setAddedToCompleteDuration(report.getAddedToCompleteDuration());
        return dto;
    }

    public ReportDetailsDto mapToReportDetailsDto(Report report) {
        User reportingUser = report.getReportingUser();
        User assignedEmployee = report.getAssignedEmployee();

        List<AttachmentDto> attachments = report.getAttachments().stream()
                .map(a -> new AttachmentDto(a.getFilePath(), a.getAddingUser(), a.getTimestamp(),
                        a.getFileName(), a.getFileSize(), a.getFileIconClass()))
                .toList();

        return new ReportDetailsDto(
                report.getTitle(),
                report.getDescription(),
                report.getDatedAdded(),
                report.getDueDate(),
                report.getTimeToRespond(),
                report.getCategory() != null ? report.getCategory().getName() : "-",
                report.getStatus(),
                reportingUser != null ? reportingUser.getEmail() : "brak",
                (reportingUser != null && reportingUser.getCompany() != null) ? reportingUser.getCompany().getName() : "brak",
                assignedEmployee != null ? assignedEmployee.getId() : null,
                assignedEmployee != null ? assignedEmployee.getEmail() : "brak",
                attachments
        );
    }
}