package dao;

import excepciones.DaoException;
import modelo.Funcionario;

import java.util.List;

/**
 * Contrato del patrón DAO para funcionarios.
 * La interfaz dice QUÉ se puede hacer; la implementación (FuncionarioDAOImpl)
 * dice CÓMO se hace con SQL. La vista solo conoce esta interfaz.
 */
public interface FuncionarioDAO {

    /** Inserta el funcionario junto con su grupo familiar y su formación (en una transacción). */
    void crear(Funcionario funcionario) throws DaoException;

    /** Lista todos los funcionarios (datos principales, con tipo de documento y estado civil). */
    List<Funcionario> listar() throws DaoException;

    /** Busca un funcionario por id, incluyendo su grupo familiar y su formación académica. */
    Funcionario buscarPorId(int id) throws DaoException;

    /** Actualiza el funcionario y reemplaza su grupo familiar y su formación (en una transacción). */
    void actualizar(Funcionario funcionario) throws DaoException;

    /** Elimina el funcionario; sus familiares y estudios se borran por ON DELETE CASCADE. */
    void eliminar(int id) throws DaoException;
}
