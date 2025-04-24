package pl.error_handling_app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.error_handling_app.company.CompanyService;
import pl.error_handling_app.exception.UserAlreadyExistsException;
import pl.error_handling_app.user.*;
import pl.error_handling_app.user.dto.UserDto;
import pl.error_handling_app.user.dto.UserEditDto;

import java.util.NoSuchElementException;

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
        model.addAttribute("users", userService.findAllUsers());
        model.addAttribute("companies", companyService.findALlCompanies());
        model.addAttribute("roles", userRoleRepository.findAll());
        return "manage-users";
    }


    @PostMapping("/add-user")
    String addUser(UserDto newUser, RedirectAttributes redirectAttributes) {
        try {
            userService.addUser(newUser);
            redirectAttributes.addFlashAttribute("success", "Użytkownik został dodany");

        } catch(UserAlreadyExistsException | NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("error", "Podczas dodawania użytkownika wystąpił błąd: " + e.getMessage());
        }
        return "redirect:manage-users";
    }

    @PostMapping("/edit-user/{id}")
    String editUser(@PathVariable Long id, UserEditDto user, RedirectAttributes redirectAttributes) {
        try {
            userService.updateUser(id, user);
            redirectAttributes.addFlashAttribute("success", "Edycja danych użytkownika przebiegła pomyślnie.");
        } catch(IllegalArgumentException | NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("error", "Podczas edycji użytkownika wystąpił błąd: " + e.getMessage());
        }
        return "redirect:/admin/manage-users";
    }

    @PostMapping("/delete-user/{id}")
    String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "Użytkownik został usunięty.");
        } catch(NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("error", "Wystąpił błąd podczas usuwania użytkownika.");
        }
            return "redirect:/admin/manage-users";
        }
    }

