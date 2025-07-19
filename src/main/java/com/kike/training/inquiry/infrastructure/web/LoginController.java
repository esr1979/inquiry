package com.kike.training.inquiry.infrastructure.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // Marcamos como un controlador de Spring MVC
public class LoginController {

    @GetMapping("/login") // Este método manejará las peticiones GET a /login
    public String loginPage() {
        // Devuelve el nombre de la plantilla (login.html en src/main/resources/templates)
        return "login";
    }
}