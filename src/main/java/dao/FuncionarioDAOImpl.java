package dao;

import conexion.ConexionBD;
import excepciones.DaoException;
import modelo.Catalogo;
import modelo.FormacionAcademica;
import modelo.Funcionario;
import modelo.GrupoFamiliar;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación JDBC del DAO de funcionarios.
 *
 * Decisiones de diseño:
 *  - Todas las consultas usan PreparedStatement (parámetros "?"), lo que evita
 *    la inyección SQL.
 *  - crear() y actualizar() usan una transacción: o se guarda todo (funcionario,
 *    familiares y estudios) o no se guarda nada.
 *  - Los errores de JDBC (SQLException) se traducen a DaoException con mensajes claros.
 */
public class FuncionarioDAOImpl implements FuncionarioDAO {

    private static final int ERROR_MYSQL_DUPLICADO = 1062;

    private static final String SELECT_BASE =
            "SELECT f.id_funcionario, f.numero_identificacion, f.nombres, f.apellidos, f.sexo, "
            + "f.direccion, f.telefono, f.fecha_nacimiento, "
            + "td.id_tipo_documento, td.nombre AS tipo_documento, "
            + "ec.id_estado_civil, ec.nombre AS estado_civil "
            + "FROM funcionarios f "
            + "JOIN tipo_documento td ON td.id_tipo_documento = f.id_tipo_documento "
            + "JOIN estado_civil ec ON ec.id_estado_civil = f.id_estado_civil ";

    private static final String INSERT_FUNCIONARIO =
            "INSERT INTO funcionarios (id_tipo_documento, numero_identificacion, nombres, apellidos, "
            + "id_estado_civil, sexo, direccion, telefono, fecha_nacimiento) VALUES (?,?,?,?,?,?,?,?,?)";

    private static final String UPDATE_FUNCIONARIO =
            "UPDATE funcionarios SET id_tipo_documento=?, numero_identificacion=?, nombres=?, apellidos=?, "
            + "id_estado_civil=?, sexo=?, direccion=?, telefono=?, fecha_nacimiento=? WHERE id_funcionario=?";

    private static final String INSERT_FAMILIAR =
            "INSERT INTO grupo_familiar (id_funcionario, nombres, apellidos, fecha_nacimiento, rol) VALUES (?,?,?,?,?)";

    private static final String INSERT_FORMACION =
            "INSERT INTO formacion_academica (id_funcionario, universidad, nivel_estudio, titulo) VALUES (?,?,?,?)";

    // ------------------------------------------------------------------ CREATE

    @Override
    public void crear(Funcionario f) throws DaoException {
        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(INSERT_FUNCIONARIO, Statement.RETURN_GENERATED_KEYS)) {
                    asignarParametros(ps, f);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            f.setId(keys.getInt(1));
                        }
                    }
                }
                insertarRelacionados(con, f);
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw traducir(e, "crear");
            }
        } catch (SQLException e) {
            throw new DaoException("Error al manejar la conexión con la base de datos.", e);
        }
    }

    // -------------------------------------------------------------------- READ

    @Override
    public List<Funcionario> listar() throws DaoException {
        List<Funcionario> lista = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY f.apellidos, f.nombres";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new DaoException("No fue posible listar los funcionarios.", e);
        }
        return lista;
    }

    @Override
    public Funcionario buscarPorId(int id) throws DaoException {
        String sql = SELECT_BASE + "WHERE f.id_funcionario = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("El funcionario seleccionado ya no existe.");
                }
                Funcionario f = mapear(rs);
                f.setGrupoFamiliar(cargarGrupoFamiliar(con, id));
                f.setFormaciones(cargarFormaciones(con, id));
                return f;
            }
        } catch (SQLException e) {
            throw new DaoException("No fue posible consultar el funcionario.", e);
        }
    }

    // ------------------------------------------------------------------ UPDATE

    @Override
    public void actualizar(Funcionario f) throws DaoException {
        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(UPDATE_FUNCIONARIO)) {
                    asignarParametros(ps, f);
                    ps.setInt(10, f.getId());
                    if (ps.executeUpdate() == 0) {
                        con.rollback();
                        throw new DaoException("El funcionario ya no existe, no se pudo actualizar.");
                    }
                }
                // Estrategia simple: se borran los relacionados y se vuelven a insertar.
                borrarRelacionados(con, f.getId());
                insertarRelacionados(con, f);
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw traducir(e, "actualizar");
            }
        } catch (SQLException e) {
            throw new DaoException("Error al manejar la conexión con la base de datos.", e);
        }
    }

    // ------------------------------------------------------------------ DELETE

    @Override
    public void eliminar(int id) throws DaoException {
        String sql = "DELETE FROM funcionarios WHERE id_funcionario = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new DaoException("El funcionario ya no existe.");
            }
        } catch (SQLException e) {
            throw new DaoException("No fue posible eliminar el funcionario.", e);
        }
    }

    // --------------------------------------------------------------- AUXILIARES

    private void asignarParametros(PreparedStatement ps, Funcionario f) throws SQLException {
        ps.setInt(1, f.getTipoDocumento().getId());
        ps.setString(2, f.getNumeroIdentificacion());
        ps.setString(3, f.getNombres());
        ps.setString(4, f.getApellidos());
        ps.setInt(5, f.getEstadoCivil().getId());
        ps.setString(6, f.getSexo());
        ps.setString(7, f.getDireccion());
        ps.setString(8, f.getTelefono());
        ps.setDate(9, Date.valueOf(f.getFechaNacimiento()));
    }

    private void insertarRelacionados(Connection con, Funcionario f) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(INSERT_FAMILIAR)) {
            for (GrupoFamiliar g : f.getGrupoFamiliar()) {
                ps.setInt(1, f.getId());
                ps.setString(2, g.getNombres());
                ps.setString(3, g.getApellidos());
                if (g.getFechaNacimiento() == null) {
                    ps.setNull(4, java.sql.Types.DATE);
                } else {
                    ps.setDate(4, Date.valueOf(g.getFechaNacimiento()));
                }
                ps.setString(5, g.getRol());
                ps.addBatch();
            }
            ps.executeBatch();
        }
        try (PreparedStatement ps = con.prepareStatement(INSERT_FORMACION)) {
            for (FormacionAcademica fa : f.getFormaciones()) {
                ps.setInt(1, f.getId());
                ps.setString(2, fa.getUniversidad());
                ps.setString(3, fa.getNivelEstudio());
                ps.setString(4, fa.getTitulo());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void borrarRelacionados(Connection con, int idFuncionario) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM grupo_familiar WHERE id_funcionario = ?")) {
            ps.setInt(1, idFuncionario);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM formacion_academica WHERE id_funcionario = ?")) {
            ps.setInt(1, idFuncionario);
            ps.executeUpdate();
        }
    }

    private List<GrupoFamiliar> cargarGrupoFamiliar(Connection con, int idFuncionario) throws SQLException {
        List<GrupoFamiliar> lista = new ArrayList<>();
        String sql = "SELECT id_grupo_familiar, nombres, apellidos, fecha_nacimiento, rol "
                + "FROM grupo_familiar WHERE id_funcionario = ? ORDER BY id_grupo_familiar";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idFuncionario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new GrupoFamiliar(
                            rs.getInt("id_grupo_familiar"),
                            rs.getString("nombres"),
                            rs.getString("apellidos"),
                            aLocalDate(rs.getDate("fecha_nacimiento")),
                            rs.getString("rol")));
                }
            }
        }
        return lista;
    }

    private List<FormacionAcademica> cargarFormaciones(Connection con, int idFuncionario) throws SQLException {
        List<FormacionAcademica> lista = new ArrayList<>();
        String sql = "SELECT id_formacion, universidad, nivel_estudio, titulo "
                + "FROM formacion_academica WHERE id_funcionario = ? ORDER BY id_formacion";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idFuncionario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new FormacionAcademica(
                            rs.getInt("id_formacion"),
                            rs.getString("universidad"),
                            rs.getString("nivel_estudio"),
                            rs.getString("titulo")));
                }
            }
        }
        return lista;
    }

    private Funcionario mapear(ResultSet rs) throws SQLException {
        Funcionario f = new Funcionario();
        f.setId(rs.getInt("id_funcionario"));
        f.setTipoDocumento(new Catalogo(rs.getInt("id_tipo_documento"), rs.getString("tipo_documento")));
        f.setNumeroIdentificacion(rs.getString("numero_identificacion"));
        f.setNombres(rs.getString("nombres"));
        f.setApellidos(rs.getString("apellidos"));
        f.setEstadoCivil(new Catalogo(rs.getInt("id_estado_civil"), rs.getString("estado_civil")));
        f.setSexo(rs.getString("sexo"));
        f.setDireccion(rs.getString("direccion"));
        f.setTelefono(rs.getString("telefono"));
        f.setFechaNacimiento(aLocalDate(rs.getDate("fecha_nacimiento")));
        return f;
    }

    private LocalDate aLocalDate(Date fecha) {
        return fecha == null ? null : fecha.toLocalDate();
    }

    /** Convierte un error técnico de MySQL en una excepción con mensaje comprensible. */
    private DaoException traducir(SQLException e, String accion) {
        if (e.getErrorCode() == ERROR_MYSQL_DUPLICADO) {
            return new DaoException("Ya existe un funcionario con ese número de identificación.", e);
        }
        return new DaoException("No fue posible " + accion + " el funcionario.", e);
    }
}
