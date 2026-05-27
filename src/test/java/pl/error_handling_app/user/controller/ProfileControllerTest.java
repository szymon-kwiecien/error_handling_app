package pl.error_handling_app.user.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.h2.H2ConsoleProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.error_handling_app.user.dto.ChangeEmailDto;
import pl.error_handling_app.user.dto.ChangePasswordDto;
import pl.error_handling_app.user.dto.UserProfileDetailsDto;
import pl.error_handling_app.user.service.UserProfileService;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserProfileService userProfileService;

    @MockitoBean
    private SimpMessagingTemplate simpMessagingTemplate;

    @MockitoBean
    private H2ConsoleProperties h2ConsoleProperties;

    private UserProfileDetailsDto mockUserDetails;

    @BeforeEach
    void setUp() {
        mockUserDetails = new UserProfileDetailsDto(
                1L, "Jan", "Kowalski", "test@fixaro.pl", "ROLE_EMPLOYEE", "FixaroCorp", "2", "48"
        );
    }

    @Test
    @WithMockUser(username = "test@fixaro.pl")
    void shouldShowProfilePageWithAllRequiredModels() throws Exception {
        //given
        given(userProfileService.findUserProfileDetailsByEmail("test@fixaro.pl")).willReturn(mockUserDetails);

        //when then
        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/profile"))
                .andExpect(model().attributeExists("userDetails"))
                .andExpect(model().attributeExists("emailChangeDto"))
                .andExpect(model().attributeExists("passwordChangeDto"))
                .andExpect(model().attribute("userDetails", mockUserDetails));
    }

    @Test
    @WithMockUser(username = "test@fixaro.pl")
    void shouldChangeEmailSuccessfullyAndRedirect() throws Exception {
        //when,then
        mockMvc.perform(post("/profile/change-email")
                        .with(csrf())
                        .param("userId", "1")
                        .param("newEmail", "nowy@fixaro.pl")
                        .param("currentPassword", "MojeHaslo123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("successMessage", "Zmieniono adres e-mail."));

        //then
        verify(userProfileService).changeEmail(any(ChangeEmailDto.class));
    }

    @Test
    @WithMockUser(username = "test@fixaro.pl")
    void shouldNotChangeEmailWhenValidationFailsAndReturnToForm() throws Exception {
        //given
        given(userProfileService.findUserProfileDetailsByEmail("test@fixaro.pl")).willReturn(mockUserDetails);

        //when, then
        mockMvc.perform(post("/profile/change-email")
                        .with(csrf())
                        .param("userId", "1")
                        .param("newEmail", "zly-email")
                        .param("currentPassword", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("user/profile"))
                .andExpect(model().attributeHasFieldErrors("emailChangeDto", "newEmail", "currentPassword"));
    }

    @Test
    @WithMockUser(username = "test@fixaro.pl")
    void shouldChangePasswordSuccessfullyAndRedirect() throws Exception {
        //when,then
        mockMvc.perform(post("/profile/change-password")
                        .with(csrf())
                        .param("userId", "1")
                        .param("currentPassword", "StareHaslo123")
                        .param("newPassword", "NoweHaslo123!")
                        .param("confirmedNewPassword", "NoweHaslo123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("successMessage", "Pomyślnie zmieniono hasło."));

        //then
        verify(userProfileService).changePassword(any(ChangePasswordDto.class));
    }

    @Test
    @WithMockUser(username = "test@fixaro.pl")
    void shouldNotChangePasswordWhenValidationFailsAndReturnToForm() throws Exception {
        //given
        given(userProfileService.findUserProfileDetailsByEmail("test@fixaro.pl")).willReturn(mockUserDetails);

        //when, then
        mockMvc.perform(post("/profile/change-password")
                        .with(csrf())
                        .param("userId", "1")
                        .param("currentPassword", "")
                        .param("newPassword", "zzzz")
                        .param("confirmedNewPassword", "lpxpxpx"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/profile"))
                .andExpect(model().attributeHasFieldErrors("passwordChangeDto", "currentPassword", "newPassword"));
    }
}