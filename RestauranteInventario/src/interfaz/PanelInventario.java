package interfaz;

import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import modelo.*;
import negocio.InventarioServicio;

/** Pantalla del Módulo 1: registro, consulta, actualización y eliminación de insumos (RF01–RF05). */
public class PanelInventario extends JPanel implements Refrescable {
    private static final long serialVersionUID = 1L;

    private final InventarioServicio servicio;
    private final DefaultTableModel modelo = Ui.modelo("Código", "Nombre", "Categoría", "Tipo", "Stock",
            "Unidad", "Stock mín.", "Costo unit. ($)", "Estado", "Alerta");
    private final JTable tabla = Ui.tabla(modelo);
    private List<Insumo> visibles = new java.util.ArrayList<>();

    private final JTextField txtBuscar = new JTextField(12);
    private final JComboBox<String> cmbFiltroCategoria = new JComboBox<>();
    private final JComboBox<String> cmbFiltroEstado = new JComboBox<>(new String[] {"Todos", "Normal", "Crítico"});

    private final JTextField txtNombre = new JTextField(14);
    private final JComboBox<String> cmbCategoria = new JComboBox<>(new String[] {
            "Carnes", "Lácteos", "Vegetales", "Granos", "Despensa", "Bebidas"});
    private final JComboBox<String> cmbUnidad = new JComboBox<>(new String[] {"kg", "g", "l", "ml", "unidad"});
    private final JTextField txtCantidad = new JTextField(8);
    private final JTextField txtCosto = new JTextField(8);
    private final JTextField txtMinimo = new JTextField(8);
    private final JTextField txtCaducidad = new JTextField(8);
    private final JLabel lblCostoUnit = new JLabel("Costo unitario: —");
    private final JLabel lblAlertas = new JLabel(" ");

    public PanelInventario(InventarioServicio servicio) {
        this.servicio = servicio;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        cmbCategoria.setEditable(true);
        txtCaducidad.setToolTipText("AAAA-MM-DD. Dejar vacío si el insumo no es perecedero.");

        // Filtros (RF02)
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        filtros.setBorder(BorderFactory.createTitledBorder("Buscar y filtrar (RF02)"));
        filtros.add(new JLabel("Nombre:")); filtros.add(txtBuscar);
        filtros.add(new JLabel("Categoría:")); filtros.add(cmbFiltroCategoria);
        filtros.add(new JLabel("Estado:")); filtros.add(cmbFiltroEstado);
        JButton btnBuscar = Ui.boton("Buscar");
        JButton btnTodos = Ui.boton("Ver todos");
        filtros.add(btnBuscar); filtros.add(btnTodos);

        lblAlertas.setForeground(Ui.CRITICO);
        lblAlertas.setFont(lblAlertas.getFont().deriveFont(Font.BOLD));
        JPanel norte = new JPanel(new BorderLayout());
        norte.add(filtros, BorderLayout.CENTER);
        norte.add(lblAlertas, BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH);

        // Tabla
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                String estado = String.valueOf(t.getValueAt(r, 8));
                comp.setForeground(!sel && "Crítico".equals(estado) ? Ui.CRITICO : t.getForeground());
                return comp;
            }
        });
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // Formulario (RF01, RF03, RF04)
        JPanel form = Ui.formulario("Datos del insumo");
        Ui.campo(form, 0, "Nombre *", txtNombre);
        Ui.campo(form, 1, "Categoría *", cmbCategoria);
        Ui.campo(form, 2, "Unidad de medida *", cmbUnidad);
        Ui.campo(form, 3, "Cantidad comprada *", txtCantidad);
        Ui.campo(form, 4, "Costo total de compra ($) *", txtCosto);
        Ui.campo(form, 5, "Stock mínimo *", txtMinimo);
        Ui.campo(form, 6, "Caducidad (AAAA-MM-DD)", txtCaducidad);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 7; g.gridwidth = 2; g.anchor = GridBagConstraints.WEST; g.insets = new Insets(4, 4, 4, 4);
        form.add(lblCostoUnit, g);

        JButton btnRegistrar = Ui.boton("Registrar insumo");
        JButton btnActualizar = Ui.boton("Actualizar / Reabastecer");
        JButton btnEliminar = Ui.boton("Eliminar");
        JButton btnLimpiar = Ui.boton("Limpiar");
        JPanel botones = new JPanel(new GridLayout(0, 1, 4, 4));
        botones.add(btnRegistrar); botones.add(btnActualizar); botones.add(btnEliminar); botones.add(btnLimpiar);
        JLabel ayuda = new JLabel("<html><small>Para reabastecer, seleccione un insumo e ingrese la cantidad y el costo de la nueva compra.</small></html>");

        JPanel este = new JPanel(new BorderLayout(4, 4));
        este.add(form, BorderLayout.NORTH);
        JPanel abajo = new JPanel(new BorderLayout(4, 4));
        abajo.add(botones, BorderLayout.NORTH);
        abajo.add(ayuda, BorderLayout.CENTER);
        este.add(abajo, BorderLayout.CENTER);
        este.setPreferredSize(new Dimension(330, 100));
        add(este, BorderLayout.EAST);

        btnBuscar.addActionListener(e -> cargarTabla());
        btnTodos.addActionListener(e -> {
            txtBuscar.setText(""); cmbFiltroCategoria.setSelectedIndex(0); cmbFiltroEstado.setSelectedIndex(0);
            cargarTabla();
        });
        btnRegistrar.addActionListener(e -> registrar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiar());
        tabla.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) alSeleccionar(); });
        refrescar();
    }

    @Override
    public void refrescar() {
        Object sel = cmbFiltroCategoria.getSelectedItem();
        cmbFiltroCategoria.removeAllItems();
        cmbFiltroCategoria.addItem("Todas");
        for (String c : servicio.categorias()) cmbFiltroCategoria.addItem(c);
        if (sel != null) cmbFiltroCategoria.setSelectedItem(sel);
        cargarTabla();
    }

    private void cargarTabla() {
        String cat = (String) cmbFiltroCategoria.getSelectedItem();
        String est = (String) cmbFiltroEstado.getSelectedItem();
        visibles = servicio.consultar(txtBuscar.getText(), "Todas".equals(cat) ? null : cat, "Todos".equals(est) ? null : est);
        modelo.setRowCount(0);
        for (Insumo i : visibles) {
            String alerta = i.getAlertaConservacion();
            modelo.addRow(new Object[] {i.getCodigo(), i.getNombre(), i.getCategoria(), i.getTipo(),
                    Ui.f2(i.getStock()), i.getUnidadMedida(), Ui.f2(i.getStockMinimo()), Ui.f2(i.getCostoUnitario()),
                    i.getEstado(), alerta == null ? "" : alerta});
        }
        if (visibles.isEmpty()) {
            lblAlertas.setText("No se encontraron insumos que coincidan con los criterios.");
        } else {
            actualizarAlertas();
        }
    }

    /** RF05: muestra la lista de insumos que requieren reabastecimiento. */
    private void actualizarAlertas() {
        List<Insumo> criticos = servicio.alertasReabastecimiento();
        if (criticos.isEmpty()) {
            lblAlertas.setText(" ");
        } else {
            StringBuilder sb = new StringBuilder("⚠ Requieren reabastecimiento: ");
            for (int i = 0; i < criticos.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(criticos.get(i).getNombre());
            }
            lblAlertas.setText(sb.toString());
        }
    }

    private void alSeleccionar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        Insumo i = visibles.get(tabla.convertRowIndexToModel(fila));
        txtNombre.setText(i.getNombre());
        cmbCategoria.setSelectedItem(i.getCategoria());
        cmbUnidad.setSelectedItem(i.getUnidadMedida());
        txtMinimo.setText(Ui.f2(i.getStockMinimo()));
        txtCantidad.setText("");
        txtCosto.setText("");
        txtCaducidad.setText(i instanceof InsumoPerecedero ? ((InsumoPerecedero) i).getFechaCaducidad().toString() : "");
        lblCostoUnit.setText("Costo unitario actual: $" + Ui.f2(i.getCostoUnitario()) + " por " + i.getUnidadMedida());
    }

    private void registrar() {
        try {
            double cant = Validar.numero("Cantidad comprada", txtCantidad.getText());
            double costo = Validar.numero("Costo total de compra", txtCosto.getText());
            double min = Validar.numero("Stock mínimo", txtMinimo.getText());
            LocalDate cad = Validar.fecha("Caducidad", txtCaducidad.getText(), false);
            Insumo i = servicio.registrar(txtNombre.getText(), String.valueOf(cmbCategoria.getSelectedItem()),
                    String.valueOf(cmbUnidad.getSelectedItem()), cant, costo, min, cad);
            Ui.info(this, "Insumo registrado con código " + i.getCodigo() + ".\nCosto unitario: $"
                    + Ui.f2(i.getCostoUnitario()) + " por " + i.getUnidadMedida() + ".");
            limpiar();
            refrescar();
        } catch (ValidacionException ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void actualizar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { Ui.error(this, "Seleccione un insumo de la tabla para actualizarlo."); return; }
        Insumo sel = visibles.get(tabla.convertRowIndexToModel(fila));
        try {
            double min = Validar.numero("Stock mínimo", txtMinimo.getText());
            double cant = txtCantidad.getText().isBlank() ? 0 : Validar.numero("Cantidad comprada", txtCantidad.getText());
            double costo = txtCosto.getText().isBlank() ? 0 : Validar.numero("Costo total de compra", txtCosto.getText());
            Insumo i = servicio.actualizar(sel.getCodigo(), txtNombre.getText(),
                    String.valueOf(cmbCategoria.getSelectedItem()), min, cant, costo);
            Ui.info(this, "Insumo actualizado. Stock: " + Ui.f2(i.getStock()) + " " + i.getUnidadMedida()
                    + " — Costo unitario: $" + Ui.f2(i.getCostoUnitario()) + ".");
            limpiar();
            refrescar();
        } catch (ValidacionException ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void eliminar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { Ui.error(this, "Seleccione un insumo de la tabla para eliminarlo."); return; }
        Insumo sel = visibles.get(tabla.convertRowIndexToModel(fila));
        if (!Ui.confirmar(this, "¿Eliminar el insumo «" + sel.getNombre() + "»?")) return;
        try {
            servicio.eliminar(sel.getCodigo());
            Ui.info(this, "Insumo eliminado del inventario.");
            limpiar();
            refrescar();
        } catch (ValidacionException ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void limpiar() {
        txtNombre.setText(""); txtCantidad.setText(""); txtCosto.setText(""); txtMinimo.setText("");
        txtCaducidad.setText(""); lblCostoUnit.setText("Costo unitario: —");
        tabla.clearSelection();
    }

    // Usados por las pruebas con capturas de pantalla
    public void rellenarFormulario(String nombre, String cantidad, String costo, String minimo) {
        txtNombre.setText(nombre); txtCantidad.setText(cantidad); txtCosto.setText(costo); txtMinimo.setText(minimo);
    }
}
