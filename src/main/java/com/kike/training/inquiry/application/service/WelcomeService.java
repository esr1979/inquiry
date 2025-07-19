package com.kike.training.inquiry.application.service;

import com.kike.training.inquiry.domain.port.in.WelcomePort;
import com.kike.training.inquiry.domain.model.WelcomeMessage;
import org.springframework.stereotype.Service;

// Esta es la implementación del puerto. Contiene la lógica de negocio (aquí muy simple).
// Está en la capa de aplicación, no en la de infraestructura.
@Service // Marca esta clase como un componente de servicio de Spring
public class WelcomeService implements WelcomePort {

    @Override
    public WelcomeMessage getWelcomeMessage() {
        // Aquí podría haber lógica de negocio más compleja,
        // por ejemplo, obtener el nombre del usuario logueado
        // o consultar alguna configuración de un puerto de salida (driven port).
        return new WelcomeMessage("¡Bienvenido a tu aplicación Spring Boot con Arquitectura Hexagonal!");
    }
}