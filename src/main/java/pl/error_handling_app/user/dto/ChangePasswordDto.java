package pl.error_handling_app.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ChangePasswordDto {

    @NotNull(message = "Id użytkownika jest wymagane")
    private Long userId;
    @Size(min = 7, max = 20, message = "Hasło musi mieć od 7 do 20 znaków")
    @Pattern(
            regexp = ".*([\\d\\W]).*",
            message = "Hasło musi zawierać przynajmniej jedną cyfrę lub znak specjalny"
    )
    private String newPassword;
    @NotBlank(message = "Powtórzenie hasła jest wymagane")
    private String confirmedNewPassword;
    @NotBlank(message = "Wpisanie obecnego hasła jest wymagane")
    private String currentPassword;

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmedNewPassword() {
        return confirmedNewPassword;
    }

    public void setConfirmedNewPassword(String confirmedNewPassword) {
        this.confirmedNewPassword = confirmedNewPassword;
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
