package com.kike.training.inquiry.domain.port.out;

import com.kike.training.inquiry.domain.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz para operaciones personalizadas sobre la entidad User
 * utilizando SQL manual (JdbcTemplate u otro enfoque imperativo).
 *
 * Esta interfaz define un conjunto de métodos que serán implementados
 * en una clase `UserRepositoryImpl`, y se combinan con los métodos
 * estándar de Spring Data en el `UserRepository`.
 *
 * Esta separación permite tener lo mejor de ambos mundos:
 * - Flexibilidad total con SQL personalizado.
 * - Rapidez de desarrollo con métodos Spring Data.
 */
public interface UserRepositoryCustom {

    /**
     * Inserta un nuevo usuario en la base de datos usando SQL nativo.
     *
     * @param user Usuario a insertar.
     */
    void insertUserNative(User user);

    /**
     * Actualiza un usuario existente por su ID.
     *
     * @param user Usuario con datos actualizados.
     */
    void updateUserNative(User user);

    /**
     * Elimina un usuario por su identificador.
     *
     * @param id ID del usuario a borrar.
     */
    void deleteUserByIdNative(Long id);

    /**
     * Borra todos los usuarios de la tabla.
     */
    void deleteAllUsersNative();

    /**
     * Recupera un usuario por su ID usando SQL personalizado.
     *
     * @param id ID del usuario a recuperar.
     * @return Usuario si se encuentra, o vacío.
     */
    Optional<User> findByIdNative(Long id);

    /**
     * Recupera todos los usuarios con una consulta SQL manual.
     *
     * @return Lista de todos los usuarios.
     */
    List<User> findAllNative();
}
