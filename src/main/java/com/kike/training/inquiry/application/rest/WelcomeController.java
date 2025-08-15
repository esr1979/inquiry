package com.kike.training.inquiry.application.rest; // Manteniendo tu paquete original

import com.kike.training.inquiry.application.port.in.WelcomePort;
import com.kike.training.inquiry.domain.model.WelcomeMessage;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WelcomeController {

    private final WelcomePort welcomePort; // Inyectamos el puerto de la aplicación

    // Constructor para inyección de dependencias
    public WelcomeController(WelcomePort welcomePort) {
        this.welcomePort = welcomePort;
    }

    // --- INICIO DE LA SECCIÓN MODIFICADA/AÑADIDA PARA ENTRA ID ---

    /**
     * Maneja la ruta raíz ("/").
     * Si el usuario ya está autenticado a través de Entra ID, lo redirige directamente a /welcome.
     * Si no está autenticado, muestra la página 'home.html' que contendrá el botón para iniciar sesión.
     *
     * @param oidcUser Objeto OidcUser inyectado por Spring Security si el usuario está autenticado.
     * @param model Modelo para pasar datos a la vista.
     * @return Nombre de la vista o redirección.
     */
    @GetMapping("/")
    public String home(@AuthenticationPrincipal OidcUser oidcUser, Model model) {
        if (oidcUser != null) {
            // Si el usuario ya está autenticado, lo redirigimos a la página de bienvenida
            return "redirect:/welcome";
        }
        // Si no está autenticado, muestra la página de inicio con el botón de login.
        return "home"; // Este mapea a src/main/resources/templates/home.html
    }

    // --- FIN DE LA SECCIÓN MODIFICADA/AÑADIDA PARA ENTRA ID ---


    /**
     * Maneja la ruta "/welcome".
     * Obtiene el mensaje de bienvenida de la capa de aplicación (WelcomePort).
     * Además, si el usuario está autenticado con Entra ID, añade sus detalles al modelo.
     *
     * @param model Modelo para pasar datos a la vista.
     * @param oidcUser Objeto OidcUser inyectado por Spring Security si el usuario está autenticado.
     * @return Nombre de la plantilla HTML 'welcome'.
     */
    @GetMapping("/welcome")
    public String welcome(Model model, @AuthenticationPrincipal OidcUser oidcUser) {
        // Obtenemos el mensaje de bienvenida de tu capa de aplicación existente
        WelcomeMessage message = welcomePort.getWelcomeMessage();
        model.addAttribute("welcomeMessage", message.message());

        // --- INICIO DE LA SECCIÓN AÑADIDA PARA ENTRA ID EN /welcome ---

        if (oidcUser != null) {
            // Añade los detalles del usuario autenticado por Entra ID al modelo
            model.addAttribute("userName", oidcUser.getFullName()); // Nombre completo del usuario
            model.addAttribute("email", oidcUser.getEmail());       // Email del usuario
            model.addAttribute("claims", oidcUser.getClaims());     // Todos los claims del token de ID
            // Puedes personalizar el mensaje de bienvenida si quieres, usando el nombre del usuario
            // model.addAttribute("welcomeMessage", "¡Bienvenido, " + oidcUser.getFullName() + "!");
        } else {
            // Si por alguna razón el OidcUser es nulo (ej. acceso directo sin autenticación),
            // puedes manejarlo aquí o dejar que el HTML lo gestione.
            model.addAttribute("userName", "Invitado");
            model.addAttribute("email", "N/A");
            model.addAttribute("claims", "No autenticado");
        }
        // --- FIN DE LA SECCIÓN AÑADIDA PARA ENTRA ID EN /welcome ---

        return "welcome"; // Retorna el nombre de la plantilla HTML
    }

    // --- INICIO DE LA SECCIÓN AÑADIDA PARA ENTRA ID (Página de Logout) ---

    /**
     * Maneja la ruta "/loggedout".
     * Esta es la página a la que Entra ID redirigirá después de un logout exitoso.
     *
     * @return Nombre de la plantilla HTML 'loggedout'.
     */
    @GetMapping("/loggedout")
    public String loggedOut() {
        return "loggedout"; // Este mapea a src/main/resources/templates/loggedout.html
    }

    // --- FIN DE LA SECCIÓN AÑADIDA PARA ENTRA ID (Página de Logout) ---

    // Tus otros endpoints REST como /api/** irían en otros controladores RESTful si tu intención es separarlos.
    // Si /api/** son endpoints de Thymeleaf, entonces pueden estar aquí o en otros controladores @Controller.
}
