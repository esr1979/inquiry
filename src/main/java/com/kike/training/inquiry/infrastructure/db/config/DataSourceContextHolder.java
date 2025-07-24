package com.kike.training.inquiry.infrastructure.db.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase de utilidad para gestionar el contexto del DataSource de forma segura en hilos.
 *
 * SU PROPÓSITO CENTRAL:
 * Esta clase actúa como un "mensajero silencioso". Su única misión es llevar el identificador
 * de la base de datos (ej: "DE", "ES") desde la capa web (donde se intercepta la URL)
 * hasta la capa de datos (donde Spring necesita decidir qué conexión usar).
 *
 * LA TECNOLOGÍA CLAVE: `ThreadLocal`
 * En un servidor de aplicaciones, cada petición de un usuario es manejada por un hilo diferente.
 * Un `ThreadLocal` es una variable especial que le da a cada hilo su propia copia privada e
 * independiente de la variable. Esto es la garantía de que la petición para la base de datos de "DE"
 * no se mezclará jamás con una petición simultánea para la base de datos de "ES".
 *
 * CICLO DE VIDA DEL CONTEXTO:
 * 1. SET: El `DataSourceSwitchAspect` llama a `setBranchContext()` ANTES de ejecutar un método del controlador.
 * 2. GET: El `DataSourceRouting` llama a `getBranchContext()` para saber qué DataSource usar.
 * 3. CLEAR: El `DataSourceSwitchAspect` llama a `clearBranchContext()` DESPUÉS de que el método ha terminado.
 *
 * Esta clase no debe ser instanciada, por eso tiene un constructor privado y es declarada 'final'.
 */
public final class DataSourceContextHolder {

    /**
     * El logger para registrar las operaciones de contexto, vital para la depuración.
     */
    private static final Logger log = LoggerFactory.getLogger(DataSourceContextHolder.class);

    /**
     * El corazón del mecanismo.
     * Es una instancia de ThreadLocal que almacenará un String (el código de país).
     * - `static`: Para que haya una única instancia de ThreadLocal compartida en toda la aplicación.
     * - `final`: Para que la referencia a esta instancia nunca pueda ser cambiada.
     *
     * Imagina esto como una oficina de correos donde cada cartero (hilo) tiene su propio
     * casillero privado (su copia de la variable).
     */
    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

    /**
     * Constructor privado para prevenir la instanciación de esta clase de utilidad.
     */
    private DataSourceContextHolder() {
        // Prevenir instanciación
    }

    /**
     * Establece el identificador del DataSource (el código de país) para el hilo de ejecución actual.
     *
     * Este método es invocado por el `DataSourceSwitchAspect` justo al inicio de una petición web.
     * Es como ponerle una etiqueta al maletín del mensajero antes de que inicie su viaje.
     *
     * @param countryCode El código de país (ej: "DE", "ES", "GB") que identifica de forma única al DataSource.
     */
    public static void setBranchContext(String countryCode) {
        log.info("HOLDER: Estableciendo el contexto de país a '{}'", countryCode);
        CONTEXT.set(countryCode);
    }

    /**
     * Obtiene el identificador del DataSource asociado al hilo de ejecución actual.
     *
     * Este método es invocado por nuestra clase `DataSourceRouting` para decidir a qué
     * base de datos debe enrutar la conexión. Es el momento en que el destinatario
     * lee la etiqueta del maletín del mensajero.
     *
     * El log de este método es VITAL, ya que nos dice exactamente qué identificador ve el
     * `DataSourceRouting` en el momento de la decisión.
     *
     * @return El código de país previamente establecido, o `null` si no se ha establecido ninguno.
     */
    public static String getBranchContext() {
        String countryCode = CONTEXT.get();
        log.info("HOLDER: Consultando contexto de país. Valor actual: '{}'", countryCode);
        return countryCode;
    }

    /**
     * ¡MÉTODO CRÍTICO! Limpia el contexto del DataSource para el hilo de ejecución actual.
     *
     * ¿POR QUÉ ES TAN IMPORTANTE?
     * Los servidores de aplicaciones modernos reutilizan los hilos (thread pools). Si no limpiamos
     * el contexto, el siguiente usuario cuya petición sea asignada a este mismo hilo podría
     * heredar el contexto del usuario anterior, provocando que sus datos se guarden en una
     * base de datos incorrecta.
     *
     * Es imperativo llamar a este método (normalmente en un bloque `finally` o en un `@After`
     * de un Aspect) para evitar fugas de contexto (context leaks) y corrupción de datos.
     */
    public static void clearBranchContext() {
        String countryCode = CONTEXT.get();
        log.info("HOLDER: Limpiando contexto de país. El valor era: '{}'", countryCode);
        CONTEXT.remove();
    }
}
