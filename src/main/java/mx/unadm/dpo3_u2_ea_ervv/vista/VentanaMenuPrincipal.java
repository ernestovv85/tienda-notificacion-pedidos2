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
    private static final String RUTA_IMAGEN = "/imagenes/notificacion.png";
    private static final int ANCHO_MAX_IMAGEN = 400;
    private static final int ALTO_MAX_IMAGEN = 280;
    private FormularioNotificacion formulario;

    public VentanaMenuPrincipal() {
        super(NOMBRE_MODULO + " - " + NOMBRE_ALUMNO);
        configurarVentana();
        setJMenuBar(crearMenu());
        add(crearPantallaInicio());
    }

    private void configurarVentana() {
        setSize(720, 540);
        setMinimumSize(new Dimension(600, 450));
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

        JMenuItem itemNotificacion = new JMenuItem(NOMBRE_MODULO);
        itemNotificacion.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        itemNotificacion.addActionListener(e -> abrirModulo());
        menuModulo.add(itemNotificacion);

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

        JLabel lblTitulo = new JLabel("Tienda en línea · Sistema multi-flujo", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(0x1F, 0x4E, 0x79));

        JLabel lblImagen = new JLabel("", SwingConstants.CENTER);
        Icon icono = cargarImagen();
        if (icono != null) {
            lblImagen.setIcon(icono);
        } else {
            lblImagen.setText("[ Imagen no encontrada: " + RUTA_IMAGEN + " ]");
            lblImagen.setForeground(Color.GRAY);
        }

        JLabel lblInstruccion = new JLabel(
                "<html><div style='text-align:center'>Módulo: <b>" + NOMBRE_MODULO + "</b><br>"
                        + "Selecciona <i>Módulo → " + NOMBRE_MODULO + "</i> para comenzar.</div></html>",
                SwingConstants.CENTER);
        lblInstruccion.setFont(new Font("SansSerif", Font.PLAIN, 14));

        panel.add(lblTitulo, BorderLayout.NORTH);
        panel.add(lblImagen, BorderLayout.CENTER);
        panel.add(lblInstruccion, BorderLayout.SOUTH);
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

    private void abrirModulo() {
        if (formulario == null || !formulario.isDisplayable()) {
            formulario = new FormularioNotificacion(this);
        }
        formulario.setVisible(true);
        formulario.toFront();
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