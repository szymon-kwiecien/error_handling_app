package pl.error_handling_app.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.ISpringTemplateEngine;

@Service
public class MailService {

    private static final Logger logger = LoggerFactory.getLogger(MailService.class);
    private final JavaMailSender mailSender;
    private final ISpringTemplateEngine templateEngine;
    private final EmailQueue emailQueue;

    @Value("${app.mail.from}")
    private String fromEmailId;

    public MailService(JavaMailSender mailSender, ISpringTemplateEngine templateEngine, EmailQueue emailQueue) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.emailQueue = emailQueue;
    }

    public void newUserWelcomeMessage(String to, String userName, String baseURL, String expirationDate) {
        String title = "Aktywacja konta";
        Context ctx = new Context();
        ctx.setVariable("UserName", userName);
        ctx.setVariable("uriLink", baseURL);
        ctx.setVariable("ExpirationDate", expirationDate);

        String htmlBody = templateEngine.process("mail-templates/new_user.html", ctx);

        emailQueue.addEmailToQueue(new EmailData(to, title, htmlBody, "New User"));
        logger.info("Email to: {} queued. Category: New User", to);
    }

    public void forgotPasswordMessage(String to, String name, String tokenURI, String expirationTime) {
        String title = "Prośba o zmianę hasła";
        Context ctx = new Context();
        ctx.setVariable("UserName", name);
        ctx.setVariable("uriLink", tokenURI);
        ctx.setVariable("ExpirationDate", expirationTime);

        String htmlBody = templateEngine.process("mail-templates/change_password.html", ctx);

        emailQueue.addEmailToQueue(new EmailData(to, title, htmlBody, "Change Password"));
        logger.info("Email to: {} queued. Category: Change Password", to);
    }

    @Scheduled(fixedRate = 5000)
    public void processEmailQueue() {
        if (emailQueue.isEmpty()) return;

        logger.info("Starting to process email queue...");

        while (!emailQueue.isEmpty()) {
            EmailData data = emailQueue.getNextEmail();
            if (data != null) {
                sendActualEmail(data);
            }
        }
    }

    private void sendActualEmail(EmailData data) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true,"utf-8");

            helper.setFrom(fromEmailId);
            helper.setTo(data.to());
            helper.setSubject(data.subject());
            helper.setText(data.body(), true);
            mimeMessage.setDescription(data.to() + " | Category: " + data.category());

            mailSender.send(mimeMessage);
            logger.info("Successfully sent email to: {}", data.to());
        } catch (MailException | MessagingException e) {
            logger.error("Failed to send email to {}: {}", data.to(), e.getMessage());
        }
    }
}
