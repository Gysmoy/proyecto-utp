package pe.edu.utp.controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.util.Map;
import pe.edu.utp.dto.UsuarioSesionDTO;
import pe.edu.utp.security.Csrf;

/**
 * Datos de la sesion para la plantilla: nombre en la barra superior y token
 * CSRF que usan las llamadas AJAX (el cierre de sesion va por POST /api/auth/logout).
 */
@Named
@RequestScoped
public class SesionBean {

    public UsuarioSesionDTO getUsuario() {
        return (UsuarioSesionDTO) sesion().get(UsuarioSesionDTO.ATRIBUTO);
    }

    public String getCsrf() {
        return (String) sesion().get(Csrf.ATRIBUTO);
    }

    private static Map<String, Object> sesion() {
        return FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
    }
}
