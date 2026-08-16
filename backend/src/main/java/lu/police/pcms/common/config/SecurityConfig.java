package lu.police.pcms.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de sécurité pour le développement.
 *
 * <p>
 * ⚠️ Cette configuration désactive l'authentification pour faciliter
 * le développement et les tests de l'API.
 * En production, il faudra la remplacer par une configuration sécurisée.
 * </p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Autorise toutes les requêtes sans authentification
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll()
            )
            // Désactive la protection CSRF (pour faciliter les tests POST, PUT, etc.)
            .csrf(csrf -> csrf.disable())
            // Désactive le formulaire de login
            .formLogin(form -> form.disable())
            // Désactive l'authentification Basic
            .httpBasic(httpBasic -> httpBasic.disable());

        return http.build();
    }
}