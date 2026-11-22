package interfaz;

import java.nio.file.Path;
import javax.swing.*;

import negocio.BaseDatos;
import negocio.DatosDemo;

/**
 * Punto de entrada del sistema.
 * <ul>
 *   <li>Sin argumentos: carga/crea la base en {@code datos/restaurante.dat}; si está vacía, carga datos de ejemplo.</li>
 *   <li>{@code --vacio}: inicia sin datos de ejemplo.</li>
 * </ul>
 */
public class Main {
    public static void main(String[] args) {
        BaseDatos db = BaseDatos.cargar(Path.of("datos", "restaurante.dat"));
        boolean vacio = args.length > 0 && args[0].equals("--vacio");
        if (db.estaVacia() && !vacio) DatosDemo.cargar(db);
        SwingUtilities.invokeLater(() -> {
            try {
                for (UIManager.LookAndFeelInfo i : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(i.getName())) { UIManager.setLookAndFeel(i.getClassName()); break; }
                }
            } catch (Exception ignorada) { /* se usa el aspecto por defecto */ }
            new VentanaPrincipal(db).setVisible(true);
        });
    }
}
