package interfaz;

import java.awt.*;
import java.util.Locale;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Utilidades de interfaz compartidas por todos los paneles. */
public final class Ui {
    public static final Color PRIMARIO = new Color(0x8B1E2D);
    public static final Color FONDO = new Color(0xF7F4F1);
    public static final Color CRITICO = new Color(0xC0392B);

    private Ui() { }

    public static DefaultTableModel modelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    public static String f2(double v) { return String.format(Locale.US, "%.2f", v); }

    public static void error(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Validación", JOptionPane.ERROR_MESSAGE);
    }

    public static void info(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void advertencia(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Advertencia", JOptionPane.WARNING_MESSAGE);
    }

    public static boolean confirmar(Component padre, String mensaje) {
        return JOptionPane.showConfirmDialog(padre, mensaje, "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    /** Agrega una fila «etiqueta + campo» a un panel con GridBagLayout. */
    public static void campo(JPanel panel, int fila, String etiqueta, JComponent componente) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = fila; g.anchor = GridBagConstraints.WEST; g.insets = new Insets(3, 4, 3, 6);
        panel.add(new JLabel(etiqueta), g);
        g.gridx = 1; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        panel.add(componente, g);
    }

    public static JPanel formulario(String titulo) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder(titulo));
        return p;
    }

    public static JButton boton(String texto) {
        JButton b = new JButton(texto);
        b.setFocusPainted(false);
        return b;
    }

    /** Ajusta el ancho de las columnas y el alto de las filas de una tabla. */
    public static JTable tabla(DefaultTableModel modelo) {
        JTable t = new JTable(modelo);
        t.setRowHeight(24);
        t.setAutoCreateRowSorter(true);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setReorderingAllowed(false);
        return t;
    }
}
