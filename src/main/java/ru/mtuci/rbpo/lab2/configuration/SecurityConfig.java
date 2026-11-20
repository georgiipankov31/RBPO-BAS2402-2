package ru.mtuci.rbpo.lab2.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.DispatcherType;

import ru.mtuci.rbpo.lab2.service.JwtService;

@Configuration
// Включает проверку @PreAuthorize перед вызовом методов.
@EnableMethodSecurity
public class SecurityConfig {
    // @Bean сохраняет объект в Spring для передачи в другие компоненты.
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 12 задает вычислительную сложность BCrypt.
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        // Spring передает нашу загрузку учетной записи из БД и BCrypt для проверки пароля.
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(DaoAuthenticationProvider provider) {
        return new ProviderManager(provider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, DaoAuthenticationProvider provider,
            JwtService jwt, UserDetailsService users) throws Exception {
        http
                .authenticationProvider(provider)
                // Токены передаются явно в заголовке и JSON, без аутентификации через cookie.
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(errors -> errors.authenticationEntryPoint((request, response, error) -> {
                    response.setHeader("WWW-Authenticate", "Bearer");
                    response.setStatus(401);
                }))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/auth/login", "/auth/register", "/auth/refresh", "/api/register").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().denyAll())
                .addFilterBefore(new JwtTokenFilter(jwt, users), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
