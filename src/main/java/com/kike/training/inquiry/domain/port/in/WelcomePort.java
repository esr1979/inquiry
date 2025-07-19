package com.kike.training.inquiry.domain.port.in;

import com.kike.training.inquiry.domain.model.WelcomeMessage;

// Esta es la interfaz que la capa de infraestructura (web) utilizará.
// Define lo que la aplicación puede hacer, sin preocuparse de cómo lo hace.
public interface WelcomePort {
    WelcomeMessage getWelcomeMessage();
}