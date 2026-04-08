package pl.error_handling_app.utils;

import java.util.HashMap;
import java.util.Map;

public class AttachmentUtils {

    private static final Map<String, String> EXTENSION_TO_ICON_MAP = new HashMap<>();

    static {
        addMapping("fa-file-word", "doc", "docx", "odt", "rft", "rtf");
        addMapping("fa-file-excel", "xls", "xlsx", "ods", "csv");
        addMapping("fa-file-powerpoint", "ppt", "pptx", "odp");
        addMapping("fa-file-zipper", "zip", "rar", "7z", "tar", "gz");
        addMapping("fa-file-image", "jpg", "jpeg", "png", "gif", "bmp", "svg", "webp");
        addMapping("fa-file-audio", "mp3", "wav", "flac", "aac");
        addMapping("fa-file-video", "mp4", "avi", "mkv", "mov");
        addMapping("fa-file-pdf", "pdf");
        addMapping("fa-file-lines", "txt", "log");
        addMapping("fa-file-code",
                "java", "py", "js", "html", "css", "cpp", "c", "xml", "json", "php",
                "cs", "swift", "go", "kt", "rb", "pl", "sh", "bat", "sql", "yml",
                "yaml", "md", "asm", "cfg", "pom", "h", "hpp"
        );
    }

    private static void addMapping(String icon, String... extensions) {
        for (String ext : extensions) {
            EXTENSION_TO_ICON_MAP.put(ext.toLowerCase(), icon);
        }
    }

    public static String getIconClass(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "fa-file";
        }
        String extension = getFileExtension(fileName);
        return EXTENSION_TO_ICON_MAP.getOrDefault(extension, "fa-file");
    }

    public static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }

    public static String getReadableFileSize(long sizeInBytes) {
        if (sizeInBytes <= 0) return "0 B";
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