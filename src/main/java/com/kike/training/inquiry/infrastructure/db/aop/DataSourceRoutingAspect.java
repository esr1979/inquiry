package com.kike.training.inquiry.infrastructure.db.aop;

import com.kike.training.inquiry.infrastructure.db.config.DataSourceContextHolder;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Aspecto de Spring AOP que intercepta las llamadas a la capa de controladores
 * para establecer y limpiar dinámicamente el contexto del DataSource.
 *
 * Este componente es el "guardia de tráfico" que activa el mecanismo de enrutamiento.
 */
@Aspect
@Component
public class DataSourceRoutingAspect {

    private static final Logger log = LoggerFactory.getLogger(DataSourceRoutingAspect.class);

    /**
     * Define un "Pointcut", que es una expresión que le dice a Spring "dónde" interceptar.
     *
     * DESGLOSE DE LA EXPRESIÓN:
     * - `execution(public * com.kike.training.inquiry.application.rest.UserController.*(..))`:
     *   Intercepta la ejecución de cualquier método público (`public *`) dentro de la clase
     *   `UserController`, sin importar su nombre (`*`) o los argumentos que reciba (`(..)`).
     *
     * - `&& args(countryCode, ..)`:
     *   Añade una condición: el método interceptado DEBE tener al menos un argumento, y el
     *   primero debe ser un String. Además, "enlaza" el valor de ese primer argumento a una
     *   variable llamada `countryCode` que podremos usar en nuestro método de advice.
     *
     * @param countryCode La variable que recibirá el valor del primer argumento del método interceptado.
     */
    @Pointcut("execution(public * com.kike.training.inquiry.application.rest.UserController.*(String, ..)) && args(countryCode, ..)")
    public void userControllerMethods(String countryCode) {}

    /**
     * Este es un "Advice" que se ejecuta ANTES (`@Before`) de los métodos capturados por nuestro Pointcut.
     *
     * Su única misión es tomar el código de país capturado y establecerlo en el
     * `DataSourceContextHolder`, preparando el terreno para la operación de base de datos.
     *
     * @param countryCode El código de país extraído de la llamada al método del controlador.
     */
    @Before("userControllerMethods(countryCode)")
    public void setRoutingContextBefore(String countryCode) {
        log.info("ASPECT: Interceptada llamada con código de país '{}'. Estableciendo contexto.", countryCode);
        DataSourceContextHolder.setBranchContext(countryCode.toUpperCase());
    }

    /**
     * Este es un "Advice" que se ejecuta DESPUÉS (`@After`) de que los métodos capturados por
     * nuestro Pointcut hayan finalizado, ya sea con éxito o con una excepción.
     *
     * Su misión es CRÍTICA: limpiar el contexto del hilo. Esto previene que una futura petición
     * que reutilice este hilo herede un contexto incorrecto.
     *
     * NOTA: Usamos una expresión de pointcut completa aquí en lugar de referenciar a
     * `userControllerMethods(..)` para evitar problemas con la vinculación de argumentos (`args`)
     * que no necesitamos en el `After`.
     */
    @After("execution(public * com.kike.training.inquiry.application.rest.UserController.*(String, ..))")
    public void clearRoutingContextAfter() {
        log.info("ASPECT: Finalizada la ejecución del método. Limpiando contexto.");
        DataSourceContextHolder.clearBranchContext();
    }
}
