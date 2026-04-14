package pl.error_handling_app.file;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.error_handling_app.attachment.entity.Attachment;
import pl.error_handling_app.exception.FileStorageException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileService {

    private static final String ATTACHMENTS_DIRECTORY = "uploads";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public List<Attachment> storeFiles(List<MultipartFile> files, String userEmail) {
        List<Attachment> attachments = new ArrayList<>();
        createDirectoryForAttachmentsIfNotExists();

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;

            String originalFileName = file.getOriginalFilename();
            Path filePath = Paths.get(ATTACHMENTS_DIRECTORY, originalFileName);
            int fileIndex = 1;

            while (filenameAlreadyExists(filePath.getFileName().toString())) {
                filePath = createPathWithUniqueFilename(originalFileName, fileIndex, ATTACHMENTS_DIRECTORY);
                fileIndex++;
            }

            try {
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                Attachment attachment = new Attachment();
                attachment.setFilePath("/" + filePath.toString().replace("\\", "/"));
                attachment.setTimestamp(LocalDateTime.now());
                attachment.setAddingUser(userEmail);
                attachment.setFileName(originalFileName);
                attachment.setFileSize(file.getSize());
                attachments.add(attachment);

            } catch (IOException e) {
                logger.error("Błąd zapisu pliku {}: {}", originalFileName, e.getMessage());
                throw new FileStorageException("Wystąpił błąd podczas zapisywania załączników.");
            }
        }
        return attachments;
    }

    public boolean areFilesValid(List<MultipartFile> files) {
        return files != null && files.stream()
                .anyMatch(file -> file != null && !file.isEmpty() && file.getOriginalFilename() != null && !file.getOriginalFilename().isBlank());
    }

    public boolean filenameAlreadyExists(String fileName) {
        Path filePath = Paths.get(ATTACHMENTS_DIRECTORY + "/" + fileName);
        return Files.exists(filePath);
    }

    public Path createPathWithUniqueFilename(String fileName, int fileIndex, String fileLocation) {
        String baseName = FilenameUtils.getBaseName(fileName);
        String extension = FilenameUtils.getExtension(fileName);
        String newFileName = baseName + "(" + fileIndex + ")" + "." + extension;
        return Paths.get(fileLocation, newFileName);
    }

    public void createDirectoryForAttachmentsIfNotExists() {
        Path attachmentsDir = Paths.get(ATTACHMENTS_DIRECTORY);
        if (!Files.exists(attachmentsDir)) {
            try {
                Files.createDirectories(attachmentsDir);
            } catch (IOException e) {
                logger.error("Błąd tworzenia katalogu: {}", e.getMessage());
            }
        }
    }
}