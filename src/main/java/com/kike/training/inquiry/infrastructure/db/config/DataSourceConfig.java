package com.kike.training.inquiry.infrastructure.db.config;

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
import org.springframework.core.env.Environment;
import org.springframework.data.relational.core.mapping.NamingStrategy;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 ******************************************************************************************
 * NOTA ARQUITECTÓNICA: ¿POR QUÉ NO SE USA @EnableJdbcRepositories?
 ******************************************************************************************
 *
 * Spring Boot opera bajo el principio de "convención sobre configuración".
 * 1. DETECTA PISTAS: Al ver la dependencia 'spring-boot-starter-data-jdbc' en build.gradle.
 * 2. HACE UNA SUPOSICIÓN: Asume que queremos usar Spring Data JDBC.
 * 3. ACTÚA EN CONSECUENCIA: Habilita automáticamente la búsqueda de Repositorios.
 * 4. DEFINE UNA RUTA: Por defecto, busca Repositorios en el paquete de la clase @SpringBootApplication
 *    y en TODOS sus sub-paquetes.
 *
 * Como nuestro UserRepository está en un sub-paquete, es encontrado automáticamente, haciendo
 * que la anotación @EnableJdbcRepositories sea innecesaria en este proyecto.
 */

/**
 * Clase de configuración maestra para todas las fuentes de datos (DataSources).
 *
 * Esta clase tiene una doble responsabilidad crítica en nuestra arquitectura dinámica:
 * 1.  Descubrir y registrar dinámicamente un número variable de beans DataSource
 *     basándose en una configuración externa (MultiDBLocal.env).
 * 2.  Configurar y crear el bean principal {@link DataSourceRouting}, que actuará
 *     como un proxy inteligente para dirigir el tráfico a la base de datos correcta.
 */
// CAMBIO FINAL 1: proxyBeanMethods=false resuelve un WARN de Spring y es la práctica recomendada
// para configuraciones tan complejas que interactúan con el ciclo de vida de bajo nivel.
// CAMBIO FINAL 2: Se implementa EnvironmentAware para recibir el Environment de forma segura.
@Configuration(proxyBeanMethods = false)
public class DataSourceConfig implements BeanDefinitionRegistryPostProcessor, ApplicationContextAware, EnvironmentAware {

    /**
     ******************************************************************************************
     * EXPLICACIÓN: ¿POR QUÉ BEANS DINÁMICOS Y NO FIJOS?
     ******************************************************************************************
     *
     * EL PROBLEMA DEL ENFOQUE FIJO (ESTÁTICO):
     * En una configuración tradicional, crearíamos un método @Bean para cada DataSource:
     *
     *     @Bean public DataSource dataSourceDE() { ... }
     *     @Bean public DataSource dataSourceES() { ... }
     *
     * Este enfoque es RÍGIDO. Si quisiéramos añadir una nueva base de datos para Portugal (PT),
     * un desarrollador tendría que:
     * 1. Modificar esta clase Java para añadir un nuevo método `dataSourcePT()`.
     * 2. Recompilar el código.
     * 3. Volver a desplegar la aplicación.
     * El código está fuertemente acoplado a la configuración.
     *
     * LA SOLUCIÓN DEL ENFOQUE DINÁMICO:
     * Nuestro objetivo es que la aplicación sea un "motor" genérico que pueda trabajar con
     * cualquier número de bases de datos sin necesidad de tocar el código fuente. La configuración
     * debe ser completamente externa (en nuestro fichero `MultiDBLocal.env`).
     *
     * LA HERRAMIENTA: `BeanDefinitionRegistryPostProcessor`
     * Esta es una interfaz especial de Spring que nos permite "engancharnos" a una fase
     * muy temprana del ciclo de vida de la aplicación. En lugar de crear los beans directamente,
     * su trabajo es registrar los "planos" o "definiciones" (BeanDefinition) de los beans
     * que Spring deberá crear más adelante.
     *
     * Esto nos permite ejecutar una lógica (leer el fichero .env, iterar sobre los países)
     * para decirle a Spring, de forma programática: "Para este arranque, vas a necesitar
     * crear un bean llamado 'DE', otro 'ES', otro 'GB', etc.".
     *
     * En resumen, pasamos de "declarar" beans fijos en el código a "registrar" beans
     * dinámicos basados en una configuración externa.
     */

    private ApplicationContext applicationContext;
    private Environment environment; // Se elimina 'final' porque ya no se inicializa en el constructor.

    /**
     ******************************************************************************************
     * EXPLICACIÓN FINAL: LA PARADOJA DEL CICLO DE VIDA Y LA SOLUCIÓN 'AWARE'
     ******************************************************************************************
     *
     * EN VERSIONES ANTERIORES, intentamos inyectar el Environment usando un constructor con @Autowired.
     * Aunque esa es la práctica recomendada para el 99% de los beans, falló en este caso. ¿Por qué?
     *
     * 1. EL PROBLEMA: Esta clase es un `BeanDefinitionRegistryPostProcessor`. Esto no es un bean
     *    normal; es un "meta-bean" que se ejecuta en una fase EXTREMADAMENTE temprana del
     *    arranque de Spring, ANTES de que la mayoría de los demás beans sean creados.
     *
     * 2. LA PARADOJA DEL HUEVO Y LA GALLINA: Al pedir el `Environment` en el constructor, le
     *    estábamos pidiendo a Spring que resolviera una dependencia para un bean que se necesita
     *    precisamente para configurar las dependencias. El sistema de inyección de dependencias
     *    aún no está completamente operativo en esa fase tan temprana, lo que causa el conflicto
     *    y el fallo.
     *
     * 3. LA SOLUCIÓN CANÓNICA (EL PATRÓN 'AWARE'): Spring provee una solución para esta paradoja:
     *    las interfaces "Aware" (`Consciente de`). Al implementar `EnvironmentAware`, establecemos
     *    un contrato con Spring:
     *      a) Spring primero crea nuestra clase usando un constructor vacío (que ahora existe
     *         porque hemos eliminado el constructor con argumentos). Esto sucede sin errores.
     *      b) Después, en un punto más maduro y seguro del ciclo de vida, Spring ve que nuestra
     *         clase es "consciente del entorno" y llama al método `setEnvironment()`,
     *         entregándonos el objeto `Environment` que necesitamos.
     *
     * Este mecanismo de "callback" es la forma robusta y correcta de obtener dependencias
     * fundamentales dentro de beans que operan en las fases más tempranas del ciclo de vida.
     */
    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * FASE 1: REGISTRAR LOS "PLANOS" DE LOS DATASOURCES DINÁMICOS.
     *
     * Este método es el corazón de la creación dinámica. Se ejecuta antes de que Spring
     * empiece a crear los beans, pero DESPUÉS de que `setEnvironment` haya sido llamado.
     *
     * @param registry El registro donde podemos añadir nuestras definiciones de beans.
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        /** 1. Leemos la lista maestra de datasources (ej: "DE,GB,ES...") que Gradle puso en las propiedades del sistema. */
        String dataSourceNamesStr = this.environment.getProperty("spring.datasource.names");

        if (dataSourceNamesStr == null || dataSourceNamesStr.isEmpty()) {
            System.out.println("No se encontraron datasources dinámicos para configurar.");
            return;
        }

        /** 2. Convertimos la cadena en un array de nombres de datasources. */
        String[] dataSourceNames = dataSourceNamesStr.split(",");

        /** 3. Iteramos sobre cada nombre de país (DE, GB, ES...). */
        Arrays.stream(dataSourceNames).forEach(name -> {
            /** 4. Para cada país, construimos una "definición de bean" genérica. */
            /**    No estamos creando el DataSource aquí, solo estamos creando la RECETA para hacerlo. */
            BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(DataSource.class, () ->
                    /** Esta es la receta: un Supplier<DataSource> que usa el DataSourceBuilder de Spring Boot. */
                    DataSourceBuilder.create()
                            /** Lee las propiedades específicas para este país desde el Environment. */
                            .url(this.environment.getProperty("spring.datasource." + name + ".url"))
                            .username(this.environment.getProperty("spring.datasource." + name + ".username"))
                            .password(this.environment.getProperty("spring.datasource." + name + ".password"))
                            .driverClassName(this.environment.getProperty("spring.datasource." + name + ".driver-class-name"))
                            .build()
            );

            /** CAMBIO FINAL 3: Marcamos explícitamente los beans dinámicos como NO primarios.
             *  Esto es crucial para resolver la ambigüedad. Cuando la autoconfiguración de Spring
             *  busque un DataSource, ignorará estos y solo encontrará nuestro `routingDataSource`
             *  que sí está marcado como `@Primary`. */
            beanBuilder.setPrimary(false);

            /** 5. Registramos la definición en Spring. Le decimos: "Oye Spring, cuando alguien pida un bean */
            /**    con el nombre 'DE' (o 'ES', etc.), usa esta receta que te acabo de dar para construirlo". */
            registry.registerBeanDefinition(name, beanBuilder.getBeanDefinition());
            System.out.println("Bean de DataSource registrado dinámicamente: " + name);
        });
    }

    /**
     * FASE 2: ENSAMBLAR EL ROUTING DATASOURCE.
     *
     * Este es un método @Bean estándar. Se ejecutará DESPUÉS de que la FASE 1 haya terminado
     * y Spring haya creado todos los beans dinámicos que registramos.
     *
     * @return El bean DataSourceRouting configurado y listo para usar.
     */
    @Bean
    @Primary // Este bean SÍ es el primario. Será el único candidato cuando se busque un DataSource.
    public DataSource routingDataSource() {
        DataSourceRouting routingDataSource = new DataSourceRouting();

        /** 1. Ahora que todos los beans existen, le pedimos al ApplicationContext que nos dé un mapa */
        /**    de todos los beans que sean de tipo DataSource. Esto nos devolverá nuestros beans */
        /**    dinámicos ('DE', 'ES', 'GB'...) y el propio 'routingDataSource' (si ya se creó). */
        Map<Object, Object> targetDataSources = this.applicationContext.getBeansOfType(DataSource.class)
                .entrySet().stream()
                /** 2. ¡PASO CRÍTICO DE SEGURIDAD! Debemos filtrar el propio routingDataSource de la lista */
                /**    de sus targets, para evitar una referencia circular infinita. */
                .filter(entry -> !entry.getKey().equals("routingDataSource"))
                /** 3. Convertimos el stream filtrado en el mapa que el DataSourceRouting espera. */
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        /** 4. Establecemos el mapa de todos los datasources reales. */
        routingDataSource.setTargetDataSources(targetDataSources);

        /** 5. Es una buena práctica establecer un datasource por defecto. Si una operación se ejecuta */
        /**    sin un identificador de país, usará este. */
        if (targetDataSources.containsKey("DE")) {
            routingDataSource.setDefaultTargetDataSource(targetDataSources.get("DE"));
            System.out.println("Establecido datasource por defecto: DE");
        } else if (!targetDataSources.isEmpty()) {
            // Fallback por si DE no existe pero hay otros datasources
            Object fallbackKey = targetDataSources.keySet().iterator().next();
            routingDataSource.setDefaultTargetDataSource(targetDataSources.get(fallbackKey));
            System.out.println("Establecido datasource por defecto (fallback): " + fallbackKey);
        }

        return routingDataSource;
    }

    /**
     * Declara nuestra estrategia de nombrado de tablas como un bean.
     *
     * Al hacer esto, Spring Data JDBC la detectará automáticamente y la usará
     * para todas las operaciones de base de datos, en lugar de su estrategia por defecto.
     * Esto resolverá la discrepancia entre el nombre de la clase 'User' y el nombre
     * de la tabla 'users'.
     *
     * @return Una instancia de nuestra estrategia de nombrado personalizada.
     */
    @Bean
    public NamingStrategy namingStrategy() {
        return new CustomNamingStrategy();
    }

    /**
     ******************************************************************************************
     * PREGUNTA CLAVE: ¿POR QUÉ NO INYECTAR ApplicationContext EN EL CONSTRUCTOR?
     ******************************************************************************************
     *
     * La respuesta es el TIMING en el ciclo de vida de Spring.
     *
     * 1. EL PROBLEMA DEL HUEVO Y LA GALLINA: Esta clase es un `BeanDefinitionRegistryPostProcessor`,
     *    lo que significa que se ejecuta MUY TEMPRANO para registrar los "planos" de otros beans.
     *    El `ApplicationContext` es el contenedor FINAL y ensamblado. Pedir el contexto en el
     *    constructor sería como pedir la tarta ya horneada para usarla como ingrediente del bizcocho.
     *
     * 2. INYECCIÓN POR CONSTRUCTOR (RIESGOSA AQUÍ): Ocurriría en un punto tan temprano que el
     *    `ApplicationContext` podría no estar completamente inicializado y estable.
     *
     * 3. SOLUCIÓN CON `ApplicationContextAware` (SEGURA): Este enfoque es un "callback". Spring
     *    primero construye nuestro bean y luego, en un punto más MADURO y SEGURO del arranque,
     *    llama a `setApplicationContext`. Esto nos garantiza que recibimos un contexto estable
     *    en el momento adecuado.
     */
    /**
     * FASE INTERMEDIA: RECIBIR LA "CAJA DE HERRAMIENTAS" (ApplicationContext).
     *
     * Este método es la pieza clave que conecta la FASE 1 (registro) con la FASE 2 (ensamblaje).
     *
     * EXPLICACIÓN: ¿PARA QUÉ SIRVE ESTE MÉTODO?
     * 1. EL CONTRATO: Al implementar la interfaz `ApplicationContextAware`, le decimos a Spring:
     *    "Cuando hayas preparado el entorno, por favor, entrégame una referencia al ApplicationContext".
     * 2. EL CALLBACK: Spring cumple este contrato llamando a este método y pasándonos el contexto.
     * 3. EL PROPÓSITO: Nuestra única labor aquí es guardar esa referencia (`this.applicationContext = ...`).
     *    La guardamos para poder usarla más tarde en el método `routingDataSource()`, que necesita
     *    buscar todos los beans de tipo DataSource que se han creado.
     *
     * ANALOGÍA: Si `postProcessBeanDefinitionRegistry` fue poner todas las piezas de un mueble en el
     * suelo, este método es el momento en que el supervisor nos entrega la caja de herramientas completa,
     * que necesitaremos para el ensamblaje final en `routingDataSource()`.
     *
     * @param applicationContext El contexto de la aplicación, inyectado por Spring.
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Este método es parte de la interfaz {@link BeanDefinitionRegistryPostProcessor}.
     * No necesitamos implementar ninguna lógica aquí para nuestro caso de uso.
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        /** No es necesario para nuestra implementación. */
    }
}
