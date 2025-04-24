package pl.error_handling_app;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pl.error_handling_app.user.User;
import pl.error_handling_app.user.UserService;

@Controller
public class HomeController {


    private final UserService userService;

    public HomeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
        String name() {
            return "redirect:/home";
        }

    @GetMapping("/home")
    String userPanel(Model model) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findUserByEmail(currentUserEmail).orElseThrow();
        model.addAttribute("userName", user.getFirstName());
        return "user-panel";
    }
}
