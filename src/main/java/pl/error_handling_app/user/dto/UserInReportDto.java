package pl.error_handling_app.user.dto;

public record UserInReportDto(
        Long id,
        String email,
        String companyName
) {
    public UserInReportDto() {
        this(null, null, null);
    }
}