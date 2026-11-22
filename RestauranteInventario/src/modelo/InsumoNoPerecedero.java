package modelo;

/** Insumo de larga duración (arroz, aceite, sal, enlatados...). */
public class InsumoNoPerecedero extends Insumo {
    private static final long serialVersionUID = 1L;

    public InsumoNoPerecedero(String codigo, String nombre, String categoria, String unidadMedida,
                              double cantidadComprada, double costoTotal, double stockMinimo) {
        super(codigo, nombre, categoria, unidadMedida, cantidadComprada, costoTotal, stockMinimo);
    }

    @Override
    public String getTipo() { return "No perecedero"; }

    @Override
    public String getAlertaConservacion() { return null; }
}
