package pe.edu.utp.controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.util.Map;
import pe.edu.utp.dto.UsuarioSesionDTO;
import pe.edu.utp.security.LlavesSesion;

/**
 * Controlador de login.xhtml. El formulario se envia por AJAX en JSON a
 * POST /api/auth/login (auth.js); aqui solo se prepara la vista.
 */
@Named
@RequestScoped
public class LoginBean {

    /** f:viewAction al abrir la pagina: con sesion va directo a sus accesos. */
    public String alEntrar() {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, Object> sesion = ec.getSessionMap();
        if (sesion.get(UsuarioSesionDTO.ATRIBUTO) != null && sesion.get(LlavesSesion.ATRIBUTO) != null) {
            return "/accesos?faces-redirect=true";
        }
        Map<String, String> parametros = ec.getRequestParameterMap();
        if (parametros.containsKey("expirado")) {
            Mensajes.error("Tu sesión expiró, vuelve a ingresar.");
        }
        if (parametros.containsKey("registrado")) {
            Mensajes.info("Cuenta creada. Ya puedes iniciar sesión.");
        }
        return null;
    }
}
