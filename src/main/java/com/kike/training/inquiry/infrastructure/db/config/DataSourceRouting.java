package com.kike.training.inquiry.infrastructure.db.config;

import com.kike.training.inquiry.infrastructure.db.aop.DataSourceContextHolder;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataSourceRouting extends AbstractRoutingDataSource {

    /**
     * Este método es el corazón del enrutamiento. Spring lo llamará
     * cada vez que necesite una conexión a la base de datos.
     */
    @Override
    protected Object determineCurrentLookupKey() {
        // Obtenemos la clave que el Aspecto ha guardado en el ThreadLocal
        String lookupKey = DataSourceContextHolder.getDataSourceKey();

        // Log VITAL para depuración
        log.info(">>>> ROUTING DECISION: La clave determinada es: '{}' <<<<", lookupKey);

        return lookupKey;
    }
}