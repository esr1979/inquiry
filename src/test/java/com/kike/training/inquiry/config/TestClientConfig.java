package com.kike.training.inquiry.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;

/**
 * Configuración específica para tests, marcada con @TestConfiguration.
 * <p>
 * Spring Boot detectará automáticamente esta clase durante la fase de test
 * (cuando se usa @SpringBootTest) y la usará para personalizar el contexto
 * de la aplicación SOLO para los tests.
 */
@TestConfiguration
public class TestClientConfig {

    /**
     * Define un Bean para nuestro interceptor.
     * Al ser un @Bean dentro de una @TestConfiguration, nos aseguramos
     * de que este interceptor solo existirá durante los tests.
     *
     * @return una instancia del interceptor de logging de cURL.
     */
    @Bean
    public CurlLoggingInterceptor curlLoggingInterceptor() {
        return new CurlLoggingInterceptor();
    }

    /**
     * Define un Bean que personaliza el RestTemplateBuilder global.
     * <p>
     * Cuando Spring Boot crea el TestRestTemplate para inyectarlo en tu clase de test,
     * usará este builder personalizado.
     * <p>
     * Aquí es donde inyectamos nuestro interceptor. Spring se encarga de resolver la
     * dependencia: ve que necesita un CurlLoggingInterceptor, busca un @Bean que lo
     * provea (el método de arriba) y lo inyecta como parámetro.
     *
     * @param curlInterceptor El interceptor creado por el @Bean anterior.
     * @return Un RestTemplateBuilder configurado con nuestro interceptor.
     */
    @Bean
    public RestTemplateBuilder restTemplateBuilder(CurlLoggingInterceptor curlInterceptor) {
        return new RestTemplateBuilder()
                .additionalInterceptors(curlInterceptor);
    }
}
