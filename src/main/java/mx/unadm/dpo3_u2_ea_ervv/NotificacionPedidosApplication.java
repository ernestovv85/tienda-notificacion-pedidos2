package mx.unadm.dpo3_u2_ea_ervv;

import mx.unadm.dpo3_u2_ea_ervv.vista.VentanaMenuPrincipal;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import javax.swing.*;

@SpringBootApplication
public class NotificacionPedidosApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(NotificacionPedidosApplication.class)
				.headless(false)
				.run(args);

		System.out.println("[" + Thread.currentThread().getName() + "] Aplicación iniciada.");

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.err.println("No se pudo aplicar el Look and Feel del sistema: " + e.getMessage());
		}

		SwingUtilities.invokeLater(() -> new VentanaMenuPrincipal().setVisible(true));
	}
}
