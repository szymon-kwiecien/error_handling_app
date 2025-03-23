package pl.error_handling_app.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class CustomSecurityConfig {

    private static final String USER_ROLE = "USER";
    private static final String EMPLOYEE_ROLE = "EMPLOYEE";
    private static final String ADMIN_ROLE = "ADMINISTRATOR";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(request -> request
                .requestMatchers("/img/**", "/styles/**", "/scripts/**").permitAll()
                .anyRequest().authenticated()
        );
        http.formLogin(login -> login.loginPage("/login").permitAll())
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout/**", HttpMethod.GET.name()))
                        .logoutSuccessUrl("/login?logout").permitAll());
        return http.build();
    }
}
