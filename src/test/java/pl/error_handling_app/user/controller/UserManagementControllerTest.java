package pl.error_handling_app.user.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.h2.H2ConsoleProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.error_handling_app.company.service.CompanyService;
import pl.error_handling_app.user.dto.UserDto;
import pl.error_handling_app.user.service.UserService;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserManagementController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CompanyService companyService;

    @MockitoBean
    private SimpMessagingTemplate simpMessagingTemplate;

    @MockitoBean
    private H2ConsoleProperties h2ConsoleProperties;

    @BeforeEach
    void setUp() {
        Page<UserDto> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        given(userService.findPagedUsers(any(Pageable.class))).willReturn(emptyPage);
        given(companyService.findAllCompanies()).willReturn(List.of());
        given(userService.findAllRoles()).willReturn(List.of());
    }

    @Test
    @WithMockUser(username = "admin@fixaro.pl")
    void shouldShowManageUsersPageWithAllRequiredModels() throws Exception {
        //given (zawarte w setUp)

        //when,then
        mockMvc.perform(get("/admin/manage-users")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/manage-users"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attributeExists("currentPage"))
                .andExpect(model().attributeExists("pageSize"))
                .andExpect(model().attributeExists("totalPages"))
                .andExpect(model().attributeExists("companies"))
                .andExpect(model().attributeExists("roles"))
                .andExpect(model().attributeExists("newUser"));
    }

    @Test
    @WithMockUser(username = "admin@fixaro.pl")
    void shouldAddUserSuccessfullyAndRedirect() throws Exception {
        //when, then
        mockMvc.perform(post("/admin/add-user")
                        .with(csrf())
                        .param("page", "2")
                        .param("size", "5")
                        .param("firstName", "Jan")
                        .param("lastName", "Kowalski")
                        .param("email", "jan@fixaro.pl")
                        .param("companyId", "1")
                        .param("roleId", "1")
                        .param("active", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/manage-users?page=2&size=5"))
                .andExpect(flash().attribute("success", "Użytkownik został dodany"));

        //then
        verify(userService).addUser(any(UserDto.class));
    }

    @Test
    @WithMockUser(username = "admin@fixaro.pl")
    void shouldNotAddUserWhenValidationFailsAndReturnToForm() throws Exception {
        //when, then
        mockMvc.perform(post("/admin/add-user")
                        .with(csrf())
                        .param("firstName", "")
                        .param("lastName", "Kowalski")
                        .param("email", "zlyemail"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/manage-users"))
                .andExpect(model().attributeHasFieldErrors("newUser", "firstName", "email"));
    }

    @Test
    @WithMockUser(username = "admin@fixaro.pl")
    void shouldEditUserSuccessfullyAndRedirect() throws Exception {
        //when, then
        mockMvc.perform(post("/admin/edit-user/10")
                        .with(csrf())
                        .param("page", "1")
                        .param("size", "10")
                        .param("firstName", "Piotr")
                        .param("lastName", "Nowak")
                        .param("email", "piotr@fixaro.pl")
                        .param("companyId", "2")
                        .param("roleId", "2")
                        .param("active", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/manage-users?page=1&size=10"))
                .andExpect(flash().attribute("success", "Edycja danych użytkownika przebiegła pomyślnie."));

        //then
        verify(userService).updateUser(eq(10L), any(UserDto.class), eq("admin@fixaro.pl"));
    }

    @Test
    @WithMockUser(username = "admin@fixaro.pl")
    void shouldNotEditUserWhenValidationFailsAndRedirectWithErrors() throws Exception {
        //when, then
        mockMvc.perform(post("/admin/edit-user/10")
                        .with(csrf())
                        .param("firstName", "")
                        .param("email", "zlymail"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/manage-users?page=1&size=10"))
                .andExpect(flash().attributeExists("editErrors"));
    }

    @Test
    @WithMockUser(username = "admin@fixaro.pl")
    void shouldDeleteUserSuccessfullyAndRedirect() throws Exception {
        //when, then
        mockMvc.perform(post("/admin/delete-user/55")
                        .with(csrf())
                        .param("page", "3")
                        .param("size", "20"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/manage-users?page=3&size=20"))
                .andExpect(flash().attribute("success", "Użytkownik został usunięty."));

        //then
        verify(userService).deleteUser(eq(55L), eq("admin@fixaro.pl"));
    }
}