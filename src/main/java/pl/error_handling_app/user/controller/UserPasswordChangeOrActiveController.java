package pl.error_handling_app.user.controller;

import jakarta.validation.Valid;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.error_handling_app.exception.TokenNotFoundException;
import pl.error_handling_app.user.entity.TokenStatus;
import pl.error_handling_app.user.service.UserPasswordChangeOrActiveService;
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
        return "user/verification-status-page";
    }

    @GetMapping("/verification/{token}")
    public String verifyAccount(@PathVariable String token, Model model) {
        return handleGetTokenRequest(token, model, true, "/account/verification/");
    }

    @PostMapping("/verification/{token}")
    public String processActivation(@PathVariable String token,
                                    @Valid @ModelAttribute("passwords") PasswordsDto passwords,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes) {
        return handlePostTokenRequest(token, passwords, bindingResult, redirectAttributes, false, "/account/verification/");
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "user/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(@RequestParam String email) {
        service.sendResetPasswordMail(email);
        return "redirect:/account/forgot-password?ok";
    }

    @GetMapping("/forgot-password/{token}")
    public String changePasswordForm(@PathVariable String token, Model model) {
        return handleGetTokenRequest(token, model, false, "/account/forgot-password/");
    }

    @PostMapping("/forgot-password/{token}")
    public String processPasswordReset(@PathVariable String token,
                                       @Valid @ModelAttribute("passwords") PasswordsDto passwords,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes) {
        return handlePostTokenRequest(token, passwords, bindingResult, redirectAttributes, true, "/account/forgot-password/");
    }

    @ExceptionHandler(TokenNotFoundException.class)
    public String handleTokenNotFound(TokenNotFoundException exception, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("tokenStatus", TokenStatus.INVALID);
        return "redirect:/account/verification";
    }

    private String handleGetTokenRequest(String token, Model model, boolean isActivation, String path) {
        TokenStatus status = service.validateToken(token, !isActivation);
        if (status != TokenStatus.VALID) {
            return showStatusPage(model, status);
        }
        preparePasswordForm(model, token, isActivation, path);
        return "user/activate-account-reset-password";
    }

    private String handlePostTokenRequest(String token, PasswordsDto passwords, BindingResult bindingResult,
                                          RedirectAttributes redirectAttributes, boolean isReset, String path) {
        if (bindingResult.hasErrors()) {
            return handleValidationError(redirectAttributes, bindingResult, path + token);
        }

        TokenStatus status = service.validateToken(token, isReset);
        if (status == TokenStatus.VALID) {
            service.setNewPassword(token, passwords.getPassword());
            return "redirect:/account/verification?success";
        }

        redirectAttributes.addFlashAttribute("tokenStatus", status);
        return "redirect:/account/verification";
    }

    private String showStatusPage(Model model, TokenStatus status) {
        model.addAttribute("tokenStatus", status);
        return "verification-status-page";
    }

    private void preparePasswordForm(Model model, String token, boolean isActivation, String actionPath) {
        model.addAttribute("activateAccount", isActivation);
        model.addAttribute("token", token);
        model.addAttribute("formAction", actionPath + token);
    }

    private String handleValidationError(RedirectAttributes redirectAttributes, BindingResult bindingResult, String path) {
        List<String> errors = bindingResult.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();
        redirectAttributes.addFlashAttribute("passwordErrors", errors);
        return "redirect:" + path;
    }
}