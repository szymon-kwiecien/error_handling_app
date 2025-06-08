package pl.error_handling_app.company;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@ValidTimeRange
public class CompanyDto {

    private Long id;
    @Size(min = 2, max = 50, message = "Nazwa musi mieć długość między 2 a 50 znaków")
    private String name;
    @Min(value = 1, message = "Czas na pierwszą odpowiedź musi wynosić co najmniej 1 godzinę")
    @Max(value = 48, message = "Czas na pierwszą odpowiedź nie może przekraczać 48 godzin")
    private int timeToFirstRespond;
    @Min(value = 3, message = "Czas na rozwiązanie musi wynosić co najmniej 3 godziny")
    @Max(value = 168, message = "Czas na rozwiązanie nie może przekraczać 168 godzin")
    private int timeToResolve;



    public CompanyDto(Long id, String name, int timeToFirstRespond, int timeToResolve) {
        this.id = id;
        this.name = name;
        this.timeToFirstRespond = timeToFirstRespond;
        this.timeToResolve = timeToResolve;
    }

    public CompanyDto() {
    }

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
}
