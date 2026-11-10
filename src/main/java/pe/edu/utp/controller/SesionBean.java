package pe.edu.utp.controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import pe.edu.utp.dto.UsuarioSesionDTO;

/**
 * Datos del usuario logueado para la barra superior y cierre de sesion.
 */
@Named
@RequestScoped
public class SesionBean {

    public UsuarioSesionDTO getUsuario() {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        return (UsuarioSesionDTO) ec.getSessionMap().get(UsuarioSesionDTO.ATRIBUTO);
    }

    public String salir() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/login?faces-redirect=true";
    }
}
