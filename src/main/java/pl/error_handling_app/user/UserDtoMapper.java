package pl.error_handling_app.user;

import java.util.List;

public class UserDtoMapper {

    static UserDto map(User user) {
        List<String> rolesNames = user.getRoles().stream().map(UserRole::getName).toList();
        return new UserDto(user.getFirstName(), user.getLastName(), user.getEmail(),
                user.getCompany().getId(), rolesNames);
    }

}
