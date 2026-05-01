package pl.error_handling_app.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChangeEmailDto(
        @NotNull(message = "Id użytkownika jest wymagane")
        Long userId,

        @NotBlank(message = "Nowy adres e-mail nie może być pusty")
        @Email(message = "Niepoprawny format adresu e-mail")
        String newEmail,

        @NotBlank(message = "Aktualne hasło nie może być puste")
        String currentPassword
) {
    public ChangeEmailDto(Long userId) {
        this(userId, null, null);
    }

    public ChangeEmailDto() {
        this(null, null, null);
    }
}