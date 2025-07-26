// Ubicación: src/main/java/com/kike/training/inquiry/domain/model/User.java
package com.kike.training.inquiry.domain.model;

import org.springframework.data.annotation.Id;

public class User {

    @Id // La identidad SÍ es un concepto de dominio. Esta se queda.
    private Long id;
    private String username;
    private String email;

    /**
     * Constructor por defecto, requerido por muchos frameworks.
     */
    public User() {
    }

    /**
     * Constructor completo para crear instancias de User.
     */
    public User(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    // --- Getters y Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

}
