package pl.error_handling_app.user.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.error_handling_app.company.entity.Company;
import pl.error_handling_app.company.repository.CompanyRepository;
import pl.error_handling_app.exception.*;
import pl.error_handling_app.report.service.ReportService;
import pl.error_handling_app.user.entity.User;
import pl.error_handling_app.user.entity.UserRole;
import pl.error_handling_app.user.repository.VerificationTokenRepository;
import pl.error_handling_app.user.dto.*;
import pl.error_handling_app.user.repository.UserRepository;
import pl.error_handling_app.user.repository.UserRoleRepository;
import pl.error_handling_app.utils.SecurityUtils;

import java.util.*;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final ReportService reportService;
    private final UserRoleRepository roleRepository;
    private final UserDtoMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final UserPasswordChangeOrActiveService userPasswordChangeOrActiveService;
    private final VerificationTokenRepository verificationTokenRepository;

    public UserService(UserRepository userRepository, CompanyRepository companyRepository, UserDtoMapper mapper, ReportService reportService, UserRoleRepository roleRepository, PasswordEncoder passwordEncoder, UserPasswordChangeOrActiveService userPasswordChangeOrActiveService, VerificationTokenRepository verificationTokenRepository) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.mapper = mapper;
        this.reportService = reportService;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userPasswordChangeOrActiveService = userPasswordChangeOrActiveService;
        this.verificationTokenRepository = verificationTokenRepository;
    }

    public List<UserInReportDto> findUsersByRoleName(String roleName) {
        return userRepository.findALlByRoles_Name(roleName).stream().map(mapper::mapToUserInReportDto).toList();
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public String getUserFirstName(String email) {
        return userRepository.findFirstNameByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Użytkownik nie został znaleziony"));
    }

    public Optional<UserCredentialsDto> findCredentialsByEmail(String email) {
        return userRepository.findByEmail(email).map(mapper::mapToCredentials);
    }

    public Page<UserDto> findPagedUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(mapper::map);
    }

    @Transactional
    public void addUser(UserDto newUser) {
        checkUserAlreadyExists(newUser.email());
        Company company = companyRepository.findById(newUser.companyId())
                .orElseThrow(() -> new CompanyNotFoundException("Firma nie została znaleziona."));
        UserRole role = roleRepository.findById(newUser.roleId())
                .orElseThrow(() -> new RoleNotFoundException("Rola nie została znaleziona"));
        User userToSave = mapper.map(newUser, company, role);
        userRepository.save(userToSave);
        userPasswordChangeOrActiveService.createVerificationToken(userToSave);
    }

    @Transactional
    public void updateUser(Long userId, UserDto userDto, String currentUserEmail) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("Nie znaleziono użytkownika."));
        boolean isSelfUpdate = user.getEmail().equalsIgnoreCase(currentUserEmail);

        if (isSelfUpdate && !user.getEmail().equalsIgnoreCase(userDto.email())) {
            throw new UnauthorizedOperationException("Nie możesz zmienić własnego adresu e-mail w tym panelu!");
        }

        if (!isSelfUpdate) {
            userRepository.findByEmail(userDto.email()).ifPresent(foundUser -> {
                if (!foundUser.getId().equals(userId))
                    throw new InvalidEmailException("Taki adres e-mail posiada już inny użytkownik.");
            });
            user.setEmail(userDto.email());
        }

        Company company = companyRepository.findById(userDto.companyId())
                .orElseThrow(() -> new CompanyNotFoundException("Wybrana firma nie została znaleziona."));
        UserRole role = roleRepository.findById(userDto.roleId())
                .orElseThrow(() -> new RoleNotFoundException("Wybrana rola nie została znaleziona"));

        if (isSelfUpdate) {
                boolean currentlyAdmin = user.getRoles().stream()
                        .anyMatch(r -> r.getName().equals(SecurityUtils.ADMIN_ROLE));
                if (currentlyAdmin && !role.getName().equals(SecurityUtils.ADMIN_ROLE)) {
                    throw new UnauthorizedOperationException("Nie możesz odebrać sobie uprawnień administratora.");
                }
        }
        mapper.updateEntity(user, userDto, company, role);
    }

    @Transactional
    public void deleteUser(Long userId, String currentUserEmail) {
        User userToDelete = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Użytkownik nie został znaleziony"));
        if (userToDelete.getEmail().equals(currentUserEmail)) {
                throw new UnauthorizedOperationException("Nie możesz usunąć własnego konta.");
        }
        boolean isTargetAdmin = userToDelete.getRoles().stream()
                .anyMatch(role -> role.getName().equals(SecurityUtils.ADMIN_ROLE));
        if (isTargetAdmin) {
                throw new UnauthorizedOperationException("Nie można usunąć innego administratora");
        }
        reportService.handleUserRemoval(userToDelete);
        verificationTokenRepository.findByUser(userToDelete)
                .ifPresent(verificationTokenRepository::delete);
        userRepository.deleteById(userId);
    }

    @Transactional
    public void prepareUsersForCompanyDeletion(Long companyId) {
        reportService.handleCompanyRemoval(companyId);
    }

    private void checkUserAlreadyExists(String email) {
        if(userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("Użytkownik %s już istnieje!".formatted(email));
        }
    }

    public boolean isPasswordInvalid(String password, String currentPassword) {
        return !passwordEncoder.matches(password, currentPassword); //Sprawdzam czy obecne hasło podane w formularzy zmianu e-maila jest nieprawidłowe
    }

    public List<RoleDto> findAllRoles() {
        return roleRepository.findAll().stream()
                .map(mapper::mapToRoleDto)
                .toList();
    }
}
