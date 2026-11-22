package modelo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Venta confirmada: número, fecha, detalle de platos y método de pago. */
public class Venta implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String numero;
    private final LocalDateTime fecha;
    private final List<DetalleVenta> detalles;
    private final MetodoPago metodoPago;

    public Venta(String numero, LocalDateTime fecha, List<DetalleVenta> detalles, MetodoPago metodoPago) {
        if (detalles == null || detalles.isEmpty()) {
            throw new ValidacionException("La venta debe incluir al menos un plato.");
        }
        if (metodoPago == null) throw new ValidacionException("Debe seleccionar un método de pago.");
        this.numero = numero;
        this.fecha = fecha;
        this.detalles = new ArrayList<>(detalles);
        this.metodoPago = metodoPago;
    }

    public double getSubtotal() {
        double t = 0;
        for (DetalleVenta d : detalles) t += d.getSubtotal();
        return t;
    }

    public String getNumero() { return numero; }
    public LocalDateTime getFecha() { return fecha; }
    public List<DetalleVenta> getDetalles() { return Collections.unmodifiableList(detalles); }
    public MetodoPago getMetodoPago() { return metodoPago; }

    public boolean incluyePlato(Plato p) {
        for (DetalleVenta d : detalles) if (d.getPlato() == p) return true;
        return false;
    }
}
