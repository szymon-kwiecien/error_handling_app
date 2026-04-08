package pl.error_handling_app.attachment.entity;

import jakarta.persistence.*;
import pl.error_handling_app.utils.AttachmentUtils;
import java.time.LocalDateTime;

@Entity
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String filePath;
    private String addingUser;
    private LocalDateTime timestamp;
    private String fileName;
    private String fileSize;
    private String fileIconClass;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        this.fileIconClass = AttachmentUtils.getIconClass(fileName);
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSizeInBytes) {
        this.fileSize = AttachmentUtils.getReadableFileSize(fileSizeInBytes);
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