package com.kike.training.inquiry.infrastructure.db.aop;


import com.kike.training.inquiry.domain.model.Wexhvloc;
import com.kike.training.inquiry.infrastructure.db.config.DataSourceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * Aspecto para enrutar las llamadas a la base de datos correcta basándose
 * en el código de país (cdisoloc).
 *
 * Esta es la versión final y simplificada gracias a la estandarización de las
 * claves del DataSource en todos los entornos. No necesita dependencias externas.
 */
@Aspect
@Component
@Slf4j
public class WexhvlocRoutingAspect {

    // ¡SIN CONSTRUCTOR! ¡SIN INYECCIÓN! ¡SIN MAPPER!
    // Esta clase ahora no tiene dependencias.

    /**
     * Intercepta todas las llamadas a los métodos públicos del servicio WexhvlocService.
     * Extrae el código de país y lo establece en el DataSourceContextHolder.
     */
    @Around("execution(public * com.kike.training.inquiry.application.service.WexhvlocService.*(..))")
    public Object route(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. Intentar encontrar el código de país en los argumentos del método.
        String cdisoloc = findCdisolocInArguments(joinPoint);

        if (cdisoloc != null) {
            // 2. El 'cdisoloc' es directamente el 'lookupKey'. No se necesita traducción.
            //    Se convierte a mayúsculas por consistencia.
            String lookupKey = cdisoloc.toUpperCase();
            log.info("[WEXHVLOC ASPECT]: Petición para el país '{}'. Usando lookup key: '{}' en el método: {}",
                    cdisoloc, lookupKey, joinPoint.getSignature().getName());

            // 3. Establecer la clave en el ThreadLocal para que el DataSourceRouter la use.
            DataSourceContextHolder.setBranchContext(lookupKey);
        } else {
            // Si no se encuentra, se registrará una advertencia y se usará el datasource por defecto.
            log.warn("[WEXHVLOC ASPECT]: No se pudo determinar 'cdisoloc' en el método: {}. Se usará el datasource por defecto.",
                    joinPoint.getSignature().getName());
        }

        try {
            // 4. Ejecutar el método original del servicio (findAll, create, etc.).
            return joinPoint.proceed();
        } finally {
            // 5. ¡CRÍTICO! Limpiar el ThreadLocal después de la ejecución del método.
            //    Esto evita "leaks" y que futuras peticiones en el mismo hilo usen la BBDD incorrecta.
            DataSourceContextHolder.clearBranchContext();
            log.trace("[WEXHVLOC ASPECT]: Contexto del DataSource limpiado.");
        }
    }

    /**
     * Método de utilidad para buscar el campo 'cdisoloc' en los argumentos de un método.
     * Puede buscarlo dentro de un objeto Wexhvloc o como un parámetro String llamado 'cdisoloc'.
     *
     * @param joinPoint El punto de unión que representa la llamada al método.
     * @return El valor del 'cdisoloc' como String, o null si no se encuentra.
     */
    private String findCdisolocInArguments(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();

        // Estrategia 1: Buscar un objeto de dominio Wexhvloc.
        for (Object arg : args) {
            if (arg instanceof Wexhvloc) {
                return ((Wexhvloc) arg).getCdisoloc();
            }
        }

        // Estrategia 2: Buscar un parámetro String llamado 'cdisoloc' (ignorando mayúsculas/minúsculas).
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        for (int i = 0; i < parameterNames.length; i++) {
            if ("cdisoloc".equalsIgnoreCase(parameterNames[i]) && args[i] instanceof String) {
                return (String) args[i];
            }
        }

        // Si no se encuentra por ninguna de las dos vías.
        return null;
    }
}
