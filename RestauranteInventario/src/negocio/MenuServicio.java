package negocio;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import modelo.*;

/** Lógica del Módulo 2: ingeniería de menú y recetas (RF06 – RF10). */
public class MenuServicio {
    private final BaseDatos db;

    public MenuServicio(BaseDatos db) { this.db = db; }

    /** RF06: crea un plato con su receta y PVP. La advertencia de precio (RF10) la entrega el propio plato. */
    public Plato registrar(String nombre, double pvp, List<Ingrediente> receta) {
        nombre = Validar.texto("Nombre del plato", nombre);
        if (buscarPorNombre(nombre) != null) {
            throw new ValidacionException("Ya existe un plato llamado «" + nombre + "».");
        }
        validarReceta(receta);
        Plato plato = new Plato(db.codigoPlato(), nombre, pvp, receta);
        db.platos.add(plato);
        db.guardar();
        return plato;
    }

    /** RF07: consulta el catálogo con filtro opcional por nombre. */
    public List<Plato> consultar(String texto) {
        List<Plato> res = new ArrayList<>();
        for (Plato p : db.platos) {
            if (texto != null && !texto.isBlank()
                    && !p.getNombre().toLowerCase().contains(texto.trim().toLowerCase())) continue;
            res.add(p);
        }
        return res;
    }

    public List<Plato> platosActivos() {
        List<Plato> res = new ArrayList<>();
        for (Plato p : db.platos) if (p.isActivo()) res.add(p);
        return res;
    }

    /** RF08: modifica nombre, PVP e ingredientes de un plato. */
    public Plato actualizar(String codigo, String nombre, double pvp, List<Ingrediente> receta) {
        Plato plato = obtener(codigo);
        nombre = Validar.texto("Nombre del plato", nombre);
        Plato otro = buscarPorNombre(nombre);
        if (otro != null && otro != plato) {
            throw new ValidacionException("Ya existe un plato llamado «" + nombre + "».");
        }
        validarReceta(receta);
        plato.setNombre(nombre);
        plato.setPvp(pvp);
        plato.setReceta(receta);
        db.guardar();
        return plato;
    }

    /**
     * RF09: elimina un plato; si ya tiene ventas lo marca como inactivo para conservar el historial.
     * @return mensaje que describe la acción realizada
     */
    public String eliminar(String codigo) {
        Plato plato = obtener(codigo);
        boolean vendido = false;
        for (Venta v : db.ventas) if (v.incluyePlato(plato)) { vendido = true; break; }
        String msg;
        if (vendido) {
            plato.setActivo(false);
            msg = "El plato «" + plato.getNombre() + "» tiene ventas registradas, por lo que se marcó como INACTIVO.";
        } else {
            db.platos.remove(plato);
            msg = "El plato «" + plato.getNombre() + "» fue eliminado del menú.";
        }
        db.guardar();
        return msg;
    }

    public Plato obtener(String codigo) {
        for (Plato p : db.platos) if (p.getCodigo().equals(codigo)) return p;
        throw new ValidacionException("No existe un plato con código " + codigo + ".");
    }

    private Plato buscarPorNombre(String nombre) {
        for (Plato p : db.platos) if (p.getNombre().equalsIgnoreCase(nombre.trim())) return p;
        return null;
    }

    private void validarReceta(List<Ingrediente> receta) {
        if (receta == null || receta.isEmpty()) {
            throw new ValidacionException("El plato debe tener al menos un ingrediente en su receta.");
        }
        Set<Insumo> vistos = new HashSet<>();
        for (Ingrediente i : receta) {
            if (!vistos.add(i.getInsumo())) {
                throw new ValidacionException("El insumo «" + i.getInsumo().getNombre() + "» está repetido en la receta.");
            }
        }
    }
}
