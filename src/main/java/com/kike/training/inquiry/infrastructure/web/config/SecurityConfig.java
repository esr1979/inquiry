package com.kike.training.inquiry.infrastructure.web.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Esta clase es el centro de control de la seguridad de toda la aplicación.
 * Es como el cuadro de mandos de un sistema de seguridad, donde definimos
 * quién puede entrar, por dónde, qué necesitan para hacerlo y cómo se van.
 */
@Configuration
@EnableWebSecurity // Activa la seguridad web de Spring. Sin esto, nada de lo que hay aquí funcionaría.
@EnableMethodSecurity // Permite usar anotaciones como @PreAuthorize en los controladores para una seguridad más granular.
public class SecurityConfig {

    /**
     * Este bean se encarga de procesar la información del usuario cuando se loguea con OpenID Connect (OIDC).
     * OIDC es el protocolo que usamos con Azure AD para saber QUIÉN es el usuario.
     *
     * Analogía: Después de que el portero (Azure) te deja entrar, este servicio coge tu DNI (el token),
     * mira si tienes alguna acreditación especial (los roles) y te la añade a tu perfil de seguridad.
     */
    @Bean
    @Profile("!local") // Este bean solo se activará si el perfil 'local' NO está activo.
    public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        final OidcUserService delegate = new OidcUserService(); // Usamos el servicio por defecto como base.
        return (userRequest) -> {
            // 1. Obtenemos el usuario tal y como lo devuelve Azure.
            OidcUser oidcUser = delegate.loadUser(userRequest);

            // 2. Buscamos en el token una "claim" (un atributo) llamada "roles".
            final List<String> roles = oidcUser.getClaimAsStringList("roles");
            if (roles == null || roles.isEmpty()) {
                return oidcUser; // Si no hay roles, no hacemos nada más.
            }

            // 3. Convertimos cada rol de Azure (ej: "Wexhvloc.Admin") en una "Autoridad" de Spring (ej: "ROLE_Wexhvloc.Admin").
            // El prefijo "ROLE_" es una convención de Spring para que funcionen las comprobaciones de roles.
            Collection<GrantedAuthority> roleAuthorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());

            // 4. Unimos las autoridades que ya tenía el usuario con las nuevas que hemos creado a partir de los roles.
            Collection<GrantedAuthority> allAuthorities = Stream.concat(
                    oidcUser.getAuthorities().stream(),
                    roleAuthorities.stream()
            ).collect(Collectors.toList());

            // 5. Devolvemos un nuevo objeto de usuario con la lista de autoridades completa.
            return new DefaultOidcUser(allAuthorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
        };
    }

    /**
     * Este es un servicio genérico de OAuth2. OIDC es una capa por encima de OAuth2.
     * Lo mantenemos por si en el futuro se conecta otro proveedor que no sea OIDC.
     * Por ahora, simplemente hace el trabajo estándar.
     */
    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        return new DefaultOAuth2UserService();
    }

    /**
     * ¡El corazón de la configuración! Aquí definimos el "libro de reglas" de la seguridad.
     * Este método es como el guion que sigue el portero de la discoteca.
     *
     * @param http El objeto principal de configuración de seguridad.
     * @param customAuthorizationRequestResolver Nuestro "director de orquesta" personalizado para el login.
     * @return La cadena de filtros de seguridad configurada.
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, OAuth2AuthorizationRequestResolver customAuthorizationRequestResolver) throws Exception {
        http
                // SECCIÓN 1: REGLAS DE ACCESO (¿Quién puede entrar a qué URL?)
                .authorizeHttpRequests(authorize -> authorize
                        // Estas son las zonas públicas: la raíz, la página de error, etc. Cualquiera puede acceder.
                        .requestMatchers("/", "/error", "/webjars/**", "/loggedout").permitAll()
                        // Estas son las URLs que usa Spring para el proceso de login. Deben ser públicas.
                        .requestMatchers("/oauth2/**", "/login/**").permitAll()
                        // Estas son nuestras zonas seguras. Para acceder, TIENES que estar autenticado.
                        .requestMatchers("/api/v1/exhibition-locations/**", "/get-api-token").authenticated()
                        // Para cualquier otra URL que no hayamos listado, por seguridad, también requerimos autenticación.
                        .anyRequest().authenticated()
                )
                // SECCIÓN 2: PROCESO DE LOGIN (¿Cómo se entra?)
                .oauth2Login(oauth2Login -> oauth2Login
                        // Si el login es exitoso, ¿a dónde vamos? A /welcome (si no intentábamos ir a otro sitio antes).
                        .defaultSuccessUrl("/welcome")
                        // Le decimos a Spring que use nuestros servicios personalizados para obtener los datos del usuario.
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(oidcUserService())
                                .userService(oauth2UserService())
                        )
                        // === ¡LA PIEZA CLAVE DE LA SOLUCIÓN! ===
                        // Aquí le decimos a Spring: "Para construir la URL que redirige a Azure,
                        // no uses tu método normal. Usa el 'director de orquesta' que hemos creado".
                        .authorizationEndpoint(authorization ->
                                authorization.authorizationRequestResolver(customAuthorizationRequestResolver)
                        )
                )
                // SECCIÓN 3: SEGURIDAD DE LA API (¿Cómo validamos tokens de API?)
                // Esto configura la aplicación para que pueda actuar como un "Servidor de Recursos".
                // Es decir, que pueda aceptar y validar tokens 'Bearer' en las llamadas a la API.
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                // SECCIÓN 4: PROCESO DE LOGOUT (¿Cómo se sale?)
                .logout(logout -> logout
                        // Usamos nuestro manejador personalizado que redirige a la página de logout de Azure.
                        .logoutSuccessHandler(oidcLogoutSuccessHandler())
                        .invalidateHttpSession(true)   // Invalida la sesión de nuestra aplicación.
                        .clearAuthentication(true)   // Limpia los datos de seguridad.
                        .deleteCookies("JSESSIONID") // Borra la cookie de sesión.
                        .permitAll()
                );
        return http.build();
    }

    /**
     * === EL BEAN QUE ARREGLA TODO ===
     * Este bean es nuestro "director de orquesta". Su misión es interceptar la creación
     * de la URL de login y hacer dos cosas cruciales:
     * 1. Asegurarse de que SIEMPRE se pide el permiso (scope) para nuestra API.
     * 2. (Temporal) Forzar a que Azure muestre la pantalla de consentimiento al usuario.
     *
     * Analogía: Spring prepara una lista de la compra ("openid", "profile"). Justo antes de ir a la
     * tienda (Azure), este director coge la lista, le añade el ingrediente que se había olvidado
     * ("api://.../access_as_user") y le pone una nota grande que dice "¡Pide permiso al cliente!".
     */
    @Bean
    public OAuth2AuthorizationRequestResolver customAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        // Usamos el resolver por defecto como base para no tener que reinventar la rueda.
        DefaultOAuth2AuthorizationRequestResolver defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, "/oauth2/authorization");

        // Creamos una implementación completa de la interfaz porque una lambda daba error (la interfaz tiene 2 métodos).
        return new OAuth2AuthorizationRequestResolver() {
            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
                OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);
                return customizeAuthorizationRequest(authorizationRequest);
            }

            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
                OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request, clientRegistrationId);
                return customizeAuthorizationRequest(authorizationRequest);
            }

            // Método privado para no repetir código.
            private OAuth2AuthorizationRequest customizeAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest) {
                if (authorizationRequest == null) {
                    return null;
                }

                // Cogemos los scopes que Spring ya había puesto (ej: "openid", "profile").
                Set<String> newScopes = new HashSet<>(authorizationRequest.getScopes());

                // 1. Y añadimos el scope de nuestra API. Debe coincidir con el de application.properties.
                newScopes.add("api://a735b66e-8bde-42e0-99de-8fcf8a4b7215/access_as_user");

                // Devolvemos una nueva petición de autorización con la lista de scopes completa y el parámetro extra.
                return OAuth2AuthorizationRequest.from(authorizationRequest)
                        .scopes(newScopes)
                        // 2. === LA SOLUCIÓN TEMPORAL ===
                        // Añadimos el parámetro "prompt=consent". Esto le dice a Azure que SIEMPRE muestre
                        // la pantalla de consentimiento al usuario, incluso si ya la aceptó antes.
                        // Es un parche necesario porque no tienes permisos para dar "Admin Consent" en el portal.
                        // Usamos la cadena "prompt" directamente porque es compatible con todas las versiones de Spring.
                        .additionalParameters(params -> params.put("prompt", "consent"))
                        .build();
            }
        };
    }

    /**
     * Este bean es como una "máquina expendedora de tokens" para la propia aplicación.
     * Cuando nuestro `TokenController` necesita un token para llamar a una API, se lo pide a este gestor.
     * Es crucial porque sabe cómo usar el "refresh_token" para obtener nuevos tokens de acceso
     * cuando los viejos caducan, sin que el usuario tenga que hacer nada.
     */
    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientRepository authorizedClientRepository) {
        DefaultOAuth2AuthorizedClientManager authorizedClientManager =
                new DefaultOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientRepository);
        authorizedClientManager.setAuthorizedClientProvider(
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .authorizationCode() // Puede usar el código de autorización del login inicial.
                        .refreshToken()      // ¡La clave! Puede usar refresh tokens para obtener nuevos access tokens.
                        .build());
        return authorizedClientManager;
    }

    /**
     * Este bean define qué hacer cuando el usuario pulsa "Logout".
     * No basta con cerrar la sesión en nuestra aplicación, también hay que decirle a Azure
     * que el usuario ha cerrado sesión. Este bean construye la URL correcta y redirige al usuario.
     */
    @Bean
    public LogoutSuccessHandler oidcLogoutSuccessHandler() {
        return (request, response, authentication) -> {
            String tenantId = "c990bb7a-51f4-439b-bd36-9c07fb1041c0";
            String postLogoutRedirectUri = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/loggedout";
            String logoutUrl = "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/logout?post_logout_redirect_uri=" + postLogoutRedirectUri;
            response.sendRedirect(logoutUrl);
        };
    }

    /**
     * Este conversor es para la parte de API. Le enseña a Spring Security cómo leer un token JWT (Bearer)
     * y extraer de él la lista de roles para entender qué permisos tiene el que hace la llamada.
     */
    @Bean
    public Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            final List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles == null) {
                return Collections.emptyList();
            }
            return roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
        });
        return converter;
    }
}
