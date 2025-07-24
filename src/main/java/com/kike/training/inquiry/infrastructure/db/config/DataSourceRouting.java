package com.kike.training.inquiry.infrastructure.db.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.lang.Nullable;

/**
 * El cerebro del enrutamiento de bases de datos.
 *
 * ¿QUÉ HACE?
 * Esta clase extiende `AbstractRoutingDataSource`, una herramienta proporcionada por Spring
 * diseñada específicamente para este propósito. Actúa como un proxy o un intermediario
 * para todas las peticiones de conexión a la base de datos.
 *
 * ¿CÓMO FUNCIONA?
 * En lugar de tener una conexión fija, esta clase consulta dinámicamente una "clave de búsqueda"
 * (lookup key) cada vez que se necesita una conexión. Luego, utiliza esa clave para buscar
 * el DataSource real en un mapa de "DataSources objetivo" que le proporcionamos
 * en nuestra clase `DataSourceConfig`.
 *
 * En nuestro caso, la "clave de búsqueda" es el código de país (ej: "DE", "ES").
 */
public class DataSourceRouting extends AbstractRoutingDataSource {

    /**
     * Este es el único método que necesitamos implementar. Es el corazón de la clase.
     *
     * Spring invoca este método ANTES de cada operación de base de datos (SELECT, INSERT, etc.)
     * para decidir a qué DataSource físico debe dirigir la petición.
     *
     * Nuestra implementación es simple y directa: le preguntamos al `DataSourceContextHolder`
     * cuál es el código de país que está actualmente almacenado en el hilo de la petición.
     * El valor que este método retorna es la clave que Spring usará para buscar en el mapa
     * de DataSources que configuramos en `DataSourceConfig`.
     *
     * @return Un objeto que representa la clave de búsqueda. En nuestro caso, un String
     *         con el código de país (ej: "DE"). Puede ser `null` si no hay contexto,
     *         en cuyo caso se usará el `defaultTargetDataSource`.
     */
    @Override
    @Nullable
    protected Object determineCurrentLookupKey() {
        return DataSourceContextHolder.getBranchContext();
    }
}
