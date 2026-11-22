package interfaz;

import java.awt.*;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import modelo.*;
import negocio.InventarioServicio;
import negocio.MenuServicio;
import negocio.VentaServicio;

/** Pantalla del Módulo 3: registro de ventas, facturación e historial (RF11–RF14). */
public class PanelVentas extends JPanel implements Refrescable {
    private static final long serialVersionUID = 1L;

    private final MenuServicio menu;
    private final VentaServicio ventas;
    private final InventarioServicio inventario;

    // Nueva venta
    private final JComboBox<Plato> cmbPlato = new JComboBox<>();
    private final JSpinner spnCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private final DefaultTableModel modeloCarrito = Ui.modelo("Plato", "Cantidad", "PVP ($)", "Subtotal ($)");
    private final JTable tablaCarrito = Ui.tabla(modeloCarrito);
    private final Map<Plato, Integer> carrito = new LinkedHashMap<>();
    private final JTextField txtNombre = new JTextField(14);
    private final JTextField txtIdentificacion = new JTextField(14);
    private final JTextField txtContacto = new JTextField(14);
    private final JComboBox<MetodoPago> cmbPago = new JComboBox<>(MetodoPago.todos());
    private final JLabel lblTotales = new JLabel("Subtotal: $0.00   IVA (15%): $0.00   TOTAL: $0.00");

    // Historial
    private final JTabbedPane pestanas = new JTabbedPane();
    private final JTextField txtDesde = new JTextField(9);
    private final JTextField txtHasta = new JTextField(9);
    private final JTextField txtNumero = new JTextField(10);
    private final JTextField txtIdent = new JTextField(10);
    private final DefaultTableModel modeloHistorial = Ui.modelo("Factura", "Fecha", "Cliente", "Identificación",
            "Subtotal ($)", "IVA ($)", "Total ($)", "Pago");
    private final JTable tablaHistorial = Ui.tabla(modeloHistorial);
    private List<Factura> facturas = new java.util.ArrayList<>();

    public PanelVentas(MenuServicio menu, VentaServicio ventas, InventarioServicio inventario) {
        this.menu = menu;
        this.ventas = ventas;
        this.inventario = inventario;
        setLayout(new BorderLayout());
        pestanas.addTab("Nueva venta", panelNuevaVenta());
        pestanas.addTab("Historial de ventas y facturas", panelHistorial());
        add(pestanas, BorderLayout.CENTER);
        refrescar();
    }

    public void seleccionarPestana(int indice) { pestanas.setSelectedIndex(indice); }

    private JPanel panelNuevaVenta() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel seleccion = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        seleccion.setBorder(BorderFactory.createTitledBorder("Seleccionar plato (RF11)"));
        seleccion.add(new JLabel("Plato:")); seleccion.add(cmbPlato);
        seleccion.add(new JLabel("Porciones:")); seleccion.add(spnCantidad);
        JButton btnAgregar = Ui.boton("Agregar a la venta");
        JButton btnQuitar = Ui.boton("Quitar seleccionado");
        seleccion.add(btnAgregar); seleccion.add(btnQuitar);
        p.add(seleccion, BorderLayout.NORTH);
        p.add(new JScrollPane(tablaCarrito), BorderLayout.CENTER);

        JPanel cliente = Ui.formulario("Cliente y pago (RF13)");
        Ui.campo(cliente, 0, "Nombre", txtNombre);
        Ui.campo(cliente, 1, "Identificación", txtIdentificacion);
        Ui.campo(cliente, 2, "Contacto", txtContacto);
        Ui.campo(cliente, 3, "Método de pago *", cmbPago);
        JLabel nota = new JLabel("<html><small>Si deja nombre e identificación vacíos se factura a «Consumidor Final».</small></html>");
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 4; g.gridwidth = 2; g.anchor = GridBagConstraints.WEST;
        cliente.add(nota, g);

        JButton btnConfirmar = Ui.boton("Confirmar venta y emitir factura");
        btnConfirmar.setFont(btnConfirmar.getFont().deriveFont(Font.BOLD));
        JButton btnCancelar = Ui.boton("Cancelar venta");
        JPanel sur = new JPanel(new BorderLayout(4, 4));
        sur.add(lblTotales, BorderLayout.NORTH);
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
        fila.add(btnCancelar); fila.add(btnConfirmar);
        JPanel abajo = new JPanel(new BorderLayout());
        abajo.add(cliente, BorderLayout.CENTER);
        abajo.add(fila, BorderLayout.SOUTH);
        sur.add(abajo, BorderLayout.CENTER);
        p.add(sur, BorderLayout.SOUTH);

        btnAgregar.addActionListener(e -> agregarAlCarrito());
        btnQuitar.addActionListener(e -> quitarDelCarrito());
        btnConfirmar.addActionListener(e -> confirmar());
        btnCancelar.addActionListener(e -> { carrito.clear(); recargarCarrito(); });
        return p;
    }

    private JPanel panelHistorial() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        filtros.setBorder(BorderFactory.createTitledBorder("Consultar ventas y facturas (RF14)"));
        filtros.add(new JLabel("Desde (AAAA-MM-DD):")); filtros.add(txtDesde);
        filtros.add(new JLabel("Hasta:")); filtros.add(txtHasta);
        filtros.add(new JLabel("Nº factura:")); filtros.add(txtNumero);
        filtros.add(new JLabel("Identificación:")); filtros.add(txtIdent);
        JButton btnBuscar = Ui.boton("Buscar");
        JButton btnDetalle = Ui.boton("Ver detalle de factura");
        filtros.add(btnBuscar); filtros.add(btnDetalle);
        p.add(filtros, BorderLayout.NORTH);
        p.add(new JScrollPane(tablaHistorial), BorderLayout.CENTER);
        btnBuscar.addActionListener(e -> cargarHistorial());
        btnDetalle.addActionListener(e -> verDetalle());
        tablaHistorial.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { if (e.getClickCount() == 2) verDetalle(); }
        });
        return p;
    }

    @Override
    public void refrescar() {
        cmbPlato.removeAllItems();
        for (Plato pl : menu.platosActivos()) cmbPlato.addItem(pl);
        cargarHistorial();
    }

    private void agregarAlCarrito() {
        Plato pl = (Plato) cmbPlato.getSelectedItem();
        if (pl == null) { Ui.error(this, "No hay platos activos en el menú."); return; }
        int cant = (Integer) spnCantidad.getValue();
        carrito.merge(pl, cant, Integer::sum);
        recargarCarrito();
    }

    private void quitarDelCarrito() {
        int fila = tablaCarrito.getSelectedRow();
        if (fila < 0) { Ui.error(this, "Seleccione un plato de la venta para quitarlo."); return; }
        Plato pl = (Plato) carrito.keySet().toArray()[tablaCarrito.convertRowIndexToModel(fila)];
        carrito.remove(pl);
        recargarCarrito();
    }

    private void recargarCarrito() {
        modeloCarrito.setRowCount(0);
        double subtotal = 0;
        for (Map.Entry<Plato, Integer> e : carrito.entrySet()) {
            double sub = e.getKey().getPvp() * e.getValue();
            subtotal += sub;
            modeloCarrito.addRow(new Object[] {e.getKey().getNombre(), e.getValue(), Ui.f2(e.getKey().getPvp()), Ui.f2(sub)});
        }
        double iva = subtotal * Factura.IVA;
        lblTotales.setText("Subtotal: $" + Ui.f2(subtotal) + "   IVA (15%): $" + Ui.f2(iva) + "   TOTAL: $" + Ui.f2(subtotal + iva));
    }

    /** RF11 + RF12 + RF13: confirma la venta, descuenta el inventario y emite la factura. */
    private void confirmar() {
        try {
            String nombre = txtNombre.getText().trim();
            String ident = txtIdentificacion.getText().trim();
            Cliente cliente = (nombre.isEmpty() && ident.isEmpty())
                    ? Cliente.consumidorFinal() : new Cliente(nombre, ident, txtContacto.getText());
            Factura f = ventas.registrar(cliente, new LinkedHashMap<>(carrito), (MetodoPago) cmbPago.getSelectedItem());
            mostrarFactura(f);
            carrito.clear();
            recargarCarrito();
            txtNombre.setText(""); txtIdentificacion.setText(""); txtContacto.setText("");
            List<Insumo> criticos = inventario.alertasReabastecimiento();
            if (!criticos.isEmpty()) {
                StringBuilder sb = new StringBuilder("Insumos que requieren reabastecimiento:\n");
                for (Insumo i : criticos) sb.append("• ").append(i.getNombre()).append(" (stock ").append(Ui.f2(i.getStock()))
                        .append(' ').append(i.getUnidadMedida()).append(")\n");
                Ui.advertencia(this, sb.toString());
            }
            refrescar();
        } catch (ValidacionException ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void cargarHistorial() {
        try {
            LocalDate d = Validar.fecha("Desde", txtDesde.getText(), false);
            LocalDate h = Validar.fecha("Hasta", txtHasta.getText(), false);
            VentaServicio.validarRango(d, h);
            facturas = ventas.consultarFacturas(d, h, txtNumero.getText(), txtIdent.getText());
        } catch (ValidacionException ex) {
            Ui.error(this, ex.getMessage());
            return;
        }
        modeloHistorial.setRowCount(0);
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (Factura f : facturas) {
            modeloHistorial.addRow(new Object[] {f.getNumero(), f.getVenta().getFecha().format(fmt), f.getCliente().getNombre(),
                    f.getCliente().getIdentificacion(), Ui.f2(f.getSubtotal()), Ui.f2(f.getIva()), Ui.f2(f.getTotal()),
                    f.getVenta().getMetodoPago().getNombre()});
        }
    }

    private void verDetalle() {
        int fila = tablaHistorial.getSelectedRow();
        if (fila < 0) { Ui.error(this, "Seleccione una factura del listado."); return; }
        mostrarFactura(facturas.get(tablaHistorial.convertRowIndexToModel(fila)));
    }

    private void mostrarFactura(Factura f) {
        JTextArea area = new JTextArea(f.generarTexto() + "\n\n" + f.getVenta().getMetodoPago().getInstruccionCobro());
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Factura " + f.getNumero(), JOptionPane.PLAIN_MESSAGE);
    }
}
