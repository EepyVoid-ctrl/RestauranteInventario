package negocio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import modelo.*;

/** Lógica del Módulo 3: ventas, descuento de inventario y facturación (RF11 – RF14). */
public class VentaServicio {
    private final BaseDatos db;

    public VentaServicio(BaseDatos db) { this.db = db; }

    /** RF11 + RF12 + RF13: registra la venta, descuenta el inventario y emite la factura. */
    public Factura registrar(Cliente cliente, Map<Plato, Integer> platos, MetodoPago metodo) {
        return registrar(cliente, platos, metodo, LocalDateTime.now());
    }

    /** Variante con fecha explícita (se usa para cargar datos de ejemplo). */
    public Factura registrar(Cliente cliente, Map<Plato, Integer> platos, MetodoPago metodo, LocalDateTime fecha) {
        if (platos == null || platos.isEmpty()) {
            throw new ValidacionException("Debe agregar al menos un plato a la venta.");
        }
        if (metodo == null) throw new ValidacionException("Debe seleccionar un método de pago.");
        if (cliente == null) cliente = Cliente.consumidorFinal();

        // 1) Calcular el consumo de insumos de toda la venta
        Map<Insumo, Double> requerido = new LinkedHashMap<>();
        List<DetalleVenta> detalles = new ArrayList<>();
        for (Map.Entry<Plato, Integer> e : platos.entrySet()) {
            Plato p = e.getKey();
            int cant = e.getValue();
            if (!p.isActivo()) throw new ValidacionException("El plato «" + p.getNombre() + "» está inactivo.");
            if (cant <= 0) throw new ValidacionException("La cantidad de porciones debe ser mayor que cero.");
            Map<String, Double> consumoLinea = new LinkedHashMap<>();
            for (Ingrediente ing : p.getReceta()) {
                double total = ing.getCantidadPorPorcion() * cant;
                requerido.merge(ing.getInsumo(), total, Double::sum);
                consumoLinea.put(ing.getInsumo().getCodigo(), total);
            }
            detalles.add(new DetalleVenta(p, cant, consumoLinea));
        }

        // 2) RF12: bloquear la venta si algún insumo no alcanza
        List<String> faltantes = new ArrayList<>();
        for (Map.Entry<Insumo, Double> e : requerido.entrySet()) {
            Insumo i = e.getKey();
            if (e.getValue() > i.getStock() + 1e-9) {
                faltantes.add(String.format(Locale.US, "%s (requerido %.2f %s, disponible %.2f %s)",
                        i.getNombre(), e.getValue(), i.getUnidadMedida(), i.getStock(), i.getUnidadMedida()));
            }
        }
        if (!faltantes.isEmpty()) {
            throw new ValidacionException("Venta rechazada por stock insuficiente de: " + String.join("; ", faltantes) + ".");
        }

        // 3) Descontar y registrar
        for (Map.Entry<Insumo, Double> e : requerido.entrySet()) e.getKey().descontar(e.getValue());
        Venta venta = new Venta(db.numeroVenta(), fecha, detalles, metodo);
        Factura factura = new Factura(db.numeroFactura(), venta, cliente);
        db.ventas.add(venta);
        db.facturas.add(factura);
        db.guardar();
        return factura;
    }

    /** RF14: consulta facturas por rango de fechas, número o identificación (todos opcionales). */
    public List<Factura> consultarFacturas(LocalDate desde, LocalDate hasta, String numero, String identificacion) {
        List<Factura> res = new ArrayList<>();
        for (Factura f : db.facturas) {
            LocalDate d = f.getVenta().getFecha().toLocalDate();
            if (desde != null && d.isBefore(desde)) continue;
            if (hasta != null && d.isAfter(hasta)) continue;
            if (numero != null && !numero.isBlank() && !f.getNumero().contains(numero.trim())) continue;
            if (identificacion != null && !identificacion.isBlank()
                    && !f.getCliente().getIdentificacion().contains(identificacion.trim())) continue;
            res.add(f);
        }
        Collections.reverse(res); // más recientes primero
        return res;
    }

    /** Facturas de un rango de fechas válido (desde no puede ser posterior a hasta). */
    public static void validarRango(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ValidacionException("La fecha «Desde» no puede ser posterior a la fecha «Hasta».");
        }
    }
}
