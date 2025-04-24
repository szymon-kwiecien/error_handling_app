package pl.error_handling_app.user;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
public class VeryficationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String token;
    private LocalDateTime expirationTime;
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    public VeryficationToken(){}

    public VeryficationToken(String token, int expirationTimeInMinute, User user) {
        this.token = token;
        this.expirationTime = getTokenExpirationTime(expirationTimeInMinute);
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getExpirationTime() {return expirationTime;}

    public void setExpirationTime(LocalDateTime expirationTime) {
        this.expirationTime = expirationTime;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    public LocalDateTime getTokenExpirationTime(int expirationInMinutes) {
        LocalDateTime now = LocalDateTime.now();
        return now.plus(expirationInMinutes, ChronoUnit.MINUTES);
    }
}
