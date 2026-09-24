package pe.edu.utp.facade;

import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import pe.edu.utp.dao.AccesoDAO;
import pe.edu.utp.dao.TipoAccesoDAO;
import pe.edu.utp.dao.impl.AccesoDAOImpl;
import pe.edu.utp.dao.impl.TipoAccesoDAOImpl;
import pe.edu.utp.dto.AccesoDTO;
import pe.edu.utp.dto.TipoAccesoDTO;
import pe.edu.utp.security.LlavesSesion;
import pe.edu.utp.security.RsaCipher;

/**
 * Fachada del modulo de accesos: CRUD de cuentas y cifrado/descifrado RSA
 * de sus contrasenas. Los servlets solo hablan con esta clase.
 */
public class AccesoFacade {

    private final AccesoDAO accesoDAO;
    private final TipoAccesoDAO tipoDAO;

    public AccesoFacade() {
        this(new AccesoDAOImpl(), new TipoAccesoDAOImpl());
    }

    public AccesoFacade(AccesoDAO accesoDAO, TipoAccesoDAO tipoDAO) {
        this.accesoDAO = accesoDAO;
        this.tipoDAO = tipoDAO;
    }

    public List<AccesoDTO> listar(int usuarioId, String buscar, Integer tipoId) {
        try {
            return accesoDAO.listarPorUsuario(usuarioId, buscar, tipoId);
        } catch (SQLException e) {
            throw new AppException("Error al listar accesos", e);
        }
    }

    public List<TipoAccesoDTO> listarTipos() {
        try {
            return tipoDAO.listar();
        } catch (SQLException e) {
            throw new AppException("Error al listar tipos de acceso", e);
        }
    }

    public Optional<AccesoDTO> obtener(int id, int usuarioId) {
        try {
            return accesoDAO.buscarPorId(id, usuarioId);
        } catch (SQLException e) {
            throw new AppException("Error al obtener acceso", e);
        }
    }

    /**
     * Crea (id == 0) o actualiza un acceso. Al editar, si password viene vacio
     * se conserva la contrasena cifrada que ya existia.
     */
    public void guardar(int usuarioId, LlavesSesion llaves, int id, int tipoId, String titulo,
            String url, String login, String password, String notas) throws ValidacionException {
        titulo = limpiar(titulo);
        url = limpiar(url);
        login = limpiar(login);
        notas = limpiar(notas);
        boolean nuevo = id == 0;

        if (titulo.isEmpty() || titulo.length() > 100) {
            throw new ValidacionException("El titulo es obligatorio (maximo 100 caracteres).");
        }
        if (login.isEmpty() || login.length() > 150) {
            throw new ValidacionException("El usuario/correo de la cuenta es obligatorio (maximo 150 caracteres).");
        }
        if (url.length() > 255) {
            throw new ValidacionException("La URL no puede superar 255 caracteres.");
        }
        if (!url.isEmpty() && !url.matches("(?i)^https?://.+")) {
            throw new ValidacionException("La URL debe empezar con http:// o https://");
        }
        if (notas.length() > 2000) {
            throw new ValidacionException("Las notas no pueden superar 2000 caracteres.");
        }
        if (password == null) {
            password = "";
        }
        if (nuevo && password.isEmpty()) {
            throw new ValidacionException("La contrasena de la cuenta es obligatoria.");
        }
        if (RsaCipher.excedeLimite(password)) {
            throw new ValidacionException("La contrasena no puede superar " + RsaCipher.MAX_BYTES + " bytes.");
        }

        try {
            if (!tipoDAO.existe(tipoId)) {
                throw new ValidacionException("Selecciona un tipo de acceso valido.");
            }

            AccesoDTO a;
            if (nuevo) {
                a = new AccesoDTO();
                a.setUsuarioId(usuarioId);
            } else {
                a = accesoDAO.buscarPorId(id, usuarioId)
                        .orElseThrow(() -> new ValidacionException("El acceso no existe."));
            }
            a.setTipoId(tipoId);
            a.setTitulo(titulo);
            a.setUrl(url.isEmpty() ? null : url);
            a.setLogin(login);
            a.setNotas(notas.isEmpty() ? null : notas);
            if (!password.isEmpty()) {
                a.setPasswordEnc(RsaCipher.cifrar(password, llaves.getPublica()));
            }

            if (nuevo) {
                accesoDAO.insertar(a);
            } else {
                accesoDAO.actualizar(a);
            }
        } catch (SQLException e) {
            throw new AppException("Error al guardar acceso", e);
        } catch (GeneralSecurityException e) {
            throw new AppException("Error al cifrar contrasena", e);
        }
    }

    public void eliminar(int id, int usuarioId) throws ValidacionException {
        try {
            if (!accesoDAO.eliminar(id, usuarioId)) {
                throw new ValidacionException("El acceso no existe.");
            }
        } catch (SQLException e) {
            throw new AppException("Error al eliminar acceso", e);
        }
    }

    /** Descifra con la llave privada la contrasena de un acceso del usuario. */
    public String revelarPassword(int id, int usuarioId, LlavesSesion llaves) throws ValidacionException {
        AccesoDTO a = obtener(id, usuarioId)
                .orElseThrow(() -> new ValidacionException("El acceso no existe."));
        try {
            return RsaCipher.descifrar(a.getPasswordEnc(), llaves.getPrivada());
        } catch (GeneralSecurityException e) {
            throw new AppException("Error al descifrar contrasena", e);
        }
    }

    private static String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
