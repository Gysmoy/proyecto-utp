package pe.edu.utp.api;

import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonString;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import jakarta.json.JsonWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import pe.edu.utp.facade.ValidacionException;

/**
 * Lectura y escritura de JSON (Jakarta JSON Processing) para la API REST.
 */
final class ApiJson {

    private ApiJson() {
    }

    static void enviar(HttpServletResponse response, int status, JsonStructure cuerpo) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        try (JsonWriter writer = Json.createWriter(response.getWriter())) {
            writer.write(cuerpo);
        }
    }

    /** Respuesta de error uniforme: {"status": 404, "error": "mensaje"}. */
    static void error(HttpServletResponse response, int status, String mensaje) throws IOException {
        enviar(response, status, Json.createObjectBuilder()
                .add("status", status)
                .add("error", mensaje)
                .build());
    }

    static boolean esJson(HttpServletRequest request) {
        String tipo = request.getContentType();
        return tipo != null && tipo.toLowerCase().startsWith("application/json");
    }

    /** Lee el cuerpo como objeto JSON; vacio o mal formado es un error de validacion (400). */
    static JsonObject leerObjeto(HttpServletRequest request) throws IOException, ValidacionException {
        try (JsonReader reader = Json.createReader(request.getReader())) {
            return reader.readObject();
        } catch (JsonException | IllegalStateException e) {
            throw new ValidacionException("El cuerpo debe ser un objeto JSON valido.");
        }
    }

    static String texto(JsonObject json, String campo) throws ValidacionException {
        JsonValue valor = json.get(campo);
        if (valor == null || valor.getValueType() == JsonValue.ValueType.NULL) {
            return null;
        }
        if (valor instanceof JsonString s) {
            return s.getString();
        }
        throw new ValidacionException("El campo \"" + campo + "\" debe ser texto.");
    }

    static int entero(JsonObject json, String campo) throws ValidacionException {
        if (json.get(campo) instanceof JsonNumber n && n.isIntegral()) {
            try {
                return n.intValueExact();
            } catch (ArithmeticException e) {
                // fuera del rango de int: cae al error de abajo
            }
        }
        throw new ValidacionException("El campo \"" + campo + "\" es obligatorio y debe ser un numero entero.");
    }

    /** JsonObjectBuilder no acepta null: se escribe null de JSON o el texto del valor. */
    static JsonObjectBuilder agregar(JsonObjectBuilder builder, String campo, Object valor) {
        return valor == null ? builder.addNull(campo) : builder.add(campo, valor.toString());
    }
}
