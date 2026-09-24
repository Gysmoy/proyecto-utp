package pe.edu.utp.filter;

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
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Proteccion CSRF: cada sesion tiene un token aleatorio que los formularios
 * envian en el campo oculto "_csrf". Todo POST sin token valido se rechaza.
 */
public class CsrfFilter implements Filter {

    public static final String ATRIBUTO = "csrf";
    public static final String PARAMETRO = "_csrf";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            HttpSession session = request.getSession(false);
            String esperado = session == null ? null : (String) session.getAttribute(ATRIBUTO);
            String recibido = request.getParameter(PARAMETRO);
            if (!tokenValido(esperado, recibido)) {
                if ("fetch".equals(request.getHeader("X-Requested-With"))) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                } else {
                    response.sendRedirect(request.getContextPath() + "/login?expirado=1");
                }
                return;
            }
        }

        nuevoTokenSiFalta(request.getSession());
        chain.doFilter(req, res);
    }

    public static void nuevoTokenSiFalta(HttpSession session) {
        if (session.getAttribute(ATRIBUTO) == null) {
            byte[] bytes = new byte[32];
            RANDOM.nextBytes(bytes);
            session.setAttribute(ATRIBUTO, Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
        }
    }

    private static boolean tokenValido(String esperado, String recibido) {
        if (esperado == null || recibido == null) {
            return false;
        }
        return MessageDigest.isEqual(esperado.getBytes(StandardCharsets.UTF_8),
                recibido.getBytes(StandardCharsets.UTF_8));
    }
}
