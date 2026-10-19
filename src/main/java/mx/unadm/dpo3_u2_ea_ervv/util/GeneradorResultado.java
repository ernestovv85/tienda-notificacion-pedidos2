package mx.unadm.dpo3_u2_ea_ervv.util;

import mx.unadm.dpo3_u2_ea_ervv.vista.VentanaMenuPrincipal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class GeneradorResultado {

    private static final Path RUTA_ARCHIVO = Paths.get("resultado.txt");
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final String SEPARADOR = "=".repeat(64);

    private GeneradorResultado() {
        // Clase de utilidad: no se instancia
    }

    public static Path escribir(String cliente, int pedidosSolicitados, List<String> notificaciones,
                                boolean completada, long duracionMs, String nombreHilo) throws IOException {
        List<String> lineas = new ArrayList<>();
        lineas.add(SEPARADOR);
        lineas.add("MÓDULO: " + VentanaMenuPrincipal.NOMBRE_MODULO.toUpperCase());
        lineas.add(SEPARADOR);
        lineas.add("Alumno:              " + VentanaMenuPrincipal.NOMBRE_ALUMNO);
        lineas.add("Fecha de ejecución:  " + LocalDateTime.now().format(FORMATO_FECHA));
        lineas.add("Hilo de ejecución:   " + nombreHilo);
        lineas.add("Cliente:             " + cliente);
        lineas.add("Pedidos solicitados: " + pedidosSolicitados);
        lineas.add("Pedidos notificados: " + notificaciones.size());
        lineas.add("Estado de la tarea:  " + (completada ? "COMPLETADA" : "INTERRUMPIDA"));
        lineas.add(String.format("Duración:            %.2f s", duracionMs / 1000.0));
        lineas.add("-".repeat(64));
        lineas.add(String.format("%-10s | %-20s | %-8s | %s", "FOLIO", "CANAL", "HORA", "ESTADO"));
        lineas.add("-".repeat(64));
        lineas.addAll(notificaciones);
        lineas.add(SEPARADOR);

        // Crea el archivo o lo sobrescribe en cada ejecución
        return Files.write(RUTA_ARCHIVO, lineas, StandardCharsets.UTF_8).toAbsolutePath();
    }
}
