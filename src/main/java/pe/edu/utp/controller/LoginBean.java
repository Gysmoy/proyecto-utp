package pe.edu.utp.controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import pe.edu.utp.dto.UsuarioSesionDTO;
import pe.edu.utp.facade.AppException;
import pe.edu.utp.facade.AuthFacade;
import pe.edu.utp.security.LlavesSesion;

/**
 * Controlador de login.xhtml.
 */
@Named
@RequestScoped
public class LoginBean {

    private static final Logger LOG = Logger.getLogger(LoginBean.class.getName());

    @Inject
    private AuthFacade authFacade;

    private String usuario;
    private String password;

    /** f:viewAction al abrir la pagina: con sesion va directo a sus accesos. */
    public String alEntrar() {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, Object> sesion = ec.getSessionMap();
        if (sesion.get(UsuarioSesionDTO.ATRIBUTO) != null && sesion.get(LlavesSesion.ATRIBUTO) != null) {
            return "/accesos?faces-redirect=true";
        }
        if (ec.getRequestParameterMap().containsKey("expirado")) {
            Mensajes.error("Tu sesión expiró, vuelve a ingresar.");
        }
        return null;
    }

    public String ingresar() {
        Optional<AuthFacade.Autenticado> resultado;
        try {
            resultado = authFacade.login(usuario, password);
        } catch (AppException e) {
            LOG.log(Level.SEVERE, "Error en login", e);
            Mensajes.error("Ocurrió un error, intenta nuevamente.");
            return null;
        }
        if (resultado.isEmpty()) {
            Mensajes.error("Usuario o contraseña incorrectos.");
            return null;
        }

        // Nueva sesion al loguearse para evitar session fixation
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.invalidateSession();
        ec.getSession(true);
        ec.getSessionMap().put(UsuarioSesionDTO.ATRIBUTO, resultado.get().usuario());
        ec.getSessionMap().put(LlavesSesion.ATRIBUTO, resultado.get().llaves());
        return "/accesos?faces-redirect=true";
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
