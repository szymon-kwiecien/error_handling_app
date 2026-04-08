package pl.error_handling_app.chat.dto;

import java.time.LocalDateTime;

public record ChatMessageDto(
        Long reportId,
        String sender,
        String content,
        LocalDateTime timestamp
) {}