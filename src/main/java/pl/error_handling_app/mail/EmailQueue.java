package pl.error_handling_app.mail;

import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Queue;

@Component
public class EmailQueue {
    private final Queue<MimeMessageHelper> emailQueue = new LinkedList<>();

    public void addEmailToQueue(MimeMessageHelper email) {
        synchronized (emailQueue) {
            emailQueue.add(email);
        }
    }

    public MimeMessageHelper getNextEmail() {
        synchronized (emailQueue) {
            return emailQueue.poll();
        }
    }
    public boolean isEmpty() {
        synchronized (emailQueue) {
            return emailQueue.isEmpty();
        }
    }
}
