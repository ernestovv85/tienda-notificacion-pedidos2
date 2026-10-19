# Sistema multi-flujo de notificación de pedidos · Versión 2

Evidencia de aprendizaje, Unidad 2 · Programación Orientada a Objetos III (UnADM)
**Alumno:** Ernesto Velázquez Velázquez
**Módulo asignado:** Notificación de pedidos procesados

Aplicación de escritorio en Java 17 con Swing (estructura de proyecto Spring Boot) que procesa
varios registros de manera concurrente, con un hilo independiente por registro, y consolida
los resultados en `resultado.txt` sin pérdida ni mezcla de información.

## Funcionalidad

- **Menú principal:** barra de título con módulo, versión 2 y nombre del alumno; botón de acceso al módulo.
- **Módulo versión 2:**
    - Captura de 3 a 10 registros (cliente, canal y contacto) con validación por canal.
    - Botón **Iniciar** que lanza un hilo independiente por registro (`Hilo-1`, `Hilo-2`, …).
    - Bitácora y consola con mensajes de inicio, avance y fin de cada hilo; `Thread.sleep()` aleatorio por canal para observar la intercalación.
    - Tabla con el estado de cada registro (Pendiente → En proceso → Notificado) y barra de progreso.
    - `resultado.txt` generado durante el procesamiento, con escritura sincronizada y verificación final.
    - Mensaje de confirmación al terminar.
- **Módulo versión 1:** se conserva la funcionalidad de la etapa 1 (menú *Módulo → versión 1*).

## Estructura

```
src/main/java/mx/unadm/dpo3_u2_ea_ervv/
├── NotificacionPedidosApplication.java   Clase principal
├── modelo/
│   ├── RegistroNotificacion.java         Registro inmutable + validación
│   └── ResultadoNotificacion.java        Resultado de un hilo (record)
├── hilo/
│   ├── HiloNotificacionPedidos.java      Hilo del módulo v1 (etapa 1)
│   └── HiloNotificacionRegistro.java     Hilo por registro (v2)
├── util/
│   ├── GeneradorResultado.java           Archivo del módulo v1 (etapa 1)
│   └── EscritorResultado.java            Escritor sincronizado compartido (v2)
└── vista/
    ├── VentanaMenuPrincipal.java         Menú principal
    ├── FormularioNotificacion.java       Formulario v1 (etapa 1)
    └── FormularioNotificacionV2.java     Formulario v2
```

## Concurrencia aplicada

| Técnica | Uso |
|---|---|
| `extends Thread` + `start()` | Un hilo independiente por registro |
| `Thread.sleep()` | Simula el tiempo de procesamiento de cada etapa |
| `synchronized` | `EscritorResultado` escribe cada bloque completo, un hilo a la vez |
| `AtomicInteger` | Contador de registros completados sin condiciones de carrera |
| `join()` en hilo coordinador | Espera a todos los hilos sin congelar la interfaz |
| `SwingUtilities.invokeLater` | Actualiza la interfaz solo desde el Event Dispatch Thread |
| `interrupt()` | Detiene los hilos si se cierra el módulo durante el proceso |

## Ejecución

Requisitos: JDK 17.

```bash
./mvnw spring-boot:run
```

O bien, desde IntelliJ IDEA, ejecutar `NotificacionPedidosApplication`.
El archivo `resultado.txt` se genera en la raíz del proyecto.