package modelo;

import java.io.Serializable;

/**
 * POLIMORFISMO: interfaz común para los métodos de pago. Cada implementación
 * define su propio nombre e instrucción de cobro.
 */
public interface MetodoPago extends Serializable {
    String getNombre();

    /** Instrucción que el cajero debe seguir para este método de pago. */
    String getInstruccionCobro();

    static MetodoPago[] todos() {
        return new MetodoPago[] { new PagoEfectivo(), new PagoTarjeta(), new PagoTransferencia() };
    }
}
