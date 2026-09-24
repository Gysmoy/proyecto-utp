package pe.edu.utp.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import pe.edu.utp.config.Conexion;
import pe.edu.utp.dao.AccesoDAO;
import pe.edu.utp.dto.AccesoDTO;

public class AccesoDAOImpl implements AccesoDAO {

    private static final String SELECT_BASE
            = "SELECT a.*, t.nombre AS tipo_nombre FROM accesos a "
            + "JOIN tipos_acceso t ON t.id = a.tipo_id ";
    private static final String INSERT
            = "INSERT INTO accesos (usuario_id, tipo_id, titulo, url, login, password_enc, notas) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE
            = "UPDATE accesos SET tipo_id = ?, titulo = ?, url = ?, login = ?, password_enc = ?, notas = ? "
            + "WHERE id = ? AND usuario_id = ?";
    private static final String DELETE = "DELETE FROM accesos WHERE id = ? AND usuario_id = ?";

    @Override
    public List<AccesoDTO> listarPorUsuario(int usuarioId, String buscar, Integer tipoId) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_BASE).append("WHERE a.usuario_id = ? ");
        List<Object> params = new ArrayList<>();
        params.add(usuarioId);
        if (buscar != null && !buscar.isBlank()) {
            sql.append("AND (a.titulo LIKE ? OR a.login LIKE ? OR a.url LIKE ?) ");
            String like = "%" + buscar.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (tipoId != null) {
            sql.append("AND a.tipo_id = ? ");
            params.add(tipoId);
        }
        sql.append("ORDER BY a.titulo");

        List<AccesoDTO> accesos = new ArrayList<>();
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accesos.add(mapear(rs));
                }
            }
        }
        return accesos;
    }

    @Override
    public Optional<AccesoDTO> buscarPorId(int id, int usuarioId) throws SQLException {
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE a.id = ? AND a.usuario_id = ?")) {
            ps.setInt(1, id);
            ps.setInt(2, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public int insertar(AccesoDTO a) throws SQLException {
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getUsuarioId());
            ps.setInt(2, a.getTipoId());
            ps.setString(3, a.getTitulo());
            ps.setString(4, a.getUrl());
            ps.setString(5, a.getLogin());
            ps.setString(6, a.getPasswordEnc());
            ps.setString(7, a.getNotas());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    @Override
    public boolean actualizar(AccesoDTO a) throws SQLException {
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(UPDATE)) {
            ps.setInt(1, a.getTipoId());
            ps.setString(2, a.getTitulo());
            ps.setString(3, a.getUrl());
            ps.setString(4, a.getLogin());
            ps.setString(5, a.getPasswordEnc());
            ps.setString(6, a.getNotas());
            ps.setInt(7, a.getId());
            ps.setInt(8, a.getUsuarioId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean eliminar(int id, int usuarioId) throws SQLException {
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(DELETE)) {
            ps.setInt(1, id);
            ps.setInt(2, usuarioId);
            return ps.executeUpdate() > 0;
        }
    }

    private AccesoDTO mapear(ResultSet rs) throws SQLException {
        AccesoDTO a = new AccesoDTO();
        a.setId(rs.getInt("id"));
        a.setUsuarioId(rs.getInt("usuario_id"));
        a.setTipoId(rs.getInt("tipo_id"));
        a.setTipoNombre(rs.getString("tipo_nombre"));
        a.setTitulo(rs.getString("titulo"));
        a.setUrl(rs.getString("url"));
        a.setLogin(rs.getString("login"));
        a.setPasswordEnc(rs.getString("password_enc"));
        a.setNotas(rs.getString("notas"));
        Timestamp creado = rs.getTimestamp("created_at");
        Timestamp actualizado = rs.getTimestamp("updated_at");
        if (creado != null) {
            a.setCreatedAt(creado.toLocalDateTime());
        }
        if (actualizado != null) {
            a.setUpdatedAt(actualizado.toLocalDateTime());
        }
        return a;
    }
}
