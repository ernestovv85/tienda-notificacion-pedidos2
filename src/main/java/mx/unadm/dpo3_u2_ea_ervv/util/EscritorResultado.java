package mx.unadm.dpo3_u2_ea_ervv.util;

import mx.unadm.dpo3_u2_ea_ervv.modelo.RegistroNotificacion;
import mx.unadm.dpo3_u2_ea_ervv.modelo.ResultadoNotificacion;
import mx.unadm.dpo3_u2_ea_ervv.vista.VentanaMenuPrincipal;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

/**
 * Escritor compartido por todos los hilos del módulo versión 2.
 * Sus métodos son synchronized: solo un hilo a la vez puede escribir,
 * por lo que cada bloque se guarda completo, sin perderse ni mezclarse.
 */
public class EscritorResultado {

    private static final Path RUTA_ARCHIVO = Paths.get("resultado.txt");
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final String SEPARADOR = "=".repeat(64);
    private static final String DIVISOR = "-".repeat(64);
    private static final String MARCA_REGISTRO = "REGISTRO #";

    private final BufferedWriter escritor;
    private final int totalRegistros;
    private final long inicioMs = System.currentTimeMillis();

    // Estado compartido: solo se modifica dentro de métodos synchronized
    private int escritos = 0;
    private int notificados = 0;
    private boolean cerrado = false;

    public EscritorResultado(int totalRegistros) throws IOException {
        this.totalRegistros = totalRegistros;
        // Crea el archivo o lo sobrescribe al iniciar cada procesamiento
        this.escritor = Files.newBufferedWriter(RUTA_ARCHIVO, StandardCharsets.UTF_8);
        escribirEncabezado();
    }

    private void escribirEncabezado() throws IOException {
        linea(SEPARADOR);
        linea("MÓDULO: " + VentanaMenuPrincipal.NOMBRE_MODULO.toUpperCase()
                + " - " + VentanaMenuPrincipal.VERSION.toUpperCase());
        linea(SEPARADOR);
        linea("Alumno:               " + VentanaMenuPrincipal.NOMBRE_ALUMNO);
        linea("Fecha de ejecución:   " + LocalDateTime.now().format(FORMATO_FECHA));
        linea("Registros a procesar: " + totalRegistros);
        linea("Procesamiento:        un hilo independiente por registro");
        linea("Nota: los registros aparecen en el orden en que cada hilo terminó.");
        escritor.flush();
    }

    /**
     * Escribe el bloque completo de un registro. Mientras un hilo está dentro
     * de este método, los demás esperan su turno en el monitor del objeto.
     */
    public synchronized void escribirResultado(ResultadoNotificacion resultado) throws IOException {
        if (cerrado) {
            throw new IOException("el archivo ya fue cerrado");
        }
        RegistroNotificacion r = resultado.registro();
        escritos++;
        if (resultado.entregada()) {
            notificados++;
        }

        linea(DIVISOR);
        linea(MARCA_REGISTRO + escritos);
        linea("Folio:      " + r.getFolio());
        linea("Hilo:       " + resultado.hilo());
        linea("Cliente:    " + r.getCliente());
        linea("Canal:      " + r.getCanal());
        linea("Contacto:   " + r.getContacto());
        linea("Inicio:     " + resultado.horaInicio());
        linea("Fin:        " + resultado.horaFin());
        linea(String.format("Duración:   %.2f s", resultado.segundos()));
        linea("Resultado:  " + (resultado.entregada() ? "NOTIFICADO" : "INTERRUMPIDO")
                + " - " + resultado.detalle());
        escritor.flush(); // El bloque queda en disco mientras los demás hilos siguen trabajando
    }

    /** Escribe el resumen y cierra el archivo. */
    public synchronized Path cerrar() throws IOException {
        if (cerrado) {
            return RUTA_ARCHIVO.toAbsolutePath();
        }
        double duracionTotal = (System.currentTimeMillis() - inicioMs) / 1000.0;
        boolean completo = escritos == totalRegistros && notificados == totalRegistros;

        linea(SEPARADOR);
        linea("RESUMEN");
        linea("Registros guardados:  " + escritos + " de " + totalRegistros);
        linea("Notificados:          " + notificados);
        linea("Interrumpidos:        " + (escritos - notificados));
        linea(String.format("Duración total:       %.2f s", duracionTotal));
        linea("Estado:               " + (completo ? "COMPLETADO" : "INCOMPLETO"));
        linea(SEPARADOR);

        escritor.close();
        cerrado = true;
        return RUTA_ARCHIVO.toAbsolutePath();
    }

    /** Relee el archivo y cuenta los bloques guardados para comprobar que no se perdió ninguno. */
    public int contarRegistrosEnArchivo() throws IOException {
        try (Stream<String> lineas = Files.lines(RUTA_ARCHIVO, StandardCharsets.UTF_8)) {
            return (int) lineas.filter(l -> l.startsWith(MARCA_REGISTRO)).count();
        }
    }

    private void linea(String texto) throws IOException {
        escritor.write(texto);
        escritor.newLine();
    }
}