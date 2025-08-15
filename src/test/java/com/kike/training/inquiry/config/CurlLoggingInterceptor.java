package com.kike.training.inquiry.config; // O tu paquete de configuración de tests

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Interceptor para un cliente HTTP que traduce cada petición SALIENTE a un comando cURL
 * y lo imprime en el log.
 * <p>
 * Esta clase está diseñada para ser usada exclusivamente en entornos de test para
 * facilitar la depuración, permitiendo copiar y pegar la petición directamente
 - * en una terminal para replicar el comportamiento del test.
 + * en una terminal para replicar el comportamiento del test. No contiene anotaciones
 + * de Spring para asegurar que solo se active cuando una clase de configuración lo decida.
 */
public class CurlLoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(CurlLoggingInterceptor.class);

    /**
     * El método principal que intercepta la petición antes de que sea enviada.
     *
     * @param request La petición HTTP saliente.
     * @param body El cuerpo de la petición.
     * @param execution El objeto que permite continuar con la ejecución de la petición.
     * @return La respuesta recibida del servidor.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        // Paso 1: Construir y loguear el comando cURL para depuración.
        log.info(buildCurlCommand(request, body));

        // Paso 2: CRÍTICO. Proceder con la ejecución original de la petición.
        // Si esta línea se omite, la petición nunca se enviaría al servidor y el test fallaría.
        return execution.execute(request, body);
    }

    /**
     * Método de utilidad para construir la cadena de texto con formato cURL.
     *
     * @param request La petición HTTP.
     * @param body El cuerpo de la petición.
     * @return Una cadena de texto formateada como un comando cURL listo para usar.
     */
    private String buildCurlCommand(HttpRequest request, byte[] body) {
        // Usamos StringBuilder para una construcción eficiente de la cadena.
        StringBuilder curl = new StringBuilder("cURL Request: \n\n");
        curl.append("curl -X ").append(request.getMethod());

        // Añadir la URI completa, escapada entre comillas simples para manejar caracteres especiales.
        curl.append(" '").append(request.getURI()).append("'");

        // Añadir todas las cabeceras, cada una con su propio flag -H.
        request.getHeaders().forEach((name, values) ->
                values.forEach(value ->
                        curl.append(" \\\n    -H '").append(name).append(": ").append(value).append("'")
                )
        );

        // Añadir el cuerpo (payload) si existe, usando el flag -d.
        if (body.length > 0) {
            // Convertimos el cuerpo de bytes a String usando UTF-8, el estándar en la web.
            String requestBody = new String(body, StandardCharsets.UTF_8);
            curl.append(" \\\n    -d '").append(requestBody).append("'");
        }

        curl.append("\n");

        return curl.toString();
    }
}
