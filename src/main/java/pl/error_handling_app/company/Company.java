package pl.error_handling_app.company;

import jakarta.persistence.*;
import pl.error_handling_app.user.User;

import java.util.List;

@Entity
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int timeToFirstRespond;
    private int timeToResolve;
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User> users;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTimeToFirstRespond() {
        return timeToFirstRespond;
    }

    public void setTimeToFirstRespond(int timeToFirstRespond) {
        this.timeToFirstRespond = timeToFirstRespond;
    }

    public int getTimeToResolve() {
        return timeToResolve;
    }

    public void setTimeToResolve(int timeToResolve) {
        this.timeToResolve = timeToResolve;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
