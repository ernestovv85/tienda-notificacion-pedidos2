package mx.unadm.dpo3_u2_ea_ervv.hilo;

import mx.unadm.dpo3_u2_ea_ervv.modelo.RegistroNotificacion;
import mx.unadm.dpo3_u2_ea_ervv.modelo.ResultadoNotificacion;
import mx.unadm.dpo3_u2_ea_ervv.util.EscritorResultado;
import mx.unadm.dpo3_u2_ea_ervv.vista.FormularioNotificacionV2;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Procesa (notifica) un solo registro en un hilo independiente
 * y guarda su resultado en el archivo compartido.
 */
public class HiloNotificacionRegistro extends Thread {

    // Se incluyen milisegundos para apreciar cómo se intercalan los hilos
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private final int fila;                      // Fila del registro en la tabla
    private final RegistroNotificacion registro; // Inmutable: seguro de compartir
    private final FormularioNotificacionV2 formulario;
    private final EscritorResultado escritor;    // Compartido y sincronizado
    private final AtomicInteger completados;     // Contador compartido entre todos los hilos
    private final int total;

    public HiloNotificacionRegistro(int fila, RegistroNotificacion registro,
                                    FormularioNotificacionV2 formulario, EscritorResultado escritor,
                                    AtomicInteger completados, int total) {
        super("Hilo-" + (fila + 1)); // Nombre propio para identificar cada hilo
        this.fila = fila;
        this.registro = registro;
        this.formulario = formulario;
        this.escritor = escritor;
        this.completados = completados;
        this.total = total;
    }

    @Override
    public void run() {
        long inicioMs = System.currentTimeMillis();
        String horaInicio = LocalTime.now().format(FORMATO_HORA);
        boolean entregada = false;

        formulario.actualizarEstado(fila, "En proceso");
        registrar("INICIO - Cliente: " + registro.getCliente() + " | Canal: " + registro.getCanal());

        try {
            registrar("Generando mensaje de confirmación...");
            pausar(400, 1200);

            registrar("Enviando por " + registro.getCanal() + " a " + registro.getContacto() + "...");
            pausarSegunCanal();

            registrar("Esperando confirmación de entrega...");
            pausar(300, 900);

            entregada = true;
        } catch (InterruptedException e) {
            registrar("Tarea interrumpida antes de terminar.");
            Thread.currentThread().interrupt(); // Conserva el estado de interrupción
        } finally {
            double segundos = (System.currentTimeMillis() - inicioMs) / 1000.0;
            guardarResultado(entregada, horaInicio, segundos);

            if (entregada) {
                registrar(String.format("FIN - Notificación entregada (%.2f s)", segundos));
                formulario.actualizarEstado(fila, "Notificado");
            } else {
                registrar(String.format("FIN - Tarea interrumpida (%.2f s)", segundos));
                formulario.actualizarEstado(fila, "Interrumpido");
            }
            // incrementAndGet() es atómico: dos hilos nunca obtienen el mismo número
            formulario.actualizarProgreso(completados.incrementAndGet(), total);
        }
    }

    private void guardarResultado(boolean entregada, String horaInicio, double segundos) {
        String detalle = entregada
                ? "Notificación entregada por " + registro.getCanal() + " a " + registro.getContacto()
                : "Proceso interrumpido antes de la entrega";
        ResultadoNotificacion resultado = new ResultadoNotificacion(registro, getName(), horaInicio,
                LocalTime.now().format(FORMATO_HORA), segundos, entregada, detalle);
        try {
            escritor.escribirResultado(resultado); // Si otro hilo está escribiendo, este espera su turno
            registrar("Resultado guardado en resultado.txt");
        } catch (IOException e) {
            registrar("ERROR al guardar el resultado: " + e.getMessage());
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
        Thread.sleep(ThreadLocalRandom.current().nextInt(minMs, maxMs + 1));
    }

    private void registrar(String mensaje) {
        String linea = String.format("[%s][%-7s][%s] %s",
                LocalTime.now().format(FORMATO_HORA), getName(), registro.getFolio(), mensaje);
        System.out.println(linea);        // Seguimiento en consola
        formulario.agregarMensaje(linea); // Seguimiento en la interfaz
    }
}