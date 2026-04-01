package pl.error_handling_app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import pl.error_handling_app.exception.UserNotFoundException;
import pl.error_handling_app.user.UserService;

import java.security.Principal;

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
    public String userPanel(Model model, Principal principal) {
        String name = userService.getUserFirstName(principal.getName());
        model.addAttribute("userName", name);
        return "user-panel";
    }

    @ExceptionHandler(UserNotFoundException.class)
    public String handleUserNotFound() {
        return "redirect:/logout";
    }
}
