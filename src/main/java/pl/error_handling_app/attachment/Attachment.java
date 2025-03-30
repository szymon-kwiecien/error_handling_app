package pl.error_handling_app.attachment;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

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
        this.fileIconClass = getFileIconClass(getFileExtension(fileName));
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSizeInBytes) {
        this.fileSize = getReadableFileSize(fileSizeInBytes);
    }

    public String getFileIconClass() {
        return fileIconClass;
    }

    public void setFileIconClass(String fileIconClass) {
        this.fileIconClass = fileIconClass;
    }
    private String getFileIconClass(String fileExtension) {
        Map<String, String> extensionToIconMap = new HashMap<>();

        extensionToIconMap.put("doc", "fa-file-word");
        extensionToIconMap.put("docx", "fa-file-word");
        extensionToIconMap.put("odt", "fa-file-word");
        extensionToIconMap.put("rft", "fa-file-word");

        extensionToIconMap.put("xls", "fa-file-excel");
        extensionToIconMap.put("xlsx", "fa-file-excel");
        extensionToIconMap.put("ods", "fa-file-excel");

        extensionToIconMap.put("ppt", "fa-file-powerpoint");
        extensionToIconMap.put("pptx", "fa-file-powerpoint");
        extensionToIconMap.put("odp", "fa-file-powerpoint");

        extensionToIconMap.put("zip", "fa-file-zipper");
        extensionToIconMap.put("rar", "fa-file-zipper");
        extensionToIconMap.put("7z", "fa-file-zipper");
        extensionToIconMap.put("tar", "fa-file-zipper");
        extensionToIconMap.put("gz", "fa-file-zipper");

        extensionToIconMap.put("jpg", "fa-file-image");
        extensionToIconMap.put("jpeg", "fa-file-image");
        extensionToIconMap.put("png", "fa-file-image");
        extensionToIconMap.put("gif", "fa-file-image");
        extensionToIconMap.put("bmp", "fa-file-image");
        extensionToIconMap.put("svg", "fa-file-image");

        extensionToIconMap.put("mp3", "fa-file-audio");
        extensionToIconMap.put("wav", "fa-file-audio");
        extensionToIconMap.put("flac", "fa-file-audio");
        extensionToIconMap.put("aac", "fa-file-audio");

        extensionToIconMap.put("mp4", "fa-file-video");
        extensionToIconMap.put("avi", "fa-file-video");
        extensionToIconMap.put("mkv", "fa-file-video");
        extensionToIconMap.put("mov", "fa-file-video");

        extensionToIconMap.put("pdf", "fa-file-pdf");

        extensionToIconMap.put("txt", "fa-file-lines");
        extensionToIconMap.put("log", "fa-file-lines");

        extensionToIconMap.put("java", "fa-file-code");
        extensionToIconMap.put("py", "fa-file-code");
        extensionToIconMap.put("js", "fa-file-code");
        extensionToIconMap.put("html", "fa-file-code");
        extensionToIconMap.put("css", "fa-file-code");
        extensionToIconMap.put("cpp", "fa-file-code");
        extensionToIconMap.put("c", "fa-file-code");
        extensionToIconMap.put("xml", "fa-file-code");
        extensionToIconMap.put("json", "fa-file-code");
        extensionToIconMap.put("php", "fa-file-code");
        extensionToIconMap.put("cs", "fa-file-code");
        extensionToIconMap.put("swift", "fa-file-code");
        extensionToIconMap.put("go", "fa-file-code");
        extensionToIconMap.put("kt", "fa-file-code");
        extensionToIconMap.put("rb", "fa-file-code");
        extensionToIconMap.put("pl", "fa-file-code");
        extensionToIconMap.put("sh", "fa-file-code");
        extensionToIconMap.put("bat", "fa-file-code");
        extensionToIconMap.put("sql", "fa-file-code");
        extensionToIconMap.put("yml", "fa-file-code");
        extensionToIconMap.put("yaml", "fa-file-code");
        extensionToIconMap.put("md", "fa-file-code");
        extensionToIconMap.put("asm", "fa-file-code");
        extensionToIconMap.put("cfg", "fa-file-code");
        extensionToIconMap.put("pom", "fa-file-code");
        extensionToIconMap.put("h", "fa-file-code");
        extensionToIconMap.put("hpp", "fa-file-code");

        return extensionToIconMap.getOrDefault(fileExtension, "fa-file");
    }

    private String getFileExtension(String fileName){
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }

    private String getReadableFileSize(long sizeInBytes) {
        if (sizeInBytes <= 0) {
            return "0 B";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB"};

        int unitIndex = 0;
        double size = sizeInBytes;
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        return String.format("%.2f %s", size, units[unitIndex]);
    }
}
