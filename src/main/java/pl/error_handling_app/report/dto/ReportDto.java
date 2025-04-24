package pl.error_handling_app.report.dto;

import pl.error_handling_app.report.RemainingTime;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class ReportDto {

    private Long id;
    private String title;
    private String description;
    private String categoryName;
    private String statusName;
    private LocalDateTime dateAdded;
    private LocalDateTime dueDate;
    private LocalDateTime toRespondDate;
    private String reportingUser;
    private String assignedEmployee;
    private LocalDateTime lastMessageTime;
    private int leftTimePercentage;
    private Double addedToFirstReactionDuration;
    private Double addedToCompleteDuration;


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

    public LocalDateTime getToRespondDate() {
        return toRespondDate;
    }

    public void setToRespondDate(LocalDateTime toRespondDate) {
        this.toRespondDate = toRespondDate;
    }

    public int getLeftTimePercentage() {
        return leftTimePercentage;
    }

    public void setLeftTimePercentage(int leftTimePercentage) {
        this.leftTimePercentage = leftTimePercentage;
    }

    public Double getAddedToFirstReactionDuration() {
        return addedToFirstReactionDuration;
    }

    public void setAddedToFirstReactionDuration(Double addedToFirstReactionDuration) {
        this.addedToFirstReactionDuration = addedToFirstReactionDuration;
    }

    public void setAddedToCompleteDuration(Double addedToCompleteDuration) {
        this.addedToCompleteDuration = addedToCompleteDuration;
    }

    public Double getAddedToCompleteDuration() {
        return addedToCompleteDuration;
    }




    public RemainingTime getRemainingTime(boolean forFirstRespond) {
        Duration duration = Duration.between(LocalDateTime.now(), forFirstRespond? toRespondDate : dueDate);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportDto reportDto = (ReportDto) o;
        return leftTimePercentage == reportDto.leftTimePercentage && Objects.equals(id, reportDto.id) && Objects.equals(title, reportDto.title) && Objects.equals(description, reportDto.description) && Objects.equals(categoryName, reportDto.categoryName) && Objects.equals(statusName, reportDto.statusName) && Objects.equals(dateAdded, reportDto.dateAdded) && Objects.equals(dueDate, reportDto.dueDate) && Objects.equals(toRespondDate, reportDto.toRespondDate) && Objects.equals(reportingUser, reportDto.reportingUser) && Objects.equals(assignedEmployee, reportDto.assignedEmployee) && Objects.equals(lastMessageTime, reportDto.lastMessageTime) && Objects.equals(addedToFirstReactionDuration, reportDto.addedToFirstReactionDuration) && Objects.equals(addedToCompleteDuration, reportDto.addedToCompleteDuration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, categoryName, statusName, dateAdded, dueDate, toRespondDate, reportingUser, assignedEmployee, lastMessageTime, leftTimePercentage, addedToFirstReactionDuration, addedToCompleteDuration);
    }
}
