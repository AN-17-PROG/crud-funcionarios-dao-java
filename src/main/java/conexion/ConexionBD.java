package conexion;

import excepciones.DaoException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Se encarga únicamente de abrir conexiones a MySQL.
 * Los datos de conexión (url, usuario y clave) se leen de config.properties
 * para no dejar la contraseña escrita en el código fuente ni subirla a GitHub.
 */
public final class ConexionBD {

    private static final Properties CONFIG = new Properties();
    private static boolean cargada = false;

    private ConexionBD() {
    }

    private static synchronized void cargarConfiguracion() throws DaoException {
        if (cargada) {
            return;
        }
        try (InputStream in = ConexionBD.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in == null) {
                throw new DaoException("No se encontró el archivo config.properties. "
                        + "Copie config.properties.example como config.properties y complete sus datos.");
            }
            CONFIG.load(in);
            cargada = true;
        } catch (IOException e) {
            throw new DaoException("No se pudo leer el archivo config.properties.", e);
        }
    }

    public static Connection obtenerConexion() throws DaoException {
        cargarConfiguracion();
        try {
            return DriverManager.getConnection(
                    CONFIG.getProperty("db.url"),
                    CONFIG.getProperty("db.usuario"),
                    CONFIG.getProperty("db.clave"));
        } catch (SQLException e) {
            throw new DaoException("No fue posible conectarse a la base de datos. "
                    + "Verifique que MySQL esté activo y que los datos de config.properties sean correctos.", e);
        }
    }
}
