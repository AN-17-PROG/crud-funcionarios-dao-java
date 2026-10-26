package excepciones;

/**
 * Excepción propia para datos de formulario inválidos
 * (campos vacíos, formatos incorrectos, fechas imposibles, etc.).
 */
public class ValidacionException extends Exception {

    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
