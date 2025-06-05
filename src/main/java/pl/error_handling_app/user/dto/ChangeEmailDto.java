package pl.error_handling_app.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ChangeEmailDto {

    @NotNull(message = "Id użytkownika jest wymagane")
    private Long userId;
    @NotBlank(message = "Nowy adres e-mail nie może być pusty")
    @Email(message = "Niepoprawny format adresu e-mail")
    private String newEmail;
    @NotBlank(message = "Aktualne hasło nie może być puste")
    private String currentPassword;

    public String getNewEmail() {
        return newEmail;
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
