package com.kike.training.inquiry;

import com.kike.training.inquiry.domain.model.User;
import com.kike.training.inquiry.application.port.in.UserServicePort;
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

    @Test
    void testCheckActiveProfile() {
        System.out.println(">>> PERFILES ACTIVOS: " + Arrays.toString(environment.getActiveProfiles()));
    }

    @Test
    void checkTableExists() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users", Integer.class
        );
        assertThat(count).isNotNull();
    }

    @Test
    void testIsolationAcrossDE_GB_ES() {
        User alice = new User(null, "Alice_DE", "alice@de.com");
        User bob   = new User(null, "Bob_GB",   "bob@gb.com");
        User carol = new User(null, "Carol_ES", "carol@es.com");

        ResponseEntity<User> respDe = restTemplate.postForEntity(
                baseUrl("DE"), alice, User.class
        );
        assertThat(respDe.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<User> respGb = restTemplate.postForEntity(
                baseUrl("GB"), bob, User.class
        );
        assertThat(respGb.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<User> respEs = restTemplate.postForEntity(
                baseUrl("ES"), carol, User.class
        );
        assertThat(respEs.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<List<User>> listDe = restTemplate.exchange(
                baseUrl("DE"),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );
        assertThat(listDe.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listDe.getBody())
                .hasSize(1)
                .extracting(User::getUsername)
                .containsExactly("Alice_DE");

        ResponseEntity<List<User>> listGb = restTemplate.exchange(
                baseUrl("GB"),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );
        assertThat(listGb.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listGb.getBody())
                .hasSize(1)
                .extracting(User::getUsername)
                .containsExactly("Bob_GB");

        ResponseEntity<List<User>> listEs = restTemplate.exchange(
                baseUrl("ES"),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );
        assertThat(listEs.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listEs.getBody())
                .hasSize(1)
                .extracting(User::getUsername)
                .containsExactly("Carol_ES");
    }
}
