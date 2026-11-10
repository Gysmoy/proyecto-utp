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
import pe.edu.utp.dto.UsuarioSesionDTO;
import pe.edu.utp.security.LlavesSesion;

/**
 * Protege el modulo interno: exige usuario logueado y sus llaves en sesion.
 */
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session = request.getSession(false);

        boolean logueado = session != null
                && session.getAttribute(UsuarioSesionDTO.ATRIBUTO) != null
                && session.getAttribute(LlavesSesion.ATRIBUTO) != null;

        if (!logueado) {
            if ("partial/ajax".equals(request.getHeader("Faces-Request"))) {
                // Una peticion ajax de Faces solo entiende la redireccion dentro de un partial-response
                response.setContentType("text/xml;charset=UTF-8");
                response.getWriter().write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><partial-response><redirect url=\""
                        + request.getContextPath() + "/login?expirado=1\"/></partial-response>");
            } else {
                response.sendRedirect(request.getContextPath() + "/login");
            }
            return;
        }

        // Que el navegador no guarde en cache paginas con datos privados
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        chain.doFilter(req, res);
    }
}
