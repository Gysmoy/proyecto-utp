package pe.edu.utp.api;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import pe.edu.utp.facade.AppException;
import pe.edu.utp.facade.AuthFacade;

/**
 * Autenticacion de la API REST con HTTP Basic (usuario o correo + contrasena).
 * La API no usa la sesion web: cada peticion trae sus credenciales. Con la
 * contrasena se recupera la llave privada RSA (igual que en el login) y se
 * deja en el request para los servlets de /api.
 */
public class ApiAuthFilter implements Filter {

    public static final String USUARIO = "apiUsuario";
    public static final String LLAVES = "apiLlaves";
    private static final Logger LOG = Logger.getLogger(ApiAuthFilter.class.getName());

    private final AuthFacade authFacade = new AuthFacade();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        // Respuestas con datos privados: que ni el navegador ni un proxy las guarden
        response.setHeader("Cache-Control", "no-store");

        String[] credenciales = leerBasic(request.getHeader("Authorization"));
        Optional<AuthFacade.Autenticado> auth = Optional.empty();
        if (credenciales != null) {
            try {
                auth = authFacade.login(credenciales[0], credenciales[1]);
            } catch (AppException e) {
                LOG.log(Level.SEVERE, "Error al autenticar en la API", e);
                ApiJson.error(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Ocurrio un error, intenta nuevamente.");
                return;
            }
        }
        if (auth.isEmpty()) {
            response.setHeader("WWW-Authenticate", "Basic realm=\"Gestor de Accesos\", charset=\"UTF-8\"");
            ApiJson.error(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Credenciales invalidas o ausentes (cabecera Authorization: Basic).");
            return;
        }

        request.setAttribute(USUARIO, auth.get().usuario());
        request.setAttribute(LLAVES, auth.get().llaves());
        chain.doFilter(req, res);
    }

    /** Devuelve {usuario, contrasena} a partir de "Basic base64(usuario:contrasena)", o null. */
    private static String[] leerBasic(String header) {
        if (header == null || !header.regionMatches(true, 0, "Basic ", 0, 6)) {
            return null;
        }
        try {
            String plano = new String(Base64.getDecoder().decode(header.substring(6).trim()), StandardCharsets.UTF_8);
            int separador = plano.indexOf(':');
            return separador < 0 ? null : new String[]{plano.substring(0, separador), plano.substring(separador + 1)};
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
