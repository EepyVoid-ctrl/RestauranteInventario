package negocio;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import modelo.*;

/**
 * Almacén de datos del sistema. Mantiene las listas en memoria y las guarda en
 * un archivo (serialización de Java) tras cada cambio. Si la ruta es null,
 * trabaja solo en memoria (útil para las pruebas automáticas).
 */
public class BaseDatos implements Serializable {
    private static final long serialVersionUID = 1L;

    final List<Insumo> insumos = new ArrayList<>();
    final List<Plato> platos = new ArrayList<>();
    final List<Venta> ventas = new ArrayList<>();
    final List<Factura> facturas = new ArrayList<>();
    private int sigInsumo = 1;
    private int sigPlato = 1;
    private int sigVenta = 1;

    private transient Path archivo;

    public static BaseDatos enMemoria() {
        return new BaseDatos();
    }

    /** Carga la base desde disco; si no existe o está dañada, crea una vacía. */
    public static BaseDatos cargar(Path archivo) {
        BaseDatos db = null;
        if (Files.exists(archivo)) {
            try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(archivo))) {
                db = (BaseDatos) in.readObject();
            } catch (IOException | ClassNotFoundException | ClassCastException e) {
                System.err.println("No se pudo leer " + archivo + ": " + e.getMessage());
            }
        }
        if (db == null) db = new BaseDatos();
        db.archivo = archivo;
        return db;
    }

    /** Guarda el estado actual en disco (no hace nada si es solo en memoria). */
    void guardar() {
        if (archivo == null) return;
        try {
            if (archivo.getParent() != null) Files.createDirectories(archivo.getParent());
            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(archivo))) {
                out.writeObject(this);
            }
        } catch (IOException e) {
            throw new ValidacionException("No se pudo guardar la información: " + e.getMessage());
        }
    }

    public boolean estaVacia() { return insumos.isEmpty() && platos.isEmpty() && ventas.isEmpty(); }

    String codigoInsumo() { return String.format("INS-%03d", sigInsumo++); }
    String codigoPlato() { return String.format("PLA-%03d", sigPlato++); }
    String numeroVenta() { return String.format("V-%04d", sigVenta); }
    String numeroFactura() { return String.format("001-001-%09d", sigVenta++); }
}
