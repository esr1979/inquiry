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
 * Esta interfaz es deliberadamente simple y no contiene referencias a la infraestructura,
 * como el identificador del DataSource. La capa de aplicación no debe tener esa responsabilidad.
 */
public interface UserServicePort {

    /**
     * Guarda un nuevo usuario o actualiza uno existente.
     *
     * @param user El objeto User a persistir.
     * @return El usuario guardado, posiblemente con un ID asignado.
     */
    User saveUser(User user);

    /**
     * Recupera todos los usuarios. El contexto de la base de datos de la que se
     * recuperan es gestionado de forma transparente por una capa inferior (AOP).
     *
     * @return Una lista de todos los usuarios.
     */
    List<User> getAllUsers();

    /**
     * Busca un usuario por su identificador único.
     *
     * @param id El ID del usuario a buscar.
     * @return Un Optional que contiene al usuario si se encuentra, o un Optional vacío si no.
     */
    Optional<User> getUserById(Long id);
}
