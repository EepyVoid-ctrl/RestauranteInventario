package interfaz;

import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import modelo.*;
import negocio.InventarioServicio;
import negocio.ReporteServicio;
import negocio.ReporteServicio.FilaConsumo;
import negocio.ReporteServicio.FilaRentabilidad;

/** Pantalla del Módulo 4: reportes de rentabilidad por plato y consumo de insumos (RF15–RF16). */
public class PanelReportes extends JPanel implements Refrescable {
    private static final long serialVersionUID = 1L;

    private final ReporteServicio reportes;
    private final InventarioServicio inventario;

    private final JTextField txtDesde = new JTextField(LocalDate.now().minusDays(30).toString(), 9);
    private final JTextField txtHasta = new JTextField(LocalDate.now().toString(), 9);
    private final JComboBox<String> cmbCategoria = new JComboBox<>();
    private final DefaultTableModel modeloRent = Ui.modelo("Plato", "Costo ($)", "PVP ($)", "Margen unit. ($)",
            "Margen (%)", "Unidades vendidas", "Utilidad total ($)");
    private final DefaultTableModel modeloCons = Ui.modelo("Insumo", "Categoría", "Unidad", "Consumo total",
            "Consumo diario prom.", "Stock actual", "Días de cobertura");
    private final GraficoBarras grafico = new GraficoBarras();

    public PanelReportes(ReporteServicio reportes, InventarioServicio inventario) {
        this.reportes = reportes;
        this.inventario = inventario;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        filtros.setBorder(BorderFactory.createTitledBorder("Período del reporte"));
        filtros.add(new JLabel("Desde (AAAA-MM-DD):")); filtros.add(txtDesde);
        filtros.add(new JLabel("Hasta:")); filtros.add(txtHasta);
        filtros.add(new JLabel("Categoría de insumo:")); filtros.add(cmbCategoria);
        JButton btnGenerar = Ui.boton("Generar reportes");
        filtros.add(btnGenerar);
        add(filtros, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        JPanel rent = new JPanel(new BorderLayout(4, 4));
        rent.add(new JScrollPane(Ui.tabla(modeloRent)), BorderLayout.CENTER);
        grafico.setPreferredSize(new Dimension(100, 220));
        rent.add(grafico, BorderLayout.SOUTH);
        tabs.addTab("Rentabilidad por plato (RF15)", rent);
        tabs.addTab("Consumo y rotación de insumos (RF16)", new JScrollPane(Ui.tabla(modeloCons)));
        add(tabs, BorderLayout.CENTER);

        btnGenerar.addActionListener(e -> generar());
        refrescar();
        generar();
    }

    @Override
    public void refrescar() {
        Object sel = cmbCategoria.getSelectedItem();
        cmbCategoria.removeAllItems();
        cmbCategoria.addItem("Todas");
        for (String c : inventario.categorias()) cmbCategoria.addItem(c);
        if (sel != null) cmbCategoria.setSelectedItem(sel);
    }

    private void generar() {
        try {
            LocalDate d = Validar.fecha("Desde", txtDesde.getText(), true);
            LocalDate h = Validar.fecha("Hasta", txtHasta.getText(), true);
            String cat = (String) cmbCategoria.getSelectedItem();
            List<FilaRentabilidad> rent = reportes.rentabilidad(d, h);
            List<FilaConsumo> cons = reportes.consumo(d, h, "Todas".equals(cat) ? null : cat);

            modeloRent.setRowCount(0);
            List<String> etiquetas = new ArrayList<>();
            List<Double> valores = new ArrayList<>();
            for (FilaRentabilidad f : rent) {
                modeloRent.addRow(new Object[] {f.plato(), Ui.f2(f.costo()), Ui.f2(f.pvp()), Ui.f2(f.margenUnitario()),
                        Ui.f2(f.margenPorcentual()), f.unidades(), Ui.f2(f.utilidadTotal())});
                etiquetas.add(f.plato());
                valores.add(f.utilidadTotal());
            }
            grafico.setDatos("Utilidad total por plato ($)", etiquetas, valores);

            modeloCons.setRowCount(0);
            for (FilaConsumo f : cons) {
                modeloCons.addRow(new Object[] {f.insumo(), f.categoria(), f.unidad(), Ui.f2(f.consumoTotal()),
                        Ui.f2(f.consumoDiario()), Ui.f2(f.stockActual()),
                        Double.isInfinite(f.diasCobertura()) ? "Sin consumo" : Ui.f2(f.diasCobertura())});
            }
            if (rent.isEmpty()) Ui.info(this, "No hay ventas registradas en el período seleccionado.");
        } catch (ValidacionException ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    /** Gráfico de barras sencillo dibujado con Java2D. */
    static class GraficoBarras extends JPanel {
        private static final long serialVersionUID = 1L;
        private String titulo = "";
        private List<String> etiquetas = new ArrayList<>();
        private List<Double> valores = new ArrayList<>();

        void setDatos(String titulo, List<String> etiquetas, List<Double> valores) {
            this.titulo = titulo; this.etiquetas = etiquetas; this.valores = valores; repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.DARK_GRAY);
            g.drawString(titulo, 10, 16);
            if (valores.isEmpty()) { g.drawString("Sin datos para graficar.", 10, 40); return; }
            double max = 0;
            for (double v : valores) max = Math.max(max, v);
            if (max <= 0) max = 1;
            int n = valores.size();
            int ancho = getWidth() - 40, alto = getHeight() - 60;
            int paso = ancho / n;
            int barra = Math.min(70, paso - 16);
            for (int i = 0; i < n; i++) {
                int h = (int) (alto * valores.get(i) / max);
                int x = 30 + i * paso + (paso - barra) / 2;
                int y = getHeight() - 28 - h;
                g.setColor(Ui.PRIMARIO);
                g.fillRect(x, y, barra, h);
                g.setColor(Color.DARK_GRAY);
                String val = Ui.f2(valores.get(i));
                g.drawString(val, x + (barra - g.getFontMetrics().stringWidth(val)) / 2, y - 3);
                String et = etiquetas.get(i);
                g.drawString(et, x + (barra - g.getFontMetrics().stringWidth(et)) / 2, getHeight() - 10);
            }
            g.drawLine(25, getHeight() - 28, getWidth() - 10, getHeight() - 28);
        }
    }
}
