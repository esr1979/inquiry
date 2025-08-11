package com.kike.training.inquiry.domain.port.out;

import com.kike.training.inquiry.domain.model.Wexhvloc;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repositorio (Puerto de Salida) para el acceso a los datos de la entidad Wexhvloc.
 *
 * <p>Esta interfaz extiende {@link CrudRepository} para obtener operaciones CRUD básicas,
 * pero define sus propias consultas SQL nativas mediante la anotación {@code @Query} para
 * manejar la lógica específica de la clave primaria compuesta y las operaciones de inserción/actualización.</p>
 *
 * <p>La clave primaria de WEXHVLOC está compuesta por: (CDISOLOC, CDCOMPANY, CDDEALER, CHASSIS, NMEXHVL, CDEXHVL).</p>
 */
@Repository
public interface WexhvlocRepository extends CrudRepository<Wexhvloc, String> {

    /**
     * Inserta un nuevo registro de localización de exhibición en la tabla WEXHVLOC.
     * <p>
     * Esta consulta utiliza una sentencia INSERT nativa y la expresión SpEL (Spring Expression Language)
     * (e.g., {@code :#{#exLoc.cdisoloc}}) para extraer los valores directamente de los campos del objeto
     * Wexhvloc pasado como parámetro.
     * </p>
     *
     * @param exLoc El objeto {@link Wexhvloc} que contiene todos los datos a insertar. No debe ser nulo.
     */
    @Modifying
    @Query("INSERT INTO WEXHVLOC (CDISOLOC, CDCOMPANY, CDDEALER, CHASSIS, NMEXHVL, CDEXHVL, " +
            "DTINILOC, DTFINLOC, ADEXHVL, LGEXHVL, TMEXHVL, DTAPPRV, SNCREATE, TSCREATE, " +
            "SNLSTUPD, TSLSTUPD, CDCHGSTS) " +
            "VALUES (:#{#exLoc.cdisoloc}, :#{#exLoc.cdcompany}, :#{#exLoc.cddealer}, :#{#exLoc.chassis}, :#{#exLoc.nmexhvl}, " +
            ":#{#exLoc.cdexhvl}, :#{#exLoc.dtiniloc}, :#{#exLoc.dtfinloc}, :#{#exLoc.adexhvl}, :#{#exLoc.lgexhvl}, " +
            ":#{#exLoc.tmexhvl}, :#{#exLoc.dtapprv}, :#{#exLoc.sncreate}, :#{#exLoc.tscreate}, :#{#exLoc.snlstupd}, " +
            ":#{#exLoc.tslstupd}, :#{#exLoc.cdchgsts})")
    void insertRecord(@Param("exLoc") Wexhvloc exLoc);

    /**
     * Busca un registro de localización de exhibición por su clave primaria compuesta completa.
     * <p>
     * La cláusula WHERE ahora incluye los seis campos que componen la clave primaria única,
     * garantizando que solo se devuelva un registro.
     * </p>
     *
     * @param cdisoloc  Parte de la clave primaria.
     * @param cdcompany Parte de la clave primaria.
     * @param cddealer  Parte de la clave primaria.
     * @param chassis   Parte de la clave primaria.
     * @param nmexhvl   Parte de la clave primaria.
     * @param cdexhvl   Parte de la clave primaria.
     * @return Un {@link Optional} que contiene el {@link Wexhvloc} si se encuentra, o un Optional vacío si no.
     */
    @Query("SELECT * FROM WEXHVLOC WHERE " +
            "CDISOLOC = :cdisoloc AND CDCOMPANY = :cdcompany AND CDDEALER = :cddealer AND " +
            "CHASSIS = :chassis AND NMEXHVL = :nmexhvl AND CDEXHVL = :cdexhvl")
    Optional<Wexhvloc> findByCompositeId(
            @Param("cdisoloc") String cdisoloc, @Param("cdcompany") BigDecimal cdcompany,
            @Param("cddealer") BigDecimal cddealer, @Param("chassis") String chassis,
            @Param("nmexhvl") BigDecimal nmexhvl, @Param("cdexhvl") String cdexhvl);

    /**
     * Actualiza un subconjunto de campos de un registro de localización de exhibición existente.
     * <p>
     * La consulta localiza el registro a actualizar utilizando la clave primaria compuesta completa
     * en la cláusula WHERE, incluyendo el campo {@code CDEXHVL}.
     * Se utiliza SpEL para extraer los valores del objeto pasado como parámetro.
     * </p>
     *
     * @param exLoc El objeto {@link Wexhvloc} que contiene los valores nuevos para los campos a actualizar
     *              y los valores de la clave primaria para identificar el registro.
     * @return {@code int} el número de filas afectadas por la operación de actualización. Debería ser 1 en caso de éxito y 0 si no se encontró el registro.
     */
    @Modifying
    @Query("UPDATE WEXHVLOC SET ADEXHVL = :#{#exLoc.adexhvl}, CDCHGSTS = :#{#exLoc.cdchgsts}, " +
            "TSLSTUPD = :#{#exLoc.tslstupd}, SNLSTUPD = :#{#exLoc.snlstupd} " +
            "WHERE CDISOLOC = :#{#exLoc.cdisoloc} AND CDCOMPANY = :#{#exLoc.cdcompany} AND CDDEALER = :#{#exLoc.cddealer} AND " +
            "CHASSIS = :#{#exLoc.chassis} AND NMEXHVL = :#{#exLoc.nmexhvl} AND CDEXHVL = :#{#exLoc.cdexhvl}")
    int updateRecord(@Param("exLoc") Wexhvloc exLoc);

    /**
     * Borra un registro de localización de exhibición usando su clave primaria compuesta completa.
     * <p>
     * La cláusula WHERE ha sido corregida para incluir los seis campos de la clave primaria,
     * asegurando que solo se borre el registro deseado.
     * </p>
     *
     * @param cdisoloc  Parte de la clave primaria.
     * @param cdcompany Parte de la clave primaria.
     * @param cddealer  Parte de la clave primaria.
     * @param chassis   Parte de la clave primaria.
     * @param nmexhvl   Parte de la clave primaria.
     * @param cdexhvl   Parte de la clave primaria.
     * @return {@code int} el número de filas afectadas. Debería ser 1 en caso de éxito y 0 si no se encontró el registro.
     */
    @Modifying
    @Query("DELETE FROM WEXHVLOC WHERE " +
            "CDISOLOC = :cdisoloc AND CDCOMPANY = :cdcompany AND CDDEALER = :cddealer AND " +
            "CHASSIS = :chassis AND NMEXHVL = :nmexhvl AND CDEXHVL = :cdexhvl")
    int deleteByCompositeId(
            @Param("cdisoloc") String cdisoloc, @Param("cdcompany") BigDecimal cdcompany,
            @Param("cddealer") BigDecimal cddealer, @Param("chassis") String chassis,
            @Param("nmexhvl") BigDecimal nmexhvl, @Param("cdexhvl") String cdexhvl);
}
