package pl.error_handling_app.mail;

import org.springframework.stereotype.Component;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class EmailQueue {

    private final Queue<EmailData> emailQueue = new ConcurrentLinkedQueue<>();

    public void addEmailToQueue(EmailData email) {
        emailQueue.add(email);
    }

    public EmailData getNextEmail() {
        return emailQueue.poll();
    }

    public boolean isEmpty() {
        return emailQueue.isEmpty();
    }
}
