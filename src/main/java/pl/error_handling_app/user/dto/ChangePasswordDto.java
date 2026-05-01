package pl.error_handling_app.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordDto(
        @NotNull(message = "Id użytkownika jest wymagane")
        Long userId,

        @Size(min = 7, max = 20, message = "Hasło musi mieć od 7 do 20 znaków")
        @Pattern(
                regexp = ".*([\\d\\W]).*",
                message = "Hasło musi zawierać przynajmniej jedną cyfrę lub znak specjalny"
        )
        String newPassword,

        @NotBlank(message = "Powtórzenie hasła jest wymagane")
        String confirmedNewPassword,

        @NotBlank(message = "Wpisanie obecnego hasła jest wymagane")
        String currentPassword
) {
    public ChangePasswordDto() {
        this(null, null, null, null);
    }

    public ChangePasswordDto(Long userId) {
        this(userId, null, null, null);
    }
}