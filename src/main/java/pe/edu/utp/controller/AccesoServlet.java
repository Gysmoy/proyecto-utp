package pe.edu.utp.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import pe.edu.utp.dto.AccesoDTO;
import pe.edu.utp.dto.UsuarioSesionDTO;
import pe.edu.utp.facade.AccesoFacade;
import pe.edu.utp.facade.AppException;
import pe.edu.utp.facade.ValidacionException;
import pe.edu.utp.security.LlavesSesion;

/**
 * Modulo interno de gestion de accesos.
 * GET  /accesos            lista (filtros: q, tipo)
 * GET  /accesos/nuevo      formulario de alta
 * GET  /accesos/editar?id  formulario de edicion
 * POST /accesos/guardar    crear / actualizar
 * POST /accesos/eliminar   eliminar
 * POST /accesos/revelar    devuelve la contrasena descifrada en JSON
 */
@WebServlet(name = "AccesoServlet", urlPatterns = {"/accesos", "/accesos/*"})
public class AccesoServlet extends HttpServlet {

    private static final String LISTA = "/WEB-INF/views/accesos/lista.jsp";
    private static final String FORM = "/WEB-INF/views/accesos/form.jsp";
    private static final Logger LOG = Logger.getLogger(AccesoServlet.class.getName());

    private final AccesoFacade accesoFacade = new AccesoFacade();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String accion = accion(request);
        int usuarioId = usuario(request).getId();
        try {
            switch (accion) {
                case "/" -> {
                    String q = request.getParameter("q");
                    Integer tipo = entero(request.getParameter("tipo"));
                    request.setAttribute("accesos", accesoFacade.listar(usuarioId, q, tipo));
                    request.setAttribute("tipos", accesoFacade.listarTipos());
                    request.setAttribute("q", q);
                    request.setAttribute("tipoSel", tipo);
                    request.getRequestDispatcher(LISTA).forward(request, response);
                }
                case "/nuevo" -> {
                    request.setAttribute("acceso", new AccesoDTO());
                    mostrarFormulario(request, response);
                }
                case "/editar" -> {
                    Integer id = entero(request.getParameter("id"));
                    Optional<AccesoDTO> acceso = id == null ? Optional.empty() : accesoFacade.obtener(id, usuarioId);
                    if (acceso.isEmpty()) {
                        flash(request, "El acceso no existe.", true);
                        redirigirLista(request, response);
                        return;
                    }
                    request.setAttribute("acceso", acceso.get());
                    mostrarFormulario(request, response);
                }
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (AppException e) {
            LOG.log(Level.SEVERE, "Error en modulo de accesos", e);
            throw new ServletException("Ocurrio un error al procesar la solicitud.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        switch (accion(request)) {
            case "/guardar" -> guardar(request, response);
            case "/eliminar" -> eliminar(request, response);
            case "/revelar" -> revelar(request, response);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void guardar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int usuarioId = usuario(request).getId();
        Integer id = entero(request.getParameter("id"));
        Integer tipoId = entero(request.getParameter("tipoId"));
        try {
            accesoFacade.guardar(usuarioId, llaves(request), id == null ? 0 : id, tipoId == null ? 0 : tipoId,
                    request.getParameter("titulo"), request.getParameter("url"),
                    request.getParameter("login"), request.getParameter("password"),
                    request.getParameter("notas"));
            flash(request, id == null || id == 0 ? "Acceso creado." : "Acceso actualizado.", false);
            redirigirLista(request, response);
        } catch (ValidacionException e) {
            // Se vuelve a mostrar el formulario con lo que el usuario escribio
            AccesoDTO a = new AccesoDTO();
            a.setId(id == null ? 0 : id);
            a.setTipoId(tipoId == null ? 0 : tipoId);
            a.setTitulo(request.getParameter("titulo"));
            a.setUrl(request.getParameter("url"));
            a.setLogin(request.getParameter("login"));
            a.setNotas(request.getParameter("notas"));
            request.setAttribute("acceso", a);
            request.setAttribute("error", e.getMessage());
            mostrarFormulario(request, response);
        } catch (AppException e) {
            LOG.log(Level.SEVERE, "Error al guardar acceso", e);
            flash(request, "Ocurrio un error al guardar el acceso.", true);
            redirigirLista(request, response);
        }
    }

    private void eliminar(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer id = entero(request.getParameter("id"));
        try {
            if (id == null) {
                throw new ValidacionException("El acceso no existe.");
            }
            accesoFacade.eliminar(id, usuario(request).getId());
            flash(request, "Acceso eliminado.", false);
        } catch (ValidacionException e) {
            flash(request, e.getMessage(), true);
        } catch (AppException e) {
            LOG.log(Level.SEVERE, "Error al eliminar acceso", e);
            flash(request, "Ocurrio un error al eliminar el acceso.", true);
        }
        redirigirLista(request, response);
    }

    private void revelar(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        Integer id = entero(request.getParameter("id"));
        try {
            if (id == null) {
                throw new ValidacionException("El acceso no existe.");
            }
            String password = accesoFacade.revelarPassword(id, usuario(request).getId(), llaves(request));
            response.getWriter().write("{\"password\":\"" + json(password) + "\"}");
        } catch (ValidacionException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"error\":\"" + json(e.getMessage()) + "\"}");
        } catch (AppException e) {
            LOG.log(Level.SEVERE, "Error al revelar contrasena", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"No se pudo descifrar la contrasena.\"}");
        }
    }

    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("tipos", accesoFacade.listarTipos());
        request.getRequestDispatcher(FORM).forward(request, response);
    }

    private void redirigirLista(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/accesos");
    }

    private static void flash(HttpServletRequest request, String mensaje, boolean error) {
        HttpSession session = request.getSession();
        session.setAttribute("flash", mensaje);
        session.setAttribute("flashError", error);
    }

    private static String accion(HttpServletRequest request) {
        String path = request.getPathInfo();
        return path == null || path.isEmpty() ? "/" : path;
    }

    private static UsuarioSesionDTO usuario(HttpServletRequest request) {
        return (UsuarioSesionDTO) request.getSession().getAttribute(LoginServlet.USUARIO);
    }

    private static LlavesSesion llaves(HttpServletRequest request) {
        return (LlavesSesion) request.getSession().getAttribute(LlavesSesion.ATRIBUTO);
    }

    private static Integer entero(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String json(String texto) {
        StringBuilder sb = new StringBuilder(texto.length() + 16);
        for (char c : texto.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '<' -> sb.append("\\u003c");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }
}
