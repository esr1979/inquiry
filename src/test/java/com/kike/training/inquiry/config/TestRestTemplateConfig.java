package com.kike.training.inquiry.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;

/**
 * Configuración específica para los tests que personaliza el RestTemplateBuilder.
 * El propósito principal es inyectar un interceptor para registrar las peticiones salientes
 * del TestRestTemplate como comandos cURL.
 */
@TestConfiguration
public class TestRestTemplateConfig {

    private final CurlClientHttpRequestInterceptor curlInterceptor;

    @Autowired
    public TestRestTemplateConfig(CurlClientHttpRequestInterceptor curlInterceptor) {
        this.curlInterceptor = curlInterceptor;
    }

    /**
     * Define un Bean de RestTemplateBuilder que será utilizado por Spring Boot
     * para crear el TestRestTemplate en los tests de integración.
     *
     * @return Un RestTemplateBuilder configurado con nuestro interceptor de logging.
     */
    @Bean
    public RestTemplateBuilder restTemplateBuilder() {
        return new RestTemplateBuilder()
                .additionalInterceptors(curlInterceptor);
    }
}
