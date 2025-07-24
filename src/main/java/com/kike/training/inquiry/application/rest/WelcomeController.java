package com.kike.training.inquiry.application.rest;

import com.kike.training.inquiry.application.port.in.WelcomePort;
import com.kike.training.inquiry.domain.model.WelcomeMessage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WelcomeController {

    private final WelcomePort welcomePort; // Inyectamos el puerto de la aplicación (no el servicio directamente)

    // Constructor para inyección de dependencias
    public WelcomeController(WelcomePort welcomePort) {
        this.welcomePort = welcomePort;
    }

    @GetMapping("/welcome")
    public String welcome(Model model) {
        // Llamamos al puerto de la capa de aplicación para obtener el mensaje.
        // El controlador no sabe la implementación del puerto, solo la interfaz.
        WelcomeMessage message = welcomePort.getWelcomeMessage();
        model.addAttribute("welcomeMessage", message.message()); // Añadimos el mensaje al modelo para Thymeleaf
        return "welcome"; // Retorna el nombre de la plantilla HTML
    }

    // Opcional: Redirigir la raíz a /welcome si quieres que al acceder a "/" vaya directamente a la página de bienvenida
    @GetMapping("/")
    public String redirectToWelcome() {
        return "redirect:/welcome";
    }
}
