package com.kike.training.inquiry.infrastructure.db.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.relational.core.mapping.NamingStrategy;

/**
 * Configuración para personalizaciones de Spring Data JDBC.
 */
@Configuration
public class JdbcCustomConfig {

    /**
     * Declara nuestra estrategia de nombrado de tablas como un bean.
     * Spring Data JDBC la detectará y usará automáticamente, resolviendo la
     * discrepancia entre nombres de clase (ej. 'User') y tablas (ej. 'users').
     */
    @Bean
    public NamingStrategy namingStrategy() {
        // Asumiendo que tienes una clase CustomNamingStrategy en alguna parte.
        return new CustomNamingStrategy();
    }
}
