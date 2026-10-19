package mx.unadm.dpo3_u2_ea_ervv.vista;

import javax.swing.*;

public class FormularioNotificacionV2 extends JDialog {

    public FormularioNotificacionV2(JFrame propietario) {
        super(propietario, "Módulo: " + VentanaMenuPrincipal.NOMBRE_MODULO
                + " - " + VentanaMenuPrincipal.VERSION, false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Contenido provisional: la interfaz completa se construye en el paso 4
        add(new JLabel("Módulo versión 2 en construcción", SwingConstants.CENTER));
        setSize(500, 300);
        setLocationRelativeTo(propietario);
    }
}
