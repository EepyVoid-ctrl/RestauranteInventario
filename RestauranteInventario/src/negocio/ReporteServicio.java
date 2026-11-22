package negocio;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

import modelo.*;

/** Lógica del Módulo 4: reportes de rentabilidad y consumo (RF15 – RF16). */
public class ReporteServicio {
    private final BaseDatos db;

    public ReporteServicio(BaseDatos db) { this.db = db; }

    /** Fila del reporte de rentabilidad por plato. */
    public record FilaRentabilidad(String plato, double costo, double pvp, double margenUnitario,
                                   double margenPorcentual, int unidades, double utilidadTotal) { }

    /** Fila del reporte de consumo y rotación de insumos. */
    public record FilaConsumo(String insumo, String categoria, String unidad, double consumoTotal,
                              double consumoDiario, double stockActual, double diasCobertura) { }

    /** RF15: rentabilidad de cada plato vendido en el período, ordenada por utilidad total. */
    public List<FilaRentabilidad> rentabilidad(LocalDate desde, LocalDate hasta) {
        validarFechas(desde, hasta);
        Map<String, double[]> acum = new LinkedHashMap<>(); // [unidades, ingresos, costos]
        for (Venta v : db.ventas) {
            LocalDate f = v.getFecha().toLocalDate();
            if (f.isBefore(desde) || f.isAfter(hasta)) continue;
            for (DetalleVenta d : v.getDetalles()) {
                double[] a = acum.computeIfAbsent(d.getNombrePlato(), k -> new double[3]);
                a[0] += d.getCantidad();
                a[1] += d.getSubtotal();
                a[2] += d.getCostoUnitario() * d.getCantidad();
            }
        }
        List<FilaRentabilidad> filas = new ArrayList<>();
        for (Map.Entry<String, double[]> e : acum.entrySet()) {
            double u = e.getValue()[0];
            double pvp = e.getValue()[1] / u;
            double costo = e.getValue()[2] / u;
            double margen = pvp - costo;
            double pct = pvp > 0 ? margen / pvp * 100 : 0;
            filas.add(new FilaRentabilidad(e.getKey(), costo, pvp, margen, pct, (int) u, margen * u));
        }
        filas.sort((a, b) -> Double.compare(b.utilidadTotal(), a.utilidadTotal()));
        return filas;
    }

    /**
     * RF16: consumo total, consumo diario promedio y días de cobertura (stock actual ÷ consumo diario)
     * de cada insumo. La categoría es opcional.
     */
    public List<FilaConsumo> consumo(LocalDate desde, LocalDate hasta, String categoria) {
        validarFechas(desde, hasta);
        long dias = ChronoUnit.DAYS.between(desde, hasta) + 1;
        Map<String, Double> consumo = new HashMap<>();
        for (Venta v : db.ventas) {
            LocalDate f = v.getFecha().toLocalDate();
            if (f.isBefore(desde) || f.isAfter(hasta)) continue;
            for (DetalleVenta d : v.getDetalles()) {
                for (Map.Entry<String, Double> e : d.getConsumoInsumos().entrySet()) {
                    consumo.merge(e.getKey(), e.getValue(), Double::sum);
                }
            }
        }
        List<FilaConsumo> filas = new ArrayList<>();
        for (Insumo i : db.insumos) {
            if (categoria != null && !categoria.isBlank() && !i.getCategoria().equalsIgnoreCase(categoria)) continue;
            double total = consumo.getOrDefault(i.getCodigo(), 0.0);
            double diario = total / dias;
            double cobertura = diario > 0 ? i.getStock() / diario : Double.POSITIVE_INFINITY;
            filas.add(new FilaConsumo(i.getNombre(), i.getCategoria(), i.getUnidadMedida(),
                    total, diario, i.getStock(), cobertura));
        }
        filas.sort((a, b) -> Double.compare(b.consumoTotal(), a.consumoTotal()));
        return filas;
    }

    private void validarFechas(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) throw new ValidacionException("Debe indicar el rango de fechas del reporte.");
        VentaServicio.validarRango(desde, hasta);
    }
}
