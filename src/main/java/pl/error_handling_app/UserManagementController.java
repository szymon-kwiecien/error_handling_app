package pl.error_handling_app;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.error_handling_app.company.CompanyService;
import pl.error_handling_app.user.UserRoleRepository;
import pl.error_handling_app.user.UserService;

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
}
