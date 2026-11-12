package perceptron;

import java.util.Locale;
import java.util.Random;

/**
 * MODELO del patrón MVC.
 *
 * Contiene toda la lógica matemática del Perceptrón de una sola neurona en 2D:
 * tablas de verdad bipolares (-1 / 1), función de activación, ley de
 * aprendizaje y criterio de parada.
 *
 * IMPORTANTE: esta clase NO depende de Swing. Se comunica con el exterior
 * mediante la interfaz {@link TrainingListener}, de modo que puede usarse
 * igual desde consola, desde pruebas unitarias o desde cualquier interfaz.
 */
public class PerceptronModel {

    /** Límite de seguridad para problemas no linealmente separables (XOR). */
    public static final int MAX_EPOCHS = 100;

    /** Compuertas lógicas soportadas. */
    public enum Gate { AND, OR, XOR }

    /** Entradas bipolares comunes a las tres compuertas: {x1, x2}. */
    private static final int[][] INPUTS = {
            {-1, -1},
            {-1,  1},
            { 1, -1},
            { 1,  1}
    };

    /** Salidas deseadas d(x) para cada fila de INPUTS. */
    private static final int[] OUT_AND = {-1, -1, -1,  1};
    private static final int[] OUT_OR  = {-1,  1,  1,  1};
    private static final int[] OUT_XOR = {-1,  1,  1, -1};

    // Parámetros de la neurona
    private double w1;
    private double w2;
    private double theta;

    private final Random random = new Random();

    // ------------------------------------------------------------------
    // Tipos auxiliares para comunicar el progreso (sin dependencia de Swing)
    // ------------------------------------------------------------------

    /**
     * Resultado de evaluar una fila de la tabla de verdad dentro de una época.
     * Si {@code error} es true, w1/w2/theta contienen los valores YA actualizados.
     */
    public record StepResult(int epoch, int row, int x1, int x2, int desired,
                             double net, int obtained, boolean error,
                             double w1, double w2, double theta) { }

    /** Resultado global del entrenamiento. */
    public record TrainingResult(int epochs, boolean converged,
                                 double w1, double w2, double theta) { }

    /** Observador del proceso de entrenamiento (lo implementa el Controlador). */
    public interface TrainingListener {
        /** Se invoca al comenzar cada época. */
        void onEpochStart(int epoch);

        /** Se invoca después de evaluar cada fila. */
        void onStep(StepResult step);

        /** Se invoca al terminar cada época con el número de errores cometidos. */
        void onEpochEnd(int epoch, int errors);

        /** Permite al llamador cancelar el entrenamiento (p. ej. al cerrar la ventana). */
        default boolean isCancelled() { return false; }
    }

    // ------------------------------------------------------------------
    // Configuración de pesos
    // ------------------------------------------------------------------

    public void setWeights(double w1, double w2, double theta) {
        this.w1 = w1;
        this.w2 = w2;
        this.theta = theta;
    }

    /**
     * Genera un valor aleatorio uniforme en [-1.0, 1.0], redondeado a 2
     * decimales para que el log sea legible.
     */
    public double randomValue() {
        double v = -1.0 + 2.0 * random.nextDouble();
        return Math.round(v * 100.0) / 100.0;
    }

    public double getW1()    { return w1; }
    public double getW2()    { return w2; }
    public double getTheta() { return theta; }

    // ------------------------------------------------------------------
    // Tablas de verdad
    // ------------------------------------------------------------------

    public int[][] getInputs() {
        return INPUTS;
    }

    public int[] getDesiredOutputs(Gate gate) {
        return switch (gate) {
            case AND -> OUT_AND;
            case OR  -> OUT_OR;
            case XOR -> OUT_XOR;
        };
    }

    // ------------------------------------------------------------------
    // Matemática del perceptrón
    // ------------------------------------------------------------------

    /** Entrada neta: x1*w1 + x2*w2 + theta. */
    public double netInput(int x1, int x2) {
        return x1 * w1 + x2 * w2 + theta;
    }

    /** Función de activación escalón bipolar: 1 si net > 0, -1 si net <= 0. */
    public int activate(double net) {
        return net > 0 ? 1 : -1;
    }

    /**
     * Ley de aprendizaje.
     * <ul>
     *   <li>d(x) =  1 : w_i(t+1) = w_i(t) + x_i ; theta(t+1) = theta(t) + 1</li>
     *   <li>d(x) = -1 : w_i(t+1) = w_i(t) - x_i ; theta(t+1) = theta(t) - 1</li>
     * </ul>
     * Solo debe invocarse cuando Y != d(x).
     */
    private void updateWeights(int x1, int x2, int desired) {
        if (desired == 1) {
            w1 += x1;
            w2 += x2;
            theta += 1;
        } else {
            w1 -= x1;
            w2 -= x2;
            theta -= 1;
        }
    }

    // ------------------------------------------------------------------
    // Algoritmo de entrenamiento
    // ------------------------------------------------------------------

    /**
     * Entrena el perceptrón para la compuerta indicada.
     * Repite épocas hasta que una época completa no tenga errores
     * (convergencia) o hasta alcanzar {@link #MAX_EPOCHS}.
     *
     * @param gate     compuerta lógica a aprender
     * @param listener observador que recibe cada paso (puede ser null)
     * @return resultado con número de épocas, si convergió y pesos finales
     */
    public TrainingResult train(Gate gate, TrainingListener listener) {
        int[] desired = getDesiredOutputs(gate);
        int epoch = 0;
        boolean converged = false;

        while (epoch < MAX_EPOCHS) {
            if (listener != null && listener.isCancelled()) {
                break;
            }
            epoch++;
            if (listener != null) listener.onEpochStart(epoch);

            int errors = 0;
            for (int i = 0; i < INPUTS.length; i++) {
                int x1 = INPUTS[i][0];
                int x2 = INPUTS[i][1];
                int d  = desired[i];

                double net = netInput(x1, x2);
                int y = activate(net);
                boolean isError = (y != d);

                if (isError) {
                    errors++;
                    updateWeights(x1, x2, d);
                }

                if (listener != null) {
                    listener.onStep(new StepResult(epoch, i + 1, x1, x2, d,
                            net, y, isError, w1, w2, theta));
                }
            }

            if (listener != null) listener.onEpochEnd(epoch, errors);

            if (errors == 0) {          // Criterio de parada: época sin errores
                converged = true;
                break;
            }
        }
        return new TrainingResult(epoch, converged, w1, w2, theta);
    }

    // ------------------------------------------------------------------
    // Utilidades de presentación (texto puro, sin Swing)
    // ------------------------------------------------------------------

    /** Formatea un número con 2 decimales y punto decimal. */
    public static String fmt(double v) {
        return String.format(Locale.US, "%.2f", v);
    }

    /**
     * Ecuación del hiperplano: [w1]x1 + [w2]x2 + [theta] = 0.
     * Los signos se ajustan para que se lea de forma natural
     * (p. ej. "1.00x1 - 2.00x2 + 0.50 = 0").
     */
    public static String hyperplaneEquation(double w1, double w2, double theta) {
        return fmt(w1) + "x1 "
                + (w2 < 0 ? "- " : "+ ") + fmt(Math.abs(w2)) + "x2 "
                + (theta < 0 ? "- " : "+ ") + fmt(Math.abs(theta)) + " = 0";
    }
}
