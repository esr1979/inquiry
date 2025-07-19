package com.kike.training.inquiry.infrastructure.web;

import com.kike.training.inquiry.application.service.UserService;
import com.kike.training.inquiry.domain.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/{dataSourceId}")
    public ResponseEntity<?> createUser(@RequestBody User user, @PathVariable String dataSourceId) {
        if (!isValidDataSource(dataSourceId)) {
            return badRequestDataSource(dataSourceId);
        }
        User savedUser = userService.saveUser(user, dataSourceId);
        URI location = URI.create(String.format("/api/users/%s/%d", dataSourceId, savedUser.getId()));
        return ResponseEntity.created(location).body(savedUser);
    }

    @GetMapping("/{dataSourceId}")
    public ResponseEntity<?> getAllUsers(@PathVariable String dataSourceId) {
        if (!isValidDataSource(dataSourceId)) {
            return badRequestDataSource(dataSourceId);
        }
        List<User> users = userService.getAllUsers(dataSourceId);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{dataSourceId}/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String dataSourceId, @PathVariable Long id) {
        if (!isValidDataSource(dataSourceId)) {
            return badRequestDataSource(dataSourceId);
        }
        return userService.getUserById(id, dataSourceId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // --- Métodos de ayuda para validación ---

    private boolean isValidDataSource(String dataSourceId) {
        return "one".equalsIgnoreCase(dataSourceId) || "two".equalsIgnoreCase(dataSourceId);
    }

    private ResponseEntity<String> badRequestDataSource(String dataSourceId) {
        return ResponseEntity.badRequest().body("DataSource '" + dataSourceId + "' no es válido. Use 'one' o 'two'.");
    }
}
