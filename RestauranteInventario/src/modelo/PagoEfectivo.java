package modelo;

/** Pago en efectivo. */
public class PagoEfectivo implements MetodoPago {
    private static final long serialVersionUID = 1L;
    @Override public String getNombre() { return "Efectivo"; }
    @Override public String getInstruccionCobro() { return "Recibir el efectivo y entregar el cambio."; }
    @Override public String toString() { return getNombre(); }
}
