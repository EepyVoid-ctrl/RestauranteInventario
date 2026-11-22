package modelo;

import java.io.Serializable;
import java.util.Map;

/**
 * Línea de una venta. Guarda una "foto" del precio, el costo y el consumo de
 * insumos al momento de vender, para que los reportes históricos no cambien
 * si luego se modifican precios o recetas.
 */
public class DetalleVenta implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Plato plato;
    private final String nombrePlato;
    private final int cantidad;
    private final double precioUnitario;
    private final double costoUnitario;
    private final Map<String, Double> consumoInsumos; // código de insumo -> cantidad total

    public DetalleVenta(Plato plato, int cantidad, Map<String, Double> consumoInsumos) {
        if (cantidad <= 0) throw new ValidacionException("La cantidad de porciones debe ser mayor que cero.");
        this.plato = plato;
        this.nombrePlato = plato.getNombre();
        this.cantidad = cantidad;
        this.precioUnitario = plato.getPvp();
        this.costoUnitario = plato.getCostoProduccion();
        this.consumoInsumos = consumoInsumos;
    }

    public Plato getPlato() { return plato; }
    public String getNombrePlato() { return nombrePlato; }
    public int getCantidad() { return cantidad; }
    public double getPrecioUnitario() { return precioUnitario; }
    public double getCostoUnitario() { return costoUnitario; }
    public Map<String, Double> getConsumoInsumos() { return consumoInsumos; }
    public double getSubtotal() { return precioUnitario * cantidad; }
    public double getUtilidad() { return (precioUnitario - costoUnitario) * cantidad; }
}
