package modelo;

import java.io.Serializable;

/**
 * Clase abstracta que representa una materia prima del restaurante.
 * <p>
 * ENCAPSULAMIENTO: todos los atributos son privados y el stock solo puede
 * cambiar mediante {@link #reabastecer}, {@link #descontar} o la compra inicial,
 * lo que garantiza que nunca quede negativo.<br>
 * HERENCIA/POLIMORFISMO: las subclases {@link InsumoPerecedero} e
 * {@link InsumoNoPerecedero} implementan {@link #getTipo()} y
 * {@link #getAlertaConservacion()} de forma distinta.
 */
public abstract class Insumo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String codigo;
    private String nombre;
    private String categoria;
    private String unidadMedida;
    private double stock;
    private double stockMinimo;
    private double costoUnitario;

    protected Insumo(String codigo, String nombre, String categoria, String unidadMedida,
                     double cantidadComprada, double costoTotal, double stockMinimo) {
        this.codigo = codigo;
        setNombre(nombre);
        setCategoria(categoria);
        setUnidadMedida(unidadMedida);
        setStockMinimo(stockMinimo);
        Validar.positivo("Cantidad comprada", cantidadComprada);
        Validar.positivo("Costo total de compra", costoTotal);
        this.stock = cantidadComprada;
        this.costoUnitario = costoTotal / cantidadComprada; // RF01
    }

    /**
     * RF03: suma lo comprado al stock y recalcula el costo unitario como
     * costo promedio ponderado entre el stock existente y la nueva compra.
     */
    public void reabastecer(double cantidad, double costoTotal) {
        Validar.positivo("Cantidad comprada", cantidad);
        Validar.positivo("Costo total de compra", costoTotal);
        double valorExistente = stock * costoUnitario;
        stock += cantidad;
        costoUnitario = (valorExistente + costoTotal) / stock;
    }

    /** RF12: descuenta stock; rechaza la operación si no alcanza. */
    public void descontar(double cantidad) {
        Validar.noNegativo("Cantidad a descontar", cantidad);
        if (cantidad > stock + 1e-9) {
            throw new ValidacionException("Stock insuficiente de " + nombre + ".");
        }
        stock = Math.max(0, stock - cantidad);
    }

    /** RF05: crítico cuando el stock actual es menor o igual al mínimo. */
    public boolean esCritico() {
        return stock <= stockMinimo + 1e-9;
    }

    public String getEstado() {
        return esCritico() ? "Crítico" : "Normal";
    }

    /** Tipo de insumo (polimórfico). */
    public abstract String getTipo();

    /** Alerta propia del tipo de insumo, o null si no hay (polimórfico). */
    public abstract String getAlertaConservacion();

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getCategoria() { return categoria; }
    public String getUnidadMedida() { return unidadMedida; }
    public double getStock() { return stock; }
    public double getStockMinimo() { return stockMinimo; }
    public double getCostoUnitario() { return costoUnitario; }

    public void setNombre(String nombre) { this.nombre = Validar.texto("Nombre", nombre); }
    public void setCategoria(String categoria) { this.categoria = Validar.texto("Categoría", categoria); }
    public void setUnidadMedida(String u) { this.unidadMedida = Validar.texto("Unidad de medida", u); }
    public void setStockMinimo(double m) { this.stockMinimo = Validar.noNegativo("Stock mínimo", m); }

    @Override
    public String toString() {
        return nombre + " (" + unidadMedida + ")";
    }
}
