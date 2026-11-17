package pe.edu.utp.security;

import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Token anti-CSRF de la sesion. Las paginas lo dejan en una etiqueta meta y el
 * JavaScript lo manda en la cabecera X-CSRF-Token en cada POST, PUT o DELETE por AJAX.
 */
public final class Csrf {

    public static final String ATRIBUTO = "csrf";
    public static final String CABECERA = "X-CSRF-Token";
    private static final SecureRandom RANDOM = new SecureRandom();

    private Csrf() {
    }

    public static String nuevo() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** Compara en tiempo constante el token recibido con el de la sesion. */
    public static boolean valido(HttpSession sesion, String recibido) {
        return sesion.getAttribute(ATRIBUTO) instanceof String esperado
                && recibido != null
                && MessageDigest.isEqual(esperado.getBytes(StandardCharsets.UTF_8),
                        recibido.getBytes(StandardCharsets.UTF_8));
    }
}
