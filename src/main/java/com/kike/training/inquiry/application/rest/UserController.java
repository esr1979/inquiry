package com.kike.training.inquiry.application.rest; // Paquete sugerido para controladores

import com.kike.training.inquiry.application.port.in.UserServicePort;
import com.kike.training.inquiry.domain.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

/**
 * Controlador REST para gestionar las operaciones de los usuarios.
 *
 * NOTA ARQUITECTÓNICA IMPORTANTE:
 * Este controlador actúa como el "disparador" de nuestro mecanismo de enrutamiento dinámico.
 *
 * 1.  El parámetro `@PathVariable String countryCode` en cada método es deliberadamente el PRIMERO.
 * 2.  Nuestro `DataSourceRoutingAspect` está configurado para interceptar este parámetro.
 * 3.  El aspecto usa el valor de `countryCode` para llamar a `DataSourceContextHolder.setBranchContext()`.
 * 4.  Dentro del cuerpo de estos métodos, las llamadas a `userService` ya NO necesitan pasar el `countryCode`.
 *     La magia del enrutamiento ocurre de forma transparente en segundo plano.
 *
 * El controlador es ahora más simple y no tiene conocimiento del mecanismo de enrutamiento.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserServicePort userService;

    public UserController(UserServicePort userService) {
        this.userService = userService;
    }

    // =============================================
    // MÉTODOS CON REPOSITORIO SPRING DATA
    // =============================================

    /**
     * Crea un nuevo usuario en la base de datos correspondiente al país.
     *
     * @param countryCode El código del país (ej: "DE", "ES"). Es interceptado por el Aspect y no se usa directamente aquí.
     * @param user El usuario a crear, proveniente del cuerpo de la petición.
     * @return Una respuesta HTTP 201 Created con la ubicación del nuevo recurso.
     */
    @PostMapping("/{countryCode}")
    public ResponseEntity<User> createUser(@PathVariable String countryCode, @RequestBody User user) {
        User savedUser = userService.saveUser(user);
        URI location = URI.create(String.format("/api/users/%s/%d", countryCode, savedUser.getId()));
        return ResponseEntity.created(location).body(savedUser);
    }

    /**
     * Obtiene todos los usuarios de la base de datos de un país específico.
     *
     * @param countryCode El código del país a consultar. Es interceptado por el Aspect.
     * @return Una lista de todos los usuarios para ese país.
     */
    @GetMapping("/{countryCode}")
    public ResponseEntity<List<User>> getAllUsers(@PathVariable String countryCode) {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Busca un usuario por su ID en la base de datos de un país específico.
     *
     * @param countryCode El código del país donde buscar. Es interceptado por el Aspect.
     * @param id El ID del usuario a buscar.
     * @return El usuario encontrado o una respuesta 404 Not Found.
     */
    @GetMapping("/{countryCode}/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String countryCode, @PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // =============================================
    // MÉTODOS CON SQL NATIVO (JdbcTemplate)
    // =============================================

    /**
     * Inserta un usuario usando SQL manual (JdbcTemplate).
     */
    @PostMapping("/{countryCode}/native")
    public ResponseEntity<Void> createUserNative(@PathVariable String countryCode, @RequestBody User user) {
        userService.insertUserNative(user);
        return ResponseEntity.created(URI.create(String.format("/api/users/%s", countryCode))).build();
    }

    /**
     * Obtiene todos los usuarios mediante SQL nativo.
     */
    @GetMapping("/{countryCode}/native")
    public ResponseEntity<List<User>> getAllUsersNative(@PathVariable String countryCode) {
        List<User> users = userService.findAllNative();
        return ResponseEntity.ok(users);
    }

    /**
     * Busca un usuario por su ID con SQL manual.
     */
    @GetMapping("/{countryCode}/native/{id}")
    public ResponseEntity<User> getUserByIdNative(@PathVariable String countryCode, @PathVariable Long id) {
        return userService.findByIdNative(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Actualiza un usuario existente con SQL manual.
     */
    @PutMapping("/{countryCode}/native")
    public ResponseEntity<Void> updateUserNative(@PathVariable String countryCode, @RequestBody User user) {
        userService.updateUserNative(user);
        return ResponseEntity.noContent().build();
    }

    /**
     * Elimina un usuario por ID con SQL manual.
     */
    @DeleteMapping("/{countryCode}/native/{id}")
    public ResponseEntity<Void> deleteUserByIdNative(@PathVariable String countryCode, @PathVariable Long id) {
        userService.deleteUserByIdNative(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Elimina todos los usuarios del país indicado con SQL manual.
     */
    @DeleteMapping("/{countryCode}/native")
    public ResponseEntity<Void> deleteAllUsersNative(@PathVariable String countryCode) {
        userService.deleteAllUsersNative();
        return ResponseEntity.noContent().build();
    }
}
