package perceptron;

import perceptron.PerceptronModel.Gate;
import perceptron.PerceptronModel.StepResult;
import perceptron.PerceptronModel.TrainingListener;
import perceptron.PerceptronModel.TrainingResult;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static perceptron.PerceptronModel.fmt;

/**
 * CONTROLADOR del patrón MVC.
 *
 * Escucha los eventos de la Vista, valida las entradas, invoca al Modelo y
 * traduce los resultados del Modelo a texto para la Vista.
 *
 * El entrenamiento se ejecuta en un {@link SwingWorker} (hilo en segundo
 * plano) para que la interfaz nunca se congele, ni siquiera con el XOR,
 * que agota las {@value PerceptronModel#MAX_EPOCHS} épocas.
 */
public class PerceptronController {

    private static final String SEPARATOR = "-".repeat(95) + "\n";

    private final PerceptronModel model;
    private final PerceptronView view;

    public PerceptronController(PerceptronModel model, PerceptronView view) {
        this.model = model;
        this.view = view;
        view.addRandomListener(e -> fillRandomValues());
        view.addTrainListener(e -> startTraining());
    }

    /** Muestra la ventana. */
    public void start() {
        view.setVisible(true);
    }

    // ------------------------------------------------------------------
    // Botón "Valores Aleatorios"
    // ------------------------------------------------------------------

    private void fillRandomValues() {
        view.setW1Text(fmt(model.randomValue()));
        view.setW2Text(fmt(model.randomValue()));
        view.setThetaText(fmt(model.randomValue()));
    }

    // ------------------------------------------------------------------
    // Botón "Iniciar Aprendizaje"
    // ------------------------------------------------------------------

    private void startTraining() {
        // 1. Validación: los campos vacíos reciben un valor aleatorio.
        boolean autoFilled = false;
        if (view.getW1Text().isEmpty())    { view.setW1Text(fmt(model.randomValue()));    autoFilled = true; }
        if (view.getW2Text().isEmpty())    { view.setW2Text(fmt(model.randomValue()));    autoFilled = true; }
        if (view.getThetaText().isEmpty()) { view.setThetaText(fmt(model.randomValue())); autoFilled = true; }

        // 2. Conversión de texto a número (se acepta coma o punto decimal).
        double w1, w2, theta;
        try {
            w1 = parse(view.getW1Text());
            w2 = parse(view.getW2Text());
            theta = parse(view.getThetaText());
        } catch (NumberFormatException ex) {
            view.showError("Los valores de w1, w2 y θ deben ser números reales.\nEjemplo: 0.5  o  -0,75");
            return;
        }

        Gate gate = Gate.valueOf(view.getSelectedGate());
        model.setWeights(w1, w2, theta);

        // 3. Preparar la interfaz.
        view.clearLog();
        view.setEpochsText("Épocas: entrenando...");
        view.setEquationText("Hiperplano: -");
        view.setStatusText(" ", true);
        view.setTrainingInProgress(true);

        StringBuilder header = new StringBuilder();
        header.append("Compuerta: ").append(gate).append("  (valores bipolares -1 / 1)\n");
        if (autoFilled) {
            header.append("* Campos vacíos completados con valores aleatorios.\n");
        }
        header.append("Pesos iniciales: w1=").append(fmt(w1))
              .append("  w2=").append(fmt(w2))
              .append("  θ=").append(fmt(theta)).append('\n');
        header.append("Activación: Y = 1 si (x1*w1 + x2*w2 + θ) > 0 ; si no, Y = -1\n");
        view.appendLog(header.toString());

        // 4. Lanzar el entrenamiento en segundo plano.
        new TrainingWorker(gate).execute();
    }

    private static double parse(String text) {
        return Double.parseDouble(text.replace(',', '.'));
    }

    // ------------------------------------------------------------------
    // SwingWorker: ejecuta el Modelo fuera del EDT y publica el log
    // ------------------------------------------------------------------

    private class TrainingWorker extends SwingWorker<TrainingResult, String> {

        private final Gate gate;

        TrainingWorker(Gate gate) {
            this.gate = gate;
        }

        /** Corre en un hilo de fondo: NO se toca Swing aquí, solo publish(). */
        @Override
        protected TrainingResult doInBackground() {
            return model.train(gate, new TrainingListener() {
                @Override
                public void onEpochStart(int epoch) {
                    publish(SEPARATOR + "ÉPOCA " + epoch + "\n" + SEPARATOR);
                }

                @Override
                public void onStep(StepResult s) {
                    publish(formatStep(s));
                }

                @Override
                public void onEpochEnd(int epoch, int errors) {
                    publish("  => Errores en la época " + epoch + ": " + errors + "\n");
                }

                @Override
                public boolean isCancelled() {
                    return TrainingWorker.this.isCancelled();
                }
            });
        }

        /** Corre en el EDT: aquí sí se actualiza la Vista. */
        @Override
        protected void process(List<String> chunks) {
            StringBuilder sb = new StringBuilder();
            for (String c : chunks) sb.append(c);
            view.appendLog(sb.toString());
        }

        /** Corre en el EDT al terminar el entrenamiento. */
        @Override
        protected void done() {
            view.setTrainingInProgress(false);
            TrainingResult r;
            try {
                r = get();
            } catch (InterruptedException | ExecutionException ex) {
                view.showError("Error durante el entrenamiento: " + ex.getMessage());
                return;
            }

            String equation = PerceptronModel.hyperplaneEquation(r.w1(), r.w2(), r.theta());
            view.setEquationText("Hiperplano: " + equation);

            if (r.converged()) {
                view.setEpochsText("Épocas necesarias para aprender: " + r.epochs());
                view.setStatusText("✔ Convergió: la compuerta " + gate
                        + " es linealmente separable.", true);
                view.appendLog(SEPARATOR + "CONVERGENCIA en " + r.epochs()
                        + " época(s).\nHiperplano final: " + equation + "\n");
            } else {
                view.setEpochsText("Épocas ejecutadas: " + r.epochs()
                        + " (límite de seguridad alcanzado)");
                view.setStatusText("✘ No convergió en " + PerceptronModel.MAX_EPOCHS
                        + " épocas: " + gate + " NO es linealmente separable.", false);
                view.appendLog(SEPARATOR + "SIN CONVERGENCIA tras " + r.epochs()
                        + " épocas. Últimos pesos: " + equation + "\n");
                view.showWarning("Se alcanzó el límite de " + PerceptronModel.MAX_EPOCHS
                        + " épocas sin converger.\n\nLa compuerta " + gate
                        + " NO es linealmente separable:\nun perceptrón de una sola neurona no puede aprenderla.");
            }
        }
    }

    /** Convierte un paso del Modelo en una línea de texto para el log. */
    private static String formatStep(StepResult s) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("  Fila %d | X=(%2d, %2d) | d(x)=%2d | net=%6s | Y=%2d | ",
                s.row(), s.x1(), s.x2(), s.desired(), fmt(s.net()), s.obtained()));
        if (s.error()) {
            sb.append("ERROR -> ")
              .append(s.desired() == 1 ? "w+=x, θ+=1" : "w-=x, θ-=1")
              .append(" | nuevos: w1=").append(fmt(s.w1()))
              .append("  w2=").append(fmt(s.w2()))
              .append("  θ=").append(fmt(s.theta()));
        } else {
            sb.append("OK");
        }
        return sb.append('\n').toString();
    }
}
