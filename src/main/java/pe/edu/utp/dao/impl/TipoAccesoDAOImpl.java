package pe.edu.utp.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import pe.edu.utp.config.Conexion;
import pe.edu.utp.dao.TipoAccesoDAO;
import pe.edu.utp.dto.TipoAccesoDTO;

public class TipoAccesoDAOImpl implements TipoAccesoDAO {

    @Override
    public List<TipoAccesoDTO> listar() throws SQLException {
        List<TipoAccesoDTO> tipos = new ArrayList<>();
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement("SELECT id, nombre FROM tipos_acceso ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tipos.add(new TipoAccesoDTO(rs.getInt("id"), rs.getString("nombre")));
            }
        }
        return tipos;
    }

    @Override
    public boolean existe(int id) throws SQLException {
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement("SELECT 1 FROM tipos_acceso WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
