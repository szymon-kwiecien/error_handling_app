package pl.error_handling_app.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.error_handling_app.user.dto.ChangeEmailDto;
import pl.error_handling_app.user.dto.ChangePasswordDto;
import pl.error_handling_app.user.UserProfileService;
import pl.error_handling_app.user.dto.UserProfileDetailsDto;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserProfileService userProfileService;

    public ProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping
    public String showProfile(Authentication authentication, Model model) {
            prepareProfileModel(authentication, model);
            return "profile";
    }

    @PostMapping("/change-email")
    public String changeEmail(@Valid @ModelAttribute("emailChangeDto") ChangeEmailDto changeEmailDto,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              Model model,
                              Authentication authentication) {

        if (bindingResult.hasErrors()) {
            prepareProfileModel(authentication, model);
            return "profile";
        }
        userProfileService.changeEmail(changeEmailDto);
        redirectAttributes.addFlashAttribute("successMessage", "Zmieniono adres e-mail.");
        return "redirect:/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(@Valid @ModelAttribute("passwordChangeDto") ChangePasswordDto changePasswordDto,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes,
                                 Authentication authentication) {

        if (bindingResult.hasErrors()) {
                prepareProfileModel(authentication, model);
                return "profile";
        }
        userProfileService.changePassword(changePasswordDto);
        redirectAttributes.addFlashAttribute("successMessage", "Pomyślnie zmieniono hasło.");

        return "redirect:/profile";
    }

    private void prepareProfileModel(Authentication authentication, Model model) {
        UserProfileDetailsDto userProfileDetails = userProfileService.findUserProfileDetailsByEmail(authentication.getName());
        model.addAttribute("userDetails", userProfileDetails);

        if (!model.containsAttribute("emailChangeDto")) {
            ChangeEmailDto changeEmailDto = new ChangeEmailDto();
            changeEmailDto.setUserId(userProfileDetails.getId());
            model.addAttribute("emailChangeDto", changeEmailDto);
        }

        if (!model.containsAttribute("passwordChangeDto")) {
            ChangePasswordDto changePasswordDto = new ChangePasswordDto();
            changePasswordDto.setUserId(userProfileDetails.getId());
            model.addAttribute("passwordChangeDto", changePasswordDto);
        }
    }
}
