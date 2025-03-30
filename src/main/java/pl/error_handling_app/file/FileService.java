package pl.error_handling_app.file;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileService {

    public boolean filenameAlreadyExists(String fileName) {
        Path filePath = Paths.get("uploads/" + fileName);
        return Files.exists(filePath);
    }


    public Path createPathWithUniqueFilename(String fileName, int fileIndex, String fileLocation) {
        String baseName = FilenameUtils.getBaseName(fileName);
        String extension = FilenameUtils.getExtension(fileName);
        String newFileName = baseName + "(" + fileIndex + ")" + "." + extension;
        return Paths.get(fileLocation,newFileName);
    }

}
