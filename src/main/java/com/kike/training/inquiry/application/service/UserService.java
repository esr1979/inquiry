package com.kike.training.inquiry.application.service;

import com.kike.training.inquiry.domain.model.User;
import com.kike.training.inquiry.domain.port.in.UserPort;
import com.kike.training.inquiry.domain.port.out.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Esta es la implementación "limpia" de nuestro puerto de entrada (UserPort).
 *
 * Fíjate en los siguientes puntos clave:
 * 1.  NO hay lógica de `if/else` basada en el `dataSourceId`.
 * 2.  NO hay métodos privados duplicados como `saveUserInDb1` o `saveUserInDb2`.
 * 3.  NO utiliza la anotación `@TargetDataSource`.
 * 4.  El parámetro `dataSourceId` existe en la firma del método (porque lo exige la interfaz UserPort),
 *     pero esta clase NO LO USA. Su único propósito es ser "leído" por nuestro Aspecto.
 */
@Service
public class UserService implements UserPort {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Guarda un usuario. La lógica es una simple llamada al repositorio.
     * El Aspecto ya ha hecho el trabajo de enrutamiento ANTES de que este método se ejecute.
     */
    @Override
    public User saveUser(User user, String dataSourceId) {
        // El parámetro 'dataSourceId' no se usa aquí. Es para el Aspecto.
        return userRepository.save(user);
    }

    /**
     * Obtiene todos los usuarios. La lógica es una simple llamada al repositorio.
     * El Aspecto ya ha preparado la conexión correcta.
     */
    @Override
    public List<User> getAllUsers(String dataSourceId) {
        // El parámetro 'dataSourceId' no se usa aquí. Es para el Aspecto.
        return (List<User>) userRepository.findAll();
    }

    /**
     * Obtiene un usuario por su ID. La lógica es una simple llamada al repositorio.
     * El Aspecto ya ha preparado la conexión correcta.
     */
    @Override
    public Optional<User> getUserById(Long id, String dataSourceId) {
        // El parámetro 'dataSourceId' no se usa aquí. Es para el Aspecto.
        return userRepository.findById(id);
    }
}
