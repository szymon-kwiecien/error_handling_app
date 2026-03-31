package pl.error_handling_app.user;

import jakarta.validation.Valid;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.error_handling_app.user.dto.PasswordsDto;

import java.util.List;

@Controller
@RequestMapping("/account")
public class UserPasswordChangeOrActiveController {

    private final UserPasswordChangeOrActiveService service;

    public UserPasswordChangeOrActiveController(UserPasswordChangeOrActiveService service) {
        this.service = service;
    }

    @GetMapping("/verification")
    public String verificationStatus() {
        return "verification-status-page";
    }

    @GetMapping("/verification/{token}")
    public String verifyAccount(@PathVariable String token, Model model) {
        TokenStatus status = service.validateToken(token, false);
        if (status != TokenStatus.VALID) {
            return redirectToStatusPage(model, status);
        }
        preparePasswordForm(model, token, true, "/account/verification/");
        return "activate-account-reset-password";
    }

    @PostMapping("/verification/{token}")
    public String processActivation(@PathVariable String token, @Valid @ModelAttribute("passwords") PasswordsDto passwords,
                                    BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return handleValidationError(redirectAttributes, bindingResult, "/account/verification/" + token);
        }

        TokenStatus status = service.validateToken(token, false);
        if (status == TokenStatus.VALID) {
            service.setNewPassword(token, passwords.getPassword());
            return "redirect:/account/verification?success";
        }

        redirectAttributes.addFlashAttribute("tokenStatus", status);
        return "redirect:/account/verification";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(@RequestParam String email) {
        service.sendResetPasswordMail(email);
        return "redirect:/account/forgot-password?ok";
    }

    @GetMapping("/forgot-password/{token}")
    public String changePasswordForm(@PathVariable String token, Model model) {
        TokenStatus status = service.validateToken(token, true);
        if (status != TokenStatus.VALID) {
            return redirectToStatusPage(model, status);
        }
        preparePasswordForm(model, token, false, "/account/forgot-password/");
        return "activate-account-reset-password";
    }

    @PostMapping("/forgot-password/{token}")
    public String processPasswordReset(@PathVariable String token, @Valid @ModelAttribute("passwords") PasswordsDto passwords,
                                       BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return handleValidationError(redirectAttributes, bindingResult, "/account/forgot-password/" + token);
        }

        TokenStatus status = service.validateToken(token, true);
        if (status == TokenStatus.VALID) {
            service.setNewPassword(token, passwords.getPassword());
            return "redirect:/account/verification?success";
        }

        redirectAttributes.addFlashAttribute("tokenStatus", status);
        return "redirect:/account/verification";
    }

    private String redirectToStatusPage(Model model, TokenStatus status) {
        model.addAttribute("tokenStatus", status);
        return "verification-status-page";
    }

    private void preparePasswordForm(Model model, String token, boolean isActivation, String actionPath) {
        model.addAttribute("activateAccount", isActivation);
        model.addAttribute("token", token);
        model.addAttribute("formAction", actionPath + token);
    }

    private String handleValidationError(RedirectAttributes redirectAttributes, BindingResult bindingResult,
                                         String path) {
        List<String> errors = bindingResult.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();
        redirectAttributes.addFlashAttribute("passwordErrors", errors);
        return "redirect:" + path;
    }
}