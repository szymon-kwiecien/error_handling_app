package pl.error_handling_app.user;


import jdk.dynalink.NoSuchDynamicMethodException;
import org.springframework.stereotype.Service;
import pl.error_handling_app.company.Company;
import pl.error_handling_app.company.CompanyRepository;

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

    User map(UserDto userDto) {

        User user = new User();
        user.setId(userDto.getId());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        Company company = companyRepository.findById(userDto.getCompanyId()).orElseThrow(() ->
                new NoSuchElementException("Firma nie została znaleziona."));
        UserRole userRole = roleRepository.findById(userDto.getRoleId()).orElseThrow(() ->
                new NoSuchElementException("Rola nie została znaleziona"));
        user.setCompany(company);
        user.getRoles().add(userRole);
        return user;
    }

    static UserDto map(User user) {

        Long userRole = user.getRoles().stream().map(UserRole::getId).toList().get(0);
        List<String> rolesNames = user.getRoles().stream().map(UserRole::getName).toList();
        return new UserDto(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(),
                user.getCompany().getId(), userRole);
    }

    static UserCredentialsDto mapToCredentials(User user) {
        String email = user.getEmail();
        String password = user.getPassword();
        Set<String> roles = user.getRoles()
                .stream()
                .map(UserRole::getName)
                .collect(Collectors.toSet());
        return new UserCredentialsDto(email, password, roles);
    }



}
