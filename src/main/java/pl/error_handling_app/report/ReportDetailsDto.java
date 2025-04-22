package pl.error_handling_app.report;

import pl.error_handling_app.attachment.AttachmentDto;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class ReportDetailsDto {

    private String title;
    private String description;
    private LocalDateTime dateAdded;
    private LocalDateTime dueDate;
    private LocalDateTime timeToRespond;
    private String categoryName;
    private ReportStatus status;
    private String reportingUser;
    private String reportingUserCompanyName;
    private Long assignedEmployeeId;
    private String assignedEmployee;
    private List<AttachmentDto> attachments;

    public ReportDetailsDto(String title, String description, LocalDateTime dateAdded, LocalDateTime dueDate,
                            LocalDateTime timeToRespond, String categoryName, ReportStatus status,
                            String reportingUser, String reportingUserCompanyName, Long assignedEmployeeId,
                            String assignedEmployee, List<AttachmentDto> attachments) {
        this.title = title;
        this.description = description;
        this.dateAdded = dateAdded;
        this.dueDate = dueDate;
        this.timeToRespond = timeToRespond;
        this.categoryName = categoryName;
        this.status = status;
        this.reportingUser = reportingUser;
        this.reportingUserCompanyName = reportingUserCompanyName;
        this.assignedEmployeeId = assignedEmployeeId;
        this.assignedEmployee = assignedEmployee;
        this.attachments = attachments;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(LocalDateTime dateAdded) {
        this.dateAdded = dateAdded;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getTimeToRespond() {
        return timeToRespond;
    }

    public void setTimeToRespond(LocalDateTime timeToRespond) {
        this.timeToRespond = timeToRespond;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public String getReportingUser() {
        return reportingUser;
    }

    public void setReportingUser(String reportingUser) {
        this.reportingUser = reportingUser;
    }

    public String getReportingUserCompanyName() {
        return reportingUserCompanyName;
    }

    public void setReportingUserCompanyName(String reportingUserCompanyName) {
        this.reportingUserCompanyName = reportingUserCompanyName;
    }

    public Long getAssignedEmployeeId() {
        return assignedEmployeeId;
    }

    public void setAssignedEmployeeId(Long assignedEmployeeId) {
        this.assignedEmployeeId = assignedEmployeeId;
    }

    public String getAssignedEmployee() {
        return assignedEmployee;
    }

    public void setAssignedEmployee(String assignedEmployee) {
        this.assignedEmployee = assignedEmployee;
    }

    public List<AttachmentDto> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentDto> attachments) {
        this.attachments = attachments;
    }

    public RemainingTime getRemainingTime(boolean forFirstRespond) {
        Duration duration = Duration.between(LocalDateTime.now(), forFirstRespond? timeToRespond : dueDate);
        long days;
        long hours;
        long minutes;
        boolean isExpired;
        if (duration.getSeconds() < 0){
            days = 0;
            hours = 0;
            minutes = 0;
            isExpired = true;
        } else {
            days = duration.toDays();
            hours = duration.toHours() % 24;
            minutes = duration.toMinutes() % 60;
            isExpired = false;
        }
        return new RemainingTime(days, hours, minutes, isExpired);
    }

}
