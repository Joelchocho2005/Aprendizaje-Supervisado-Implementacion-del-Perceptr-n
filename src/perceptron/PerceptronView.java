package perceptron;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * VISTA del patrón MVC.
 *
 * Solo construye y expone componentes Swing. No contiene lógica de negocio:
 * no calcula nada ni decide nada; el Controlador le indica qué mostrar.
 */
public final class PerceptronView extends JFrame {

    private static final long serialVersionUID = 1L;

    // --- Panel de configuración ---
    private final JTextField txtW1 = new JTextField(7);
    private final JTextField txtW2 = new JTextField(7);
    private final JTextField txtTheta = new JTextField(7);
    private final JButton btnRandom = new JButton("Valores Aleatorios");
    private final JComboBox<String> cmbGate = new JComboBox<>(new String[]{"AND", "OR", "XOR"});
    private final JButton btnTrain = new JButton("Iniciar Aprendizaje");

    // --- Panel de visualización del proceso ---
    private final JTextArea txtLog = new JTextArea(22, 90);

    // --- Panel de resultado final ---
    private final JLabel lblEpochs = new JLabel("Épocas: -");
    private final JLabel lblEquation = new JLabel("Hiperplano: -");
    private final JLabel lblStatus = new JLabel(" ");

    public PerceptronView() {
        super("Perceptrón 2D - Aprendizaje Supervisado (bipolar)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        add(buildConfigPanel(), BorderLayout.NORTH);
        add(buildLogPanel(), BorderLayout.CENTER);
        add(buildResultPanel(), BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(760, 520));
        setLocationRelativeTo(null);
    }

    // ------------------------------------------------------------------
    // Construcción de paneles
    // ------------------------------------------------------------------

    private JPanel buildConfigPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        panel.setBorder(new TitledBorder("Configuración"));

        panel.add(new JLabel("w1:"));
        panel.add(txtW1);
        panel.add(new JLabel("w2:"));
        panel.add(txtW2);
        panel.add(new JLabel("θ (theta):"));
        panel.add(txtTheta);
        panel.add(btnRandom);
        panel.add(new JLabel("Compuerta:"));
        panel.add(cmbGate);
        panel.add(btnTrain);
        return panel;
    }

    private JPanel buildLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Proceso de entrenamiento (paso a paso)"));

        txtLog.setEditable(false);
        txtLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        panel.add(new JScrollPane(txtLog), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildResultPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 4, 4));
        panel.setBorder(new TitledBorder("Resultado final"));

        Font bold = lblEpochs.getFont().deriveFont(Font.BOLD, 14f);
        lblEpochs.setFont(bold);
        lblEquation.setFont(bold);

        panel.add(lblEpochs);
        panel.add(lblEquation);
        panel.add(lblStatus);
        return panel;
    }

    // ------------------------------------------------------------------
    // Registro de oyentes (los provee el Controlador)
    // ------------------------------------------------------------------

    public void addRandomListener(ActionListener l) { btnRandom.addActionListener(l); }

    public void addTrainListener(ActionListener l)  { btnTrain.addActionListener(l); }

    // ------------------------------------------------------------------
    // Lectura de entradas del usuario
    // ------------------------------------------------------------------

    public String getW1Text()    { return txtW1.getText().trim(); }
    public String getW2Text()    { return txtW2.getText().trim(); }
    public String getThetaText() { return txtTheta.getText().trim(); }

    public String getSelectedGate() { return (String) cmbGate.getSelectedItem(); }

    // ------------------------------------------------------------------
    // Actualización de la interfaz
    // ------------------------------------------------------------------

    public void setW1Text(String s)    { txtW1.setText(s); }
    public void setW2Text(String s)    { txtW2.setText(s); }
    public void setThetaText(String s) { txtTheta.setText(s); }

    public void clearLog() { txtLog.setText(""); }

    /** Agrega texto al log y desplaza el scroll al final. */
    public void appendLog(String text) {
        txtLog.append(text);
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
    }

    public void setEpochsText(String s)   { lblEpochs.setText(s); }
    public void setEquationText(String s) { lblEquation.setText(s); }

    public void setStatusText(String s, boolean ok) {
        lblStatus.setText(s);
        lblStatus.setForeground(ok ? new Color(0, 128, 0) : new Color(180, 0, 0));
    }

    /** Habilita/deshabilita controles mientras se entrena. */
    public void setTrainingInProgress(boolean running) {
        btnTrain.setEnabled(!running);
        btnRandom.setEnabled(!running);
        cmbGate.setEnabled(!running);
        txtW1.setEditable(!running);
        txtW2.setEditable(!running);
        txtTheta.setEditable(!running);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Aviso", JOptionPane.WARNING_MESSAGE);
    }

    public void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Información", JOptionPane.INFORMATION_MESSAGE);
    }
}
