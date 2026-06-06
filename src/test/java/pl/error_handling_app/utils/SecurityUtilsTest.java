package pl.error_handling_app.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityUtilsTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    //Testy metody hasRole

    @Test
    void shouldReturnTrueWhenUserHasRole() {
        //given
        Authentication authentication = mock(Authentication.class);
        GrantedAuthority authority = mock(GrantedAuthority.class);
        when(authority.getAuthority()).thenReturn("ROLE_USER");

        doReturn(List.of(authority)).when(authentication).getAuthorities();

        //when, then
        assertThat(SecurityUtils.hasRole(authentication, "ROLE_USER")).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUserDoesNotHaveRole() {
        //given
        Authentication authentication = mock(Authentication.class);
        GrantedAuthority authority = mock(GrantedAuthority.class);
        when(authority.getAuthority()).thenReturn("ROLE_USER");

        doReturn(List.of(authority)).when(authentication).getAuthorities();

        //when, then
        assertThat(SecurityUtils.hasRole(authentication, "ROLE_ADMINISTRATOR")).isFalse();
    }

    @Test
    void shouldReturnFalseWhenAuthenticationIsNullInHasRole() {
        assertThat(SecurityUtils.hasRole(null, "ROLE_USER")).isFalse();
    }

    //Testy metody isAdmin

    @Test
    void shouldReturnTrueWhenUserIsAdmin() {
        //given
        Authentication authentication = mock(Authentication.class);
        GrantedAuthority authority = mock(GrantedAuthority.class);

        when(authority.getAuthority()).thenReturn("ROLE_ADMINISTRATOR");

        doReturn(List.of(authority)).when(authentication).getAuthorities();

        //when, then
        assertThat(SecurityUtils.isAdmin(authentication)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUserIsNotAdmin() {
        //given
        Authentication authentication = mock(Authentication.class);
        GrantedAuthority authority = mock(GrantedAuthority.class);
        when(authority.getAuthority()).thenReturn("ROLE_EMPLOYEE");

        doReturn(List.of(authority)).when(authentication).getAuthorities();

        //when, then
        assertThat(SecurityUtils.isAdmin(authentication)).isFalse();
    }

    //Testy metody getCurrentUserEmail

    @Test
    void shouldReturnEmailWhenUserIsAuthenticated() {
        //given
        String expectedEmail = "jan@kowalski.pl";
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(expectedEmail);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        //when
        String result = SecurityUtils.getCurrentUserEmail();

        //then
        assertThat(result).isEqualTo(expectedEmail);
    }

    @Test
    void shouldReturnNullWhenAuthenticationIsNullInContext() {
        //given
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);

        SecurityContextHolder.setContext(securityContext);

        //when, then
        assertThat(SecurityUtils.getCurrentUserEmail()).isNull();
    }
}