package negocio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import modelo.*;

/** Lógica del Módulo 1: gestión de insumos e inventario (RF01 – RF05). */
public class InventarioServicio {
    private final BaseDatos db;

    public InventarioServicio(BaseDatos db) { this.db = db; }

    /** RF01: registra un insumo. Si se indica fecha de caducidad es perecedero. */
    public Insumo registrar(String nombre, String categoria, String unidad, double cantidadComprada,
                            double costoTotal, double stockMinimo, LocalDate caducidad) {
        nombre = Validar.texto("Nombre", nombre);
        categoria = Validar.texto("Categoría", categoria);
        unidad = Validar.texto("Unidad de medida", unidad);
        if (buscarPorNombre(nombre) != null) {
            throw new ValidacionException("Ya existe un insumo llamado «" + nombre + "».");
        }
        String codigo = db.codigoInsumo();
        Insumo insumo = (caducidad == null)
                ? new InsumoNoPerecedero(codigo, nombre, categoria, unidad, cantidadComprada, costoTotal, stockMinimo)
                : new InsumoPerecedero(codigo, nombre, categoria, unidad, cantidadComprada, costoTotal, stockMinimo, caducidad);
        db.insumos.add(insumo);
        db.guardar();
        return insumo;
    }

    /**
     * RF02: lista insumos filtrando por texto del nombre, categoría y estado.
     * Los criterios null o vacíos se ignoran.
     */
    public List<Insumo> consultar(String texto, String categoria, String estado) {
        List<Insumo> res = new ArrayList<>();
        for (Insumo i : db.insumos) {
            if (texto != null && !texto.isBlank()
                    && !i.getNombre().toLowerCase().contains(texto.trim().toLowerCase())) continue;
            if (categoria != null && !categoria.isBlank() && !i.getCategoria().equalsIgnoreCase(categoria)) continue;
            if (estado != null && !estado.isBlank() && !i.getEstado().equalsIgnoreCase(estado)) continue;
            res.add(i);
        }
        return res;
    }

    public List<String> categorias() {
        List<String> cats = new ArrayList<>();
        for (Insumo i : db.insumos) if (!cats.contains(i.getCategoria())) cats.add(i.getCategoria());
        java.util.Collections.sort(cats);
        return cats;
    }

    /**
     * RF03: modifica los datos de un insumo. Si cantidadComprada &gt; 0 registra además un
     * reabastecimiento (suma al stock y recalcula el costo unitario).
     */
    public Insumo actualizar(String codigo, String nombre, String categoria, double stockMinimo,
                             double cantidadComprada, double costoTotal) {
        Insumo insumo = obtener(codigo);
        nombre = Validar.texto("Nombre", nombre);
        Insumo otro = buscarPorNombre(nombre);
        if (otro != null && otro != insumo) {
            throw new ValidacionException("Ya existe un insumo llamado «" + nombre + "».");
        }
        Validar.noNegativo("Cantidad comprada", cantidadComprada);
        Validar.noNegativo("Costo total de compra", costoTotal);
        if (cantidadComprada > 0 && costoTotal <= 0) {
            throw new ValidacionException("El campo «Costo total de compra» debe ser mayor que cero.");
        }
        insumo.setNombre(nombre);
        insumo.setCategoria(categoria);
        insumo.setStockMinimo(stockMinimo);
        if (cantidadComprada > 0) insumo.reabastecer(cantidadComprada, costoTotal);
        db.guardar();
        return insumo;
    }

    /** RF04: elimina un insumo solo si ninguna receta lo usa. */
    public void eliminar(String codigo) {
        Insumo insumo = obtener(codigo);
        List<String> usados = new ArrayList<>();
        for (Plato p : db.platos) if (p.usaInsumo(insumo)) usados.add(p.getNombre());
        if (!usados.isEmpty()) {
            throw new ValidacionException("No se puede eliminar «" + insumo.getNombre()
                    + "» porque forma parte de las recetas de: " + String.join(", ", usados) + ".");
        }
        db.insumos.remove(insumo);
        db.guardar();
    }

    /** RF05: insumos cuyo stock es menor o igual al mínimo. */
    public List<Insumo> alertasReabastecimiento() {
        List<Insumo> res = new ArrayList<>();
        for (Insumo i : db.insumos) if (i.esCritico()) res.add(i);
        return res;
    }

    public Insumo obtener(String codigo) {
        for (Insumo i : db.insumos) if (i.getCodigo().equals(codigo)) return i;
        throw new ValidacionException("No existe un insumo con código " + codigo + ".");
    }

    private Insumo buscarPorNombre(String nombre) {
        for (Insumo i : db.insumos) if (i.getNombre().equalsIgnoreCase(nombre.trim())) return i;
        return null;
    }
}
