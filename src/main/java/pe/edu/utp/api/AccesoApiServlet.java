package pe.edu.utp.api;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import pe.edu.utp.dto.AccesoDTO;
import pe.edu.utp.dto.UsuarioSesionDTO;
import pe.edu.utp.facade.AccesoFacade;
import pe.edu.utp.facade.AppException;
import pe.edu.utp.facade.ValidacionException;
import pe.edu.utp.security.LlavesSesion;

/**
 * API REST de accesos en JSON. Requiere HTTP Basic (ver ApiAuthFilter) y
 * reutiliza AccesoFacade, asi que aplica las mismas reglas que la web.
 * GET    /api/accesos                lista (filtros: q, tipo)             200
 * GET    /api/accesos/{id}           detalle                              200 | 404
 * GET    /api/accesos/{id}/password  contrasena descifrada con RSA        200 | 404
 * POST   /api/accesos                crea                                 201 | 400 | 415
 * PUT    /api/accesos/{id}           actualiza (password opcional)        200 | 400 | 404 | 415
 * DELETE /api/accesos/{id}           elimina                              204 | 404
 */
@WebServlet(name = "AccesoApiServlet", urlPatterns = {"/api/accesos", "/api/accesos/*"})
public class AccesoApiServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(AccesoApiServlet.class.getName());

    private final AccesoFacade accesoFacade = new AccesoFacade();

    /** Ruta despues de /api/accesos: coleccion, /{id} o /{id}/password. */
    private record Ruta(boolean coleccion, int id, boolean password) {

        private static final Pattern ITEM = Pattern.compile("^/(\\d{1,9})(/password)?/?$");

        /** null si la ruta no corresponde a ningun recurso. */
        static Ruta de(HttpServletRequest request) {
            String path = request.getPathInfo();
            if (path == null || "/".equals(path)) {
                return new Ruta(true, 0, false);
            }
            Matcher m = ITEM.matcher(path);
            return m.matches() ? new Ruta(false, Integer.parseInt(m.group(1)), m.group(2) != null) : null;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Ruta ruta = Ruta.de(request);
        if (ruta == null) {
            noEncontrado(response);
            return;
        }
        int usuarioId = usuario(request).getId();
        try {
            if (ruta.coleccion()) {
                JsonArrayBuilder lista = Json.createArrayBuilder();
                for (AccesoDTO a : accesoFacade.listar(usuarioId, request.getParameter("q"),
                        entero(request.getParameter("tipo")))) {
                    lista.add(aJson(a));
                }
                ApiJson.enviar(response, HttpServletResponse.SC_OK, lista.build());
            } else if (ruta.password()) {
                String password = accesoFacade.revelarPassword(ruta.id(), usuarioId, llaves(request));
                ApiJson.enviar(response, HttpServletResponse.SC_OK, Json.createObjectBuilder()
                        .add("id", ruta.id())
                        .add("password", password)
                        .build());
            } else {
                Optional<AccesoDTO> acceso = accesoFacade.obtener(ruta.id(), usuarioId);
                if (acceso.isEmpty()) {
                    noEncontrado(response);
                    return;
                }
                ApiJson.enviar(response, HttpServletResponse.SC_OK, aJson(acceso.get()));
            }
        } catch (ValidacionException e) {
            // revelarPassword: el acceso no existe o es de otro usuario
            ApiJson.error(response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (AppException e) {
            errorInterno(response, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Ruta ruta = Ruta.de(request);
        if (ruta == null) {
            noEncontrado(response);
            return;
        }
        if (!ruta.coleccion()) {
            metodoNoPermitido(response, ruta.password() ? "GET" : "GET, PUT, DELETE");
            return;
        }
        if (!ApiJson.esJson(request)) {
            tipoNoSoportado(response);
            return;
        }
        int usuarioId = usuario(request).getId();
        try {
            int id = guardar(request, usuarioId, 0, ApiJson.leerObjeto(request));
            AccesoDTO creado = accesoFacade.obtener(id, usuarioId)
                    .orElseThrow(() -> new AppException("No se encontro el acceso recien creado", null));
            response.setHeader("Location", request.getContextPath() + "/api/accesos/" + id);
            ApiJson.enviar(response, HttpServletResponse.SC_CREATED, aJson(creado));
        } catch (ValidacionException e) {
            ApiJson.error(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (AppException e) {
            errorInterno(response, e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Ruta ruta = Ruta.de(request);
        if (ruta == null) {
            noEncontrado(response);
            return;
        }
        if (ruta.coleccion() || ruta.password()) {
            metodoNoPermitido(response, ruta.coleccion() ? "GET, POST" : "GET");
            return;
        }
        if (!ApiJson.esJson(request)) {
            tipoNoSoportado(response);
            return;
        }
        int usuarioId = usuario(request).getId();
        try {
            if (accesoFacade.obtener(ruta.id(), usuarioId).isEmpty()) {
                noEncontrado(response);
                return;
            }
            guardar(request, usuarioId, ruta.id(), ApiJson.leerObjeto(request));
            AccesoDTO actualizado = accesoFacade.obtener(ruta.id(), usuarioId)
                    .orElseThrow(() -> new AppException("No se encontro el acceso actualizado", null));
            ApiJson.enviar(response, HttpServletResponse.SC_OK, aJson(actualizado));
        } catch (ValidacionException e) {
            ApiJson.error(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (AppException e) {
            errorInterno(response, e);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Ruta ruta = Ruta.de(request);
        if (ruta == null) {
            noEncontrado(response);
            return;
        }
        if (ruta.coleccion() || ruta.password()) {
            metodoNoPermitido(response, ruta.coleccion() ? "GET, POST" : "GET");
            return;
        }
        try {
            accesoFacade.eliminar(ruta.id(), usuario(request).getId());
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (ValidacionException e) {
            ApiJson.error(response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (AppException e) {
            errorInterno(response, e);
        }
    }

    /** Crea (id == 0) o actualiza con los campos del JSON; las reglas las valida AccesoFacade. */
    private int guardar(HttpServletRequest request, int usuarioId, int id, JsonObject json)
            throws ValidacionException {
        return accesoFacade.guardar(usuarioId, llaves(request), id, ApiJson.entero(json, "tipoId"),
                ApiJson.texto(json, "titulo"), ApiJson.texto(json, "url"), ApiJson.texto(json, "login"),
                ApiJson.texto(json, "password"), ApiJson.texto(json, "notas"));
    }

    /** La contrasena nunca sale en este JSON; solo por /{id}/password. */
    private static JsonObject aJson(AccesoDTO a) {
        JsonObjectBuilder b = Json.createObjectBuilder()
                .add("id", a.getId())
                .add("titulo", a.getTitulo())
                .add("tipoId", a.getTipoId())
                .add("tipo", a.getTipoNombre())
                .add("login", a.getLogin());
        ApiJson.agregar(b, "url", a.getUrl());
        ApiJson.agregar(b, "notas", a.getNotas());
        ApiJson.agregar(b, "creado", a.getCreatedAt());
        ApiJson.agregar(b, "actualizado", a.getUpdatedAt());
        return b.build();
    }

    private static void noEncontrado(HttpServletResponse response) throws IOException {
        ApiJson.error(response, HttpServletResponse.SC_NOT_FOUND, "El acceso no existe.");
    }

    private static void metodoNoPermitido(HttpServletResponse response, String permitidos) throws IOException {
        response.setHeader("Allow", permitidos);
        ApiJson.error(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Metodo no permitido. Usa: " + permitidos + ".");
    }

    private static void tipoNoSoportado(HttpServletResponse response) throws IOException {
        ApiJson.error(response, HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                "Envia el cuerpo con Content-Type: application/json.");
    }

    private static void errorInterno(HttpServletResponse response, AppException e) throws IOException {
        LOG.log(Level.SEVERE, "Error en la API de accesos", e);
        ApiJson.error(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ocurrio un error, intenta nuevamente.");
    }

    private static UsuarioSesionDTO usuario(HttpServletRequest request) {
        return (UsuarioSesionDTO) request.getAttribute(ApiAuthFilter.USUARIO);
    }

    private static LlavesSesion llaves(HttpServletRequest request) {
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
