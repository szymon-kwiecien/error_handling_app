package pl.error_handling_app.user.dto;

import jakarta.validation.constraints.*;

public class UserDto {

    private Long id;
    @NotBlank(message = "Imię jest wymagane")
    @Size(min = 3, max = 30, message = "Imię musi mieć długość między 3 a 30 znaków")
    @Pattern(regexp = "^[a-zA-ZąćęłńóśźżĄĆĘŁŃÓŚŹŻ]+$", message = "Imię może zawierać tylko litery alfabetu polskiego")
    private String firstName;
    @NotBlank(message = "Nazwisko jest wymagane")
    @Size(min = 3, max = 50, message = "Nazwisko musi mieć długość między 3 a 50 znaków")
    @Pattern(regexp = "^[a-zA-ZąćęłńóśźżĄĆĘŁŃÓŚŹŻ]+$", message = "Nazwisko może zawierać tylko litery alfabetu polskiego")
    private String lastName;
    @NotBlank(message = "Email jest wymagany")
    @Email(message = "Niepoprawny format adresu email")
    private String email;
    @NotNull(message = "Firma jest wymagana")
    private Long companyId;
    @NotNull(message = "Rola użytkownika jest wymagana")
    private Long roleId;
    private boolean isActive;

    public UserDto(Long id, String firstName, String lastName, String email, Long companyId, Long roleId, boolean isActive) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.companyId = companyId;
        this.roleId = roleId;
        this.isActive = isActive;
    }

    public UserDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
