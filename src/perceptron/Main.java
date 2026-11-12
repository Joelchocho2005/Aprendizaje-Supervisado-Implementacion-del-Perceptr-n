package perceptron;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Punto de entrada de la aplicación.
 * Instancia Modelo, Vista y Controlador y arranca la interfaz en el EDT.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Apariencia nativa del sistema operativo (opcional)
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Si falla, se usa el Look & Feel por defecto (Metal)
            }

            PerceptronModel model = new PerceptronModel();
            PerceptronView view = new PerceptronView();
            PerceptronController controller = new PerceptronController(model, view);
            controller.start();
        });
    }
}
