package mx.unadm.dpo3_u2_ea_ervv.vista;

import javax.swing.*;
import java.awt.*;

import mx.unadm.dpo3_u2_ea_ervv.hilo.HiloNotificacionPedidos;

public class FormularioNotificacion extends JDialog {

    private static final int MAX_PEDIDOS = 20;

    private final JTextField txtCliente = new JTextField(22);
    private final JTextField txtCantidad = new JTextField(6);
    private final JButton btnIniciar = new JButton("Iniciar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JTextArea txtMensajes = new JTextArea(16, 60);
    private final JProgressBar barraProgreso = new JProgressBar(0, 100);
    private HiloNotificacionPedidos hilo;

    public FormularioNotificacion(JFrame propietario) {
        super(propietario, "Módulo: " + VentanaMenuPrincipal.NOMBRE_MODULO, false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel contenido = new JPanel(new BorderLayout(10, 10));
        contenido.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        contenido.add(crearPanelCaptura(), BorderLayout.NORTH);
        contenido.add(crearPanelMensajes(), BorderLayout.CENTER);
        contenido.add(crearPanelProgreso(), BorderLayout.SOUTH);
        setContentPane(contenido);

        btnIniciar.addActionListener(e -> iniciarTarea());
        btnLimpiar.addActionListener(e -> limpiar());
        getRootPane().setDefaultButton(btnIniciar); // Enter = Iniciar

        pack();
        setLocationRelativeTo(propietario);
    }

    // ---------- Construcción de la interfaz ----------

    private JPanel crearPanelCaptura() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Datos de la notificación"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 8, 5, 8);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0;
        panel.add(new JLabel("Nombre del cliente:"), c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        panel.add(txtCliente, c);

        c.gridx = 0; c.gridy = 1; c.fill = GridBagConstraints.NONE; c.weightx = 0;
        panel.add(new JLabel("Cantidad de pedidos procesados (1-" + MAX_PEDIDOS + "):"), c);
        c.gridx = 1;
        panel.add(txtCantidad, c);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(btnLimpiar);
        botones.add(btnIniciar);
        c.gridx = 0; c.gridy = 2; c.gridwidth = 2; c.anchor = GridBagConstraints.EAST;
        panel.add(botones, c);

        return panel;
    }

    private JScrollPane crearPanelMensajes() {
        txtMensajes.setEditable(false);
        txtMensajes.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(txtMensajes);
        scroll.setBorder(BorderFactory.createTitledBorder("Bitácora de ejecución"));
        return scroll;
    }

    private JProgressBar crearPanelProgreso() {
        barraProgreso.setStringPainted(true);
        barraProgreso.setString("En espera");
        return barraProgreso;
    }

    // ---------- Lógica del formulario ----------

    private void iniciarTarea() {
        String cliente = txtCliente.getText().trim();
        String cantidadTexto = txtCantidad.getText().trim();

        if (cliente.isEmpty()) {
            mostrarError("El nombre del cliente no puede estar vacío.", txtCliente);
            return;
        }
        if (cantidadTexto.isEmpty()) {
            mostrarError("La cantidad de pedidos no puede estar vacía.", txtCantidad);
            return;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(cantidadTexto);
        } catch (NumberFormatException ex) {
            mostrarError("La cantidad de pedidos debe ser un número entero.", txtCantidad);
            return;
        }
        if (cantidad < 1 || cantidad > MAX_PEDIDOS) {
            mostrarError("La cantidad debe estar entre 1 y " + MAX_PEDIDOS + ".", txtCantidad);
            return;
        }

        if (hilo != null && hilo.isAlive()) {
            return; // Ya hay una tarea en ejecución
        }

        txtMensajes.setText("");
        barraProgreso.setValue(0);
        establecerEnEjecucion(true);

        hilo = new HiloNotificacionPedidos(cliente, cantidad, this);
        hilo.start(); // start() crea el nuevo flujo; run() directo se ejecutaría en el EDT

        agregarMensaje("[" + Thread.currentThread().getName() + "] Se inició '" + hilo.getName()
                + "'. La interfaz sigue disponible.");
    }

    @Override
    public void dispose() {
        if (hilo != null && hilo.isAlive()) {
            hilo.interrupt();
        }
        super.dispose();
    }

    private void limpiar() {
        txtCliente.setText("");
        txtCantidad.setText("");
        txtMensajes.setText("");
        barraProgreso.setValue(0);
        barraProgreso.setString("En espera");
        txtCliente.requestFocusInWindow();
    }

    private void mostrarError(String mensaje, JTextField campo) {
        JOptionPane.showMessageDialog(this, mensaje, "Dato inválido", JOptionPane.WARNING_MESSAGE);
        campo.requestFocusInWindow();
        campo.selectAll();
    }

    // ---------- Métodos que usará el hilo (Paso 5) ----------
    // Swing no es thread-safe: los cambios a la interfaz se encolan en el Event Dispatch Thread.

    public void agregarMensaje(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            txtMensajes.append(mensaje + System.lineSeparator());
            txtMensajes.setCaretPosition(txtMensajes.getDocument().getLength());
        });
    }

    public void actualizarProgreso(int porcentaje) {
        SwingUtilities.invokeLater(() -> {
            barraProgreso.setValue(porcentaje);
            barraProgreso.setString(porcentaje + " %");
        });
    }

    public void establecerEnEjecucion(boolean enEjecucion) {
        SwingUtilities.invokeLater(() -> {
            btnIniciar.setEnabled(!enEjecucion);
            btnLimpiar.setEnabled(!enEjecucion);
            txtCliente.setEditable(!enEjecucion);
            txtCantidad.setEditable(!enEjecucion);
            if (!enEjecucion) {
                barraProgreso.setString("Finalizado");
            }
        });
    }
}
