package dao;

import excepciones.DaoException;
import modelo.Catalogo;

import java.util.List;

/** Consulta los catálogos que alimentan los desplegables del formulario. */
public interface CatalogoDAO {

    List<Catalogo> listarTiposDocumento() throws DaoException;

    List<Catalogo> listarEstadosCiviles() throws DaoException;
}
