package pe.edu.utp.api;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import pe.edu.utp.dto.TipoAccesoDTO;
import pe.edu.utp.facade.AccesoFacade;
import pe.edu.utp.facade.AppException;

/**
 * GET /api/tipos: catalogo de tipos de acceso, para saber que tipoId enviar.
 */
@WebServlet(name = "TipoApiServlet", urlPatterns = {"/api/tipos"})
public class TipoApiServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(TipoApiServlet.class.getName());

    private final AccesoFacade accesoFacade = new AccesoFacade();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            JsonArrayBuilder tipos = Json.createArrayBuilder();
            for (TipoAccesoDTO t : accesoFacade.listarTipos()) {
                tipos.add(Json.createObjectBuilder().add("id", t.getId()).add("nombre", t.getNombre()));
            }
            ApiJson.enviar(response, HttpServletResponse.SC_OK, tipos.build());
        } catch (AppException e) {
            LOG.log(Level.SEVERE, "Error al listar tipos en la API", e);
            ApiJson.error(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ocurrio un error, intenta nuevamente.");
        }
    }
}
