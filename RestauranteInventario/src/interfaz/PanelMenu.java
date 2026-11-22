package interfaz;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import modelo.*;
import negocio.InventarioServicio;
import negocio.MenuServicio;

/** Pantalla del Módulo 2: platos, recetas y cálculo de costos (RF06–RF10). */
public class PanelMenu extends JPanel implements Refrescable {
    private static final long serialVersionUID = 1L;

    private final MenuServicio menu;
    private final InventarioServicio inventario;

    private final DefaultTableModel modeloPlatos = Ui.modelo("Código", "Plato", "Costo prod. ($)", "PVP ($)",
            "Margen ($)", "Margen (%)", "Estado");
    private final JTable tablaPlatos = Ui.tabla(modeloPlatos);
    private final DefaultTableModel modeloReceta = Ui.modelo("Insumo", "Cantidad por porción", "Unidad", "Costo ($)");
    private final JTable tablaReceta = Ui.tabla(modeloReceta);

    private List<Plato> visibles = new ArrayList<>();
    private final List<Ingrediente> receta = new ArrayList<>();
    private Plato seleccionado;

    private final JTextField txtBuscar = new JTextField(14);
    private final JTextField txtNombre = new JTextField(16);
    private final JTextField txtPvp = new JTextField(8);
    private final JComboBox<Insumo> cmbInsumo = new JComboBox<>();
    private final JTextField txtCantidad = new JTextField(6);
    private final JLabel lblCosto = new JLabel("Costo de producción: $0.00   |   Margen: —");
    private final JLabel lblAdvertencia = new JLabel(" ");

    public PanelMenu(MenuServicio menu, InventarioServicio inventario) {
        this.menu = menu;
        this.inventario = inventario;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Catálogo (RF07)
        JPanel buscar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        buscar.setBorder(BorderFactory.createTitledBorder("Catálogo de platos (RF07)"));
        buscar.add(new JLabel("Nombre:")); buscar.add(txtBuscar);
        JButton btnBuscar = Ui.boton("Buscar");
        JButton btnTodos = Ui.boton("Ver todos");
        buscar.add(btnBuscar); buscar.add(btnTodos);
        JPanel izq = new JPanel(new BorderLayout(4, 4));
        izq.add(buscar, BorderLayout.NORTH);
        izq.add(new JScrollPane(tablaPlatos), BorderLayout.CENTER);
        add(izq, BorderLayout.CENTER);

        // Formulario de plato + receta (RF06, RF08, RF09)
        JPanel form = Ui.formulario("Plato y receta");
        Ui.campo(form, 0, "Nombre del plato *", txtNombre);
        Ui.campo(form, 1, "PVP ($) *", txtPvp);
        JPanel agregar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        agregar.add(cmbInsumo); agregar.add(new JLabel("Cant.:")); agregar.add(txtCantidad);
        JButton btnAgregar = Ui.boton("Agregar");
        agregar.add(btnAgregar);
        JButton btnQuitar = Ui.boton("Quitar ingrediente seleccionado");
        JPanel recetaPanel = new JPanel(new BorderLayout(2, 2));
        recetaPanel.setBorder(BorderFactory.createTitledBorder("Receta (por porción)"));
        recetaPanel.add(agregar, BorderLayout.NORTH);
        JScrollPane spReceta = new JScrollPane(tablaReceta);
        spReceta.setPreferredSize(new Dimension(380, 130));
        recetaPanel.add(spReceta, BorderLayout.CENTER);
        recetaPanel.add(btnQuitar, BorderLayout.SOUTH);

        lblAdvertencia.setForeground(Ui.CRITICO);
        JPanel resumen = new JPanel(new GridLayout(0, 1));
        resumen.add(lblCosto);
        resumen.add(lblAdvertencia);

        JButton btnRegistrar = Ui.boton("Registrar plato");
        JButton btnActualizar = Ui.boton("Actualizar plato");
        JButton btnEliminar = Ui.boton("Eliminar plato");
        JButton btnLimpiar = Ui.boton("Limpiar");
        JPanel botones = new JPanel(new GridLayout(2, 2, 4, 4));
        botones.add(btnRegistrar); botones.add(btnActualizar); botones.add(btnEliminar); botones.add(btnLimpiar);

        JPanel este = new JPanel(new BorderLayout(4, 4));
        este.add(form, BorderLayout.NORTH);
        JPanel centro = new JPanel(new BorderLayout(4, 4));
        centro.add(recetaPanel, BorderLayout.CENTER);
        JPanel sur = new JPanel(new BorderLayout(4, 4));
        sur.add(resumen, BorderLayout.NORTH);
        sur.add(botones, BorderLayout.SOUTH);
        centro.add(sur, BorderLayout.SOUTH);
        este.add(centro, BorderLayout.CENTER);
        este.setPreferredSize(new Dimension(430, 100));
        add(este, BorderLayout.EAST);

        btnBuscar.addActionListener(e -> cargarTabla());
        btnTodos.addActionListener(e -> { txtBuscar.setText(""); cargarTabla(); });
        btnAgregar.addActionListener(e -> agregarIngrediente());
        btnQuitar.addActionListener(e -> quitarIngrediente());
        btnRegistrar.addActionListener(e -> registrar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiar());
        txtPvp.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { actualizarResumen(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { actualizarResumen(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { actualizarResumen(); }
        });
        tablaPlatos.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) alSeleccionar(); });
        refrescar();
    }

    @Override
    public void refrescar() {
        cmbInsumo.removeAllItems();
        for (Insumo i : inventario.consultar(null, null, null)) cmbInsumo.addItem(i);
        cargarTabla();
    }

    private void cargarTabla() {
        visibles = menu.consultar(txtBuscar.getText());
        modeloPlatos.setRowCount(0);
        for (Plato p : visibles) {
            modeloPlatos.addRow(new Object[] {p.getCodigo(), p.getNombre(), Ui.f2(p.getCostoProduccion()), Ui.f2(p.getPvp()),
                    Ui.f2(p.getMargenUnitario()), Ui.f2(p.getMargenPorcentual()), p.isActivo() ? "Activo" : "Inactivo"});
        }
    }

    private void alSeleccionar() {
        int fila = tablaPlatos.getSelectedRow();
        if (fila < 0) return;
        seleccionado = visibles.get(tablaPlatos.convertRowIndexToModel(fila));
        txtNombre.setText(seleccionado.getNombre());
        txtPvp.setText(Ui.f2(seleccionado.getPvp()));
        receta.clear();
        receta.addAll(seleccionado.getReceta());
        recargarReceta();
    }

    private void agregarIngrediente() {
        try {
            Insumo insumo = (Insumo) cmbInsumo.getSelectedItem();
            if (insumo == null) throw new ValidacionException("No hay insumos registrados. Registre insumos primero.");
            double cant = Validar.numero("Cantidad por porción", txtCantidad.getText());
            Ingrediente nuevo = new Ingrediente(insumo, cant);
            receta.removeIf(i -> i.getInsumo() == insumo);
            receta.add(nuevo);
            txtCantidad.setText("");
            recargarReceta();
        } catch (ValidacionException ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void quitarIngrediente() {
        int fila = tablaReceta.getSelectedRow();
        if (fila < 0) { Ui.error(this, "Seleccione un ingrediente de la receta para quitarlo."); return; }
        receta.remove(tablaReceta.convertRowIndexToModel(fila));
        recargarReceta();
    }

    private void recargarReceta() {
        modeloReceta.setRowCount(0);
        for (Ingrediente i : receta) {
            modeloReceta.addRow(new Object[] {i.getInsumo().getNombre(), Ui.f2(i.getCantidadPorPorcion()),
                    i.getInsumo().getUnidadMedida(), Ui.f2(i.getCosto())});
        }
        actualizarResumen();
    }

    /** RF10: muestra el costo de producción, el margen y la advertencia de precio en tiempo real. */
    private void actualizarResumen() {
        double costo = 0;
        for (Ingrediente i : receta) costo += i.getCosto();
        double pvp;
        try { pvp = Validar.numero("PVP", txtPvp.getText()); } catch (ValidacionException e) { pvp = 0; }
        if (pvp > 0) {
            lblCosto.setText("Costo de producción: $" + Ui.f2(costo) + "   |   Margen: $" + Ui.f2(pvp - costo)
                    + " (" + Ui.f2((pvp - costo) / pvp * 100) + " %)");
            lblAdvertencia.setText(pvp <= costo + 1e-9 ? "⚠ El PVP es menor o igual al costo: el plato genera pérdida." : " ");
        } else {
            lblCosto.setText("Costo de producción: $" + Ui.f2(costo) + "   |   Margen: —");
            lblAdvertencia.setText(" ");
        }
    }

    private void registrar() {
        try {
            double pvp = Validar.numero("PVP", txtPvp.getText());
            Plato p = menu.registrar(txtNombre.getText(), pvp, receta);
            String adv = p.getAdvertenciaPrecio();
            Ui.info(this, "Plato registrado con código " + p.getCodigo() + ".\nCosto de producción: $"
                    + Ui.f2(p.getCostoProduccion()) + " — PVP: $" + Ui.f2(p.getPvp()));
            if (adv != null) Ui.advertencia(this, adv);
            limpiar();
            refrescar();
        } catch (ValidacionException ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void actualizar() {
        if (seleccionado == null) { Ui.error(this, "Seleccione un plato de la tabla para actualizarlo."); return; }
        try {
            double pvp = Validar.numero("PVP", txtPvp.getText());
            Plato p = menu.actualizar(seleccionado.getCodigo(), txtNombre.getText(), pvp, receta);
            Ui.info(this, "Plato actualizado. Costo de producción: $" + Ui.f2(p.getCostoProduccion()) + ".");
            if (p.getAdvertenciaPrecio() != null) Ui.advertencia(this, p.getAdvertenciaPrecio());
            limpiar();
            refrescar();
        } catch (ValidacionException ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void eliminar() {
        if (seleccionado == null) { Ui.error(this, "Seleccione un plato de la tabla para eliminarlo."); return; }
        if (!Ui.confirmar(this, "¿Eliminar el plato «" + seleccionado.getNombre() + "»?")) return;
        try {
            Ui.info(this, menu.eliminar(seleccionado.getCodigo()));
            limpiar();
            refrescar();
        } catch (ValidacionException ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void limpiar() {
        seleccionado = null;
        txtNombre.setText(""); txtPvp.setText(""); txtCantidad.setText("");
        receta.clear();
        tablaPlatos.clearSelection();
        recargarReceta();
    }
}
