/*
package com.kike.training.inquiry.infrastructure.db.config;

import com.kike.training.inquiry.infrastructure.db.aop.DataSourceContextHolder;
import jakarta.annotation.PostConstruct;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.data.relational.core.mapping.NamingStrategy;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@TestConfiguration
@Profile("test")
@EnableJdbcRepositories(basePackages = "com.kike.training.inquiry.domain.port.out")
public class TestDataSourceConfig {

    @PostConstruct
    public void init() {
        System.out.println(">>> Cargando TestDataSourceConfig de TEST");
    }

    @Bean
    @ConfigurationProperties("spring.datasource.one")
    public DataSourceProperties oneDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.two")
    public DataSourceProperties twoDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "oneDataSource")
    public DataSource oneDataSource() {
        return oneDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean(name = "twoDataSource")
    public DataSource twoDataSource() {
        return twoDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean
    public Flyway flywayOne(@Qualifier("oneDataSource") DataSource ds) {
        return Flyway.configure()
                .dataSource(ds)
                .locations("classpath:db/migration/one")
                .baselineOnMigrate(true)
                .cleanDisabled(false)
                .load();
    }

    @Bean
    public Flyway flywayTwo(@Qualifier("twoDataSource") DataSource ds) {
        return Flyway.configure()
                .dataSource(ds)
                .locations("classpath:db/migration/two")
                .baselineOnMigrate(true)
                .cleanDisabled(false)
                .load();
    }

    @Bean
    public FlywayMigrationInitializer flywayInitOne(Flyway flywayOne) {
        return new FlywayMigrationInitializer(flywayOne);
    }

    @Bean
    public FlywayMigrationInitializer flywayInitTwo(Flyway flywayTwo) {
        return new FlywayMigrationInitializer(flywayTwo);
    }

    @Bean
    public NamingStrategy namingStrategy() {
        return new CustomNamingStrategy();
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @Primary
    public DataSource routingDataSource(@Qualifier("oneDataSource") DataSource ds1,
                                        @Qualifier("twoDataSource") DataSource ds2) {
        AbstractRoutingDataSource rds = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                return DataSourceContextHolder.getDataSourceKey();
            }
        };
        Map<Object, Object> map = new HashMap<>();
        map.put("one", ds1);
        map.put("two", ds2);
        rds.setTargetDataSources(map);
        rds.setDefaultTargetDataSource(ds1);
        rds.afterPropertiesSet();
        return rds;
    }
}
*/
