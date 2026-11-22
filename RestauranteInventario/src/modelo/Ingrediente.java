package modelo;

import java.io.Serializable;

/** Línea de una receta: un insumo y la cantidad requerida por porción. */
public class Ingrediente implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Insumo insumo;
    private final double cantidadPorPorcion;

    public Ingrediente(Insumo insumo, double cantidadPorPorcion) {
        if (insumo == null) throw new ValidacionException("Debe seleccionar un insumo para el ingrediente.");
        Validar.positivo("Cantidad por porción", cantidadPorPorcion);
        this.insumo = insumo;
        this.cantidadPorPorcion = cantidadPorPorcion;
    }

    public Insumo getInsumo() { return insumo; }
    public double getCantidadPorPorcion() { return cantidadPorPorcion; }

    /** Costo de este ingrediente por porción (cantidad × costo unitario actual). */
    public double getCosto() { return cantidadPorPorcion * insumo.getCostoUnitario(); }
}
