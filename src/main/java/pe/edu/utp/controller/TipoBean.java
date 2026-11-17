package pe.edu.utp.controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;
import pe.edu.utp.dto.TipoAccesoDTO;
import pe.edu.utp.facade.AccesoFacade;

/**
 * Catalogo de tipos para los p:selectOneMenu de accesos.xhtml. Los accesos en si
 * no pasan por Faces: accesos.js los pide y los guarda por AJAX en JSON a /api/accesos.
 */
@Named
@RequestScoped
public class TipoBean {

    @Inject
    private AccesoFacade accesoFacade;

    private List<TipoAccesoDTO> tipos;

    public List<TipoAccesoDTO> getTipos() {
        if (tipos == null) {
            tipos = accesoFacade.listarTipos();
        }
        return tipos;
    }
}
