package mx.unadm.dpo3_u2_ea_ervv.vista;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

public class VentanaMenuPrincipal extends JFrame {

    public static final String NOMBRE_MODULO = "Notificación de pedidos procesados";
    public static final String NOMBRE_ALUMNO = "Ernesto Velázquez Velázquez";
    public static final String VERSION = "versión 2";
    private static final String RUTA_IMAGEN = "/imagenes/notificacion.png";
    private static final int ANCHO_MAX_IMAGEN = 400;
    private static final int ALTO_MAX_IMAGEN = 260;

    private FormularioNotificacion formulario;     // Módulo versión 1 (etapa 1)
    private FormularioNotificacionV2 formularioV2; // Módulo versión 2 (etapa 2)

    public VentanaMenuPrincipal() {
        // Barra de título: módulo asignado + "versión 2" + nombre completo
        super(NOMBRE_MODULO + " - " + VERSION + " - " + NOMBRE_ALUMNO);
        configurarVentana();
        setJMenuBar(crearMenu());
        add(crearPantallaInicio());
    }

    private void configurarVentana() {
        setSize(760, 580);
        setMinimumSize(new Dimension(640, 500));
        setLocationRelativeTo(null);
        // La X de la ventana pasa por la misma confirmación que la opción Salir
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                salir();
            }
        });
    }

    private JMenuBar crearMenu() {
        JMenuBar barraMenu = new JMenuBar();

        JMenu menuModulo = new JMenu("Módulo");
        menuModulo.setMnemonic(KeyEvent.VK_M);

        JMenuItem itemVersion1 = new JMenuItem(NOMBRE_MODULO + " (versión 1)");
        itemVersion1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_DOWN_MASK));
        itemVersion1.addActionListener(e -> abrirModulo());

        JMenuItem itemVersion2 = new JMenuItem(NOMBRE_MODULO + " (" + VERSION + ")");
        itemVersion2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_DOWN_MASK));
        itemVersion2.addActionListener(e -> abrirModuloV2());

        menuModulo.add(itemVersion1);
        menuModulo.add(itemVersion2);

        JMenu menuSistema = new JMenu("Sistema");
        menuSistema.setMnemonic(KeyEvent.VK_S);

        JMenuItem itemSalir = new JMenuItem("Salir");
        itemSalir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        itemSalir.addActionListener(e -> salir());
        menuSistema.add(itemSalir);

        barraMenu.add(menuModulo);
        barraMenu.add(menuSistema);
        return barraMenu;
    }

    private JPanel crearPantallaInicio() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        panel.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel("Tienda en línea · Sistema multi-flujo · Versión 2",
                SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(0x1F, 0x4E, 0x79));

        JLabel lblImagen = new JLabel("", SwingConstants.CENTER);
        Icon icono = cargarImagen();
        if (icono != null) {
            lblImagen.setIcon(icono);
        } else {
            lblImagen.setText("[ Imagen no encontrada: " + RUTA_IMAGEN + " ]");
            lblImagen.setForeground(Color.GRAY);
        }

        panel.add(lblTitulo, BorderLayout.NORTH);
        panel.add(lblImagen, BorderLayout.CENTER);
        panel.add(crearPanelAcceso(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelAcceso() {
        JLabel lblInstruccion = new JLabel(
                "<html><div style='text-align:center'>Módulo: <b>" + NOMBRE_MODULO + "</b> · "
                        + VERSION + "<br>Da clic en el botón para procesar registros con hilos "
                        + "independientes.</div></html>",
                SwingConstants.CENTER);
        lblInstruccion.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Botón de acceso al módulo versión 2 desde el menú principal
        JButton btnModuloV2 = new JButton("Abrir módulo (" + VERSION + ")");
        btnModuloV2.setFont(new Font("SansSerif", Font.BOLD, 15));
        btnModuloV2.setFocusPainted(false);
        btnModuloV2.setToolTipText("Abre el módulo " + NOMBRE_MODULO + " - " + VERSION);
        btnModuloV2.addActionListener(e -> abrirModuloV2());

        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBoton.setOpaque(false);
        panelBoton.add(btnModuloV2);

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.add(lblInstruccion, BorderLayout.CENTER);
        panel.add(panelBoton, BorderLayout.SOUTH);
        return panel;
    }

    private Icon cargarImagen() {
        URL url = getClass().getResource(RUTA_IMAGEN);
        if (url == null) {
            return null;
        }
        ImageIcon original = new ImageIcon(url);
        // Escala conservando la proporción para que no se deforme
        double escala = Math.min(
                (double) ANCHO_MAX_IMAGEN / original.getIconWidth(),
                (double) ALTO_MAX_IMAGEN / original.getIconHeight());
        int ancho = (int) (original.getIconWidth() * escala);
        int alto = (int) (original.getIconHeight() * escala);
        return new ImageIcon(original.getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH));
    }

    // Módulo de la etapa 1 (se conserva sin cambios)
    private void abrirModulo() {
        if (formulario == null || !formulario.isDisplayable()) {
            formulario = new FormularioNotificacion(this);
        }
        formulario.setVisible(true);
        formulario.toFront();
    }

    // Módulo de la etapa 2
    private void abrirModuloV2() {
        if (formularioV2 == null || !formularioV2.isDisplayable()) {
            formularioV2 = new FormularioNotificacionV2(this);
        }
        formularioV2.setVisible(true);
        formularioV2.toFront();
    }

    private void salir() {
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Deseas salir de la aplicación?", "Salir",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (respuesta == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }
}
