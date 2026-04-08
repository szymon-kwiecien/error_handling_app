package pl.error_handling_app.chat.controller;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import pl.error_handling_app.chat.dto.ChatMessageDto;
import pl.error_handling_app.chat.service.ChatService;
import pl.error_handling_app.report.service.ReportService;
import java.security.Principal;

@Controller
public class ChatWebSocketController {

    private final ChatService chatService;
    private final ReportService reportService;

    public ChatWebSocketController(ChatService chatService, ReportService reportService) {
        this.chatService = chatService;
        this.reportService = reportService;
    }

    @MessageMapping("/chat")
    public void handleChatMessage(ChatMessageDto messageDto, Principal principal) {
        String currentUserEmail = principal.getName();
        reportService.getReportForChat(messageDto.reportId(), currentUserEmail);
        chatService.sendMessage(messageDto, currentUserEmail);
    }
}