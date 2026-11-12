# Perceptrón 2D – Aprendizaje Supervisado (Java Swing, MVC)

Simulador del aprendizaje supervisado de un perceptrón de **una sola neurona** con entradas y salidas **bipolares (-1 / 1)** para las compuertas `AND`, `OR` y `XOR`.

## Estructura

```
src/perceptron/
├── Main.java                  # Punto de entrada: arma el MVC
├── PerceptronModel.java       # Modelo: tablas de verdad, activación, ley de aprendizaje (sin Swing)
├── PerceptronView.java        # Vista: interfaz Swing (sin lógica de negocio)
└── PerceptronController.java  # Controlador: eventos, validación, SwingWorker
```

## Reglas implementadas

- **Activación:** `Y = 1` si `x1*w1 + x2*w2 + θ > 0`; en caso contrario `Y = -1`.
- **Aprendizaje** (solo si `Y != d(x)`):
  - `d(x) = 1`  → `w_i = w_i + x_i`, `θ = θ + 1`
  - `d(x) = -1` → `w_i = w_i - x_i`, `θ = θ - 1`
- **Parada:** una época completa sin errores. Límite de seguridad de **100 épocas**; si se alcanza (caso XOR) se informa que la compuerta no es linealmente separable.
- Si `w1`, `w2` o `θ` están vacíos al pulsar **Iniciar Aprendizaje**, se rellenan con valores aleatorios en `[-1, 1]`.
- Se acepta punto o coma como separador decimal.

## Requisitos

- JDK **17 o superior** (se usan `record` y `switch` con flechas).

## Compilar y generar el `.jar` (línea de comandos)

Desde la carpeta raíz del proyecto (donde está este README):

```bash
# 1. Compilar las clases en la carpeta out/
javac -encoding UTF-8 -d out src/perceptron/*.java

# 2. Empaquetar en un JAR ejecutable indicando la clase principal (e = entry point)
jar cfe Perceptron.jar perceptron.Main -C out .

# 3. Ejecutar
java -jar Perceptron.jar
```

En Windows (PowerShell / CMD) los comandos son idénticos; solo cambia el separador de rutas si escribes los archivos uno por uno: `src\perceptron\*.java`.

## Desde un IDE

- **VS Code / Antigravity IDE** (con *Extension Pack for Java*): abre la carpeta, ejecuta `Main.java` con *Run*. Para el `.jar`: panel *Java Projects* → *Export Jar…* → elige `perceptron.Main` como clase principal. O simplemente usa los comandos anteriores en la terminal integrada.
- **IntelliJ IDEA**: marca `src` como *Sources Root*. Para el `.jar`: *File → Project Structure → Artifacts → + → JAR → From modules with dependencies*, clase principal `perceptron.Main`; luego *Build → Build Artifacts → Build*.
