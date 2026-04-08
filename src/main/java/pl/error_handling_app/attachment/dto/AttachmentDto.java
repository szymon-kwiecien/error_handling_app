package pl.error_handling_app.attachment.dto;

import java.time.LocalDateTime;

public record AttachmentDto(
        String filePath,
        String addingUser,
        LocalDateTime timestamp,
        String fileName,
        String fileSize,
        String fileIconClass
) {}