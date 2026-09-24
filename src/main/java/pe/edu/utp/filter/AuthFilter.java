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
import pe.edu.utp.controller.LoginServlet;
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
                && session.getAttribute(LoginServlet.USUARIO) != null
                && session.getAttribute(LlavesSesion.ATRIBUTO) != null;

        if (!logueado) {
            if ("fetch".equals(request.getHeader("X-Requested-With"))) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
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
