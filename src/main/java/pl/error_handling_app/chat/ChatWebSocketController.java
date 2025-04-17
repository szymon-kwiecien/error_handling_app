package pl.error_handling_app.chat;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;


@Controller
public class ChatWebSocketController {

    private final ChatService chatService;

    public ChatWebSocketController(ChatService chatService) {
        this.chatService = chatService;

    }

    @MessageMapping("/chat")
    public void handleChatMessage(ChatMessageDto messageDto) {
        chatService.sendMessage(messageDto);
    }
}