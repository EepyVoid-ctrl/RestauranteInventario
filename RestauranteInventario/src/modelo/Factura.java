package modelo;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Factura emitida al confirmar una venta (RF13). */
public class Factura implements Serializable {
    private static final long serialVersionUID = 1L;
    /** Tasa de IVA vigente en Ecuador (15 %). */
    public static final double IVA = 0.15;

    private final String numero;
    private final Venta venta;
    private final Cliente cliente;

    public Factura(String numero, Venta venta, Cliente cliente) {
        this.numero = numero;
        this.venta = venta;
        this.cliente = cliente;
    }

    public String getNumero() { return numero; }
    public Venta getVenta() { return venta; }
    public Cliente getCliente() { return cliente; }
    public double getSubtotal() { return venta.getSubtotal(); }
    public double getIva() { return getSubtotal() * IVA; }
    public double getTotal() { return getSubtotal() + getIva(); }

    /** Representación en texto de la factura para mostrarla en pantalla. */
    public String generarTexto() {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        sb.append("FACTURA Nº ").append(numero).append('\n');
        sb.append("Venta: ").append(venta.getNumero()).append("   Fecha: ").append(venta.getFecha().format(f)).append('\n');
        sb.append("Cliente: ").append(cliente.getNombre()).append('\n');
        sb.append("Identificación: ").append(cliente.getIdentificacion());
        if (!cliente.getContacto().isEmpty()) sb.append("   Contacto: ").append(cliente.getContacto());
        sb.append("\n\n");
        sb.append(String.format(Locale.US, "%-24s %5s %10s %10s%n", "Plato", "Cant.", "P. unit.", "Subtotal"));
        for (DetalleVenta d : venta.getDetalles()) {
            sb.append(String.format(Locale.US, "%-24s %5d %10.2f %10.2f%n",
                    d.getNombrePlato(), d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal()));
        }
        sb.append('\n');
        sb.append(String.format(Locale.US, "Subtotal:        $%.2f%n", getSubtotal()));
        sb.append(String.format(Locale.US, "IVA (%d%%):        $%.2f%n", Math.round(IVA * 100), getIva()));
        sb.append(String.format(Locale.US, "TOTAL A PAGAR:   $%.2f%n", getTotal()));
        sb.append("Método de pago: ").append(venta.getMetodoPago().getNombre());
        return sb.toString();
    }
}
