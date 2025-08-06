
package com.kike.training.inquiry;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class InquiryApplicationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
        assertNotNull(jdbcTemplate, "JdbcTemplate no debería ser nulo, la configuración de DataSource falló.");
    }

    @Test
    void canExecuteQuery() {
        Integer result = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertNotNull(result, "La consulta no debería devolver null.");
        assertTrue(result >= 0, "El conteo de usuarios debería ser 0 o más.");
    }

}
