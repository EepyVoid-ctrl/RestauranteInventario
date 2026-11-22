package modelo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Insumo con fecha de caducidad (carnes, lácteos, vegetales frescos...). */
public class InsumoPerecedero extends Insumo {
    private static final long serialVersionUID = 1L;
    private static final int DIAS_AVISO = 3;

    private LocalDate fechaCaducidad;

    public InsumoPerecedero(String codigo, String nombre, String categoria, String unidadMedida,
                            double cantidadComprada, double costoTotal, double stockMinimo,
                            LocalDate fechaCaducidad) {
        super(codigo, nombre, categoria, unidadMedida, cantidadComprada, costoTotal, stockMinimo);
        setFechaCaducidad(fechaCaducidad);
    }

    public LocalDate getFechaCaducidad() { return fechaCaducidad; }

    public void setFechaCaducidad(LocalDate f) {
        if (f == null) throw new ValidacionException("El campo «Fecha de caducidad» es obligatorio.");
        this.fechaCaducidad = f;
    }

    public long getDiasParaVencer() {
        return ChronoUnit.DAYS.between(LocalDate.now(), fechaCaducidad);
    }

    @Override
    public String getTipo() { return "Perecedero"; }

    @Override
    public String getAlertaConservacion() {
        long d = getDiasParaVencer();
        if (d < 0) return "VENCIDO";
        if (d <= DIAS_AVISO) return "Vence en " + d + " día(s)";
        return null;
    }
}
