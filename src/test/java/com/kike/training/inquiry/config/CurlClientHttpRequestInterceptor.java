package com.kike.training.inquiry.config; // O tu paquete de configuración de tests

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Interceptor para el cliente HTTP (RestTemplate) que registra cada petición SALIENTE
 * como un comando cURL. Ideal para depurar los tests de integración, permitiendo
 * ver exactamente qué está enviando el TestRestTemplate a la aplicación.
 */
@Component
public class CurlClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(CurlClientHttpRequestInterceptor.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        log.info(buildCurlCommand(request, body));

        // ¡MUY IMPORTANTE! No olvidar ejecutar la petición original para que el test continúe.
        return execution.execute(request, body);
    }

    /**
     * Construye la cadena de texto con formato cURL a partir de la petición saliente.
     *
     * @param request La petición HTTP saliente.
     * @param body    El cuerpo (payload) de la petición como un array de bytes.
     * @return Una cadena de texto formateada como un comando cURL.
     */
    private String buildCurlCommand(HttpRequest request, byte[] body) {
        StringBuilder curl = new StringBuilder("curl -X ").append(request.getMethod());

        // Añadir la URI completa.
        curl.append(" '").append(request.getURI()).append("'");

        // Añadir las cabeceras.
        request.getHeaders().forEach((name, values) ->
                values.forEach(value ->
                        curl.append(" -H '").append(name).append(": ").append(value).append("'")
                )
        );

        // Añadir el cuerpo si existe.
        if (body.length > 0) {
            String requestBody = new String(body, StandardCharsets.UTF_8);
            curl.append(" -d '").append(requestBody).append("'");
        }

        return curl.toString();
    }
}
