package com.kike.training.inquiry.infrastructure.db.repository;

import com.kike.training.inquiry.domain.model.User;
import com.kike.training.inquiry.domain.port.out.UserRepositoryCustom;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

/*============================================================
🔍 ¿POR QUÉ SPRING NO USA AUTOMÁTICAMENTE UserRepositoryCustomImpl?
============================================================

Spring Data solo reconoce implementaciones personalizadas si siguen una convención de nombres muy específica:

1️⃣ Si tienes una interfaz principal:

    public interface UserRepository
        extends ListCrudRepository<User, Long>, UserRepositoryCustom

2️⃣ Entonces, la implementación personalizada DEBE llamarse:

    UserRepositoryImpl

🔴 NO debe llamarse UserRepositoryCustomImpl.
    Spring NO la reconocerá automáticamente y NO la enlazará.

------------------------------------------------------------
🧠 ¿Qué hace Spring internamente?
------------------------------------------------------------

Cuando encuentra que tu repositorio extiende una interfaz como `UserRepositoryCustom`,
Spring busca automáticamente una clase llamada:

    <NombreDelRepositorioPrincipal>Impl

En este caso:

    UserRepositoryImpl

🔸 No busca una clase basada en el nombre de `UserRepositoryCustom`.
🔸 No analiza anotaciones como `@Repository` o `@Component` para esto.
🔸 Si no encuentra `UserRepositoryImpl`, asumirá que debe crear el método `findAllNative()` como si fuera un query method (y fallará si no tiene una propiedad con ese nombre).

------------------------------------------------------------
💥 ¿Qué error obtienes si el nombre no es el correcto?
------------------------------------------------------------

Error típico si nombras mal la clase:

    Could not create query for public abstract java.util.List
    com.kike.training.inquiry.domain.port.out.UserRepositoryCustom.findAllNative();
    Reason: No property 'findAllNative' found for type 'User'

------------------------------------------------------------
✅ SOLUCIÓN
------------------------------------------------------------

👉 Cambia el nombre de tu clase de implementación personalizada de:

    UserRepositoryCustomImpl

a:

    UserRepositoryImpl

Con eso Spring la reconocerá automáticamente y la inyectará correctamente.

------------------------------------------------------------
🛠️ OPCIÓN AVANZADA: wiring manual
------------------------------------------------------------

Si quieres seguir usando otro nombre (como UserRepositoryCustomImpl), puedes registrar el bean manualmente:

    @Configuration
    public class CustomRepoConfig {

        @Bean
        public UserRepositoryCustom userRepositoryCustom(JdbcTemplate jdbcTemplate) {
            return new UserRepositoryCustomImpl(jdbcTemplate);
        }
    }

Pero esto **rompe el autowiring automático de Spring Data**, y solo es recomendable en casos avanzados.

------------------------------------------------------------
🎓 NOTA DIDÁCTICA
------------------------------------------------------------

Este comportamiento está documentado pero suele ser fuente de errores comunes.
Es una excelente oportunidad para explicar el mecanismo de autoconfiguración por convención en Spring.

============================================================
*/


/**
 * Implementación de las operaciones personalizadas para la entidad `User`
 * utilizando SQL manual con `JdbcTemplate`.
 *
 * Spring detecta automáticamente esta clase por su nombre: `UserRepositoryImpl`.
 * Al estar en el mismo paquete base o subpaquete de la interfaz `UserRepository`,
 * y al implementar `UserRepositoryCustom`, Spring unirá automáticamente esta
 * clase con la interfaz principal `UserRepository`.
 */
@Repository
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JdbcTemplate jdbcTemplate;

    public UserRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ===============================
    // = Mapeador de filas a objetos =
    // ===============================

    /**
     * Conversor de filas de la tabla a objetos `User`.
     */
    private final RowMapper<User> rowMapper = (rs, rowNum) -> new User(
            rs.getLong("id"),
            rs.getString("username"),
            rs.getString("email")
    );

    // =====================================
    // = Implementaciones de la interfaz ==
    // =====================================

    @Override
    public User insertUserNative(User user) {
        String sql = "INSERT INTO users (username, email) VALUES (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            return ps;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();
        if (generatedId != null) {
            user.setId(generatedId.longValue());
        }
        return user;
    }


    @Override
    public void updateUserNative(User user) {
        String sql = "UPDATE users SET username = ?, email = ? WHERE id = ?";
        jdbcTemplate.update(sql, user.getUsername(), user.getEmail(), user.getId());
    }

    @Override
    public void deleteUserByIdNative(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void deleteAllUsersNative() {
        String sql = "DELETE FROM users";
        jdbcTemplate.update(sql);
    }

    @Override
    public Optional<User> findByIdNative(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> results = jdbcTemplate.query(sql, rowMapper, id);
        return results.stream().findFirst();
    }

    @Override
    public List<User> findAllNative() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, rowMapper);
    }
}
