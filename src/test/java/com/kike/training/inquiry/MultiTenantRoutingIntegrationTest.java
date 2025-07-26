package com.kike.training.inquiry;

import com.kike.training.inquiry.application.port.in.UserServicePort;
import com.kike.training.inquiry.domain.model.User;
import com.kike.training.inquiry.infrastructure.db.config.DataSourceContextHolder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

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

        ResponseEntity<User> respDe = restTemplate.postForEntity(baseUrl("DE"), alice, User.class);
        ResponseEntity<User> respGb = restTemplate.postForEntity(baseUrl("GB"), bob,   User.class);
        ResponseEntity<User> respEs = restTemplate.postForEntity(baseUrl("ES"), carol, User.class);

        assertThat(respDe.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(respGb.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(respEs.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<List<User>> listDe = restTemplate.exchange(baseUrl("DE"), HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
        ResponseEntity<List<User>> listGb = restTemplate.exchange(baseUrl("GB"), HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
        ResponseEntity<List<User>> listEs = restTemplate.exchange(baseUrl("ES"), HttpMethod.GET, null, new ParameterizedTypeReference<>() {});

        System.out.println(">>> DE: " + listDe.getBody());
        System.out.println(">>> GB: " + listGb.getBody());
        System.out.println(">>> ES: " + listEs.getBody());

        assertThat(listDe.getBody()).hasSize(1).extracting(User::getUsername).containsExactly("Alice_DE");
        assertThat(listGb.getBody()).hasSize(1).extracting(User::getUsername).containsExactly("Bob_GB");
        assertThat(listEs.getBody()).hasSize(1).extracting(User::getUsername).containsExactly("Carol_ES");
    }

    // ============ TESTS CON MÉTODOS NATIVOS ===================

    @Test
    void testNativeInsertAndFindById() {
        System.out.println(">>> testNativeInsertAndFindById");

        DataSourceContextHolder.setBranchContext("DE");
        User user = new User(null, "Klaus", "klaus@de.com");
        userServicePort.insertUserNative(user);
        System.out.println(">>> Usuario insertado: " + user);

        Optional<User> found = userServicePort.findByIdNative(user.getId());
        System.out.println(">>> Usuario recuperado: " + found);
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("Klaus");
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
}
