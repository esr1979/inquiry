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

    @Override
    public User saveUser(User user) {
        // La lógica es una simple delegación.
        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        // El método findAll() de CrudRepository devuelve un Iterable, lo convertimos a List.
        Iterable<User> usersIterable = userRepository.findAll();
        return StreamSupport.stream(usersIterable.spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<User> getUserById(Long id) {
        // Delegación directa.
        return userRepository.findById(id);
    }
}
