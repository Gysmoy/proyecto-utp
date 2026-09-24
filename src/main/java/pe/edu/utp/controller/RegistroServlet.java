package pe.edu.utp.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import pe.edu.utp.facade.AppException;
import pe.edu.utp.facade.AuthFacade;
import pe.edu.utp.facade.ValidacionException;

@WebServlet(name = "RegistroServlet", urlPatterns = {"/registro"})
public class RegistroServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/views/registro.jsp";
    private static final Logger LOG = Logger.getLogger(RegistroServlet.class.getName());

    private final AuthFacade authFacade = new AuthFacade();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            authFacade.registrar(
                    request.getParameter("nombre"),
                    request.getParameter("usuario"),
                    request.getParameter("email"),
                    request.getParameter("password"),
                    request.getParameter("confirmar"));
            request.getSession().setAttribute("flash", "Cuenta creada. Ya puedes iniciar sesion.");
            response.sendRedirect(request.getContextPath() + "/login");
        } catch (ValidacionException e) {
            reenviar(request, response, e.getMessage());
        } catch (AppException e) {
            LOG.log(Level.SEVERE, "Error en registro", e);
            reenviar(request, response, "Ocurrio un error, intenta nuevamente.");
        }
    }

    private void reenviar(HttpServletRequest request, HttpServletResponse response, String error)
            throws ServletException, IOException {
        // Se devuelven los datos ingresados (menos las contrasenas) para no volver a escribirlos
        request.setAttribute("error", error);
        request.setAttribute("nombre", request.getParameter("nombre"));
        request.setAttribute("usuarioIngresado", request.getParameter("usuario"));
        request.setAttribute("email", request.getParameter("email"));
        request.getRequestDispatcher(VISTA).forward(request, response);
    }
}
