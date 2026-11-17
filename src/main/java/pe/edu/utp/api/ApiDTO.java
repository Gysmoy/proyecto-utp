package pe.edu.utp.api;

import jakarta.json.bind.annotation.JsonbNillable;
import jakarta.json.bind.annotation.JsonbPropertyOrder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pe.edu.utp.dto.AccesoDTO;

/**
 * DTO de la API REST: lo que entra y sale en JSON. JSON-B los convierte
 * automaticamente (records de Java), sin armar el JSON a mano.
 */
public final class ApiDTO {

    private ApiDTO() {
    }

    /** Acceso que devuelve la API. La contrasena nunca va aqui: solo por /{id}/password. */
    @JsonbNillable
    @JsonbPropertyOrder({"id", "titulo", "tipoId", "tipo", "url", "login", "notas", "creado", "actualizado"})
    public record Acceso(int id, String titulo, int tipoId, String tipo, String url, String login,
            String notas, String creado, String actualizado) {

        static Acceso de(AccesoDTO a) {
            return new Acceso(a.getId(), a.getTitulo(), a.getTipoId(), a.getTipoNombre(), a.getUrl(), a.getLogin(),
                    a.getNotas(), texto(a.getCreatedAt()), texto(a.getUpdatedAt()));
        }
    }

    /** Cuerpo de POST y PUT /api/accesos. Al editar, password vacio conserva la contrasena actual. */
    public record AccesoPeticion(Integer tipoId, String titulo, String url, String login, String password,
            String notas) {
    }

    public record Tipo(int id, String nombre) {
    }

    @JsonbPropertyOrder({"id", "password"})
    public record Password(int id, String password) {
    }

    /** Cuerpo de POST /api/auth/login. */
    public record Credenciales(String usuario, String password) {
    }

    /** Cuerpo de POST /api/auth/registro. */
    public record Registro(String nombre, String usuario, String email, String password, String confirmar) {
    }

    /** Respuesta del login: a donde debe ir el navegador. */
    @JsonbPropertyOrder({"nombre", "usuario", "destino"})
    public record Sesion(String nombre, String usuario, String destino) {
    }

    @JsonbPropertyOrder({"mensaje", "destino"})
    public record Mensaje(String mensaje, String destino) {
    }

    /** Error uniforme de toda la API: {"status": 404, "error": "mensaje"}. */
    @JsonbPropertyOrder({"status", "error"})
    public record ErrorApi(int status, String error) {

        static Response respuesta(int status, String mensaje) {
            return Response.status(status).type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(new ErrorApi(status, mensaje)).build();
        }
    }

    private static String texto(Object valor) {
        return valor == null ? null : valor.toString();
    }
}
