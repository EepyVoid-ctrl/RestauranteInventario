package modelo;

/** Pago con tarjeta. */
public class PagoTarjeta implements MetodoPago {
    private static final long serialVersionUID = 1L;
    @Override public String getNombre() { return "Tarjeta"; }
    @Override public String getInstruccionCobro() { return "Cobrar en el datáfono y conservar el voucher."; }
    @Override public String toString() { return getNombre(); }
}
