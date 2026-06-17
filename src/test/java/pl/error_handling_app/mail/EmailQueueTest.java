package pl.error_handling_app.mail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailQueueTest {

    private EmailQueue emailQueue;

    @BeforeEach
    void setUp() {
        emailQueue = new EmailQueue();
    }

    @Test
    void shouldBeEmptyWhenCreated() {
        //then
        assertThat(emailQueue.isEmpty()).isTrue();
    }

    @Test
    void shouldNotBeEmptyAfterAddingEmail() {
        //given
        EmailData email = new EmailData("test@test.pl", "Temat testowy", "Treść testowa", "Kat 1");

        //when
        emailQueue.addEmailToQueue(email);

        //then
        assertThat(emailQueue.isEmpty()).isFalse();
    }

    @Test
    void shouldReturnEmailsInFifoOrder() {
        //given
        EmailData email1 = new EmailData("pierwszy@test.pl", "Temat 1", "Treść 1", "Kat 1");
        EmailData email2 = new EmailData("drugi@test.pl", "Temat 2", "Treść 2", "Kat 2");
        emailQueue.addEmailToQueue(email1);
        emailQueue.addEmailToQueue(email2);

        //when
        EmailData retrieved1 = emailQueue.getNextEmail();
        EmailData retrieved2 = emailQueue.getNextEmail();

        //then
        //Sprawdzam czy kolejność się zgadza
        assertThat(retrieved1).isEqualTo(email1);
        assertThat(retrieved2).isEqualTo(email2);

        //Sprawdzam czy kolejka jest znów pusta
        assertThat(emailQueue.isEmpty()).isTrue();
    }

    @Test
    void shouldReturnNullWhenGettingFromEmptyQueue() {
        //when
        EmailData retrieved = emailQueue.getNextEmail();

        //then
        assertThat(retrieved).isNull();
    }
}