package pl.error_handling_app.user.dto;

import java.util.Set;

public record UserCredentialsDto(
        String email,
        String password,
        Set<String> roles
) {}