package com.kike.training.inquiry.infrastructure.db.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class JdbcTemplateConfig {

    /**
     * Asegura que siempre exista un JdbcTemplate apuntando
     * al DataSource @Primary (tu routingDataSource).
     */
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
