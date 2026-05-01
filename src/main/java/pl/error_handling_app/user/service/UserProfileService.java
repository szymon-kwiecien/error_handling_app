package pl.error_handling_app.user.service;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.error_handling_app.company.entity.Company;
import pl.error_handling_app.exception.InvalidEmailException;
import pl.error_handling_app.exception.InvalidPasswordException;
import pl.error_handling_app.exception.UnauthorizedOperationException;
import pl.error_handling_app.user.entity.User;
import pl.error_handling_app.user.entity.UserRole;
import pl.error_handling_app.user.dto.ChangeEmailDto;
import pl.error_handling_app.user.dto.ChangePasswordDto;
import pl.error_handling_app.user.dto.UserProfileDetailsDto;
import pl.error_handling_app.user.repository.UserRepository;

import java.util.Optional;
import java.util.function.Function;

@Service
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserProfileService(UserRepository userRepository, UserService userService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }


    public UserProfileDetailsDto findUserProfileDetailsByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new UsernameNotFoundException("Użytkownik %s nie został znaleziony.".formatted(email)));
        String roleName = user.getRoles().stream()
                .findFirst()
                .map(UserRole::getName)
                .orElse("-");
        Company company = user.getCompany();
        String companyName = getCompanyName(company);
        String companyTimeToFirstRespond = getCompanyTime(company, Company::getTimeToFirstRespond);
        String companyTimeToResolve = getCompanyTime(company, Company::getTimeToResolve);
        return new UserProfileDetailsDto(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(),
                roleName, companyName, companyTimeToFirstRespond, companyTimeToResolve);
    }

    @Transactional
    public void changeEmail(ChangeEmailDto changeEmailDto) {

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        String newUserEmail = changeEmailDto.newEmail();

        if (newUserEmail.equalsIgnoreCase(currentUserEmail)) {
            throw new InvalidEmailException("Próbujesz zmienić adres e-mail na taki sam jaki obecnie posiadasz!");
        }

        Optional<User> existingUser = userRepository.findByEmail(newUserEmail);
        if (existingUser.isPresent() && !existingUser.get().getId().equals(changeEmailDto.userId())) {
            throw new InvalidEmailException("Podany adres e-mail posiada już inny użytkownik!");
        }

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UnauthorizedOperationException("Błąd uwierzytelnienia. Spróbuj jeszcze raz."));
        if (changeEmailDto.currentPassword() == null || userService.isPasswordInvalid(changeEmailDto.currentPassword(),
                currentUser.getPassword())) {
                throw new InvalidEmailException("Obecne hasło jest nieprawidłowe!");
        }
        currentUser.setEmail(changeEmailDto.newEmail());
        updateAuthentication(changeEmailDto.newEmail());
    }

    @Transactional
    public void changePassword(ChangePasswordDto changePasswordDto) {

        if (changePasswordDto.newPassword() == null || changePasswordDto.confirmedNewPassword() == null
                || changePasswordDto.newPassword().isEmpty() || changePasswordDto.confirmedNewPassword().isEmpty()
                || !changePasswordDto.newPassword().equals(changePasswordDto.confirmedNewPassword())) {
            throw new InvalidPasswordException("Hasła nie są takie same!");
        }
        User currentUser = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new UnauthorizedOperationException("Błąd uwierzytelnienia. Spróbuj jeszcze raz."));
        if (changePasswordDto.currentPassword() == null || userService.isPasswordInvalid(changePasswordDto.currentPassword(), currentUser.getPassword())) {
            throw new InvalidPasswordException("Obecne hasło jest nieprawidłowe!");
        }
        if (!userService.isPasswordInvalid(changePasswordDto.newPassword(), currentUser.getPassword())) {
            throw new InvalidPasswordException("Nowe hasło jest takie samo jak obecne!");
        }
        currentUser.setPassword(passwordEncoder.encode(changePasswordDto.newPassword()));
    }

    private String getCompanyName(Company company) {
        return company != null ? company.getName() : "-";
    }

    private String getCompanyTime(Company company, Function<Company, Integer> extractor) {
        return company != null ? String.valueOf(extractor.apply(company)) : "-";
    }

    private void updateAuthentication(String newEmail) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                newEmail, authentication.getCredentials(), authentication.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }

}

