package modelo;

/** Pago por transferencia bancaria. */
public class PagoTransferencia implements MetodoPago {
    private static final long serialVersionUID = 1L;
    @Override public String getNombre() { return "Transferencia"; }
    @Override public String getInstruccionCobro() { return "Verificar el comprobante de la transferencia."; }
    @Override public String toString() { return getNombre(); }
}
