package pe.edu.utp.api;

import jakarta.json.bind.JsonbException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;
import pe.edu.utp.facade.ValidacionException;

/**
 * Convierte cualquier excepcion de la API en {"status", "error"} con su codigo HTTP:
 * reglas de negocio 400, JSON mal formado 400, rutas o metodos invalidos 404/405/415
 * y errores tecnicos 500 (el detalle va al log, no al cliente).
 */
@Provider
public class ErrorMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(ErrorMapper.class.getName());
    private static final String JSON_INVALIDO = "El cuerpo debe ser un objeto JSON valido.";

    @Override
    public Response toResponse(Throwable e) {
        if (e instanceof ValidacionException) {
            return ApiDTO.ErrorApi.respuesta(400, e.getMessage());
        }
        if (e instanceof WebApplicationException w) {
            return desdeHttp(w.getResponse());
        }
        if (e instanceof ProcessingException || e instanceof JsonbException) {
            return ApiDTO.ErrorApi.respuesta(400, JSON_INVALIDO);
        }
        LOG.log(Level.SEVERE, "Error en la API REST", e);
        return ApiDTO.ErrorApi.respuesta(500, "Ocurrio un error, intenta nuevamente.");
    }

    /** Errores que lanza el propio JAX-RS (ruta inexistente, metodo o Content-Type no soportado). */
    private static Response desdeHttp(Response original) {
        int status = original.getStatus();
        String allow = original.getHeaderString(HttpHeaders.ALLOW);
        String mensaje = switch (status) {
            case 400 -> JSON_INVALIDO;
            case 404 -> "El recurso no existe.";
            case 405 -> "Metodo no permitido" + (allow == null ? "." : ". Usa: " + allow + ".");
            case 406 -> "La API solo responde application/json.";
            case 415 -> "Envia el cuerpo con Content-Type: application/json.";
            default -> "Error HTTP " + status + ".";
        };
        Response respuesta = ApiDTO.ErrorApi.respuesta(status, mensaje);
        if (allow != null) {
            respuesta.getHeaders().putSingle(HttpHeaders.ALLOW, allow);
        }
        return respuesta;
    }
}
