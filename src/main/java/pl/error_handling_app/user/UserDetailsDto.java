package pl.error_handling_app.user;

import java.util.Set;

public class UserDetailsDto {

    private String email;
    private Set<String> roles;

    public UserDetailsDto(String email, Set<String> roles) {
        this.email = email;
        this.roles = roles;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
