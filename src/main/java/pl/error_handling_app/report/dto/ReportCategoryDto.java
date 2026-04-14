package pl.error_handling_app.report.dto;

import jakarta.validation.constraints.Size;

public record ReportCategoryDto(
        Long id,
        @Size(min = 3, max = 50, message = "Nazwa musi mieć długość między 3 a 50 znaków")
        String name
) {
    public static ReportCategoryDto empty() {
        return new ReportCategoryDto(null, "");
    }
}
