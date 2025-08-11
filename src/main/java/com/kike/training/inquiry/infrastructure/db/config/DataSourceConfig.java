package com.kike.training.inquiry.infrastructure.db.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class DataSourceConfig {

    // =================================================================================
    // MOTOR DE CONFIGURACIÓN PARA DESARROLLO LOCAL
    // Se activa únicamente cuando el perfil 'local' está activo.
    // Lee de application-local.properties y crea datasources H2 en memoria.
    // =================================================================================
    @Configuration
    @Profile({"local", "test"})
    public static class LocalDataSourceConfiguration implements BeanDefinitionRegistryPostProcessor, ApplicationContextAware {

        private ApplicationContext applicationContext;
        private Environment env;

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            this.applicationContext = applicationContext;
            this.env = applicationContext.getEnvironment();
        }

        @Override
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
            log.info("PERFIL 'local' ACTIVO: Registrando datasources H2 desde application-local.properties...");
            String dataSourceNamesStr = env.getProperty("spring.datasource.names");

            if (dataSourceNamesStr == null || dataSourceNamesStr.isEmpty()) {
                throw new IllegalStateException("La propiedad 'spring.datasource.names' no está definida para el perfil 'local'.");
            }

            Arrays.stream(dataSourceNamesStr.split(",")).forEach(name -> {
                BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(DataSource.class, () ->
                        DataSourceBuilder.create()
                                .url(env.getProperty("spring.datasource." + name + ".url"))
                                .username(env.getProperty("spring.datasource." + name + ".username"))
                                .password(env.getProperty("spring.datasource." + name + ".password"))
                                .driverClassName(env.getProperty("spring.datasource." + name + ".driver-class-name"))
                                .build()
                );
                registry.registerBeanDefinition(name, beanBuilder.getBeanDefinition());
                log.info("Bean definition para H2 registrado: {}", name);
            });
        }

        @Bean
        @Primary
        public DataSource routingDataSource() {
            log.info("PERFIL 'local' ACTIVO: Creando el bean 'routingDataSource' para H2.");
            DataSourceRouting routingDataSource = new DataSourceRouting();
            Map<Object, Object> targetDataSources = this.applicationContext.getBeansOfType(DataSource.class).entrySet().stream()
                    .filter(entry -> !entry.getKey().equals("routingDataSource"))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            routingDataSource.setTargetDataSources(targetDataSources);
            routingDataSource.setDefaultTargetDataSource(targetDataSources.values().iterator().next());
            return routingDataSource;
        }

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {}
    }

    // =================================================================================
    // MOTOR DE CONFIGURACIÓN PARA ENTORNOS SUPERIORES (OpenShift)
    // Se activa con los perfiles dev2, qa2, prod2, etc.
    // Lee las variables de entorno del sistema con el prefijo 'DB-AS400-'.
    // Es lo suficientemente inteligente para usar H2 o lo que sea en modo simulación
    // =================================================================================
    @Configuration
    @Profile({"dev2", "qa2", "prod2", "qa-cn2", "prod-cn2"})
    public static class OpenShiftDataSourceConfiguration implements EnvironmentAware {

        private Environment env;

        @Override
        public void setEnvironment(Environment environment) {
            this.env = environment;
        }

        /**
         * MÉTODO AUXILIAR PARA EJECUTAR LAS MIGRACIONES COMUNES DE FLYWAY.
         * Ahora apunta a una ubicación fija: 'db/migration/common'.
         */
        private void runCommonFlywayMigration(DataSource dataSource) {
            log.info("Ejecutando migraciones COMUNES de Flyway...");

            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .locations("classpath:db/migration/common") // ¡Ubicación fija!
                    .baselineOnMigrate(true)
                    .load();

            flyway.migrate();
            log.info("Migraciones comunes completadas con éxito.");
        }

        /**
         * Extrae el código de país de 2 letras de un nombre de pool.
         * Ejemplo: "DB-AS400-ES" -> "ES"
         */
        private String extractCountryCodeFromPoolName(String poolName) {
            if (poolName == null || !poolName.contains("-")) {
                // Fallo rápido si el formato no es el esperado
                throw new IllegalArgumentException("El formato del nombre del pool es inválido: " + poolName);
            }
            return poolName.substring(poolName.lastIndexOf('-') + 1);
        }

        @Bean
        @Primary
        public DataSource datasourceOpenShift() {
            log.info("PERFILES SUPERIORES ACTIVOS: Configurando datasources...");

            // ¡AQUÍ ESTÁ LA LÓGICA INTELIGENTE!
            // 1. Buscamos la propiedad 'db.driverClassName'.
            // 2. Si existe (en dev2.env), la usamos.
            // 3. Si NO existe (en un OpenShift real), usamos el valor por defecto de AS400.
            String driverClassName = env.getProperty("db.driverClassName", DatabaseDriver.DB2_AS400.getDriverClassName());
            String validationQuery = env.getProperty("db.validationQuery", DatabaseDriver.DB2_AS400.getValidationQuery());

            log.info("Usando Driver: [{}]. Usando Validation Query: [{}]", driverClassName, validationQuery);

            final boolean isLocalSimulation = Arrays.asList(env.getActiveProfiles()).contains("localOS");

            // La lógica de abajo ya no necesita ser modificada, es genérica.
            Map<String, String> allProperties = ((org.springframework.core.env.ConfigurableEnvironment) env).getPropertySources().stream()
                    .filter(ps -> ps instanceof org.springframework.core.env.MapPropertySource)
                    .map(ps -> ((org.springframework.core.env.MapPropertySource) ps).getSource())
                    .flatMap(map -> map.entrySet().stream())
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue()), (a, b) -> a));

            Map<Object, Object> datasourceMap = allProperties.entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith("DB-AS400-") && entry.getKey().endsWith("-URL"))
                    .map(entry -> {
                        String url = entry.getValue();
                        String prefix = entry.getKey().replace("-URL", "");
                        String username = allProperties.get(prefix + "-USERNAME");
                        String password = allProperties.get(prefix + "-PASSWORD");

                        HikariDataSource dataSource = (HikariDataSource) DataSourceBuilder.create()
                                .type(HikariDataSource.class)
                                .url(url)
                                .username(username)
                                .password(password)
                                // Usamos las variables que obtuvimos dinámicamente
                                .driverClassName(driverClassName)
                                .build();

                        dataSource.setConnectionTestQuery(validationQuery);
                        dataSource.setMaximumPoolSize(4);
                        dataSource.setMinimumIdle(1);
                        dataSource.setAutoCommit(true);
                        dataSource.setIdleTimeout(60000);
                        dataSource.setPoolName(prefix);

                        // =================================================================
                        // ¡LA LÓGICA CONDICIONAL QUE PEDISTE!
                        // Solo ejecutamos la migración si el perfil 'localOS' está activo.
                        // =================================================================
                        if (isLocalSimulation) {
                            log.info("Perfil 'localOS' detectado. Aplicando migraciones comunes al DataSource '{}'...", dataSource.getPoolName());
                            runCommonFlywayMigration(dataSource);
                        } else {
                            // Esto es importante para saber por qué no se ejecutan en un entorno real.
                            log.info("Perfil 'localOS' NO detectado. Omitiendo migraciones de Flyway para el DataSource '{}' en entorno real.", dataSource.getPoolName());
                        }

                        return dataSource;
                    })
                    //La clave en vez de ser DB-AS400-XX va a ser XX
                    //.collect(Collectors.toMap(HikariDataSource::getPoolName, Function.identity()));
                    .collect(Collectors.toMap(ds -> extractCountryCodeFromPoolName(ds.getPoolName()), Function.identity()));

            if (datasourceMap.isEmpty()) {
                log.error("¡ALERTA! No se encontraron propiedades para datasources (DB-AS400-*-URL).");
                return new HikariDataSource();
            }

            log.info("--- DataSources Creados y Configurados ---");
            datasourceMap.forEach((poolName, dataSource) -> {
                HikariDataSource ds = (HikariDataSource) dataSource;
                log.info("  -> Pool Name: {}", ds.getPoolName());
                log.info("     - URL: {}", ds.getJdbcUrl());
                log.info("     - User: {}", ds.getUsername());
            });
            log.info("------------------------------------------");

            AbstractRoutingDataSource abstractRoutingDataSource = new DataSourceRouting();
            abstractRoutingDataSource.setTargetDataSources(datasourceMap);

            String defaultDataSourceKey = datasourceMap.keySet().stream()
                    .map(Object::toString)
                    .sorted()
                    .findFirst()
                    .orElse(null);

            abstractRoutingDataSource.setDefaultTargetDataSource(datasourceMap.get(defaultDataSourceKey));

            log.info("DataSourceRouting configurado con {} destinos. Default: {}", datasourceMap.size(), datasourceMap.keySet().iterator().next());

            return abstractRoutingDataSource;
        }
    }
}
