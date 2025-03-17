package pl.error_handling_app;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.error_handling_app.company.CompanyService;
import pl.error_handling_app.exception.UserAlreadyExistsException;
import pl.error_handling_app.user.UserDto;
import pl.error_handling_app.user.UserRoleRepository;
import pl.error_handling_app.user.UserService;

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
    String addUser(UserDto newUser,RedirectAttributes redirectAttributes) {
        try {
            userService.addUser(newUser);
            redirectAttributes.addFlashAttribute("success", "Użytkownik został dodany");
            return "redirect:manage-users";

        } catch(UserAlreadyExistsException | NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("error", "Podczas dodawania użytkownika wystąpił błąd.");
            return "redirect:manage-users";
        }
    }
}
