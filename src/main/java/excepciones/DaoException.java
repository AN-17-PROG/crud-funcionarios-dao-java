package excepciones;

/**
 * Excepción propia de la capa de acceso a datos (DAO).
 * Envuelve los errores técnicos de JDBC (SQLException) en un mensaje
 * entendible para el usuario, sin exponer detalles internos de la base de datos.
 */
public class DaoException extends Exception {

    public DaoException(String mensaje) {
        super(mensaje);
    }

    public DaoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
