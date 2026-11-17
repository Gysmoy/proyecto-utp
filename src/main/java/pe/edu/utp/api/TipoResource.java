package pe.edu.utp.api;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import pe.edu.utp.facade.AccesoFacade;

/**
 * GET /api/tipos: catalogo de tipos de acceso, para saber que tipoId enviar.
 */
@Path("tipos")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class TipoResource {

    @Inject
    private AccesoFacade accesoFacade;

    @GET
    public List<ApiDTO.Tipo> listar() {
        return accesoFacade.listarTipos().stream()
                .map(t -> new ApiDTO.Tipo(t.getId(), t.getNombre()))
                .toList();
    }
}
