package com.kike.training.inquiry.infrastructure.db.aop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataSourceContextHolder {
    private static final Logger log = LoggerFactory.getLogger(DataSourceContextHolder.class);
    private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();

    public static void setDataSourceKey(String key) {
        log.info("HOLDER: Estableciendo la clave del DataSource a '{}'", key);
        contextHolder.set(key);
    }

    public static String getDataSourceKey() {
        String key = contextHolder.get();
        // ESTE LOG ES VITAL. NOS DICE QUÉ VE EL ROUTING DATASOURCE.
        log.info("HOLDER: Alguien ha preguntado por la clave. Valor actual: '{}'", key);
        return key;
    }

    public static void clearDataSourceKey() {
        String key = contextHolder.get();
        log.info("HOLDER: Limpiando la clave del DataSource. El valor era: '{}'", key);
        contextHolder.remove();
    }
}
