package com.kike.training.inquiry.application.port.in;

import com.kike.training.inquiry.domain.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de entrada (Input Port) para el servicio de gestión de usuarios.
 *
 * Define el contrato de las operaciones de negocio que se pueden realizar con los usuarios,
 * independientemente de la tecnología de persistencia o del mecanismo de enrutamiento.
 *
 * Esta interfaz abstrae tanto operaciones estándar como consultas personalizadas con SQL manual.
 */
public interface UserServicePort {

    // ============================================================
    // === MÉTODOS ESTÁNDAR (basados en Spring Data Repository) ===
    // ============================================================

    /**
     * Guarda un nuevo usuario o actualiza uno existente.
     * Usa internamente Spring Data.
     *
     * @param user El objeto User a persistir.
     * @return El usuario guardado, posiblemente con un ID asignado.
     */
    User saveUser(User user);

    /**
     * Recupera todos los usuarios desde el repositorio Spring Data.
     * La base de datos seleccionada depende del contexto actual (multi-tenant).
     *
     * @return Una lista de todos los usuarios.
     */
    List<User> getAllUsers();

    /**
     * Busca un usuario por ID usando Spring Data.
     *
     * @param id El identificador del usuario.
     * @return Un Optional con el usuario si se encuentra.
     */
    Optional<User> getUserById(Long id);


    // =================================================================
    // === MÉTODOS PERSONALIZADOS (usando SQL manual con JdbcTemplate) ==
    // =================================================================

    /**
     * Inserta un usuario utilizando SQL nativo.
     *
     * @param user El usuario a insertar.
     */
    User insertUserNative(User user);

    /**
     * Actualiza un usuario existente mediante SQL nativo.
     *
     * @param user El usuario con datos modificados.
     */
    void updateUserNative(User user);

    /**
     * Elimina un usuario por su ID mediante SQL nativo.
     *
     * @param id ID del usuario a eliminar.
     */
    void deleteUserByIdNative(Long id);

    /**
     * Elimina todos los usuarios de la base de datos actual usando SQL.
     */
    void deleteAllUsersNative();

    /**
     * Busca un usuario por ID usando una query SQL nativa.
     *
     * @param id El ID del usuario.
     * @return Un Optional con el usuario si existe.
     */
    Optional<User> findByIdNative(Long id);

    /**
     * Recupera todos los usuarios utilizando SQL directo.
     *
     * @return Una lista de todos los usuarios.
     */
    List<User> findAllNative();
}
