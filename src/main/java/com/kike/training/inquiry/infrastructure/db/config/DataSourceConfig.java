package com.kike.training.inquiry.infrastructure.db.config;

import com.kike.training.inquiry.infrastructure.db.aop.DataSourceContextHolder;
import jakarta.annotation.PostConstruct;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.data.relational.core.mapping.NamingStrategy;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Profile("!test")
@EnableJdbcRepositories(basePackages = "com.kike.training.inquiry.domain.port.out")
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);


    @PostConstruct
    public void init() {
        System.out.println(">>> Cargando DataSourceConfig de PRODUCCIÓN");
    }
    // --- BEANS DE PROPIEDADES ---
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

    // --- BEANS DE DATASOURCE INDIVIDUALES ---
    // ¡NINGUNO DE ESTOS ES PRIMARIO! Solo están calificados para poder inyectarlos en el router.
    @Bean
    @Qualifier("oneDataSource")
    public DataSource oneDataSource() {
        return oneDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean
    @Qualifier("twoDataSource")
    public DataSource twoDataSource() {
        return twoDataSourceProperties().initializeDataSourceBuilder().build();
    }

    // --- BEANS DE FLYWAY ---
    // Configuración manual para cada DataSource
    @Bean(name = "flywayOne")
    public Flyway flywayOne(@Qualifier("oneDataSource") DataSource dataSource) {
        return Flyway.configure().dataSource(dataSource).locations("classpath:db/migration/one").load();
    }

    @Bean(name = "flywayTwo")
    public Flyway flywayTwo(@Qualifier("twoDataSource") DataSource dataSource) {
        return Flyway.configure().dataSource(dataSource).locations("classpath:db/migration/two").load();
    }

    // Beans que ejecutan las migraciones de Flyway al arrancar
    @Bean
    public FlywayMigrationInitializer flywayInitializerOne(@Qualifier("flywayOne") Flyway flyway) {
        return new FlywayMigrationInitializer(flyway, null);
    }

    @Bean
    public FlywayMigrationInitializer flywayInitializerTwo(@Qualifier("flywayTwo") Flyway flyway) {
        return new FlywayMigrationInitializer(flyway, null);
    }

    // --- BEAN DE ESTRATEGIA DE NOMBRADO (Opcional, pero bueno tenerlo) ---
    @Bean
    public NamingStrategy namingStrategy() {
        return new CustomNamingStrategy();
    }

    // --- EL BEAN DE ENRUTAMIENTO: ¡LA PIEZA CENTRAL Y AHORA EL BEAN PRIMARIO! ---
    @Bean
    @Primary // <-- ESTA ES LA CORRECCIÓN CLAVE
    public DataSource routingDataSource(
            @Qualifier("oneDataSource") DataSource oneDataSource,
            @Qualifier("twoDataSource") DataSource twoDataSource) {

        DataSourceRouting routingDataSource = new DataSourceRouting();

        //Este código de abajo es lo mismo que la línea inmediatamente superior.
        /**
         * Este método es el corazón del enrutamiento. Spring lo llamará
         * cada vez que necesite una conexión a la base de datos.
         */

        /**AbstractRoutingDataSource routingDataSource = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                // Obtenemos la clave que el Aspecto ha guardado en el ThreadLocal
                String lookupKey = DataSourceContextHolder.getDataSourceKey();

                // Log VITAL para depuración
                log.info(">>>> ROUTING DECISION: La clave determinada es: '{}' <<<<", lookupKey);

                return lookupKey;
            }
        };**/

        // Mapeamos las claves ("one", "two") a los beans de DataSource reales
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("one", oneDataSource);
        targetDataSources.put("two", twoDataSource);
        routingDataSource.setTargetDataSources(targetDataSources);

        // Definimos cuál usar si `determineCurrentLookupKey()` devuelve null
        routingDataSource.setDefaultTargetDataSource(oneDataSource);

        return routingDataSource;
    }
}
