package pl.error_handling_app.chat;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.error_handling_app.report.Report;
import pl.error_handling_app.report.ReportRepository;

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
                .map(message -> new ChatMessageDto(message.getId(), message.getSender(), message.getContent(),
                        message.getTimestamp()));
    }

    @Transactional
    public void sendMessage(ChatMessageDto dto) {
        chatProducer.send(dto);
        saveMessage(dto);
    }

    public void saveMessage(ChatMessageDto dto) {
        Report report = reportRepository.findById(dto.getReportId())
                .orElseThrow(() -> new IllegalArgumentException("Zgłoszenie nie zostało znalezione."));
        ChatMessage message = new ChatMessage();
        message.setContent(dto.getContent());
        message.setSender(dto.getSender());
        message.setTimestamp(LocalDateTime.now());
        message.setReport(report);
        chatMessageRepository.save(message);
    }

    public LocalDateTime getLastMessageTimeByReportId(Long reportId) {
        return chatMessageRepository.findLastMessageTimeByReportId(reportId);
    }
}
