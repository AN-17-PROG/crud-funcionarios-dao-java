package dao;

import conexion.ConexionBD;
import excepciones.DaoException;
import modelo.Catalogo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CatalogoDAOImpl implements CatalogoDAO {

    @Override
    public List<Catalogo> listarTiposDocumento() throws DaoException {
        return listar("SELECT id_tipo_documento AS id, nombre FROM tipo_documento ORDER BY id_tipo_documento",
                "los tipos de documento");
    }

    @Override
    public List<Catalogo> listarEstadosCiviles() throws DaoException {
        return listar("SELECT id_estado_civil AS id, nombre FROM estado_civil ORDER BY id_estado_civil",
                "los estados civiles");
    }

    private List<Catalogo> listar(String sql, String descripcion) throws DaoException {
        List<Catalogo> lista = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Catalogo(rs.getInt("id"), rs.getString("nombre")));
            }
        } catch (SQLException e) {
            throw new DaoException("No fue posible consultar " + descripcion + ".", e);
        }
        return lista;
    }
}
