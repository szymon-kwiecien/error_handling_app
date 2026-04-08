package pl.error_handling_app.chat.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.error_handling_app.chat.repository.ChatMessageRepository;
import pl.error_handling_app.chat.messaging.ChatProducer;
import pl.error_handling_app.chat.dto.ChatMessageDto;
import pl.error_handling_app.chat.entity.ChatMessage;
import pl.error_handling_app.report.entity.Report;
import pl.error_handling_app.report.repository.ReportRepository;

import java.time.LocalDateTime;

@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ReportRepository reportRepository;
    private final ChatProducer chatProducer;

    public ChatService(ChatMessageRepository chatMessageRepository, ReportRepository reportRepository, ChatProducer chatProducer) {
        this.chatMessageRepository = chatMessageRepository;
        this.reportRepository = reportRepository;
        this.chatProducer = chatProducer;
    }


    public Page<ChatMessageDto> getMessagesForReport(Long reportId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return chatMessageRepository.findByReportId(reportId, pageable)
                .map(message -> new ChatMessageDto(reportId, message.getSender(), message.getContent(),
                        message.getTimestamp()));
    }

    @Transactional
    public void sendMessage(ChatMessageDto dto, String senderEmail) {
        LocalDateTime now = LocalDateTime.now();
        ChatMessageDto messageToSend = new ChatMessageDto(
                dto.reportId(),
                senderEmail,
                dto.content(),
                now
        );
        saveMessage(messageToSend, now);
        chatProducer.send(messageToSend);
    }

    private void saveMessage(ChatMessageDto dto, LocalDateTime time) {
        Report report = reportRepository.getReferenceById(dto.reportId());
        ChatMessage message = new ChatMessage();
        message.setContent(dto.content());
        message.setSender(dto.sender());
        message.setTimestamp(time);
        message.setReport(report);
        chatMessageRepository.save(message);
    }

    public LocalDateTime getLastMessageTimeByReportId(Long reportId) {
        return chatMessageRepository.findLastMessageTimeByReportId(reportId);
    }
}
