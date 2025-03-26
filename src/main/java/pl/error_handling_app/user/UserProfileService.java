package pl.error_handling_app.user;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.error_handling_app.exception.InvalidEmailException;
import pl.error_handling_app.exception.InvalidPasswordException;

import java.util.Optional;

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
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Użytkownik %s nie został znaleziony.".formatted(email)));
        String roleName = user.getRoles().iterator().next().getName();
        String companyName = user.getCompany() != null ? user.getCompany().getName() : "-";
        return new UserProfileDetailsDto(user.getId(),user.getFirstName(), user.getLastName(), user.getEmail(), roleName,companyName);
    }

    @Transactional
    public void changeEmail(ChangeEmailDto changeEmailDto) {

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        String newUserEmail = changeEmailDto.getNewEmail();

        if (newUserEmail == null || newUserEmail.isEmpty()) {
            throw new InvalidEmailException("Wprowadzony nowy e-mail jest za krótki!");
        }

        if (newUserEmail.equalsIgnoreCase(currentUserEmail)) {
            throw new InvalidEmailException("Próbujesz zmienić adres e-mail na taki sam jaki obecnie posiadasz!");
        }

        Optional<User> existingUser = userRepository.findByEmail(newUserEmail);
        if (existingUser.isPresent() && !existingUser.get().getId().equals(changeEmailDto.getUserId())) {
            throw new InvalidEmailException("Podany adres e-mail posiada już inny użytkownik!");
        }

        User currentUser = userRepository.findByEmail(currentUserEmail).orElseThrow();
        if (changeEmailDto.getCurrentPassword() == null || userService.isPasswordInvalid(changeEmailDto.getCurrentPassword(),currentUser.getPassword())) {
            throw new InvalidEmailException("Obecne hasło jest nieprawidłowe!");
        }
        currentUser.setEmail(changeEmailDto.getNewEmail());
    }

    @Transactional
    public void changePassword(ChangePasswordDto changePasswordDto) {

        if (changePasswordDto.getNewPassword() == null || changePasswordDto.getConfirmedNewPassword() == null
                || changePasswordDto.getNewPassword().isEmpty() || changePasswordDto.getConfirmedNewPassword().isEmpty()
                || !changePasswordDto.getNewPassword().equals(changePasswordDto.getConfirmedNewPassword())) {
            throw new InvalidPasswordException("Hasła nie są takie same lub są za krótkie!");
        }
        User currentUser = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow();
        if (changePasswordDto.getCurrentPassword() == null || userService.isPasswordInvalid(changePasswordDto.getCurrentPassword(),currentUser.getPassword())) {
            throw new InvalidPasswordException("Obecne hasło jest nieprawidłowe!");
        }
        currentUser.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
    }
}
