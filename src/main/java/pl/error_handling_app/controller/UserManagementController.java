package pl.error_handling_app.controller;

import jakarta.validation.Valid;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.error_handling_app.company.CompanyService;
import pl.error_handling_app.user.*;
import pl.error_handling_app.user.dto.UserDto;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class UserManagementController {

    private final UserService userService;
    private final CompanyService companyService;
    private final UserRoleRepository userRoleRepository;

    public UserManagementController(UserService userService, CompanyService companyService, UserRoleRepository userRoleRepository) {
        this.userService = userService;
        this.companyService = companyService;
        this.userRoleRepository = userRoleRepository;
    }

    @GetMapping("/manage-users")
    String manageUsers(Model model) {
        if (!model.containsAttribute("newUser")) {
            model.addAttribute("newUser", new UserDto());
        }
        prepareUserManagementData(model);
        return "manage-users";
    }

    @PostMapping("/add-user")
    String addUser(@Valid @ModelAttribute("newUser") UserDto newUser,
                   BindingResult bindingResult,
                   Model model,
                   RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            prepareUserManagementData(model);
            return "manage-users";
        }

        userService.addUser(newUser);
        addSuccessMessage(redirectAttributes, "Użytkownik został dodany");
        return redirectToUserManagementPage();
    }

    @PostMapping("/edit-user/{id}")
    String editUser(@PathVariable Long id,
                    @Valid @ModelAttribute("user") UserDto user,
                    BindingResult bindingResult,
                    RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            redirectAttributes.addFlashAttribute("editErrors", errors);
            return redirectToUserManagementPage();
        }

        userService.updateUser(id, user);
        addSuccessMessage(redirectAttributes, "Edycja danych użytkownika przebiegła pomyślnie.");
        return redirectToUserManagementPage();
    }

    @PostMapping("/delete-user/{id}")
    String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.deleteUser(id);
        addSuccessMessage(redirectAttributes, "Użytkownik został usunięty.");
        return redirectToUserManagementPage();
    }

    private void prepareUserManagementData(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        model.addAttribute("companies", companyService.findALlCompanies());
        model.addAttribute("roles", userRoleRepository.findAll());
    }

    private void addSuccessMessage(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute("success", message);
    }

    private String redirectToUserManagementPage() {
        return "redirect:/admin/manage-users";
    }
}
