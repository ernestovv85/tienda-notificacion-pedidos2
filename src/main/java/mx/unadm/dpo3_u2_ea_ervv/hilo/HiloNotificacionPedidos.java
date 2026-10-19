package mx.unadm.dpo3_u2_ea_ervv.hilo;

import mx.unadm.dpo3_u2_ea_ervv.util.GeneradorResultado;
import mx.unadm.dpo3_u2_ea_ervv.vista.FormularioNotificacion;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HiloNotificacionPedidos extends Thread {

    private static final String[] CANALES = {"Correo electrónico", "SMS", "Notificación push"};
    private static final int PAUSA_MS = 700;
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final String cliente;
    private final int cantidadPedidos;
    private final FormularioNotificacion formulario;
    private final List<String> notificaciones = new ArrayList<>();

    public HiloNotificacionPedidos(String cliente, int cantidadPedidos, FormularioNotificacion formulario) {
        super("Hilo-Notificacion"); // Nombre propio para distinguirlo de main y del EDT
        this.cliente = cliente;
        this.cantidadPedidos = cantidadPedidos;
        this.formulario = formulario;
    }

    @Override
    public void run() {
        long inicio = System.currentTimeMillis();
        boolean completada = false;

        registrar("INICIO - Notificación de pedidos procesados para: " + cliente);
        try {
            registrar("Conectando con el servicio de notificaciones...");
            Thread.sleep(PAUSA_MS);

            for (int i = 1; i <= cantidadPedidos; i++) {
                String folio = String.format("PED-%04d", i);
                String canal = CANALES[(i - 1) % CANALES.length];

                registrar(folio + ": generando mensaje de confirmación...");
                Thread.sleep(PAUSA_MS);

                registrar(folio + ": enviando por " + canal + "...");
                Thread.sleep(PAUSA_MS);

                String hora = LocalTime.now().format(FORMATO_HORA);
                registrar(folio + ": notificación entregada al cliente.");
                notificaciones.add(String.format("%-10s | %-20s | %-8s | %s", folio, canal, hora, "Entregada"));

                formulario.actualizarProgreso(i * 100 / cantidadPedidos);
            }

            completada = true;
            registrar("FIN - Se notificaron " + cantidadPedidos + " pedido(s) a " + cliente + ".");
        } catch (InterruptedException e) {
            registrar("Tarea interrumpida: " + notificaciones.size() + " de " + cantidadPedidos + " notificados.");
            Thread.currentThread().interrupt(); // Conserva el estado de interrupción
        } finally {
            guardarResultado(completada, System.currentTimeMillis() - inicio);
            formulario.establecerEnEjecucion(false);
        }
    }

    private void guardarResultado(boolean completada, long duracionMs) {
        try {
            Path ruta = GeneradorResultado.escribir(cliente, cantidadPedidos, notificaciones,
                    completada, duracionMs, getName());
            registrar("Archivo generado: " + ruta);
        } catch (IOException e) {
            registrar("ERROR al generar resultado.txt: " + e.getMessage());
        }
    }

    private void registrar(String mensaje) {
        String linea = String.format("[%s][%s] %s",
                LocalTime.now().format(FORMATO_HORA), getName(), mensaje);
        System.out.println(linea);       // Evidencia en consola
        formulario.agregarMensaje(linea); // Evidencia en la interfaz
    }
}
