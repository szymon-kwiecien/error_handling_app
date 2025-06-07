package pl.error_handling_app.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.error_handling_app.company.Company;
import pl.error_handling_app.company.CompanyRepository;
import pl.error_handling_app.exception.*;
import pl.error_handling_app.report.Report;
import pl.error_handling_app.report.ReportRepository;
import pl.error_handling_app.user.dto.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserDtoMapper mapper;
    private final ReportRepository reportRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserPasswordChangeOrActiveService userPasswordChangeOrActiveService;
    private final VeryficationTokenRepository veryficationTokenRepository;

    public UserService(UserRepository userRepository, CompanyRepository companyRepository, UserRoleRepository userRoleRepository, UserDtoMapper mapper, ReportRepository reportRepository, PasswordEncoder passwordEncoder, UserPasswordChangeOrActiveService userPasswordChangeOrActiveService, VeryficationTokenRepository veryficationTokenRepository) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.userRoleRepository = userRoleRepository;
        this.mapper = mapper;
        this.reportRepository = reportRepository;
        this.passwordEncoder = passwordEncoder;
        this.userPasswordChangeOrActiveService = userPasswordChangeOrActiveService;
        this.veryficationTokenRepository = veryficationTokenRepository;
    }

    public List<UserInReportDto> findUsersByRoleName(String roleName) {
        return userRepository.findALlByRoles_Name(roleName).stream().map(UserDtoMapper::mapToUserInReportDto).toList();
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<UserDetailsDto> findUserDetailsByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(user -> new UserDetailsDto(user.getEmail(), user.getRoles().stream()
                        .map(UserRole::getName)
                        .collect(Collectors.toSet())));
    }

    public Optional<UserCredentialsDto> findCredentialsByEmail(String email) {
        return userRepository.findByEmail(email).map(UserDtoMapper::mapToCredentials);
    }

    public List<UserDto> findAllUsers() {
        return userRepository.findAll().stream().map(UserDtoMapper::map).toList();
    }

    public void addUser(UserDto newUser) {
            checkUserAlreadyExists(newUser.getEmail());
            User userToSave = mapper.map(newUser);
            userRepository.save(userToSave);
            userPasswordChangeOrActiveService.NewVerification(userToSave);
    }

    @Transactional
    public void updateUser(Long userId, UserDto userDto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("Nie znaleziono użytkownika."));
        userRepository.findByEmail(userDto.getEmail()).ifPresent(foundUser -> {
            if(!foundUser.getId().equals(userId))
                throw new InvalidEmailException("Taki adres e-mail posiada już inny użytkownik.");
        });

        Company company = companyRepository.findById(userDto.getCompanyId())
                .orElseThrow(() -> new CompanyNotFoundException("Wybrana firma nie została znaleziona."));
        UserRole role = userRoleRepository.findById(userDto.getRoleId())
                .orElseThrow(() -> new RoleNotFoundException("Wybrana rola nie została znaleziona"));

        user.setEmail(userDto.getEmail());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setCompany(company);
        Set<UserRole> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
    }

        @Transactional
        public void deleteUser(Long userId) {
            User userToDelete = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("Użytkownik nie został znaleziony"));
            detachUserFromReports(userToDelete);
            Optional<VeryficationToken> veryficationToken = veryficationTokenRepository.findByUser(userToDelete);
            veryficationToken.ifPresent(veryficationTokenRepository::delete);
            userRepository.deleteById(userId);
        }

        @Transactional
        public void detachUserFromReports(User userToDelete) {
        reportRepository.findAllByAssignedEmployee(userToDelete).forEach(report -> report.setAssignedEmployee(null));
        List<Report> reportsToDelete = reportRepository.findAllByReportingUser(userToDelete); //Gdy usuwamy użytkownika to jego zgłoszenia również usuwamy
        reportRepository.deleteAll(reportsToDelete);
    }



    private void checkUserAlreadyExists(String email) {
        if(userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("Użytkownik %s już istnieje!".formatted(email));
        }
    }

    public boolean isPasswordInvalid(String password, String currentPassword) {
        return !passwordEncoder.matches(password, currentPassword); //Sprawdzam czy obecne hasło podane w formularzy zmianu e-maila jest nieprawidłowe
    }
}
