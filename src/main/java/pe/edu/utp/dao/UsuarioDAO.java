package pe.edu.utp.dao;

import java.sql.SQLException;
import java.util.Optional;
import pe.edu.utp.dto.UsuarioDTO;

public interface UsuarioDAO {

    int insertar(UsuarioDTO usuario) throws SQLException;

    Optional<UsuarioDTO> buscarPorUsuarioOEmail(String usuarioOEmail) throws SQLException;

    boolean existeUsuario(String usuario) throws SQLException;

    boolean existeEmail(String email) throws SQLException;
}
