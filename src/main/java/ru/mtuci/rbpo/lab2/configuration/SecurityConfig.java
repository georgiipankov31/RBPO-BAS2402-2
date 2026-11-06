package ru.mtuci.rbpo.lab2.configuration;

import jakarta.servlet.DispatcherType;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;

import static org.springframework.security.config.Customizer.withDefaults;

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
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, DaoAuthenticationProvider provider) throws Exception {
        http
                .authenticationProvider(provider)
                // Результат входа хранится только в текущем запросе.
                .securityContext(context -> context
                        .securityContextRepository(new RequestAttributeSecurityContextRepository()))
                // Для POST/PATCH/DELETE нужны CSRF-токен и cookie его сессии.
                .csrf(withDefaults())
                // Лямбда задает правила маршрутов; первое подходящее правило применяется.
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        // Доступны без входа; защита CSRF остается включенной.
                        .requestMatchers(HttpMethod.GET, "/csrf-token").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/register").permitAll()
                        // Остальные API требуют входа; роли проверяем через @PreAuthorize.
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().denyAll())
                // Логин и пароль приходят в заголовке Authorization: Basic ...
                .httpBasic(withDefaults());
        return http.build();
    }
}
