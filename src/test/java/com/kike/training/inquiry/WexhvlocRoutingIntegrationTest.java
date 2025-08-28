package com.kike.training.inquiry;

import com.kike.training.inquiry.config.TestClientConfig;
import com.kike.training.inquiry.domain.model.Wexhvloc;
import com.kike.training.inquiry.infrastructure.db.config.DataSourceContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
// === IMPORT ORIGINAL MANTENIDO, TAL COMO SOLICITASTE ===
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de integración para el controlador de Wexhvloc y su lógica de enrutamiento multi-tenant.
 *
 * <p>Esta clase de test valida la cadena completa de la aplicación:
 * 1. Petición HTTP al Controller.
 * 2. Interceptación por parte del Aspecto de enrutamiento (WexhvlocRoutingAspect).
 * 3. Ejecución del Servicio y Repositorio.
 * 4. Selección del DataSource correcto por parte de AbstractRoutingDataSource.
 * 5. Persistencia y consulta en la base de datos en memoria (H2) correcta para cada tenant.
 *
 * <p>Se utiliza el perfil "test" para cargar la configuración de DataSources en memoria.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestClientConfig.class)
@DisplayName("Tests de Integración de Enrutamiento para Wexhvloc")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
// Le dices a esta clase de test, y solo a esta, que reemplace el
// ClientRegistrationRepository por un mock.
class WexhvlocRoutingIntegrationTest {

    /**
     * URL base para el API endpoint que se está probando.
     */
    private static final String BASE_URL = "/api/v1/exhibition-locations";

    /**
     * Lista de identificadores de tenant (países) que se usarán en los tests.
     * Es crucial que coincida con los datasources configurados en el perfil de test.
     */
    private static final List<String> TENANTS = List.of("DE", "GB", "ES");

    /**
     * Cliente REST proporcionado por Spring Boot para realizar peticiones HTTP
     * en un entorno de test.
     */
    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * Cliente JDBC para interactuar directamente con la base de datos.
     * Se usa principalmente para la preparación y limpieza del entorno de test.
     */
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // =========================================================================
    // === LA SOLUCIÓN DEFINITIVA, MODERNA Y SIN ADVERTENCIAS ===
    //
    // Usamos @MockitoBean, el reemplazo oficial de @MockBean.
    // Declaramos un campo del tipo que queremos mockear y lo anotamos.
    // Spring se encarga de crear el mock y reemplazar el bean en el contexto.
    //
    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;
    // =========================================================================


    /**
     * Método de limpieza que se ejecuta ANTES de cada test (@Test).
     *
     * <p>Su propósito es garantizar la independencia de los tests. Para ello,
     * itera sobre todos los tenants conocidos, establece el contexto del DataSource
     * para cada uno y trunca la tabla WEXHVLOC. Esto asegura que cada test
     * comienza con todas las bases de datos completamente vacías.</p>
     */
    @BeforeEach
    void cleanAllTenantDatabases() {
        for (String tenantId : TENANTS) {
            DataSourceContextHolder.setBranchContext(tenantId);
            jdbcTemplate.execute("TRUNCATE TABLE WEXHVLOC");
        }
        // Limpia el contexto al final para no dejar estado residual.
        DataSourceContextHolder.clearBranchContext();
    }

    /**
     * MÉTODO DE LIMPIEZA CRÍTICO.
     * Se ejecuta después de CADA método @Test en esta clase.
     * Su única misión es limpiar el ThreadLocal para evitar que un test
     * contamine al siguiente.
     */
    @AfterEach
    void tearDown() {
        // Limpia el contexto del tenant para asegurar que el thread se devuelve limpio al pool.
        DataSourceContextHolder.clearBranchContext();
    }

    /**
     * Valida la característica más crítica del sistema: el aislamiento de datos entre tenants.
     *
     * <p><b>Escenario:</b>
     * 1. <b>Arrange:</b> Se crean dos registros, uno para el tenant 'ES' y otro para 'DE'.
     * 2. <b>Act:</b> Se persisten ambos registros a través de la API.
     * 3. <b>Assert:</b> Se verifica que el registro de 'ES' solo se puede encontrar cuando se
     *    consulta el tenant 'ES', y que devuelve un 404 (Not Found) si se intenta
     *    buscar en el tenant 'DE'. Esto prueba que el enrutamiento funciona correctamente.
     * </p>
     */
    @Test
    @DisplayName("Aislamiento de Tenants: Un dato creado en ES no debe ser visible en DE")
    void testTenantIsolation() {
        // Arrange: Preparar datos para dos tenants distintos.
        Wexhvloc locEs = createSampleWexhvloc("ES", "CHASSIS_ES_001", "L01");
        Wexhvloc locDe = createSampleWexhvloc("DE", "CHASSIS_DE_002", "L02");

        // Act: Persistir ambos registros.
        // === PASO 3: AÑADIMOS AUTENTICACIÓN BÁSICA A LA LLAMADA ===
        restTemplate.withBasicAuth("testuser", "testpassword")
                .postForEntity(BASE_URL, locEs, Wexhvloc.class);
        // === PASO 3: AÑADIMOS AUTENTICACIÓN BÁSICA A LA LLAMADA ===
        restTemplate.withBasicAuth("testuser", "testpassword")
                .postForEntity(BASE_URL, locDe, Wexhvloc.class);

        // Assert: Verificar que cada registro existe solo en su propio tenant.
        URI uriEs = buildGetUri(locEs);
        // === PASO 3: AÑADIMOS AUTENTICACIÓN BÁSICA A LA LLAMADA ===
        assertThat(restTemplate.withBasicAuth("testuser", "testpassword")
                .getForEntity(uriEs, Wexhvloc.class).getStatusCode()).isEqualTo(HttpStatus.OK);

        URI uriDe = buildGetUri(locDe);
        // === PASO 3: AÑADIMOS AUTENTICACIÓN BÁSICA A LA LLAMADA ===
        assertThat(restTemplate.withBasicAuth("testuser", "testpassword")
                .getForEntity(uriDe, Wexhvloc.class).getStatusCode()).isEqualTo(HttpStatus.OK);

        // Assert (La prueba clave): Intentar buscar el registro de ES en el tenant de DE.
        URI uriEsInDe = buildGetUri(locEs, "DE");
        // === PASO 3: AÑADIMOS AUTENTICACIÓN BÁSICA A LA LLAMADA ===
        assertThat(restTemplate.withBasicAuth("testuser", "testpassword")
                .getForEntity(uriEsInDe, Wexhvloc.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    /**
     * Valida el ciclo de vida completo de una entidad (Crear, Leer, Actualizar, Borrar)
     * para un único tenant.
     *
     * <p><b>Escenario:</b>
     * Este test asegura que todas las operaciones CRUD funcionan de extremo a extremo y son
     * correctamente interceptadas por el aspecto de enrutamiento.
     * 1. <b>POST:</b> Se crea un nuevo registro y se verifica que la respuesta es 201 (Created).
     * 2. <b>GET:</b> Se lee el registro recién creado y se validan sus datos.
     * 3. <b>PUT:</b> Se actualiza el registro y se verifica que la operación es exitosa.
     * 4. <b>GET (Verificación):</b> Se vuelve a leer para confirmar que los cambios se persistieron.
     * 5. <b>DELETE:</b> Se elimina el registro.
     * 6. <b>GET (Verificación):</b> Se intenta leer el registro eliminado para confirmar que ya no existe (404).
     * </p>
     */
    @Test
    @DisplayName("CRUD: Ciclo completo (POST, GET, PUT, DELETE) para un solo tenant (GB)")
    void testFullCrudCycleForSingleTenant() {
        // 1. CREATE (POST)
        Wexhvloc locGb = createSampleWexhvloc("GB", "CHASSIS_GB_999", "L09");
        // === PASO 3: AÑADIMOS AUTENTICACIÓN BÁSICA A LA LLAMADA ===
        ResponseEntity<Wexhvloc> postResponse = restTemplate.withBasicAuth("testuser", "testpassword")
                .postForEntity(BASE_URL, locGb, Wexhvloc.class);
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // 2. READ (GET)
        URI uriGb = buildGetUri(locGb);
        // === PASO 3: AÑADIMOS AUTENTICACIÓN BÁSICA A LA LLAMADA ===
        ResponseEntity<Wexhvloc> getResponse = restTemplate.withBasicAuth("testuser", "testpassword")
                .getForEntity(uriGb, Wexhvloc.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getAdexhvl()).isEqualTo("Dirección de prueba para GB");

        // 3. UPDATE (PUT)
        Wexhvloc updatedLoc = getResponse.getBody();
        updatedLoc.setAdexhvl("Nueva Dirección Actualizada en Londres");
        HttpEntity<Wexhvloc> requestUpdate = new HttpEntity<>(updatedLoc);
        // === PASO 3: AÑADIMOS AUTENTICACIÓN BÁSICA A LA LLAMADA ===
        ResponseEntity<Void> putResponse = restTemplate.withBasicAuth("testuser", "testpassword")
                .exchange(BASE_URL, HttpMethod.PUT, requestUpdate, Void.class);
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // 4. VERIFY UPDATE
        // === PASO 3: AÑADIMOS AUTENTICACIÓN BÁSICA A LA LLAMADA ===
        ResponseEntity<Wexhvloc> getUpdatedResponse = restTemplate.withBasicAuth("testuser", "testpassword")
                .getForEntity(uriGb, Wexhvloc.class);
        assertThat(getUpdatedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getUpdatedResponse.getBody().getAdexhvl()).isEqualTo("Nueva Dirección Actualizada en Londres");

        // 5. DELETE
        // === PASO 3: AÑADIMOS AUTENTICACIÓN BÁSICA A LA LLAMADA ===
        restTemplate.withBasicAuth("testuser", "testpassword")
                .delete(uriGb);

        // 6. VERIFY DELETE
        // === PASO 3: AÑADIMOS AUTENTICACIÓN BÁSICA A LA LLAMADA ===
        ResponseEntity<Wexhvloc> getDeletedResponse = restTemplate.withBasicAuth("testuser", "testpassword")
                .getForEntity(uriGb, Wexhvloc.class);
        assertThat(getDeletedResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    /**
     * Método de utilidad (factory) para crear instancias de {@link Wexhvloc} para los tests.
     *
     * <p>Esta función es fundamental para la mantenibilidad de los tests. Se encarga de
     * construir un objeto válido, poblando todos los campos definidos como {@code NOT NULL}
     * en el esquema de la base de datos con valores que respetan su tipo y longitud.</p>
     *
     * @param cdisoloc El código de país (tenant).
     * @param chassis El identificador de chasis.
     * @param cdexhvl El código de la exposición.
     * @return Un objeto {@link Wexhvloc} listo para ser serializado y enviado a la API.
     */
    private Wexhvloc createSampleWexhvloc(String cdisoloc, String chassis, String cdexhvl) {
        Wexhvloc loc = new Wexhvloc();
        LocalDateTime now = LocalDateTime.now();
        BigDecimal dateAsDecimal = new BigDecimal(now.format(DateTimeFormatter.ofPattern("yyyyMMdd")));

        // Poblar todos los campos obligatorios (NOT NULL) según el schema.sql
        loc.setCdisoloc(cdisoloc);                                  // CHAR(2)
        loc.setCdcompany(BigDecimal.ONE);                           // NUMERIC(1)
        loc.setCddealer(new BigDecimal("54321"));                   // NUMERIC(5)
        loc.setChassis(chassis);                                    // CHAR(17)
        loc.setNmexhvl(new BigDecimal("123456789"));                // DECIMAL(9)
        loc.setCdexhvl(cdexhvl);                                    // CHAR(3)
        loc.setDtfinloc(dateAsDecimal);                             // DECIMAL(8)
        loc.setDtiniloc(dateAsDecimal);                             // DECIMAL(8)
        loc.setDtapprv(dateAsDecimal);                              // DECIMAL(8)
        loc.setLgexhvl(BigDecimal.ZERO);                            // DECIMAL(6)
        loc.setTmexhvl(now.format(DateTimeFormatter.ofPattern("HHmm"))); // CHAR(4)
        loc.setAdexhvl("Dirección de prueba para " + cdisoloc);      // VARCHAR(100)
        loc.setSncreate("JUNIT-TEST");                              // CHAR(10)
        loc.setSnlstupd("JUNIT-TEST");                              // CHAR(10)
        loc.setTscreate(now);                                       // TIMESTAMP
        loc.setTslstupd(now);                                       // TIMESTAMP
        loc.setCdchgsts("NW");                                      // CHAR(2)

        return loc;
    }

    /**
     * Método de utilidad para construir la URI para las operaciones GET, PUT y DELETE.
     *
     * <p>La entidad Wexhvloc utiliza una clave primaria compuesta. Este método
     * ensambla la URI con todos los campos de la clave como parámetros de consulta (query params).</p>
     *
     * @param loc El objeto Wexhvloc del cual extraer los identificadores.
     * @return La URI completa para la petición.
     */
    private URI buildGetUri(Wexhvloc loc) {
        return buildGetUri(loc, loc.getCdisoloc());
    }

    /**
     * Versión sobrecargada de {@link #buildGetUri(Wexhvloc)} que permite especificar
     * un tenant de destino diferente al del objeto.
     *
     * <p>Este método es la clave para el test de aislamiento, ya que permite simular
     * la búsqueda de un objeto de un tenant en la base de datos de otro.</p>
     *
     * @param loc El objeto Wexhvloc del cual extraer los identificadores.
     * @param targetTenant El código de país (tenant) al que se debe dirigir la consulta.
     * @return La URI completa para la petición.
     */
    private URI buildGetUri(Wexhvloc loc, String targetTenant) {
        return UriComponentsBuilder.fromPath(BASE_URL)
                .queryParam("cdisoloc", targetTenant)
                .queryParam("cdcompany", loc.getCdcompany())
                .queryParam("cddealer", loc.getCddealer())
                .queryParam("chassis", loc.getChassis())
                .queryParam("nmexhvl", loc.getNmexhvl())
                .queryParam("cdexhvl", loc.getCdexhvl())
                .build().toUri();
    }
}
