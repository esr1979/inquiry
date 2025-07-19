package com.kike.training.inquiry.infrastructure.db.config;

import com.kike.training.inquiry.domain.model.User;
import org.springframework.data.relational.core.mapping.NamingStrategy;

public class CustomNamingStrategy implements NamingStrategy {

    /**
     * Este método es invocado por Spring Data JDBC para obtener el nombre de la tabla
     * para un tipo de entidad dado.
     */
    @Override
    public String getTableName(Class<?> type) {
        // Regla específica para nuestra clase User
        if (type.equals(User.class)) {
            return "users"; // Mapea la clase User a la tabla "users"
        }

        // Para cualquier otra entidad que podamos añadir en el futuro,
        // recurrimos a la estrategia por defecto de Spring Data.
        return NamingStrategy.super.getTableName(type);
    }
}