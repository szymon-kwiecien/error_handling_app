package pl.error_handling_app.chat;

import java.time.LocalDateTime;

public class ChatMessageDto {

    private Long reportId;
    private String sender;
    private String content;
    private LocalDateTime timestamp;

    public ChatMessageDto(Long reportId, String sender, String content, LocalDateTime timestamp) {
        this.reportId = reportId;
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
    }


    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
