package pl.error_handling_app.user.dto;

import org.springframework.stereotype.Component;
import pl.error_handling_app.company.entity.Company;
import pl.error_handling_app.user.entity.User;
import pl.error_handling_app.user.entity.UserRole;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserDtoMapper {

    public User map(UserDto userDto, Company company, UserRole role) {
        User user = new User();
        user.setId(userDto.id());
        user.setFirstName(userDto.firstName());
        user.setLastName(userDto.lastName());
        user.setEmail(userDto.email());
        user.setCompany(company);
        user.getRoles().add(role);
        user.setActive(userDto.isActive());
        return user;
    }

    public UserDto map(User user) {
        Long roleId = user.getRoles().stream()
                .map(UserRole::getId)
                .findFirst()
                .orElse(null);

        return new UserDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getCompany().getId(),
                roleId,
                user.isActive()
        );
    }

    public UserCredentialsDto mapToCredentials(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(UserRole::getName)
                .collect(Collectors.toSet());

        return new UserCredentialsDto(user.getEmail(), user.getPassword(), roles);
    }

    public UserInReportDto mapToUserInReportDto(User user) {
        return new UserInReportDto(user.getId(), user.getEmail(), user.getCompany().getName());
    }

    public void updateEntity(User user, UserDto userDto, Company company, UserRole role) {
        user.setFirstName(userDto.firstName());
        user.setLastName(userDto.lastName());
        user.setCompany(company);

        user.getRoles().clear();
        user.getRoles().add(role);
    }

    public RoleDto mapToRoleDto(UserRole role) {
        return new RoleDto(role.getId(), role.getName());
    }
}