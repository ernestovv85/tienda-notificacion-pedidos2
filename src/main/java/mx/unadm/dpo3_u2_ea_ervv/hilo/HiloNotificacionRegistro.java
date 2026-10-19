package mx.unadm.dpo3_u2_ea_ervv.hilo;

import mx.unadm.dpo3_u2_ea_ervv.modelo.RegistroNotificacion;
import mx.unadm.dpo3_u2_ea_ervv.vista.FormularioNotificacionV2;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Procesa (notifica) un solo registro en un hilo independiente.
 */
public class HiloNotificacionRegistro extends Thread {

    // Se incluyen milisegundos para apreciar cómo se intercalan los hilos
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private final int fila;                      // Fila del registro en la tabla
    private final RegistroNotificacion registro; // Inmutable: seguro de compartir
    private final FormularioNotificacionV2 formulario;
    private final AtomicInteger completados;     // Contador compartido entre todos los hilos
    private final int total;

    public HiloNotificacionRegistro(int fila, RegistroNotificacion registro,
                                    FormularioNotificacionV2 formulario,
                                    AtomicInteger completados, int total) {
        super("Hilo-" + (fila + 1)); // Nombre propio para identificar cada hilo
        this.fila = fila;
        this.registro = registro;
        this.formulario = formulario;
        this.completados = completados;
        this.total = total;
    }

    @Override
    public void run() {
        long inicio = System.currentTimeMillis();
        formulario.actualizarEstado(fila, "En proceso");
        registrar("INICIO - Cliente: " + registro.getCliente() + " | Canal: " + registro.getCanal());

        try {
            registrar("Generando mensaje de confirmación...");
            pausar(400, 1200);

            registrar("Enviando por " + registro.getCanal() + " a " + registro.getContacto() + "...");
            pausarSegunCanal();

            registrar("Esperando confirmación de entrega...");
            pausar(300, 900);

            double segundos = (System.currentTimeMillis() - inicio) / 1000.0;
            registrar(String.format("FIN - Notificación entregada (%.2f s)", segundos));
            formulario.actualizarEstado(fila, "Notificado");
        } catch (InterruptedException e) {
            registrar("Tarea interrumpida antes de terminar.");
            formulario.actualizarEstado(fila, "Interrumpido");
            Thread.currentThread().interrupt(); // Conserva el estado de interrupción
        } finally {
            // incrementAndGet() es atómico: dos hilos nunca obtienen el mismo número
            formulario.actualizarProgreso(completados.incrementAndGet(), total);
        }
    }

    /** Cada canal tarda distinto, así los hilos terminan en diferente orden. */
    private void pausarSegunCanal() throws InterruptedException {
        switch (registro.getCanal()) {
            case RegistroNotificacion.CANAL_CORREO -> pausar(1000, 1800);
            case RegistroNotificacion.CANAL_SMS -> pausar(600, 1200);
            default -> pausar(300, 800);
        }
    }

    /** Simula tiempo de procesamiento con una duración aleatoria entre min y max (ms). */
    private void pausar(int minMs, int maxMs) throws InterruptedException {
        // ThreadLocalRandom: generador independiente por hilo, sin competencia entre ellos
        Thread.sleep(ThreadLocalRandom.current().nextInt(minMs, maxMs + 1));
    }

    private void registrar(String mensaje) {
        String linea = String.format("[%s][%-7s][%s] %s",
                LocalTime.now().format(FORMATO_HORA), getName(), registro.getFolio(), mensaje);
        System.out.println(linea);        // Seguimiento en consola
        formulario.agregarMensaje(linea); // Seguimiento en la interfaz
    }
}
