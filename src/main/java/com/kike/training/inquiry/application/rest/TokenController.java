package com.kike.training.inquiry.application.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TokenController {

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    /**
     * Este endpoint es una herramienta de depuración para obtener el Access Token correcto.
     * Al acceder a él desde el navegador, después de iniciar sesión, se mostrará
     * el Access Token que tiene los scopes correctos para llamar a nuestra propia API.
     * @param authentication El token de autenticación inyectado por Spring Security.
     * @return El Access Token como texto plano.
     */
    @GetMapping("/get-api-token")
    public String getApiToken(OAuth2AuthenticationToken authentication) {

        OAuth2AuthorizedClient authorizedClient = this.authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );

        if (authorizedClient == null) {
            return "Error: No se pudo encontrar el cliente autorizado. Asegúrate de que el scope de la API está en application.properties.";
        }

        if (authorizedClient.getAccessToken() == null) {
            return "Error: El cliente autorizado no tiene un Access Token. Revisa la configuración.";
        }

        // Devolvemos el valor del token directamente como texto en la página
        return authorizedClient.getAccessToken().getTokenValue();
    }
}