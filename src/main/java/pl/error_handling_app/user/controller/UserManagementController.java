package pl.error_handling_app.user.controller;

import jakarta.validation.Valid;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;
import pl.error_handling_app.company.service.CompanyService;
import pl.error_handling_app.user.repository.UserRoleRepository;
import pl.error_handling_app.user.service.UserService;
import pl.error_handling_app.user.dto.UserDto;
import pl.error_handling_app.utils.PaginationUtils;

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
    String manageUsers(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size, Model model) {
        prepareUserManagementModel(model, page, size);
        if (!model.containsAttribute("newUser")) {
            model.addAttribute("newUser", new UserDto());
        }
        return "user/manage-users";
    }

    @PostMapping("/add-user")
    String addUser(@Valid @ModelAttribute("newUser") UserDto newUser,
                   BindingResult bindingResult,
                   @RequestParam(defaultValue = "1") int page,
                   @RequestParam(defaultValue = "10") int size,
                   Model model,
                   RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            prepareUserManagementModel(model, page, size);
            return "user/manage-users";
        }

        userService.addUser(newUser);
        addSuccessMessage(redirectAttributes, "Użytkownik został dodany");
        return redirectToUserManagementPage(page,size);
    }

    @PostMapping("/edit-user/{id}")
    String editUser(@PathVariable Long id,
                    @Valid @ModelAttribute("user") UserDto user,
                    BindingResult bindingResult,
                    @RequestParam(defaultValue = "1") int page,
                    @RequestParam(defaultValue = "10") int size,
                    Authentication authentication,
                    RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            redirectAttributes.addFlashAttribute("editErrors", errors);
            return redirectToUserManagementPage(page,size);
        }

        userService.updateUser(id, user, authentication.getName());
        addSuccessMessage(redirectAttributes, "Edycja danych użytkownika przebiegła pomyślnie.");
        return redirectToUserManagementPage(page,size);
    }

    @PostMapping("/delete-user/{id}")
    String deleteUser(@PathVariable Long id,
                      @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size,
                      Authentication authentication, RedirectAttributes redirectAttributes) {
        userService.deleteUser(id, authentication.getName());
        addSuccessMessage(redirectAttributes, "Użytkownik został usunięty.");
        return redirectToUserManagementPage(page,size);
    }

    private void prepareUserManagementModel(Model model, int page, int size) {
        Pageable pageable = PaginationUtils.createPageable(page, size, Sort.unsorted());
        Page<UserDto> usersPage = userService.findPagedUsers(pageable);

        model.addAttribute("users", usersPage);
        model.addAttribute("currentPage", pageable.getPageNumber() + 1);
        model.addAttribute("pageSize", pageable.getPageSize());
        model.addAttribute("totalPages", usersPage.getTotalPages());
        model.addAttribute("companies", companyService.findAllCompanies());
        model.addAttribute("roles", userRoleRepository.findAll());
    }

    private void addSuccessMessage(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute("success", message);
    }

    private String redirectToUserManagementPage(int page, int size) {
        return UriComponentsBuilder.fromPath("redirect:/admin/manage-users")
                .queryParam("page", page)
                .queryParam("size", size)
                .toUriString();
    }
}
