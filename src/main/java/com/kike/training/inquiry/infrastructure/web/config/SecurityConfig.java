package com.kike.training.inquiry.infrastructure.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // Importar para @EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import static org.springframework.security.config.Customizer.withDefaults; // Importar para withDefaults() si se usa

@Configuration
@EnableWebSecurity // Habilita la configuración de seguridad web de Spring
@EnableMethodSecurity // Habilita seguridad a nivel de método con @PreAuthorize, @PostAuthorize, etc.
public class SecurityConfig {

    // Bean para procesar la información del usuario OIDC (OpenID Connect)
    // Es el servicio que Spring Security usa para obtener los detalles del usuario
    // una vez que Entra ID ha autenticado al usuario y devuelto un token ID.
    @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        final OidcUserService delegate = new OidcUserService();
        return (userRequest) -> {
            OidcUser oidcUser = delegate.loadUser(userRequest);
            // Aquí puedes añadir lógica personalizada para procesar el OidcUser
            // Por ejemplo, mapear roles de Entra ID a GrantedAuthorities de Spring Security
            // o enriquecer el perfil del usuario.
            return oidcUser;
        };
    }

    // Bean para procesar la información del usuario OAuth2 (si se usa un flujo OAuth2 genérico)
    // Aunque Entra ID usa OIDC, este bean puede ser útil para otros proveedores o si necesitas
    // acceder a recursos protegidos con el Access Token.
    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        return (userRequest) -> {
            OAuth2User oauth2User = delegate.loadUser(userRequest);
            // Aquí puedes añadir lógica personalizada para procesar el OAuth2User.
            return oauth2User;
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF: Spring Security maneja CSRF automáticamente para OAuth2/OIDC,
                // por lo que no es necesario deshabilitarlo explícitamente a menos que tengas un caso de uso muy específico.
                // Si lo necesitas deshabilitar por alguna razón, podrías volver a añadir: .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        // Permite acceso público a la raíz y a la página de error.
                        // La página de login ya no será un formulario, sino un punto de inicio para el flujo OAuth2.
                        .requestMatchers("/", "/error", "/webjars/**", "/loggedout").permitAll()
                        // Permite que Spring Security maneje las redirecciones de OAuth2
                        // Esto incluye /oauth2/authorization/azure y /login/oauth2/code/azure
                        .requestMatchers("/oauth2/**", "/login/**").permitAll()
                        // Desprotege tus endpoints REST si son accesibles sin autenticación
                        // OJO: Si /api/** debe ser seguro, cambia .permitAll() a .authenticated()
                        .requestMatchers("/api/**").permitAll()
                        // Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated()
                )
                // Configuración para el inicio de sesión OAuth2/OIDC
                .oauth2Login(oauth2Login -> oauth2Login
                        // Si un usuario no autenticado intenta acceder a un recurso protegido,
                        // será redirigido automáticamente a la página de login de Entra ID.
                        // No necesitamos especificar un loginPage si queremos que la redirección sea automática.
                        // Si quisieras una página local antes de redirigir a Entra ID, la pondrías aquí,
                        // y esa página tendría un botón/enlace a /oauth2/authorization/azure.
                        // defaultSuccessUrl es a donde se redirige después de un login exitoso.
                        .defaultSuccessUrl("/welcome", true)
                        // Configura los servicios para cargar la información del usuario OIDC/OAuth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(oidcUserService())
                                .userService(oauth2UserService())
                        )
                )
                // Configuración para el cierre de sesión
                .logout(logout -> logout
                        // Manejador de éxito de logout para OIDC. Es crucial para que la sesión se cierre
                        // correctamente también en el proveedor de identidad (Entra ID).
                        .logoutSuccessHandler(oidcLogoutSuccessHandler())
                        .invalidateHttpSession(true) // Invalida la sesión HTTP de la aplicación
                        .clearAuthentication(true)   // Limpia el contexto de seguridad
                        .deleteCookies("JSESSIONID") // Elimina la cookie de sesión
                        .permitAll() // Permite que todos accedan al endpoint de logout
                );

        return http.build();
    }

    // Manejador de logout para Entra ID (OIDC)
    // Esto asegura que la sesión de Entra ID también se cierre si el usuario hace logout en tu app.
    // Para un logout completo que cierre la sesión en Entra ID, necesitas redirigir al endpoint
    // de logout de Entra ID con un post_logout_redirect_uri.
    @Bean
    public LogoutSuccessHandler oidcLogoutSuccessHandler() {
        return (request, response, authentication) -> {
            // Obtén el tenant-id de tus propiedades (puedes inyectarlas o hardcodearlas si es solo para prueba)
            // Para producción, es mejor inyectarlo de application.properties/env.
            // Aquí lo hardcodeo como ejemplo, pero idealmente lo inyectarías como un @Value("${entra.id.tenant-id}")
            String tenantId = "c990bb7a-51f4-439b-bd36-9c07fb1041c0"; // <--- REEMPLAZA CON TU TENANT ID REAL
            String postLogoutRedirectUri = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/loggedout";

            // URL completa para el logout de Entra ID
            String logoutUrl = "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/logout?post_logout_redirect_uri=" + postLogoutRedirectUri;

            response.sendRedirect(logoutUrl);
        };
    }
}
