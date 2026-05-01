package pl.error_handling_app.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import pl.error_handling_app.validation.FieldsMatch;

@FieldsMatch(
        field = "password",
        fieldMatch = "confirmPassword",
        message = "Hasła muszą być takie same"
)
public record PasswordsDto(
        @NotBlank(message = "Hasło nie może być puste")
        @Size(min = 7, max = 20, message = "Hasło musi mieć od 7 do 20 znaków")
        @Pattern(
                regexp = ".*([\\d\\W]).*",
                message = "Hasło musi zawierać przynajmniej jedną cyfrę lub znak specjalny"
        )
        String password,

        @NotBlank(message = "Powtórzenie hasła jest wymagane")
        String confirmPassword
) {
    public PasswordsDto() {
        this(null, null);
    }
}