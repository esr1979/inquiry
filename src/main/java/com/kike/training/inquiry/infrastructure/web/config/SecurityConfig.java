package com.kike.training.inquiry.infrastructure.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // Habilita la configuración de seguridad web de Spring
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Deshabilita CSRF temporalmente para simplificar la prueba
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/welcome").authenticated() // /welcome requiere autenticación
                        .requestMatchers("/").permitAll() // La raíz puede ser accedida por todos (para redirigir a login si no autenticado)
                        .requestMatchers("/api/**").permitAll() // Desprotege tus endpoints REST
                        .anyRequest().authenticated() // Cualquier otra petición requiere autenticación
                )
                .formLogin(form -> form
                        .loginPage("/login") // Usa la página de login por defecto de Spring Security (o una custom si la creas)
                        .defaultSuccessUrl("/welcome", true) // Redirige SIEMPRE a /welcome después del login exitoso
                        .permitAll() // Permite que todos accedan a la página de login
                )
                .logout(logout -> logout
                        .permitAll()); // Permite que todos accedan al endpoint de logout

        return http.build();
    }

    // NOTA: Si estás usando spring.security.user.name/password en application.properties,
    // NO necesitas definir un @Bean de UserDetailsService aquí. Spring Boot lo configura automáticamente.
    // Si necesitas más usuarios o una lógica más compleja para usuarios en memoria, entonces sí lo necesitarías.
}
