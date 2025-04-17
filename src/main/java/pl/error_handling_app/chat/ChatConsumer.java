package pl.error_handling_app.chat;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import pl.error_handling_app.config.chat.RabbitMQConfig;


@Component
public class ChatConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatConsumer(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void receive(ChatMessageDto message) {
        messagingTemplate.convertAndSend("/topic/chat." + message.getReportId(), message);
    }
}
