package interfaz;

import java.awt.*;
import javax.swing.*;

import negocio.*;

/** Ventana principal: menú de navegación por pestañas que une los cuatro módulos. */
public class VentanaPrincipal extends JFrame {
    private static final long serialVersionUID = 1L;

    private final PanelInventario panelInventario;
    private final PanelMenu panelMenu;
    private final PanelVentas panelVentas;
    private final PanelReportes panelReportes;
    private final JTabbedPane pestanas = new JTabbedPane();

    public VentanaPrincipal(BaseDatos db) {
        super("Sistema de Inventario y Ventas para Restaurantes");
        InventarioServicio inv = new InventarioServicio(db);
        MenuServicio menu = new MenuServicio(db);
        VentaServicio ventas = new VentaServicio(db);
        ReporteServicio reportes = new ReporteServicio(db);

        panelInventario = new PanelInventario(inv);
        panelMenu = new PanelMenu(menu, inv);
        panelVentas = new PanelVentas(menu, ventas, inv);
        panelReportes = new PanelReportes(reportes, inv);

        pestanas.addTab("1. Inventario", panelInventario);
        pestanas.addTab("2. Menú y recetas", panelMenu);
        pestanas.addTab("3. Ventas y facturación", panelVentas);
        pestanas.addTab("4. Reportes", panelReportes);
        pestanas.addChangeListener(e -> {
            Component c = pestanas.getSelectedComponent();
            if (c instanceof Refrescable) ((Refrescable) c).refrescar();
        });

        JLabel encabezado = new JLabel("  Restaurante — Control de inventario, recetas y ventas");
        encabezado.setOpaque(true);
        encabezado.setBackground(Ui.PRIMARIO);
        encabezado.setForeground(Color.WHITE);
        encabezado.setFont(encabezado.getFont().deriveFont(Font.BOLD, 16f));
        encabezado.setBorder(BorderFactory.createEmptyBorder(10, 6, 10, 6));

        setLayout(new BorderLayout());
        add(encabezado, BorderLayout.NORTH);
        add(pestanas, BorderLayout.CENTER);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1150, 700);
        setLocationRelativeTo(null);
    }

    public void seleccionarPestana(int indice) { pestanas.setSelectedIndex(indice); }
}
