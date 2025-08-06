package com.kike.training.inquiry.infrastructure.db.config;

import org.flywaydb.core.Flyway;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@Profile({"local", "test"})
public class FlywayConfig {
    @Bean
    @DependsOn("routingDataSource")
    public Object runFlywayMigrations(ApplicationContext context) {
        System.out.println("Iniciando migraciones dinámicas de Flyway...");
        Map<String, DataSource> dataSources = context.getBeansOfType(DataSource.class);
        dataSources.forEach((name, dataSource) -> {
            if (!name.equals("routingDataSource")) {
                System.out.println("Aplicando migraciones de Flyway para el datasource: " + name);
                Flyway.configure()
                        .dataSource(dataSource)
                        .locations("classpath:db/migration/common") // Carpeta única de scripts
                        .load()
                        .migrate();
                System.out.println("Migración completada para: " + name);
            }
        });
        System.out.println("Todas las migraciones dinámicas de Flyway han finalizado.");
        return new Object();
    }
}
