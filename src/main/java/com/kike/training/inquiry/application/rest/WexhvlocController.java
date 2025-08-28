package com.kike.training.inquiry.application.rest;

import com.kike.training.inquiry.application.service.WexhvlocService;
import com.kike.training.inquiry.domain.model.Wexhvloc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Optional;

/**
 * Controlador REST para gestionar las operaciones CRUD de la entidad Wexhvloc.
 *
 * <p>Esta clase expone los endpoints de la API para interactuar con los recursos de localización
 * de exhibición. Actúa como la capa de entrada de la aplicación, manejando las peticiones HTTP
 * y delegando la lógica de negocio al {@link WexhvlocService}.</p>
 */
@RestController
@RequestMapping("/api/v1/exhibition-locations")
public class WexhvlocController {

    private final WexhvlocService wexhvlocService;

    @Autowired
    public WexhvlocController(WexhvlocService wexhvlocService) {
        this.wexhvlocService = wexhvlocService;
    }

    /**
     * Endpoint para CREAR una nueva localización de exhibición.
     * Mapeado a: POST /api/v1/exhibition-locations
     *
     * @param exhibitionLocation El objeto Wexhvloc a crear, deserializado desde el cuerpo de la petición.
     * @return Una respuesta {@link ResponseEntity} con:
     *         - Status 201 Created si la creación es exitosa.
     *         - El objeto creado en el cuerpo de la respuesta.
     *         - Una cabecera 'Location' con la URI para acceder al nuevo recurso.
     */
    @PostMapping
    @PreAuthorize("hasRole('Wexhvloc.Creator') or hasRole('Wexhvloc.Admin')") // <--- Protección añadida
    public ResponseEntity<Wexhvloc> createExhibitionLocation(@RequestBody Wexhvloc exhibitionLocation) {
        Wexhvloc createdLocation = wexhvlocService.createExhibitionLocation(exhibitionLocation);

        // Construye la URI del nuevo recurso. Es una buena práctica REST devolver esta URI.
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .queryParam("cdisoloc", createdLocation.getCdisoloc())
                .queryParam("cdcompany", createdLocation.getCdcompany())
                .queryParam("cddealer", createdLocation.getCddealer())
                .queryParam("chassis", createdLocation.getChassis())
                .queryParam("nmexhvl", createdLocation.getNmexhvl())
                .queryParam("cdexhvl", createdLocation.getCdexhvl()) // Clave completa
                .build()
                .toUri();

        return ResponseEntity.created(location).body(createdLocation);
    }

    /**
     * Endpoint para LEER una localización de exhibición por su clave primaria compuesta.
     * Mapeado a: GET /api/v1/exhibition-locations?cdisoloc=...&cdcompany=... etc.
     *
     * @param cdisoloc  Parte de la clave primaria, desde los parámetros de la URL.
     * @param cdcompany Parte de la clave primaria.
     * @param cddealer  Parte de la clave primaria.
     * @param chassis   Parte de la clave primaria.
     * @param nmexhvl   Parte de la clave primaria.
     * @param cdexhvl   Parte de la clave primaria.
     * @return Una respuesta {@link ResponseEntity} con:
     *         - Status 200 OK y el objeto en el cuerpo si se encuentra.
     *         - Status 404 Not Found si no se encuentra.
     */
    @GetMapping
    @PreAuthorize("hasRole('Wexhvloc.Reader') or hasRole('Wexhvloc.Admin')") // <--- Protección añadida
    public ResponseEntity<Wexhvloc> findExhibitionLocation(
            @RequestParam String cdisoloc, @RequestParam BigDecimal cdcompany,
            @RequestParam BigDecimal cddealer, @RequestParam String chassis,
            @RequestParam BigDecimal nmexhvl, @RequestParam String cdexhvl) { // Parámetro añadido

        // Llama al método de servicio que ahora requiere la clave completa.
        Optional<Wexhvloc> exhibitionLocation = wexhvlocService.findExhibitionLocation(cdisoloc, cdcompany, cddealer, chassis, nmexhvl, cdexhvl);

        return exhibitionLocation
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Endpoint para ACTUALIZAR una localización de exhibición existente.
     * Mapeado a: PUT /api/v1/exhibition-locations
     *
     * @param exhibitionLocation El objeto con los datos a actualizar y la clave, desde el cuerpo de la petición.
     * @return Una respuesta {@link ResponseEntity} con:
     *         - Status 204 No Content si la actualización es exitosa.
     *         - Status 404 Not Found si el registro a actualizar no existe (gestionado por la excepción del servicio).
     */
    @PutMapping
    @PreAuthorize("hasRole('Wexhvloc.Updater') or hasRole('Wexhvloc.Admin')") // <--- Protección añadida
    public ResponseEntity<Void> updateExhibitionLocation(@RequestBody Wexhvloc exhibitionLocation) {
        // La lógica ha sido simplificada. El servicio ahora devuelve void o lanza una excepción.
        // Si no se encuentra la entidad, el servicio lanzará una ResponseStatusException.
        // Spring Boot la capturará y generará automáticamente una respuesta 404.
        wexhvlocService.updateExhibitionLocation(exhibitionLocation);

        // Si el servicio finaliza sin errores, significa que la actualización fue exitosa.
        // Devolvemos 204 No Content, que es el estándar para una actualización exitosa sin cuerpo de respuesta.
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint para BORRAR una localización de exhibición por su clave compuesta.
     * Mapeado a: DELETE /api/v1/exhibition-locations?cdisoloc=...&cdcompany=... etc.
     *
     * @param cdisoloc  Parte de la clave primaria, desde los parámetros de la URL.
     * @param cdcompany Parte de la clave primaria.
     * @param cddealer  Parte de la clave primaria.
     * @param chassis   Parte de la clave primaria.
     * @param nmexhvl   Parte de la clave primaria.
     * @param cdexhvl   Parte de la clave primaria.
     * @return Una respuesta {@link ResponseEntity} con:
     *         - Status 204 No Content si el borrado es exitoso.
     *         - Status 404 Not Found si el registro a borrar no existe (gestionado por la excepción del servicio).
     */
    @DeleteMapping
    @PreAuthorize("hasRole('Wexhvloc.Deleter') or hasRole('Wexhvloc.Admin')") // <--- Protección añadida
    public ResponseEntity<Void> deleteExhibitionLocation(
            @RequestParam String cdisoloc, @RequestParam BigDecimal cdcompany,
            @RequestParam BigDecimal cddealer, @RequestParam String chassis,
            @RequestParam BigDecimal nmexhvl, @RequestParam String cdexhvl) { // Parámetro añadido

        // La lógica es idéntica a la de la actualización. El servicio se encarga de todo.
        wexhvlocService.deleteExhibitionLocation(cdisoloc, cdcompany, cddealer, chassis, nmexhvl, cdexhvl);

        return ResponseEntity.noContent().build();
    }
}
