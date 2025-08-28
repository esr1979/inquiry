package com.kike.training.inquiry.infrastructure.web.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

// Esta configuración solo se activará cuando el perfil "local" esté activo
@Configuration
@Profile("local")
public class LocalSecurityDebugConfig {

    private static final Logger logger = LoggerFactory.getLogger(LocalSecurityDebugConfig.class);

    // Este bean oidcUserService() SÓLO se activará cuando el perfil "local" esté activo.
    // Sobrescribe el bean del mismo nombre en SecurityConfig.
    // Contiene la lógica de mapeo de roles Y la lógica de impresión de tokens.
    @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        final OidcUserService delegate = new OidcUserService(); // Delegado base de Spring Security
        return (userRequest) -> {
            OidcUser oidcUser = delegate.loadUser(userRequest); // Carga el OidcUser base

            // --- LA SOLUCIÓN DEFINITIVA: Enfoque Clásico y Seguro ---
            // 1. Creamos nuestra propia lista mutable a partir de las autoridades originales.
            //    ESTE PASO ES LA CLAVE. Al usar `new ArrayList<>`, creamos una colección
            //    de tipo `ArrayList<GrantedAuthority>`, eliminando el problemático `? extends`.
            //    IntelliJ ya no se quejará aquí.
            Collection<GrantedAuthority> allAuthorities = new ArrayList<>(oidcUser.getAuthorities());

            // 2. Obtenemos la lista de roles del token.
            final List<String> roles = oidcUser.getClaimAsStringList("roles");

            // 3. Si hay roles, los procesamos y los añadimos a nuestra lista.
            if (roles != null && !roles.isEmpty()) {
                Collection<GrantedAuthority> roleAuthorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());

                allAuthorities.addAll(roleAuthorities);
            }

            // Crea un nuevo OidcUser con las autoridades combinadas (incluidos los roles).
            OidcUser finalOidcUser = new DefaultOidcUser(allAuthorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
            // --- FIN de la lógica de mapeo de ROLES ---


            // --- CÓDIGO PARA IMPRIMIR TOKENS EN EL LOG (SOLO PARA DESARROLLO LOCAL) ---
            logger.debug("\n--- ID Token (Base64 Encoded) ---");
            logger.debug(finalOidcUser.getIdToken().getTokenValue());
            logger.debug("----------------------------------\n");

            if (userRequest.getAccessToken() != null) {
                logger.debug("\n--- Access Token (Base64 Encoded) ---");
                logger.debug(userRequest.getAccessToken().getTokenValue());
                logger.debug("----------------------------------\n");
            }

            // Imprime todos los claims (atributos) que ha recibido del proveedor de identidad.
            logger.debug("--- OIDC User Claims (Atributos del Usuario) ---");
            finalOidcUser.getClaims().forEach((key, value) -> logger.debug("Claim: [{}], Value: [{}]", key, value));
            logger.debug("-------------------------------------------------");

            // --- FIN: CÓDIGO PARA IMPRIMIR TOKENS EN EL LOG ---

            return finalOidcUser; // Retorna el OidcUser con roles y logging
        };
    }
}
