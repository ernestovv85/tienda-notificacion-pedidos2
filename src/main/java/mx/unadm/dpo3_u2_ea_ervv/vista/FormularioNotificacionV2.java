package mx.unadm.dpo3_u2_ea_ervv.vista;

import mx.unadm.dpo3_u2_ea_ervv.modelo.RegistroNotificacion;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import mx.unadm.dpo3_u2_ea_ervv.hilo.HiloNotificacionRegistro;
import java.util.concurrent.atomic.AtomicInteger;

public class FormularioNotificacionV2 extends JDialog {

    private static final int MIN_REGISTROS = 3;
    private static final int MAX_REGISTROS = 10;
    private static final String[] COLUMNAS = {"Folio", "Cliente", "Canal", "Contacto", "Estado"};
    private static final int COL_ESTADO = 4;

    // Captura
    private final JTextField txtCliente = new JTextField(20);
    private final JComboBox<String> cmbCanal = new JComboBox<>(RegistroNotificacion.CANALES);
    private final JTextField txtContacto = new JTextField(20);
    private final JLabel lblAyudaContacto = new JLabel();
    private final JButton btnAgregar = new JButton("Agregar");

    // Registros y acciones
    private final DefaultTableModel modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false; // La tabla solo muestra datos
        }
    };
    private final JTable tablaRegistros = new JTable(modeloTabla);
    private final JLabel lblTotal = new JLabel();
    private final JButton btnQuitar = new JButton("Quitar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnIniciar = new JButton("Iniciar");

    // Seguimiento
    private final JTextArea txtBitacora = new JTextArea(12, 70);
    private final JProgressBar barraProgreso = new JProgressBar(0, 100);

    // Datos: la lista y la tabla se mantienen en el mismo orden (índice = fila)
    private final List<RegistroNotificacion> registros = new ArrayList<>();
    private int consecutivoFolio = 0;

    // Hilos de la ejecución actual
    private final List<HiloNotificacionRegistro> hilos = new ArrayList<>();
    private Thread coordinador;

    public FormularioNotificacionV2(JFrame propietario) {
        super(propietario, "Módulo: " + VentanaMenuPrincipal.NOMBRE_MODULO
                + " - " + VentanaMenuPrincipal.VERSION, false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JSplitPane division = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                crearPanelRegistros(), crearPanelBitacora());
        division.setResizeWeight(0.45);
        division.setBorder(null);

        JPanel contenido = new JPanel(new BorderLayout(10, 10));
        contenido.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        contenido.add(crearPanelCaptura(), BorderLayout.NORTH);
        contenido.add(division, BorderLayout.CENTER);
        contenido.add(crearBarraProgreso(), BorderLayout.SOUTH);
        setContentPane(contenido);

        btnAgregar.addActionListener(e -> agregarRegistro());
        btnQuitar.addActionListener(e -> quitarRegistro());
        btnLimpiar.addActionListener(e -> limpiar());
        btnIniciar.addActionListener(e -> iniciarProcesamiento());
        cmbCanal.addActionListener(e -> actualizarAyudaContacto());
        getRootPane().setDefaultButton(btnAgregar); // Enter = Agregar

        actualizarAyudaContacto();
        actualizarControles();
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(propietario);
    }

    // ---------- Construcción de la interfaz ----------

    private JPanel crearPanelCaptura() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                "Captura de registros (mínimo " + MIN_REGISTROS + ", máximo " + MAX_REGISTROS + ")"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 8, 4, 8);
        c.anchor = GridBagConstraints.WEST;

        agregarFila(panel, c, 0, "Cliente:", txtCliente);
        agregarFila(panel, c, 1, "Canal de notificación:", cmbCanal);
        agregarFila(panel, c, 2, "Contacto:", txtContacto);

        // Ancho fijo para que el formulario no "brinque" al cambiar de canal
        lblAyudaContacto.setForeground(Color.GRAY);
        lblAyudaContacto.setPreferredSize(new Dimension(220, txtContacto.getPreferredSize().height));
        c.gridx = 2; c.gridy = 2; c.fill = GridBagConstraints.NONE; c.weightx = 0;
        panel.add(lblAyudaContacto, c);

        c.gridx = 0; c.gridy = 3; c.gridwidth = 3; c.anchor = GridBagConstraints.EAST;
        panel.add(btnAgregar, c);
        return panel;
    }

    private void agregarFila(JPanel panel, GridBagConstraints c, int fila,
                             String etiqueta, JComponent campo) {
        c.gridx = 0; c.gridy = fila; c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE; c.weightx = 0;
        panel.add(new JLabel(etiqueta), c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        panel.add(campo, c);
    }

    private JPanel crearPanelRegistros() {
        tablaRegistros.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaRegistros.getTableHeader().setReorderingAllowed(false);
        tablaRegistros.setRowHeight(22);
        tablaRegistros.setFillsViewportHeight(true);

        JScrollPane scroll = new JScrollPane(tablaRegistros);
        scroll.setPreferredSize(new Dimension(780, 160));

        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD));
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(btnQuitar);
        botones.add(btnLimpiar);
        botones.add(btnIniciar);

        JPanel acciones = new JPanel(new BorderLayout());
        acciones.add(lblTotal, BorderLayout.WEST);
        acciones.add(botones, BorderLayout.EAST);

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Registros por procesar"));
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private JScrollPane crearPanelBitacora() {
        txtBitacora.setEditable(false);
        txtBitacora.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(txtBitacora);
        scroll.setBorder(BorderFactory.createTitledBorder("Bitácora de ejecución (hilos)"));
        return scroll;
    }

    private JProgressBar crearBarraProgreso() {
        barraProgreso.setStringPainted(true);
        barraProgreso.setString("En espera");
        return barraProgreso;
    }

    // ---------- Lógica del formulario ----------

    private void agregarRegistro() {
        if (registros.size() >= MAX_REGISTROS) {
            mostrarAviso("Se alcanzó el máximo de " + MAX_REGISTROS + " registros.", txtCliente);
            return;
        }

        String cliente = txtCliente.getText().trim();
        String canal = (String) cmbCanal.getSelectedItem();
        String contacto = txtContacto.getText().trim();

        String error = RegistroNotificacion.validar(cliente, canal, contacto);
        if (error != null) {
            mostrarAviso(error, cliente.isEmpty() ? txtCliente : txtContacto);
            return;
        }

        consecutivoFolio++;
        RegistroNotificacion registro = new RegistroNotificacion(
                String.format("PED-%04d", consecutivoFolio), cliente, canal, contacto);
        registros.add(registro);
        modeloTabla.addRow(new Object[]{
                registro.getFolio(), cliente, canal, contacto, "Pendiente"});

        txtCliente.setText("");
        txtContacto.setText("");
        txtCliente.requestFocusInWindow();
        actualizarControles();
    }

    private void quitarRegistro() {
        int fila = tablaRegistros.getSelectedRow();
        if (fila < 0) {
            mostrarAviso("Selecciona en la tabla el registro que deseas quitar.", null);
            return;
        }
        registros.remove(fila);
        modeloTabla.removeRow(fila);
        actualizarControles();
    }

    private void limpiar() {
        registros.clear();
        modeloTabla.setRowCount(0);
        consecutivoFolio = 0;
        txtCliente.setText("");
        txtContacto.setText("");
        txtBitacora.setText("");
        barraProgreso.setValue(0);
        barraProgreso.setString("En espera");
        actualizarControles();
        txtCliente.requestFocusInWindow();
    }

    private void iniciarProcesamiento() {
        if (registros.size() < MIN_REGISTROS) {
            mostrarAviso("Captura al menos " + MIN_REGISTROS + " registros para iniciar.", txtCliente);
            return;
        }
        if (coordinador != null && coordinador.isAlive()) {
            return; // Ya hay un procesamiento en curso
        }

        int total = registros.size();
        txtBitacora.setText("");
        for (int i = 0; i < total; i++) {
            modeloTabla.setValueAt("Pendiente", i, COL_ESTADO);
        }
        actualizarProgreso(0, total);
        establecerEnEjecucion(true);

        // Un hilo independiente por cada registro capturado
        AtomicInteger completados = new AtomicInteger();
        hilos.clear();
        for (int i = 0; i < total; i++) {
            hilos.add(new HiloNotificacionRegistro(i, registros.get(i), this, completados, total));
        }

        agregarMensaje("[" + Thread.currentThread().getName() + "] Iniciando " + total
                + " hilos independientes...");
        for (HiloNotificacionRegistro hilo : hilos) {
            hilo.start(); // start() crea un flujo nuevo; run() directo se ejecutaría en el EDT
        }

        // El coordinador espera a los hilos sin bloquear la interfaz
        List<HiloNotificacionRegistro> lanzados = List.copyOf(hilos);
        coordinador = new Thread(() -> esperarFinalizacion(lanzados), "Hilo-Coordinador");
        coordinador.start();
    }

    private void esperarFinalizacion(List<HiloNotificacionRegistro> lanzados) {
        try {
            for (HiloNotificacionRegistro hilo : lanzados) {
                hilo.join(); // Espera a que ese hilo termine
            }
            String mensaje = "[Hilo-Coordinador] Todos los hilos finalizaron ("
                    + lanzados.size() + " registros).";
            System.out.println(mensaje);
            agregarMensaje(mensaje);
            // Paso 6: aquí se confirmará que resultado.txt se guardó correctamente
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            establecerEnEjecucion(false);
        }
    }

    @Override
    public void dispose() {
        // Si se cierra la ventana a mitad del proceso, se detienen los hilos
        for (HiloNotificacionRegistro hilo : hilos) {
            if (hilo.isAlive()) {
                hilo.interrupt();
            }
        }
        if (coordinador != null) {
            coordinador.interrupt();
        }
        super.dispose();
    }

    private void actualizarAyudaContacto() {
        lblAyudaContacto.setText(RegistroNotificacion.ayudaContacto((String) cmbCanal.getSelectedItem()));
    }

    private void actualizarControles() {
        int total = registros.size();
        lblTotal.setText("Registros capturados: " + total
                + (total < MIN_REGISTROS ? "  (faltan " + (MIN_REGISTROS - total) + ")" : ""));
        btnAgregar.setEnabled(total < MAX_REGISTROS);
        btnQuitar.setEnabled(total > 0);
        btnLimpiar.setEnabled(true);
        btnIniciar.setEnabled(total >= MIN_REGISTROS);
    }

    private void mostrarAviso(String mensaje, JTextField campo) {
        JOptionPane.showMessageDialog(this, mensaje, "Dato inválido", JOptionPane.WARNING_MESSAGE);
        if (campo != null) {
            campo.requestFocusInWindow();
            campo.selectAll();
        }
    }

    // ---------- Métodos que usarán los hilos (Paso 5) ----------
    // Swing no es thread-safe: todo cambio a la interfaz se encola en el Event Dispatch Thread.

    public void agregarMensaje(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            txtBitacora.append(mensaje + System.lineSeparator());
            txtBitacora.setCaretPosition(txtBitacora.getDocument().getLength());
        });
    }

    public void actualizarEstado(int fila, String estado) {
        SwingUtilities.invokeLater(() -> modeloTabla.setValueAt(estado, fila, COL_ESTADO));
    }

    public void actualizarProgreso(int completados, int total) {
        SwingUtilities.invokeLater(() -> {
            barraProgreso.setValue(completados * 100 / total);
            barraProgreso.setString(completados + " de " + total + " registros procesados");
        });
    }

    public void establecerEnEjecucion(boolean enEjecucion) {
        SwingUtilities.invokeLater(() -> {
            txtCliente.setEditable(!enEjecucion);
            cmbCanal.setEnabled(!enEjecucion);
            txtContacto.setEditable(!enEjecucion);
            if (enEjecucion) {
                btnAgregar.setEnabled(false);
                btnQuitar.setEnabled(false);
                btnLimpiar.setEnabled(false);
                btnIniciar.setEnabled(false);
            } else {
                actualizarControles();
            }
        });
    }
}
