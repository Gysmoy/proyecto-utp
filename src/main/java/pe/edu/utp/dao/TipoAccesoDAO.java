package pe.edu.utp.dao;

import java.sql.SQLException;
import java.util.List;
import pe.edu.utp.dto.TipoAccesoDTO;

public interface TipoAccesoDAO {

    List<TipoAccesoDTO> listar() throws SQLException;

    boolean existe(int id) throws SQLException;
}
