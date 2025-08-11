
package com.kike.training.inquiry;

import com.kike.training.inquiry.infrastructure.db.config.DataSourceContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class InquiryApplicationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
