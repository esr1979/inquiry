package com.kike.training.inquiry.application.service;

import com.kike.training.inquiry.domain.model.Wexhvloc;
import com.kike.training.inquiry.domain.port.out.WexhvlocRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Servicio de aplicación para gestionar las localizaciones de exhibición (Wexhvloc).
 *
 * <p>Esta clase encapsula la lógica de negocio (o casos de uso) para la entidad Wexhvloc.
 * Actúa como un intermediario entre el controlador (la capa de API) y el repositorio (la capa de persistencia),
 * orquestando las operaciones y asegurando la integridad de los datos y las reglas de negocio.</p>
 */
@Service
public class WexhvlocService {

    private final WexhvlocRepository wexhvlocRepository;

    @Autowired
    public WexhvlocService(WexhvlocRepository wexhvlocRepository) {
        this.wexhvlocRepository = wexhvlocRepository;
    }

    /**
     * Crea un nuevo registro de localización de exhibición.
     * <p>
     * Antes de la inserción, enriquece el objeto con metadatos de auditoría,
     * como las marcas de tiempo de creación y última actualización.
     * </p>
     *
     * @param exhibitionLocation El objeto a crear, proporcionado por la capa de API.
     * @return El objeto {@link Wexhvloc} persistido, incluyendo los campos de auditoría.
     */
    @Transactional
    public Wexhvloc createExhibitionLocation(Wexhvloc exhibitionLocation) {
        LocalDateTime now = LocalDateTime.now();
        exhibitionLocation.setTscreate(now);
        exhibitionLocation.setTslstupd(now);
        wexhvlocRepository.insertRecord(exhibitionLocation);
        return exhibitionLocation;
    }

    /**
     * Busca una localización de exhibición por su clave primaria compuesta completa.
     *
     * @param cdisoloc  Parte de la clave primaria.
     * @param cdcompany Parte de la clave primaria.
     * @param cddealer  Parte de la clave primaria.
     * @param chassis   Parte de la clave primaria.
     * @param nmexhvl   Parte de la clave primaria.
     * @param cdexhvl   Parte de la clave primaria.
     * @return Un {@link Optional} que contiene la localización si se encuentra; de lo contrario, está vacío.
     */
    @Transactional(readOnly = true)
    public Optional<Wexhvloc> findExhibitionLocation(String cdisoloc, BigDecimal cdcompany, BigDecimal cddealer, String chassis, BigDecimal nmexhvl, String cdexhvl) {
        // Llama al método corregido del repositorio que incluye todos los campos de la clave.
        return wexhvlocRepository.findByCompositeId(cdisoloc, cdcompany, cddealer, chassis, nmexhvl, cdexhvl);
    }

    /**
     * Actualiza una localización de exhibición existente.
     * <p>
     * Este método intenta realizar la actualización directamente en la base de datos. Si la operación
     * no afecta a ninguna fila (es decir, el repositorio devuelve 0), significa que el registro
     * no existía. En ese caso, se lanza una {@link ResponseStatusException} que resultará en
     * una respuesta HTTP 404 Not Found.
     * </p>
     *
     * @param exhibitionLocation El objeto con los datos a actualizar y la clave primaria completa.
     * @throws ResponseStatusException si el registro a actualizar no se encuentra en la base de datos.
     */
    @Transactional
    public void updateExhibitionLocation(Wexhvloc exhibitionLocation) {
        // Establece la lógica de negocio para la actualización (campos de auditoría).
        exhibitionLocation.setTslstupd(LocalDateTime.now());
        // Aquí se podrían establecer otros campos, como el usuario que realiza la actualización.
        // exhibitionLocation.setSnlstupd("API_USER");

        // Llama al método de actualización del repositorio que ahora devuelve el número de filas afectadas.
        int rowsAffected = wexhvlocRepository.updateRecord(exhibitionLocation);

        // Si no se actualizó ninguna fila, el registro no existía. Lanza una excepción.
        if (rowsAffected == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El registro de Wexhvloc no fue encontrado para ser actualizado.");
        }
        // Si el método termina sin excepción, la actualización fue exitosa. No necesita devolver nada.
    }

    /**
     * Borra una localización de exhibición por su clave primaria compuesta completa.
     * <p>
     * Al igual que el método de actualización, intenta el borrado directamente y comprueba el
     * número de filas afectadas. Si es cero, lanza una excepción para indicar que
     * el recurso a borrar no existía.
     * </p>
     *
     * @param cdisoloc  Parte de la clave primaria.
     * @param cdcompany Parte de la clave primaria.
     * @param cddealer  Parte de la clave primaria.
     * @param chassis   Parte de la clave primaria.
     * @param nmexhvl   Parte de la clave primaria.
     * @param cdexhvl   Parte de la clave primaria.
     * @throws ResponseStatusException si el registro a borrar no se encuentra en la base de datos.
     */
    @Transactional
    public void deleteExhibitionLocation(String cdisoloc, BigDecimal cdcompany, BigDecimal cddealer, String chassis, BigDecimal nmexhvl, String cdexhvl) {
        // Llama al método de borrado del repositorio que devuelve las filas afectadas.
        int rowsAffected = wexhvlocRepository.deleteByCompositeId(cdisoloc, cdcompany, cddealer, chassis, nmexhvl, cdexhvl);

        // Si no se borró ninguna fila, el registro no existía.
        if (rowsAffected == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El registro de Wexhvloc no fue encontrado para ser eliminado.");
        }
    }
}
