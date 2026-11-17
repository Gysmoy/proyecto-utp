package pe.edu.utp.api;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import pe.edu.utp.dto.AccesoDTO;
import pe.edu.utp.dto.UsuarioSesionDTO;
import pe.edu.utp.facade.AccesoFacade;
import pe.edu.utp.facade.AppException;
import pe.edu.utp.facade.ValidacionException;
import pe.edu.utp.security.LlavesSesion;

/**
 * Recurso REST de accesos (JSON). Lo consume la pagina por AJAX con la sesion
 * y cualquier cliente externo con HTTP Basic (ver ApiAuthFilter). Reutiliza
 * AccesoFacade, asi que aplica las mismas reglas de negocio.
 * GET    /api/accesos                lista (filtros: q, tipo)             200
 * GET    /api/accesos/{id}           detalle                              200 | 404
 * GET    /api/accesos/{id}/password  contrasena descifrada con RSA        200 | 404
 * POST   /api/accesos                crea                                 201 | 400 | 415
 * PUT    /api/accesos/{id}           actualiza (password opcional)        200 | 400 | 404 | 415
 * DELETE /api/accesos/{id}           elimina                              204 | 404
 */
@Path("accesos")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class AccesoResource {

    private static final String ID = "{id: \\d{1,9}}";

    @Inject
    private AccesoFacade accesoFacade;

    @Context
    private HttpServletRequest request;

    @GET
    public List<ApiDTO.Acceso> listar(@QueryParam("q") String q, @QueryParam("tipo") String tipo) {
        return accesoFacade.listar(usuarioId(), q, entero(tipo)).stream().map(ApiDTO.Acceso::de).toList();
    }

    @GET
    @Path(ID)
    public Response obtener(@PathParam("id") int id) {
        return accesoFacade.obtener(id, usuarioId())
                .map(a -> Response.ok(ApiDTO.Acceso.de(a)).build())
                .orElseGet(AccesoResource::noEncontrado);
    }

    @GET
    @Path(ID + "/password")
    public Response password(@PathParam("id") int id) {
        try {
            String password = accesoFacade.revelarPassword(id, usuarioId(), llaves());
            return Response.ok(new ApiDTO.Password(id, password)).build();
        } catch (ValidacionException e) {
            // El acceso no existe o es de otro usuario
            return noEncontrado();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crear(ApiDTO.AccesoPeticion peticion, @Context UriInfo uriInfo) throws ValidacionException {
        int id = guardar(0, peticion);
        return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(id)).build())
                .entity(ApiDTO.Acceso.de(buscar(id)))
                .build();
    }

    @PUT
    @Path(ID)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response actualizar(@PathParam("id") int id, ApiDTO.AccesoPeticion peticion) throws ValidacionException {
        if (accesoFacade.obtener(id, usuarioId()).isEmpty()) {
            return noEncontrado();
        }
        guardar(id, peticion);
        return Response.ok(ApiDTO.Acceso.de(buscar(id))).build();
    }

    @DELETE
    @Path(ID)
    public Response eliminar(@PathParam("id") int id) {
        try {
            accesoFacade.eliminar(id, usuarioId());
            return Response.noContent().build();
        } catch (ValidacionException e) {
            return noEncontrado();
        }
    }

    /** Crea (id == 0) o actualiza; las reglas las valida AccesoFacade y salen como 400. */
    private int guardar(int id, ApiDTO.AccesoPeticion p) throws ValidacionException {
        if (p == null) {
            throw new ValidacionException("El cuerpo debe ser un objeto JSON valido.");
        }
        int tipoId = p.tipoId() == null ? 0 : p.tipoId();
        return accesoFacade.guardar(usuarioId(), llaves(), id, tipoId, p.titulo(), p.url(), p.login(),
                p.password(), p.notas());
    }

    private AccesoDTO buscar(int id) {
        return accesoFacade.obtener(id, usuarioId())
                .orElseThrow(() -> new AppException("No se encontro el acceso " + id + " recien guardado", null));
    }

    private static Response noEncontrado() {
        return ApiDTO.ErrorApi.respuesta(404, "El acceso no existe.");
    }

    private int usuarioId() {
        return ((UsuarioSesionDTO) request.getAttribute(ApiAuthFilter.USUARIO)).getId();
    }

    private LlavesSesion llaves() {
        return (LlavesSesion) request.getAttribute(ApiAuthFilter.LLAVES);
    }

    private static Integer entero(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
