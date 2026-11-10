package pe.edu.utp.controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.logging.Level;
import java.util.logging.Logger;
import pe.edu.utp.facade.AppException;
import pe.edu.utp.facade.AuthFacade;
import pe.edu.utp.facade.ValidacionException;

/**
 * Controlador de registro.xhtml.
 */
@Named
@RequestScoped
public class RegistroBean {

    private static final Logger LOG = Logger.getLogger(RegistroBean.class.getName());

    @Inject
    private AuthFacade authFacade;

    private String nombre;
    private String usuario;
    private String email;
    private String password;
    private String confirmar;

    public String registrar() {
        try {
            authFacade.registrar(nombre, usuario, email, password, confirmar);
        } catch (ValidacionException e) {
            Mensajes.error(e.getMessage());
            return null;
        } catch (AppException e) {
            LOG.log(Level.SEVERE, "Error en registro", e);
            Mensajes.error("Ocurrió un error, intenta nuevamente.");
            return null;
        }
        // El mensaje sobrevive a la redireccion gracias al Flash
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        Mensajes.info("Cuenta creada. Ya puedes iniciar sesión.");
        return "/login?faces-redirect=true";
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmar() {
        return confirmar;
    }

    public void setConfirmar(String confirmar) {
        this.confirmar = confirmar;
    }
}
