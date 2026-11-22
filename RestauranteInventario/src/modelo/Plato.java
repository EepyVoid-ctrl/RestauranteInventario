package modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Plato del menú con su receta, precio de venta (PVP) y estado. */
public class Plato implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String codigo;
    private String nombre;
    private double pvp;
    private boolean activo = true;
    private List<Ingrediente> receta = new ArrayList<>();

    public Plato(String codigo, String nombre, double pvp, List<Ingrediente> receta) {
        this.codigo = codigo;
        setNombre(nombre);
        setPvp(pvp);
        setReceta(receta);
    }

    /** RF10: costo de producción = suma(cantidad por porción × costo unitario). */
    public double getCostoProduccion() {
        double total = 0;
        for (Ingrediente i : receta) total += i.getCosto();
        return total;
    }

    public double getMargenUnitario() { return pvp - getCostoProduccion(); }

    /** Margen porcentual = (PVP − costo) ÷ PVP × 100. */
    public double getMargenPorcentual() { return pvp > 0 ? getMargenUnitario() / pvp * 100 : 0; }

    /** RF10: advertencia cuando el PVP no supera el costo; null si el precio es válido. */
    public String getAdvertenciaPrecio() {
        if (pvp <= getCostoProduccion() + 1e-9) {
            return String.format(java.util.Locale.US,
                "ADVERTENCIA: el PVP ($%.2f) es menor o igual al costo de producción ($%.2f). El plato genera pérdida.",
                pvp, getCostoProduccion());
        }
        return null;
    }

    public void setReceta(List<Ingrediente> receta) {
        if (receta == null || receta.isEmpty()) {
            throw new ValidacionException("El plato debe tener al menos un ingrediente en su receta.");
        }
        this.receta = new ArrayList<>(receta);
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public double getPvp() { return pvp; }
    public boolean isActivo() { return activo; }
    public List<Ingrediente> getReceta() { return Collections.unmodifiableList(receta); }

    public void setNombre(String nombre) { this.nombre = Validar.texto("Nombre del plato", nombre); }
    public void setPvp(double pvp) { this.pvp = Validar.positivo("PVP", pvp); }
    public void setActivo(boolean activo) { this.activo = activo; }

    public boolean usaInsumo(Insumo insumo) {
        for (Ingrediente i : receta) if (i.getInsumo() == insumo) return true;
        return false;
    }

    @Override
    public String toString() { return nombre; }
}
