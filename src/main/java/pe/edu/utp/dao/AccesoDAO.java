package pe.edu.utp.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import pe.edu.utp.dto.AccesoDTO;

/**
 * Todas las operaciones reciben el usuarioId para que un usuario
 * nunca pueda leer ni modificar accesos de otro.
 */
public interface AccesoDAO {

    List<AccesoDTO> listarPorUsuario(int usuarioId, String buscar, Integer tipoId) throws SQLException;

    Optional<AccesoDTO> buscarPorId(int id, int usuarioId) throws SQLException;

    int insertar(AccesoDTO acceso) throws SQLException;

    boolean actualizar(AccesoDTO acceso) throws SQLException;

    boolean eliminar(int id, int usuarioId) throws SQLException;
}
