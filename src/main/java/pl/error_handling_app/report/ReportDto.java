package pl.error_handling_app.report;

import java.time.LocalDateTime;

public class ReportDto {

    private Long id;
    private String title;
    private String description;
    private String categoryName;
    private String statusName;
    private LocalDateTime dateAdded;
    private LocalDateTime dueDate;
    private LocalDateTime toFirstRespondDate;
    private String reportingUser;
    private String assignedEmployee;
    private LocalDateTime lastMessageTime;
    private int leftTimePercentage;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public LocalDateTime getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(LocalDateTime dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getAssignedEmployee() {
        return assignedEmployee;
    }

    public void setAssignedEmployee(String assignedEmployee) {
        this.assignedEmployee = assignedEmployee;
    }

    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(LocalDateTime lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReportingUser() {
        return reportingUser;
    }

    public void setReportingUser(String reportingUser) {
        this.reportingUser = reportingUser;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getToFirstRespondDate() {
        return toFirstRespondDate;
    }

    public void setToFirstRespondDate(LocalDateTime toFirstRespondDate) {
        this.toFirstRespondDate = toFirstRespondDate;
    }

    public int getLeftTimePercentage() {
        return leftTimePercentage;
    }

    public void setLeftTimePercentage(int leftTimePercentage) {
        this.leftTimePercentage = leftTimePercentage;
    }
}
