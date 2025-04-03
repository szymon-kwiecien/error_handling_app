package pl.error_handling_app.attachment;

import java.time.LocalDateTime;

public class AttachmentDto {

    private String filePath;
    private String addingUser;
    private LocalDateTime timestamp;
    private String fileName;
    private String fileSize;
    private String fileIconClass;

    public AttachmentDto(String filePath, String addingUser, LocalDateTime timestamp, String fileName, String fileSize, String fileIconClass) {
        this.filePath = filePath;
        this.addingUser = addingUser;
        this.timestamp = timestamp;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileIconClass = fileIconClass;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getAddingUser() {
        return addingUser;
    }

    public void setAddingUser(String addingUser) {
        this.addingUser = addingUser;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileIconClass() {
        return fileIconClass;
    }

    public void setFileIconClass(String fileIconClass) {
        this.fileIconClass = fileIconClass;
    }
}
