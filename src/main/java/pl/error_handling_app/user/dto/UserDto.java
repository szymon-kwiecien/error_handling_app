package pl.error_handling_app.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserDto(
        Long id,
        @NotBlank(message = "Imię jest wymagane")
        @Size(min = 3, max = 30, message = "Imię musi mieć długość między 3 a 30 znaków")
        String firstName,
        @NotBlank(message = "Nazwisko jest wymagane")
        String lastName,
        @NotBlank(message = "Email jest wymagany")
        @Email
        String email,
        @NotNull(message = "Firma jest wymagana")
        Long companyId,
        @NotNull(message = "Rola użytkownika jest wymagana")
        Long roleId,
        Boolean isActive
) {
    public UserDto {
        if (isActive == null) {
            isActive = false;
        }
    }

    public UserDto() {
        this(null, null, null, null, null, null, false);
    }
}