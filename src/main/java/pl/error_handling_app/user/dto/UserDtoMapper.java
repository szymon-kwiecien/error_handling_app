package pl.error_handling_app.user.dto;


import org.springframework.stereotype.Service;
import pl.error_handling_app.company.Company;
import pl.error_handling_app.company.CompanyRepository;
import pl.error_handling_app.exception.CompanyNotFoundException;
import pl.error_handling_app.exception.RoleNotFoundException;
import pl.error_handling_app.user.User;
import pl.error_handling_app.user.UserRole;
import pl.error_handling_app.user.UserRoleRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserDtoMapper {

    private final CompanyRepository companyRepository;
    private final UserRoleRepository roleRepository;

    public UserDtoMapper(CompanyRepository companyRepository, UserRoleRepository roleRepository) {
        this.companyRepository = companyRepository;
        this.roleRepository = roleRepository;
    }

     public User map(UserDto userDto) {

        User user = new User();
        user.setId(userDto.getId());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        Company company = companyRepository.findById(userDto.getCompanyId()).orElseThrow(() ->
                new CompanyNotFoundException("Firma nie została znaleziona."));
        UserRole userRole = roleRepository.findById(userDto.getRoleId()).orElseThrow(() ->
                new RoleNotFoundException("Rola nie została znaleziona"));
        user.setCompany(company);
        user.getRoles().add(userRole);
        user.setActive(userDto.isActive());
        return user;
    }

    public static UserDto map(User user) {

        Long userRole = user.getRoles().stream().map(UserRole::getId).toList().get(0);
        List<String> rolesNames = user.getRoles().stream().map(UserRole::getName).toList();
        return new UserDto(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(),
                user.getCompany().getId(), userRole, user.isActive());
    }

    public static UserCredentialsDto mapToCredentials(User user) {
        String email = user.getEmail();
        String password = user.getPassword();
        Set<String> roles = user.getRoles()
                .stream()
                .map(UserRole::getName)
                .collect(Collectors.toSet());
        return new UserCredentialsDto(email, password, roles);
    }

    public static UserInReportDto mapToUserInReportDto(User user) {
        return new UserInReportDto(user.getId(), user.getEmail(), user.getCompany().getName());
    }



}
