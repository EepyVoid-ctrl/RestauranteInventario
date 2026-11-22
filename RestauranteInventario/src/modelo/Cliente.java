package modelo;

import java.io.Serializable;

/** Cliente al que se emite la factura. */
public class Cliente implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String nombre;
    private final String identificacion;
    private final String contacto;

    public Cliente(String nombre, String identificacion, String contacto) {
        this.nombre = Validar.texto("Nombre del cliente", nombre);
        this.identificacion = Validar.texto("Identificación del cliente", identificacion);
        this.contacto = contacto == null ? "" : contacto.trim();
    }

    public static Cliente consumidorFinal() {
        return new Cliente("Consumidor Final", "9999999999999", "");
    }

    public String getNombre() { return nombre; }
    public String getIdentificacion() { return identificacion; }
    public String getContacto() { return contacto; }
}
