package pe.edu.utp.api;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import pe.edu.utp.dto.UsuarioSesionDTO;
import pe.edu.utp.facade.AppException;
import pe.edu.utp.facade.AuthFacade;
import pe.edu.utp.security.Csrf;
import pe.edu.utp.security.LlavesSesion;

/**
 * Autenticacion de /api/accesos y /api/tipos. Acepta dos formas:
 * 1. Sesion web: las llamadas AJAX de la pagina. En POST, PUT y DELETE exige
 *    ademas la cabecera X-CSRF-Token con el token de la sesion.
 * 2. HTTP Basic (usuario o correo + contrasena) para clientes externos, sin sesion:
 *    con la contrasena se recupera la llave privada RSA, igual que en el login.
 * Deja el usuario y sus llaves en el request para los recursos REST.
 */
public class ApiAuthFilter implements Filter {

    public static final String USUARIO = "apiUsuario";
    public static final String LLAVES = "apiLlaves";
    private static final Set<String> METODOS_SEGUROS = Set.of("GET", "HEAD", "OPTIONS");
    private static final Logger LOG = Logger.getLogger(ApiAuthFilter.class.getName());
    private static final Jsonb JSONB = JsonbBuilder.create();

    private final AuthFacade authFacade = new AuthFacade();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        // Respuestas con datos privados: que ni el navegador ni un proxy las guarden
        response.setHeader("Cache-Control", "no-store");

        UsuarioSesionDTO usuario;
        LlavesSesion llaves;
        String authorization = request.getHeader("Authorization");
        if (authorization != null) {
            Optional<AuthFacade.Autenticado> auth = basic(authorization, response);
            if (auth == null) {
                return;
            }
            if (auth.isEmpty()) {
                noAutenticado(request, response, "Credenciales invalidas (cabecera Authorization: Basic).");
                return;
            }
            usuario = auth.get().usuario();
            llaves = auth.get().llaves();
        } else {
            HttpSession sesion = request.getSession(false);
            usuario = sesion == null ? null : (UsuarioSesionDTO) sesion.getAttribute(UsuarioSesionDTO.ATRIBUTO);
            llaves = sesion == null ? null : (LlavesSesion) sesion.getAttribute(LlavesSesion.ATRIBUTO);
            if (usuario == null || llaves == null) {
                noAutenticado(request, response,
                        "Inicia sesion o envia la cabecera Authorization: Basic.");
                return;
            }
            if (!METODOS_SEGUROS.contains(request.getMethod())
                    && !Csrf.valido(sesion, request.getHeader(Csrf.CABECERA))) {
                error(response, HttpServletResponse.SC_FORBIDDEN, "Falta el token CSRF o no es valido.");
                return;
            }
        }

        request.setAttribute(USUARIO, usuario);
        request.setAttribute(LLAVES, llaves);
        chain.doFilter(req, res);
    }

    /** Login con HTTP Basic; null si ya se respondio un error 500. */
    private Optional<AuthFacade.Autenticado> basic(String header, HttpServletResponse response) throws IOException {
        String[] credenciales = leerBasic(header);
        if (credenciales == null) {
            return Optional.empty();
        }
        try {
            return authFacade.login(credenciales[0], credenciales[1]);
        } catch (AppException e) {
            LOG.log(Level.SEVERE, "Error al autenticar en la API", e);
            error(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ocurrio un error, intenta nuevamente.");
            return null;
        }
    }

    /**
     * 401. Solo a clientes externos se les ofrece Basic: si una llamada AJAX recibiera
     * WWW-Authenticate, el navegador abriria su propia ventana de usuario y contrasena.
     */
    private static void noAutenticado(HttpServletRequest request, HttpServletResponse response, String mensaje)
            throws IOException {
        if (!"XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            response.setHeader("WWW-Authenticate", "Basic realm=\"Gestor de Accesos\", charset=\"UTF-8\"");
        }
        error(response, HttpServletResponse.SC_UNAUTHORIZED, mensaje);
    }

    private static void error(HttpServletResponse response, int status, String mensaje) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        JSONB.toJson(new ApiDTO.ErrorApi(status, mensaje), response.getWriter());
    }

    /** Devuelve {usuario, contrasena} a partir de "Basic base64(usuario:contrasena)", o null. */
    private static String[] leerBasic(String header) {
        if (!header.regionMatches(true, 0, "Basic ", 0, 6)) {
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
