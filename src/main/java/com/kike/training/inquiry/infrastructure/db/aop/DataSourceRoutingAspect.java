package com.kike.training.inquiry.infrastructure.db.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class DataSourceRoutingAspect {

    private static final Logger log = LoggerFactory.getLogger(DataSourceRoutingAspect.class);

    //OJO QUE TIENE QUE SER LA INTERFAZ
    //@Pointcut("execution(* com.kike.training.inquiry.application.service.UserService.*(..))")
    @Pointcut("execution(* com.kike.training.inquiry.domain.port.in.UserPort.*(..))")
    public void serviceMethods() {}

    @Around("serviceMethods()")
    public Object routeDataSource(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("--- ASPECTO INICIO: Interceptado método '{}' ---", joinPoint.getSignature().getName());
        Object[] args = joinPoint.getArgs();
        String dataSourceId = null;

        for (Object arg : args) {
            if (arg instanceof String) {
                String potentialId = (String) arg;
                if ("one".equals(potentialId) || "two".equals(potentialId)) {
                    dataSourceId = potentialId;
                    break;
                }
            }
        }

        if (dataSourceId != null) {
            DataSourceContextHolder.setDataSourceKey(dataSourceId);
        } else {
            log.warn("ASPECTO: No se encontró un 'dataSourceId' válido en el método '{}'. Se usará el DataSource por defecto.", joinPoint.getSignature().getName());
        }

        try {
            log.info("ASPECTO: Procediendo a ejecutar el método original...");
            return joinPoint.proceed();
        } finally {
            log.info("--- ASPECTO FIN: Limpiando después del método '{}' ---", joinPoint.getSignature().getName());
            DataSourceContextHolder.clearDataSourceKey();
        }
    }
}
