package mx.unadm.dpo3_u2_ea_ervv.modelo;

/**
 * Resultado inmutable del procesamiento de un registro por un hilo.
 */
public record ResultadoNotificacion(
        RegistroNotificacion registro,
        String hilo,
        String horaInicio,
        String horaFin,
        double segundos,
        boolean entregada,
        String detalle) {
}