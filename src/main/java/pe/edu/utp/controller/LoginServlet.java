package pe.edu.utp.controller;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import pe.edu.utp.facade.AppException;
import pe.edu.utp.facade.AuthFacade;
import pe.edu.utp.filter.CsrfFilter;
import pe.edu.utp.security.LlavesSesion;

/**
 *
 * @author Christiam Calero
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    public static final String USUARIO = "usuario";
    private static final int MINUTOS_SESION = 15;
    private static final Logger LOG = Logger.getLogger(LoginServlet.class.getName());

    private final AuthFacade authFacade = new AuthFacade();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(USUARIO) != null
                && session.getAttribute(LlavesSesion.ATRIBUTO) != null) {
            response.sendRedirect(request.getContextPath() + "/accesos");
            return;
        }
        if (request.getParameter("expirado") != null) {
            request.setAttribute("error", "Tu sesion expiro, vuelve a ingresar.");
        }
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String user = request.getParameter("usuario");
        String pass = request.getParameter("password");

        Optional<AuthFacade.Autenticado> resultado;
        try {
            resultado = authFacade.login(user, pass);
        } catch (AppException e) {
            LOG.log(Level.SEVERE, "Error en login", e);
            request.setAttribute("error", "Ocurrio un error, intenta nuevamente.");
            request.setAttribute("usuarioIngresado", user);
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            return;
        }

        if (resultado.isPresent()) {
            // Nueva sesion al loguearse para evitar session fixation
            HttpSession anterior = request.getSession(false);
            if (anterior != null) {
                anterior.invalidate();
            }
            HttpSession session = request.getSession(true);
            session.setMaxInactiveInterval(MINUTOS_SESION * 60);
            session.setAttribute(USUARIO, resultado.get().usuario());
            session.setAttribute(LlavesSesion.ATRIBUTO, resultado.get().llaves());
            CsrfFilter.nuevoTokenSiFalta(session);

            response.sendRedirect(request.getContextPath() + "/accesos");
        } else {
            request.setAttribute("error", "Usuario o contrasena incorrectos.");
            request.setAttribute("usuarioIngresado", user);
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
        }

    }

}
