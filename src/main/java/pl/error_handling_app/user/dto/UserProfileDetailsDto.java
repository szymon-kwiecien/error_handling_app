package pl.error_handling_app.user.dto;

public record UserProfileDetailsDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        String role,
        String companyName,
        String companyTimeToFirstRespond,
        String companyTimeToResolve
) {}