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
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;


import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@ActiveProfiles("test")
@Import(TestDataSourceConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MultiTenantRoutingIntegrationTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    Environment environment;

    @Autowired
    @Qualifier("flywayOne")
    private Flyway flywayOne;

    @Autowired
    @Qualifier("flywayTwo")
    private Flyway flywayTwo;

    private UserServicePort userServicePort;

    @BeforeEach
    void setUp() {
        this.userServicePort = context.getBean(UserServicePort.class);
    }

    @BeforeEach
    void cleanWithFlyway() {
        flywayOne.clean();
        flywayOne.migrate();
        flywayTwo.clean();
        flywayTwo.migrate();
    }

    @Test
    void testCheckActiveProfile() {
        System.out.println(">>> PERFILES ACTIVOS: " + Arrays.toString(environment.getActiveProfiles()));
    }

    @Test
    void checkTableExists() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users", Integer.class);
        assertThat(count).isNotNull();
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
