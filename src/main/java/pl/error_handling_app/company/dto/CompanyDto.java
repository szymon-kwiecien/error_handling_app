package pl.error_handling_app.company.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import pl.error_handling_app.validation.ValidTimeRange;

@ValidTimeRange
public record CompanyDto(
        Long id,
        @Size(min = 2, max = 50, message = "Nazwa musi mieć długość między 2 a 50 znaków")
        String name,
        @Min(value = 1, message = "Czas na pierwszą odpowiedź musi wynosić co najmniej 1 godzinę")
        @Max(value = 48, message = "Czas na pierwszą odpowiedź nie może przekraczać 48 godzin")
        int timeToFirstRespond,
        @Min(value = 3, message = "Czas na rozwiązanie musi wynosić co najmniej 3 godziny")
        @Max(value = 168, message = "Czas na rozwiązanie nie może przekraczać 168 godzin")
        int timeToResolve
) {
    public static CompanyDto empty() {
        return new CompanyDto(null, "", 0, 0);
    }
}
