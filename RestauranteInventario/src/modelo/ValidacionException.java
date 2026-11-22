package modelo;

/**
 * Excepción lanzada cuando un dato ingresado incumple una regla de negocio
 * (campo vacío, valor negativo, stock insuficiente, etc.).
 * El mensaje siempre está redactado para mostrarse directamente al usuario.
 */
public class ValidacionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
