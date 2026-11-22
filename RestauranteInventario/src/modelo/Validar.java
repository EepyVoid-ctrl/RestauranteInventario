package modelo;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** Utilidades estáticas de validación y conversión de datos de entrada. */
public final class Validar {
    private Validar() { }

    /** Verifica que un texto no esté vacío y lo devuelve sin espacios sobrantes. */
    public static String texto(String campo, String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new ValidacionException("El campo «" + campo + "» es obligatorio.");
        }
        return valor.trim();
    }

    /** Convierte un texto a número (acepta coma o punto decimal). */
    public static double numero(String campo, String valor) {
        texto(campo, valor);
        try {
            double d = Double.parseDouble(valor.trim().replace(',', '.'));
            if (!Double.isFinite(d)) throw new NumberFormatException();
            return d;
        } catch (NumberFormatException e) {
            throw new ValidacionException("El campo «" + campo + "» debe ser un número válido.");
        }
    }

    public static double positivo(String campo, double valor) {
        if (!(valor > 0)) {
            throw new ValidacionException("El campo «" + campo + "» debe ser mayor que cero.");
        }
        return valor;
    }

    public static double noNegativo(String campo, double valor) {
        if (!(valor >= 0)) {
            throw new ValidacionException("El campo «" + campo + "» no puede ser negativo.");
        }
        return valor;
    }

    /** Convierte texto AAAA-MM-DD a fecha; si no es obligatoria y está vacío devuelve null. */
    public static LocalDate fecha(String campo, String valor, boolean obligatoria) {
        if (valor == null || valor.trim().isEmpty()) {
            if (obligatoria) throw new ValidacionException("El campo «" + campo + "» es obligatorio.");
            return null;
        }
        try {
            return LocalDate.parse(valor.trim());
        } catch (DateTimeParseException e) {
            throw new ValidacionException("El campo «" + campo + "» debe tener el formato AAAA-MM-DD.");
        }
    }
}
