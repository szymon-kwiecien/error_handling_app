package pl.error_handling_app.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

    private final JavaMailSender mailSender;
    private final ISpringTemplateEngine templateEngine;
    private final EmailQueue emailQueue;

    public MailService(JavaMailSender mailSender, ISpringTemplateEngine templateEngine, EmailQueue emailQueue) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.emailQueue = emailQueue;
    }

    @Value("$spring.mail.username")
    private String fromEmailId;
    Logger logger = LoggerFactory.getLogger(MailService.class);

    public void NewUserWelcomeMessage(String to, String UserName, String baseURL, String ExpirationDate) {
        final String title = "Aktywacja konta";
        try {

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setFrom(fromEmailId);
            helper.setTo(to);
            helper.setSubject(title);
            mimeMessage.setDescription(to + " | Category: New User");
            Context ctx = new Context();
            ctx.setVariable("UserName", UserName);
            ctx.setVariable("pageTitle", title);
            ctx.setVariable("uriLink", baseURL);
            ctx.setVariable("ExpirationDate", ExpirationDate);

            String httpBody = templateEngine.process("mail-templates/new_user.html", ctx);
            helper.setText(httpBody, true);

            emailQueue.addEmailToQueue(helper);
            logger.info("New Mail to: {} Queued Category: New User", to);

        }catch (MessagingException e){
            logger.error("Can't add email to queue:  " + e.getMessage());
        }
    }

    public void ForgotPasswordMessage(String to, String name, String tokenURI, String expirationTime){

        try {

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setFrom(fromEmailId);
            helper.setTo(to);
            helper.setSubject("Prośba o zmianę hasła");
            mimeMessage.setDescription(to + " Category: Change Password");
            Context ctx = new Context();
            ctx.setVariable("UserName", name);
            ctx.setVariable("uriLink", tokenURI);
            ctx.setVariable("ExpirationDate", expirationTime);

            String httpBody = templateEngine.process("mail-templates/change_password.html", ctx);
            helper.setText(httpBody, true);

            emailQueue.addEmailToQueue(helper);
            logger.info("New Mail to: {} Queued Category: Change Password", to);

        }catch (MessagingException e){
            logger.error("Can't add e-mail to queue: " + e.getMessage());
        }
    }

    @Scheduled(fixedRate = 5000)
    @Async
    public void proccessEmailQueue() {
        while (!emailQueue.isEmpty()) {
            MimeMessageHelper mimeMessageHelper = emailQueue.getNextEmail();

            if (mimeMessageHelper != null) {
                try{
                    String descripion = "";
                    mailSender.send(mimeMessageHelper.getMimeMessage());

                    try {
                        descripion = mimeMessageHelper.getMimeMessage().getDescription();
                    }catch (MessagingException e){
                        logger.error("Can't get Mail info: {}", e.getMessage());
                    }

                    logger.info("Send e-mail to: {}", descripion);
                } catch (MailException e) {
                    logger.error("Can't send e-mail: {}", e.getMessage());
                }
            }
        }
    }

}
