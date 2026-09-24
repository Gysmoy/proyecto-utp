package pe.edu.utp.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Optional;
import pe.edu.utp.config.Conexion;
import pe.edu.utp.dao.UsuarioDAO;
import pe.edu.utp.dto.UsuarioDTO;

public class UsuarioDAOImpl implements UsuarioDAO {

    private static final String INSERT
            = "INSERT INTO usuarios (nombre, usuario, email, password, public_key, private_key_enc, kdf_salt) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_LOGIN
            = "SELECT * FROM usuarios WHERE usuario = ? OR email = ? LIMIT 1";
    private static final String EXISTE_USUARIO = "SELECT 1 FROM usuarios WHERE usuario = ?";
    private static final String EXISTE_EMAIL = "SELECT 1 FROM usuarios WHERE email = ?";

    @Override
    public int insertar(UsuarioDTO u) throws SQLException {
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getUsuario());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getPassword());
            ps.setString(5, u.getPublicKey());
            ps.setString(6, u.getPrivateKeyEnc());
            ps.setString(7, u.getKdfSalt());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    @Override
    public Optional<UsuarioDTO> buscarPorUsuarioOEmail(String usuarioOEmail) throws SQLException {
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_LOGIN)) {
            ps.setString(1, usuarioOEmail);
            ps.setString(2, usuarioOEmail);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public boolean existeUsuario(String usuario) throws SQLException {
        return existe(EXISTE_USUARIO, usuario);
    }

    @Override
    public boolean existeEmail(String email) throws SQLException {
        return existe(EXISTE_EMAIL, email);
    }

    private boolean existe(String sql, String valor) throws SQLException {
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, valor);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private UsuarioDTO mapear(ResultSet rs) throws SQLException {
        UsuarioDTO u = new UsuarioDTO();
        u.setId(rs.getInt("id"));
        u.setNombre(rs.getString("nombre"));
        u.setUsuario(rs.getString("usuario"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setPublicKey(rs.getString("public_key"));
        u.setPrivateKeyEnc(rs.getString("private_key_enc"));
        u.setKdfSalt(rs.getString("kdf_salt"));
        Timestamp creado = rs.getTimestamp("created_at");
        if (creado != null) {
            u.setCreatedAt(creado.toLocalDateTime());
        }
        return u;
    }
}
