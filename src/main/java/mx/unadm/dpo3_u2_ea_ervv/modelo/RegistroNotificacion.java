package mx.unadm.dpo3_u2_ea_ervv.modelo;

/**
 * Representa un pedido procesado que debe notificarse al cliente.
 * Es inmutable: una vez capturado, ningún hilo puede modificar sus datos.
 */
public final class RegistroNotificacion {

    public static final String CANAL_CORREO = "Correo electrónico";
    public static final String CANAL_SMS = "SMS";
    public static final String CANAL_PUSH = "Notificación push";
    public static final String[] CANALES = {CANAL_CORREO, CANAL_SMS, CANAL_PUSH};

    private final String folio;
    private final String cliente;
    private final String canal;
    private final String contacto;

    public RegistroNotificacion(String folio, String cliente, String canal, String contacto) {
        this.folio = folio;
        this.cliente = cliente;
        this.canal = canal;
        this.contacto = contacto;
    }

    /**
     * Valida los datos antes de crear un registro.
     * @return mensaje de error, o null si los datos son válidos.
     */
    public static String validar(String cliente, String canal, String contacto) {
        if (cliente == null || cliente.isBlank()) {
            return "El nombre del cliente no puede estar vacío.";
        }
        if (contacto == null || contacto.isBlank()) {
            return "El contacto no puede estar vacío.";
        }
        if (CANAL_CORREO.equals(canal) && !contacto.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
            return "Para correo electrónico captura una dirección válida (ej. ana@correo.com).";
        }
        if (CANAL_SMS.equals(canal) && !contacto.matches("\\d{10}")) {
            return "Para SMS captura un teléfono de 10 dígitos, sin espacios.";
        }
        return null;
    }

    /** Texto de ayuda que se muestra junto al campo Contacto. */
    public static String ayudaContacto(String canal) {
        if (CANAL_CORREO.equals(canal)) {
            return "ej. ana@correo.com";
        }
        if (CANAL_SMS.equals(canal)) {
            return "10 dígitos, ej. 5512345678";
        }
        return "usuario de la app, ej. ana.lopez";
    }

    public String getFolio() {
        return folio;
    }

    public String getCliente() {
        return cliente;
    }

    public String getCanal() {
        return canal;
    }

    public String getContacto() {
        return contacto;
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | %s", folio, cliente, canal, contacto);
    }
}
