package pl.error_handling_app.mail;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.ISpringTemplateEngine;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private ISpringTemplateEngine templateEngine;

    @Mock
    private EmailQueue emailQueue;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private MailService mailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mailService, "fromEmailId", "system@firma.pl");
    }

    @Test
    void shouldQueueNewUserWelcomeMessageWithCorrectData() {
        //given
        String expectedHtmlBody = "<html>Witaj nowy uzytkowniku!</html>";
        when(templateEngine.process(eq("mail-templates/new_user.html"), any(Context.class)))
                .thenReturn(expectedHtmlBody);

        //when
        mailService.newUserWelcomeMessage("nowy@test.pl", "Jan Kowalski", "http://link", "2026-12-31");

        //then
        ArgumentCaptor<EmailData> captor = ArgumentCaptor.forClass(EmailData.class);
        verify(emailQueue).addEmailToQueue(captor.capture());

        EmailData queuedEmail = captor.getValue();
        assertThat(queuedEmail.to()).isEqualTo("nowy@test.pl");
        assertThat(queuedEmail.subject()).isEqualTo("Aktywacja konta");
        assertThat(queuedEmail.body()).isEqualTo(expectedHtmlBody);
        assertThat(queuedEmail.category()).isEqualTo("New User");
    }

    @Test
    void shouldQueueForgotPasswordMessageWithCorrectData() {
        //given
        String expectedHtmlBody = "<html>Zresetuj hasło!</html>";
        when(templateEngine.process(eq("mail-templates/change_password.html"), any(Context.class)))
                .thenReturn(expectedHtmlBody);

        //when
        mailService.forgotPasswordMessage("test@test.pl", "Anna", "http://token", "15 minut");

        //then
        ArgumentCaptor<EmailData> captor = ArgumentCaptor.forClass(EmailData.class);
        verify(emailQueue).addEmailToQueue(captor.capture());

        EmailData queuedEmail = captor.getValue();
        assertThat(queuedEmail.to()).isEqualTo("test@test.pl");
        assertThat(queuedEmail.subject()).isEqualTo("Prośba o zmianę hasła");
        assertThat(queuedEmail.body()).isEqualTo(expectedHtmlBody);
        assertThat(queuedEmail.category()).isEqualTo("Change Password");
    }

    @Test
    void shouldNotProcessWhenQueueIsEmpty() {
        //given
        when(emailQueue.isEmpty()).thenReturn(true);

        //when
        mailService.processEmailQueue();

        //then
        verify(emailQueue, never()).getNextEmail();
        verify(mailSender, never()).createMimeMessage();
    }

    @Test
    void shouldProcessQueueAndSendActualEmail() {
        //given
        EmailData mockEmail = new EmailData("test@test.pl", "Test", "Test body", "Kategoria");

        when(emailQueue.isEmpty()).thenReturn(false, false, true);
        when(emailQueue.getNextEmail()).thenReturn(mockEmail);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        //when
        mailService.processEmailQueue();

        //then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void shouldCatchMailExceptionAndNotThrowItOutside() {
        //given
        EmailData mockEmail = new EmailData("blad@test.pl", "Test", "Test body", "Kategoria");

        when(emailQueue.isEmpty()).thenReturn(false, false, true);
        when(emailQueue.getNextEmail()).thenReturn(mockEmail);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        doThrow(new MailSendException("Błąd połączenia SMTP")).when(mailSender).send(mimeMessage);

        //when
        mailService.processEmailQueue();

        //then
        verify(mailSender).send(mimeMessage);
    }
}