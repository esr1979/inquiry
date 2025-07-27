package com.kike.training.inquiry.application.service;

import com.kike.training.inquiry.application.port.in.UserServicePort;
import com.kike.training.inquiry.domain.model.User;
import com.kike.training.inquiry.domain.port.out.UserRepository; // Asumiendo que tu repo tiene un puerto
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Implementación del servicio de gestión de usuarios.
 *
 * Esta clase cumple el contrato definido por `UserServicePort`.
 * Su responsabilidad es orquestar la lógica de negocio, en este caso,
 * delegando las operaciones de persistencia al `UserRepository`.
 *
 * Gracias al enrutamiento dinámico gestionado por el Aspect, esta clase
 * opera de forma "ignorante" sobre qué base de datos se está utilizando en
 * cada momento. Simplemente llama al repositorio y confía en que la
 * infraestructura subyacente hará lo correcto.
 */
@Service
public class UserService implements UserServicePort {

    // Inyectamos el puerto de salida (el repositorio)
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
// =========================================================
    // === MÉTODOS ESTÁNDAR (usando Spring Data Repository) ===
    // =========================================================

    /**
     * Inserta o actualiza un usuario usando el repositorio clásico (Spring Data).
     */
    @Override
    public User saveUser(User user) {
        return userRepository.save(user); // método heredado de ListCrudRepository
    }

    /**
     * Devuelve todos los usuarios usando Spring Data.
     */
    @Override
    public List<User> getAllUsers() {
        Iterable<User> iterable = userRepository.findAll(); // Spring Data
        return StreamSupport.stream(iterable.spliterator(), false)
                .collect(Collectors.toList());
    }

    /**
     * Busca un usuario por ID usando el repositorio Spring Data.
     */
    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id); // Spring Data
    }

    // =========================================================
    // === MÉTODOS PERSONALIZADOS (usando SQL con JdbcTemplate)
    // =========================================================

    /**
     * Inserta un usuario usando SQL manual (JdbcTemplate).
     */
    public User insertUserNative(User user) {
        return userRepository.insertUserNative(user);  // Ya devuelve el User con ID
    }

    /**
     * Actualiza un usuario con SQL puro.
     */
    public void updateUserNative(User user) {
        userRepository.updateUserNative(user);
    }

    /**
     * Borra un usuario por su ID usando SQL manual.
     */
    public void deleteUserByIdNative(Long id) {
        userRepository.deleteUserByIdNative(id);
    }

    /**
     * Borra todos los usuarios de la tabla con SQL directo.
     */
    public void deleteAllUsersNative() {
        userRepository.deleteAllUsersNative();
    }

    /**
     * Busca un usuario por ID con SQL manual.
     */
    public Optional<User> findByIdNative(Long id) {
        return userRepository.findByIdNative(id); // Implementado en la parte custom
    }

    /**
     * Obtiene todos los usuarios mediante SQL directo.
     */
    public List<User> findAllNative() {
        return userRepository.findAllNative();
    }
}