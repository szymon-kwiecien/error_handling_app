package pl.error_handling_app.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final VerificationTokenRepository verificationTokenRepository;

    public UserService(UserRepository userRepository, CompanyRepository companyRepository, UserRoleRepository userRoleRepository, UserDtoMapper mapper, ReportRepository reportRepository, PasswordEncoder passwordEncoder, UserPasswordChangeOrActiveService userPasswordChangeOrActiveService, VerificationTokenRepository verificationTokenRepository) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.userRoleRepository = userRoleRepository;
        this.mapper = mapper;
        this.reportRepository = reportRepository;
        this.passwordEncoder = passwordEncoder;
        this.userPasswordChangeOrActiveService = userPasswordChangeOrActiveService;
        this.verificationTokenRepository = verificationTokenRepository;
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

    public Page<UserDto> findPagedUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserDtoMapper::map);
    }

    public void addUser(UserDto newUser) {
            checkUserAlreadyExists(newUser.getEmail());
            User userToSave = mapper.map(newUser);
            userRepository.save(userToSave);
            userPasswordChangeOrActiveService.createVerificationToken(userToSave);
    }

    @Transactional
    public void updateUser(Long userId, UserDto userDto, String currentUserEmail) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("Nie znaleziono użytkownika."));
        boolean isSelfUpdate = user.getEmail().equalsIgnoreCase(currentUserEmail);

        if (isSelfUpdate && !user.getEmail().equalsIgnoreCase(userDto.getEmail())) {
            throw new UnauthorizedOperationException("Nie możesz zmienić własnego adresu e-mail w tym panelu!");
        }

        if (!isSelfUpdate) {
            userRepository.findByEmail(userDto.getEmail()).ifPresent(foundUser -> {
                if (!foundUser.getId().equals(userId))
                    throw new InvalidEmailException("Taki adres e-mail posiada już inny użytkownik.");
            });
            user.setEmail(userDto.getEmail());
        }

        Company company = companyRepository.findById(userDto.getCompanyId())
                .orElseThrow(() -> new CompanyNotFoundException("Wybrana firma nie została znaleziona."));
        UserRole role = userRoleRepository.findById(userDto.getRoleId())
                .orElseThrow(() -> new RoleNotFoundException("Wybrana rola nie została znaleziona"));

        if (isSelfUpdate) {
                boolean currentlyAdmin = user.getRoles().stream()
                        .anyMatch(r -> r.getName().equals("ADMINISTRATOR"));
                if (currentlyAdmin && !role.getName().equals("ADMINISTRATOR")) {
                    throw new UnauthorizedOperationException("Nie możesz odebrać sobie uprawnień administratora.");
                }
        }
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setCompany(company);
        Set<UserRole> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
    }

        @Transactional
        public void deleteUser(Long userId, String currentUserEmail) {
            User userToDelete = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("Użytkownik nie został znaleziony"));
            if (userToDelete.getEmail().equals(currentUserEmail)) {
                throw new UnauthorizedOperationException("Nie możesz usunąć własnego konta.");
            }
            boolean isTargetAdmin = userToDelete.getRoles().stream()
                    .anyMatch(role -> role.getName().equals("ADMINISTRATOR"));
            if (isTargetAdmin) {
                throw new UnauthorizedOperationException("Nie można usunąć innego administratora");
            }
            detachUserFromReports(userToDelete);
            verificationTokenRepository.findByUser(userToDelete)
                    .ifPresent(verificationTokenRepository::delete);
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
