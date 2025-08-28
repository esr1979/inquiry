package com.kike.training.inquiry.infrastructure.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Esta clase es el centro de control de la seguridad.
 * MODELO SIMPLIFICADO: Cualquier usuario que se autentique con éxito en Azure
 * tiene permiso para acceder a todas las partes seguras de la aplicación, incluida la API.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * ¡El corazón de la configuración! Aquí definimos el "libro de reglas" de la seguridad.
     *
     * @param http El objeto principal de configuración de seguridad.
     * @return La cadena de filtros de seguridad configurada.
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // SECCIÓN 1: REGLAS DE ACCESO (¿Quién puede entrar a qué URL?)
                .authorizeHttpRequests(authorize -> authorize
                        // Zonas públicas: la raíz, errores, logout, etc. Cualquiera puede acceder.
                        .requestMatchers("/", "/error", "/webjars/**", "/loggedout").permitAll()
                        // URLs del proceso de login de Spring. Deben ser públicas.
                        .requestMatchers("/oauth2/**", "/login/**").permitAll()
                        // === LA CLAVE DE LA SIMPLIFICACIÓN ===
                        // AHORA, CUALQUIER OTRA URL, sea web o API, solo requiere estar autenticado.
                        // No se mira ningún rol ni permiso especial.
                        .anyRequest().authenticated()
                )
                // SECCIÓN 2: PROCESO DE LOGIN (¿Cómo se entra?)
                .oauth2Login(oauth2Login -> oauth2Login
                        // Si el login es exitoso, vamos a /welcome.
                        .defaultSuccessUrl("/welcome")
                )
                // Habilita la autenticación básica, que usarán nuestros tests.
                .httpBasic(withDefaults())
                // SECCIÓN 3: SEGURIDAD DE LA API (ELIMINADA)
                // Hemos quitado el bloque .oauth2ResourceServer(...).
                // La API ya no se valida como un recurso externo, sino como parte de la sesión web.
                .csrf(csrf -> csrf.disable()) // Deshabilitar CSRF para simplificar los tests con POST/PUT
                // SECCIÓN 4: PROCESO DE LOGOUT (¿Cómo se sale?)
                .logout(logout -> logout
                        // Usamos nuestro manejador personalizado que redirige a la página de logout de Azure.
                        .logoutSuccessHandler(oidcLogoutSuccessHandler())
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );
        return http.build();
    }

    /**
     * Este bean define qué hacer cuando el usuario pulsa "Logout".
     * Redirige a la página de logout de Azure para cerrar la sesión globalmente.
     */
    @Bean
    public LogoutSuccessHandler oidcLogoutSuccessHandler() {
        // Sería ideal leer el tenant-id de properties, pero para simplificar lo dejamos aquí.
        return (request, response, authentication) -> {
            String tenantId = "c990bb7a-51f4-439b-bd36-9c07fb1041c0";
            String postLogoutRedirectUri = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/loggedout";
            String logoutUrl = "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/logout?post_logout_redirect_uri=" + postLogoutRedirectUri;
            response.sendRedirect(logoutUrl);
        };
    }

    // Todos los demás beans (oidcUserService, jwtAuthenticationConverter, etc.) que tuvieras
    // relacionados con la validación de tokens o roles de API ya no son necesarios.
}
