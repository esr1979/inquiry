/*
package com.kike.training.inquiry;

import com.kike.training.inquiry.domain.model.User;
import com.kike.training.inquiry.application.port.in.UserServicePort;
import com.kike.training.inquiry.infrastructure.db.config.TestDataSourceConfig;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestDataSourceConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MultiTenantRoutingIntegrationTestV2 {

    @Autowired
    private UserServicePort userServicePort;

    @Autowired
    @Qualifier("flywayOne")
    private Flyway flywayOne;

    @Autowired
    @Qualifier("flywayTwo")
    private Flyway flywayTwo;

    */
/**
     * Sobrescribe en caliente las propiedades de DataSource para forzar H2 en memoria.
     *//*

    @DynamicPropertySource
    static void overrideDatasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.one.url",
                () -> "jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        registry.add("spring.datasource.one.username", () -> "sa");
        registry.add("spring.datasource.one.password", () -> "");
        registry.add("spring.datasource.one.driver-class-name",
                () -> "org.h2.Driver");

        registry.add("spring.datasource.two.url",
                () -> "jdbc:h2:mem:db2;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        registry.add("spring.datasource.two.username", () -> "sa");
        registry.add("spring.datasource.two.password", () -> "");
        registry.add("spring.datasource.two.driver-class-name",
                () -> "org.h2.Driver");

        // Deshabilita Flyway en tests
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @BeforeEach
    void cleanWithFlyway() {
        flywayOne.clean();
        flywayOne.migrate();
        flywayTwo.clean();
        flywayTwo.migrate();
    }

    @Test
    void testSaveUserToDataSourceOne() {
        userServicePort.saveUser(new User(null, "Alice", "alice@one.com"), "one");
        List<User> users = userServicePort.getAllUsers("one");
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getUsername()).isEqualTo("Alice");
    }

    @Test
    void testSaveUserToDataSourceTwo() {
        userServicePort.saveUser(new User(null, "Bob", "bob@two.com"), "two");
        List<User> users = userServicePort.getAllUsers("two");
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getUsername()).isEqualTo("Bob");
    }

    @Test
    void testUserIsolationBetweenDataSources() {
        userServicePort.saveUser(new User(null, "Charlie", "charlie@one.com"), "one");
        userServicePort.saveUser(new User(null, "Diana", "diana@two.com"), "two");

        List<User> usersOne = userServicePort.getAllUsers("one");
        List<User> usersTwo = userServicePort.getAllUsers("two");

        assertThat(usersOne).hasSize(1);
        assertThat(usersOne.get(0).getUsername()).isEqualTo("Charlie");

        assertThat(usersTwo).hasSize(1);
        assertThat(usersTwo.get(0).getUsername()).isEqualTo("Diana");
    }
}
*/
