package pl.error_handling_app.user.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.h2.H2ConsoleProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.error_handling_app.exception.TokenNotFoundException;
import pl.error_handling_app.user.entity.TokenStatus;
import pl.error_handling_app.user.service.UserPasswordChangeOrActiveService;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserPasswordChangeOrActiveController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserPasswordChangeOrActiveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserPasswordChangeOrActiveService service;

    @MockitoBean
    private SimpMessagingTemplate simpMessagingTemplate;

    @MockitoBean
    private H2ConsoleProperties h2ConsoleProperties;

    private static final String SAMPLE_TOKEN = "abc-123-xyz";

    @Test
    void shouldShowVerificationStatusPage() throws Exception {
        //when, then
        mockMvc.perform(get("/account/verification"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/verification-status-page"));
    }

    @Test
    void shouldShowForgotPasswordForm() throws Exception {
        //when then
        mockMvc.perform(get("/account/forgot-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/forgot-password"));
    }

    @Test
    void shouldHandleForgotPasswordPostAndRedirect() throws Exception {
        //when, then
        mockMvc.perform(post("/account/forgot-password")
                        .with(csrf())
                        .param("email", "test@fixaro.pl"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account/forgot-password?ok"));

        //then
        verify(service).sendResetPasswordMail("test@fixaro.pl");
    }

    @Test
    void shouldShowActivationFormWhenTokenIsValid() throws Exception {
        //given
        given(service.validateToken(SAMPLE_TOKEN, false)).willReturn(TokenStatus.VALID);

        //when, then
        mockMvc.perform(get("/account/verification/{token}", SAMPLE_TOKEN))
                .andExpect(status().isOk())
                .andExpect(view().name("user/activate-account-reset-password"))
                .andExpect(model().attribute("activateAccount", true))
                .andExpect(model().attribute("token", SAMPLE_TOKEN))
                .andExpect(model().attribute("formAction", "/account/verification/" + SAMPLE_TOKEN));
    }

    @Test
    void shouldShowStatusPageWhenTokenIsInvalidOrExpired() throws Exception {
        //given
        given(service.validateToken(SAMPLE_TOKEN, false)).willReturn(TokenStatus.EXPIRED);

        //when, then
        mockMvc.perform(get("/account/verification/{token}", SAMPLE_TOKEN))
                .andExpect(status().isOk())
                .andExpect(view().name("user/verification-status-page"))
                .andExpect(model().attribute("tokenStatus", TokenStatus.EXPIRED));
    }

    @Test
    void shouldHandleTokenNotFoundExceptionWithGlobalHandler() throws Exception {
        //given
        given(service.validateToken(eq(SAMPLE_TOKEN), anyBoolean())).willThrow(new TokenNotFoundException("Token not found"));

        //when, then
        mockMvc.perform(get("/account/verification/{token}", SAMPLE_TOKEN))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account/verification"))
                .andExpect(flash().attribute("tokenStatus", TokenStatus.INVALID));
    }

    @Test
    void shouldProcessActivationSuccessfullyAndRedirect() throws Exception {
        //given
        given(service.validateToken(SAMPLE_TOKEN, false)).willReturn(TokenStatus.VALID);

        //when, then
        mockMvc.perform(post("/account/verification/{token}", SAMPLE_TOKEN)
                        .with(csrf())
                        .param("password", "NoweSilneHaslo123!")
                        .param("confirmPassword", "NoweSilneHaslo123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account/verification?success"));

        //then
        verify(service).setNewPassword(SAMPLE_TOKEN, "NoweSilneHaslo123!");
    }

    @Test
    void shouldNotProcessActivationWhenValidationFailsAndRedirectBackToForm() throws Exception {
        //when, then
        mockMvc.perform(post("/account/verification/{token}", SAMPLE_TOKEN)
                        .with(csrf())
                        .param("password", "123") // za krotkie hasło
                        .param("confirmPassword", "123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account/verification/" + SAMPLE_TOKEN))
                .andExpect(flash().attributeExists("passwordErrors"));
    }

    @Test
    void shouldRedirectToVerificationWhenTokenExpiresDuringPost() throws Exception {
        //given(Użytkownik wszedł w formularz, ale zanim kliknął zapisz, token wygasł)
        given(service.validateToken(SAMPLE_TOKEN, false)).willReturn(TokenStatus.INVALID);

        //when, then
        mockMvc.perform(post("/account/verification/{token}", SAMPLE_TOKEN)
                        .with(csrf())
                        .param("password", "DobreHaslo123!")
                        .param("confirmPassword", "DobreHaslo123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account/verification"))
                .andExpect(flash().attribute("tokenStatus", TokenStatus.INVALID));
    }
}