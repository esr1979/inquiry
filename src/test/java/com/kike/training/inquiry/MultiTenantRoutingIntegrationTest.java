package com.kike.training.inquiry;

import com.kike.training.inquiry.application.port.in.UserServicePort;
import com.kike.training.inquiry.config.TestClientConfig;
import com.kike.training.inquiry.domain.model.User;
import com.kike.training.inquiry.infrastructure.db.config.DataSourceContextHolder;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = com.kike.training.inquiry.InquiryApplication.class
)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestClientConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MultiTenantRoutingIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private Environment environment;

    @Autowired
    private UserServicePort userServicePort;

    @LocalServerPort
    private int port;

    private static final List<String> KEYS = List.of("DE", "GB", "ES");

    private String baseUrl(String iso) {
        return "http://localhost:" + port + "/api/users/" + iso;
    }

    private String baseNativeUrl(String iso) {
        return baseUrl(iso) + "/native";
    }

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

    @BeforeEach
    void cleanAllSchemas() {
        for (String key : KEYS) {
            DataSourceContextHolder.setBranchContext(key);
            jdbcTemplate.execute("TRUNCATE TABLE users");
        }
        DataSourceContextHolder.clearBranchContext();
    }

    // ============ TESTS CLÁSICOS CON REST ====================

    @Test
    void testCheckActiveProfile() {
        System.out.println(">>> PERFILES ACTIVOS: " + Arrays.toString(environment.getActiveProfiles()));
    }

    @Test
    void checkTableExists() {
        System.out.println(">>> Verificando existencia de tabla users");
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        assertThat(count).isNotNull();
    }

    @Test
    void testIsolationAcrossDE_GB_ES() {
        System.out.println(">>> testIsolationAcrossDE_GB_ES");

        User alice = new User(null, "Alice_DE", "alice@de.com");
        User bob   = new User(null, "Bob_GB",   "bob@gb.com");
        User carol = new User(null, "Carol_ES", "carol@es.com");

        // === AÑADIDA AUTENTICACIÓN ===
        ResponseEntity<User> respDe = restTemplate.withBasicAuth("testuser", "testpassword").postForEntity(baseUrl("DE"), alice, User.class);
        // === AÑADIDA AUTENTICACIÓN ===
        ResponseEntity<User> respGb = restTemplate.withBasicAuth("testuser", "testpassword").postForEntity(baseUrl("GB"), bob,   User.class);
        // === AÑADIDA AUTENTICACIÓN ===
        ResponseEntity<User> respEs = restTemplate.withBasicAuth("testuser", "testpassword").postForEntity(baseUrl("ES"), carol, User.class);

        assertThat(respDe.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(respGb.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(respEs.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // === AÑADIDA AUTENTICACIÓN ===
        ResponseEntity<List<User>> listDe = restTemplate.withBasicAuth("testuser", "testpassword").exchange(baseUrl("DE"), HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
        // === AÑADIDA AUTENTICACIÓN ===
        ResponseEntity<List<User>> listGb = restTemplate.withBasicAuth("testuser", "testpassword").exchange(baseUrl("GB"), HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
        // === AÑADIDA AUTENTICACIÓN ===
        ResponseEntity<List<User>> listEs = restTemplate.withBasicAuth("testuser", "testpassword").exchange(baseUrl("ES"), HttpMethod.GET, null, new ParameterizedTypeReference<>() {});

        System.out.println(">>> DE: " + listDe.getBody());
        System.out.println(">>> GB: " + listGb.getBody());
        System.out.println(">>> ES: " + listEs.getBody());

        assertThat(listDe.getBody()).hasSize(1).extracting(User::getUsername).containsExactly("Alice_DE");
        assertThat(listGb.getBody()).hasSize(1).extracting(User::getUsername).containsExactly("Bob_GB");
        assertThat(listEs.getBody()).hasSize(1).extracting(User::getUsername).containsExactly("Carol_ES");
    }

    // ============ TESTS CON MÉTODOS NATIVOS (NO REQUIEREN CAMBIOS) ===================

    @Test
    void testNativeInsertAndFindById() {
        DataSourceContextHolder.setBranchContext("DE");

        User user = new User(null, "Test", "test@example.com");
        User inserted = userServicePort.insertUserNative(user);
        System.out.println(">>> Usuario insertado: " + inserted);

        Optional<User> loaded = userServicePort.findByIdNative(inserted.getId());
        System.out.println(">>> Usuario recuperado: " + loaded);
        assertThat(loaded).isPresent();
    }

    @Test
    void testNativeFindAll() {
        System.out.println(">>> testNativeFindAll");

        DataSourceContextHolder.setBranchContext("ES");
        userServicePort.insertUserNative(new User(null, "Ana", "ana@es.com"));
        userServicePort.insertUserNative(new User(null, "Luis", "luis@es.com"));

        List<User> users = userServicePort.findAllNative();
        System.out.println(">>> Usuarios encontrados: " + users);
        assertThat(users).hasSize(2);
    }

    @Test
    void testNativeUpdateUser() {
        System.out.println(">>> testNativeUpdateUser");

        DataSourceContextHolder.setBranchContext("GB");
        User user = new User(null, "Mike", "mike@gb.com");
        userServicePort.insertUserNative(user);
        System.out.println(">>> Usuario original: " + user);

        user.setUsername("Michael");
        user.setEmail("michael@gb.com");
        userServicePort.updateUserNative(user);
        System.out.println(">>> Usuario actualizado: " + user);

        Optional<User> updated = userServicePort.findByIdNative(user.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getUsername()).isEqualTo("Michael");
    }

    @Test
    void testNativeDeleteById() {
        System.out.println(">>> testNativeDeleteById");

        DataSourceContextHolder.setBranchContext("DE");
        User user = new User(null, "Tobias", "tobias@de.com");
        userServicePort.insertUserNative(user);
        System.out.println(">>> Insertado: " + user);

        userServicePort.deleteUserByIdNative(user.getId());
        System.out.println(">>> Usuario eliminado con ID: " + user.getId());

        Optional<User> deleted = userServicePort.findByIdNative(user.getId());
        assertThat(deleted).isEmpty();
    }

    @Test
    void testNativeDeleteAll() {
        System.out.println(">>> testNativeDeleteAll");

        DataSourceContextHolder.setBranchContext("ES");
        userServicePort.insertUserNative(new User(null, "María", "maria@es.com"));
        userServicePort.insertUserNative(new User(null, "Pablo", "pablo@es.com"));

        List<User> before = userServicePort.findAllNative();
        System.out.println(">>> Antes de borrar: " + before);
        assertThat(before).hasSize(2);

        userServicePort.deleteAllUsersNative();

        List<User> after = userServicePort.findAllNative();
        System.out.println(">>> Después de borrar: " + after);
        assertThat(after).isEmpty();
    }

    // ============ TESTS CON REST PERO USANDO LOS ENDPOINTS NATIVOS ============

    @Test
    void testRestNativeInsertAndFindById() {
        System.out.println(">>> testRestNativeInsertAndFindById");

        User user = new User(null, "Nati", "nati@de.com");
        // === AÑADIDA AUTENTICACIÓN ===
        ResponseEntity<User> created = restTemplate.withBasicAuth("testuser", "testpassword").postForEntity(baseNativeUrl("DE"), user, User.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        System.out.println(">>> Usuario insertado (REST): " + created.getBody());

        Long userId = created.getBody().getId();
        // === AÑADIDA AUTENTICACIÓN ===
        ResponseEntity<User> response = restTemplate.withBasicAuth("testuser", "testpassword").getForEntity(baseNativeUrl("DE") + "/" + userId, User.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        System.out.println(">>> Usuario recuperado (REST): " + response.getBody());
    }

    @Test
    void testRestNativeFindAll() {
        System.out.println(">>> testRestNativeFindAll");

        // === AÑADIDA AUTENTICACIÓN ===
        restTemplate.withBasicAuth("testuser", "testpassword").postForEntity(baseNativeUrl("ES"), new User(null, "Eva", "eva@es.com"), User.class);
        // === AÑADIDA AUTENTICACIÓN ===
        restTemplate.withBasicAuth("testuser", "testpassword").postForEntity(baseNativeUrl("ES"), new User(null, "Pedro", "pedro@es.com"), User.class);

        // === AÑADIDA AUTENTICACIÓN ===
        ResponseEntity<List<User>> response = restTemplate.withBasicAuth("testuser", "testpassword").exchange(
                baseNativeUrl("ES"),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<User> users = response.getBody();
        System.out.println(">>> Usuarios recuperados (REST): " + users);
        assertThat(users).hasSize(2);
    }

    @Test
    void testRestNativeUpdateUser() {
        System.out.println(">>> testRestNativeUpdateUser");

        User user = new User(null, "John", "john@gb.com");
        // === AÑADIDA AUTENTICACIÓN ===
        ResponseEntity<User> created = restTemplate.withBasicAuth("testuser", "testpassword").postForEntity(baseNativeUrl("GB"), user, User.class);
        User toUpdate = created.getBody();
        assertThat(toUpdate).isNotNull();

        toUpdate.setUsername("Johnny");
        toUpdate.setEmail("johnny@gb.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> entity = new HttpEntity<>(toUpdate, headers);

        // === AÑADIDA AUTENTICACIÓN ===
        ResponseEntity<Void> response = restTemplate.withBasicAuth("testuser", "testpassword").exchange(
                baseNativeUrl("GB"),
                HttpMethod.PUT,
                entity,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        System.out.println(">>> Usuario actualizado (REST): " + toUpdate);

        // === AÑADIDA AUTENTICACIÓN ===
        ResponseEntity<User> updated = restTemplate.withBasicAuth("testuser", "testpassword").getForEntity(baseNativeUrl("GB") + "/" + toUpdate.getId(), User.class);
        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updated.getBody().getUsername()).isEqualTo("Johnny");
    }

    @Test
    void testRestNativeDeleteById() {
        System.out.println(">>> testRestNativeDeleteById");

        User user = new User(null, "Carlos", "carlos@de.com");
        // === AÑADIDA AUTENTICACIÓN ===
        ResponseEntity<User> created = restTemplate.withBasicAuth("testuser", "testpassword").postForEntity(baseNativeUrl("DE"), user, User.class);
        Long id = created.getBody().getId();

        // === AÑADIDA AUTENTICACIÓN ===
        restTemplate.withBasicAuth("testuser", "testpassword").delete(baseNativeUrl("DE") + "/" + id);
        System.out.println(">>> Usuario eliminado por ID (REST): " + id);

        // === AÑADIDA AUTENTICACIÓN ===
        ResponseEntity<User> response = restTemplate.withBasicAuth("testuser", "testpassword").getForEntity(baseNativeUrl("DE") + "/" + id, User.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testRestNativeDeleteAll() {
        System.out.println(">>> testRestNativeDeleteAll");

        // === AÑADIDA AUTENTICACIÓN ===
        restTemplate.withBasicAuth("testuser", "testpassword").postForEntity(baseNativeUrl("ES"), new User(null, "Pepa", "pepa@es.com"), User.class);
        // === AÑADIDA AUTENTICACIÓN ===
        restTemplate.withBasicAuth("testuser", "testpassword").postForEntity(baseNativeUrl("ES"), new User(null, "Luis", "luis@es.com"), User.class);

        // === AÑADIDA AUTENTICACIÓN ===
        restTemplate.withBasicAuth("testuser", "testpassword").delete(baseNativeUrl("ES"));
        System.out.println(">>> Todos los usuarios eliminados (REST)");

        // === AÑADIDA AUTENTICACIÓN ===
        ResponseEntity<List<User>> response = restTemplate.withBasicAuth("testuser", "testpassword").exchange(
                baseNativeUrl("ES"),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getBody()).isEmpty();
    }
}
