package pe.edu.utp.facade;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.SQLException;
import java.util.Optional;
import java.util.regex.Pattern;
import pe.edu.utp.dao.UsuarioDAO;
import pe.edu.utp.dao.impl.UsuarioDAOImpl;
import pe.edu.utp.dto.UsuarioDTO;
import pe.edu.utp.dto.UsuarioSesionDTO;
import pe.edu.utp.security.KeyProtector;
import pe.edu.utp.security.LlavesSesion;
import pe.edu.utp.security.PasswordHasher;
import pe.edu.utp.security.RsaCipher;

/**
 * Fachada de autenticacion: registro y login. Coordina el DAO de usuarios
 * con el hash BCrypt y la generacion/proteccion del par de llaves RSA.
 */
public class AuthFacade {

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern USUARIO = Pattern.compile("^[a-zA-Z0-9._-]{3,50}$");

    private final UsuarioDAO usuarioDAO;

    public AuthFacade() {
        this(new UsuarioDAOImpl());
    }

    public AuthFacade(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    /** Resultado de un login correcto: datos para la sesion + llaves RSA ya descifradas. */
    public record Autenticado(UsuarioSesionDTO usuario, LlavesSesion llaves) {
    }

    public void registrar(String nombre, String usuario, String email, String password, String confirmar)
            throws ValidacionException {
        nombre = limpiar(nombre);
        usuario = limpiar(usuario);
        email = limpiar(email).toLowerCase();

        if (nombre.isEmpty() || nombre.length() > 100) {
            throw new ValidacionException("Ingresa tu nombre (maximo 100 caracteres).");
        }
        if (!USUARIO.matcher(usuario).matches()) {
            throw new ValidacionException("El usuario debe tener de 3 a 50 caracteres: letras, numeros, punto, guion o guion bajo.");
        }
        if (email.length() > 120 || !EMAIL.matcher(email).matches()) {
            throw new ValidacionException("Ingresa un correo valido.");
        }
        if (password == null || password.length() < 8) {
            throw new ValidacionException("La contrasena debe tener al menos 8 caracteres.");
        }
        if (PasswordHasher.excedeLimite(password)) {
            throw new ValidacionException("La contrasena no puede superar los 72 bytes.");
        }
        if (!password.equals(confirmar)) {
            throw new ValidacionException("Las contrasenas no coinciden.");
        }

        try {
            if (usuarioDAO.existeUsuario(usuario)) {
                throw new ValidacionException("Ese nombre de usuario ya esta registrado.");
            }
            if (usuarioDAO.existeEmail(email)) {
                throw new ValidacionException("Ese correo ya esta registrado.");
            }

            KeyPair par = RsaCipher.generarPar();
            String salt = KeyProtector.nuevoSalt();

            UsuarioDTO dto = new UsuarioDTO();
            dto.setNombre(nombre);
            dto.setUsuario(usuario);
            dto.setEmail(email);
            dto.setPassword(PasswordHasher.hash(password));
            dto.setPublicKey(RsaCipher.publicaABase64(par.getPublic()));
            dto.setPrivateKeyEnc(KeyProtector.proteger(par.getPrivate(), password, salt));
            dto.setKdfSalt(salt);
            usuarioDAO.insertar(dto);
        } catch (SQLException e) {
            // Carrera entre dos registros con el mismo usuario/correo
            if ("23000".equals(e.getSQLState())) {
                throw new ValidacionException("El usuario o correo ya esta registrado.");
            }
            throw new AppException("Error al registrar usuario", e);
        } catch (GeneralSecurityException e) {
            throw new AppException("Error generando llaves RSA", e);
        }
    }

    public Optional<Autenticado> login(String usuarioOEmail, String password) {
        usuarioOEmail = limpiar(usuarioOEmail);
        if (usuarioOEmail.isEmpty() || password == null || password.isEmpty()) {
            return Optional.empty();
        }
        try {
            Optional<UsuarioDTO> encontrado = usuarioDAO.buscarPorUsuarioOEmail(usuarioOEmail);
            if (encontrado.isEmpty()) {
                PasswordHasher.verificarFicticio(password);
                return Optional.empty();
            }
            UsuarioDTO u = encontrado.get();
            if (!PasswordHasher.verificar(password, u.getPassword())) {
                return Optional.empty();
            }

            PublicKey publica = RsaCipher.publicaDesdeBase64(u.getPublicKey());
            PrivateKey privada = KeyProtector.recuperar(u.getPrivateKeyEnc(), password, u.getKdfSalt());

            UsuarioSesionDTO sesion = new UsuarioSesionDTO(u.getId(), u.getNombre(), u.getUsuario(), u.getEmail());
            return Optional.of(new Autenticado(sesion, new LlavesSesion(publica, privada)));
        } catch (SQLException e) {
            throw new AppException("Error al consultar usuario", e);
        } catch (GeneralSecurityException e) {
            throw new AppException("No se pudo recuperar la llave privada del usuario", e);
        }
    }

    private static String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
