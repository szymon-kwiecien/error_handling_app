package pl.error_handling_app.report.dto;

import jakarta.validation.constraints.Size;

public class ReportCategoryDto {

    private Long id;
    @Size(min = 3, max = 50, message = "Nazwa musi mieć długość między 3 a 50 znaków")
    private String name;

    public ReportCategoryDto() {
    }

    public ReportCategoryDto(Long id, String name) {
        this.id = id;
        this.name = name;
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
    
}
