package pl.error_handling_app.report;

import jakarta.persistence.*;
import pl.error_handling_app.attachment.Attachment;
import pl.error_handling_app.user.User;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private LocalDateTime datedAdded;
    private LocalDateTime dueDate;
    private LocalDateTime timeToRespond;
    @ManyToOne
    private ReportCategory category;
    @Enumerated(EnumType.STRING)
    private ReportStatus status;
    @ManyToOne
    private User reportingUser;
    @ManyToOne
    private User assignedEmployee;
    @OneToMany
    @JoinColumn(name = "report_id")
    private List<Attachment> attachments;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDatedAdded() {
        return datedAdded;
    }

    public void setDatedAdded(LocalDateTime datedAdded) {
        this.datedAdded = datedAdded;
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

    public ReportCategory getCategory() {
        return category;
    }

    public void setCategory(ReportCategory category) {
        this.category = category;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public User getReportingUser() {
        return reportingUser;
    }

    public void setReportingUser(User reportingUser) {
        this.reportingUser = reportingUser;
    }

    public User getAssignedEmployee() {
        return assignedEmployee;
    }

    public void setAssignedEmployee(User assignedEmployee) {
        this.assignedEmployee = assignedEmployee;
    }

    public Double getAddedToFirstReactionDuration() {
        return addedToFirstReactionDuration;
    }

    public void setAddedToFirstReactionDuration(Double addedToFirstReactionDuration) {
        this.addedToFirstReactionDuration = addedToFirstReactionDuration;
    }

    public Double getAddedToCompleteDuration() {
        return addedToCompleteDuration;
    }

    public void setAddedToCompleteDuration(Double addedToCompleteDuration) {
        this.addedToCompleteDuration = addedToCompleteDuration;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }
}
