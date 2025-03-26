package pl.error_handling_app;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.error_handling_app.exception.InvalidEmailException;
import pl.error_handling_app.exception.InvalidPasswordException;
import pl.error_handling_app.user.ChangeEmailDto;
import pl.error_handling_app.user.ChangePasswordDto;
import pl.error_handling_app.user.UserProfileService;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserProfileService userProfileService;

    public ProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping()
    public String showProfile(Authentication authentication, Model model) {
        try {
            model.addAttribute("userDetails", userProfileService.findUserProfileDetailsByEmail(authentication.getName()));
            model.addAttribute("emailChangeDto", new ChangeEmailDto());
            model.addAttribute("passwordChangeDto", new ChangePasswordDto());

            return "profile";
        } catch(UsernameNotFoundException e) {
            return "login-form";
        }
    }

    @PostMapping("/change-email")
    public String changeEmail(ChangeEmailDto changeEmailDto, RedirectAttributes redirectAttributes) {
        try {
            userProfileService.changeEmail(changeEmailDto);
            redirectAttributes.addFlashAttribute("successMessage", "Zmieniono adres e-mail.");
        } catch(InvalidEmailException e) {
            redirectAttributes.addFlashAttribute("error", "Podczas zmiany adresu e-mail wystąpił błąd: " + e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(ChangePasswordDto changePasswordDto, RedirectAttributes redirectAttributes) {
        try {
            userProfileService.changePassword(changePasswordDto);
            redirectAttributes.addFlashAttribute("successMessage", "Pomyślnie zmieniono hasło.");
        } catch(InvalidPasswordException e) {
            redirectAttributes.addFlashAttribute("error", "Podczas zmiany hasła wystąpił błąd: " + e.getMessage());
        }
        return "redirect:/profile";
    }
}
