package pe.edu.utp.api;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Optional;
import pe.edu.utp.dto.UsuarioSesionDTO;
import pe.edu.utp.facade.AuthFacade;
import pe.edu.utp.facade.ValidacionException;
import pe.edu.utp.security.Csrf;
import pe.edu.utp.security.LlavesSesion;

/**
 * Autenticacion por AJAX en JSON para las paginas login y registro.
 * POST /api/auth/login     {usuario, password}                      200 | 400 | 401
 * POST /api/auth/registro  {nombre, usuario, email, password, ...}  201 | 400
 * POST /api/auth/logout    cabecera X-CSRF-Token                    204 | 403
 */
@Path("auth")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    private AuthFacade authFacade;

    @Context
    private HttpServletRequest request;

    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(ApiDTO.Credenciales credenciales) {
        if (credenciales == null) {
            return ApiDTO.ErrorApi.respuesta(400, "El cuerpo debe ser un objeto JSON valido.");
        }
        Optional<AuthFacade.Autenticado> auth = authFacade.login(credenciales.usuario(), credenciales.password());
        if (auth.isEmpty()) {
            // Sin WWW-Authenticate: el navegador no debe abrir su ventana de login
            return ApiDTO.ErrorApi.respuesta(401, "Usuario o contraseña incorrectos.");
        }

        // Nueva sesion al loguearse para evitar session fixation
        HttpSession anterior = request.getSession(false);
        if (anterior != null) {
            anterior.invalidate();
        }
        HttpSession sesion = request.getSession(true);
        UsuarioSesionDTO usuario = auth.get().usuario();
        sesion.setAttribute(UsuarioSesionDTO.ATRIBUTO, usuario);
        sesion.setAttribute(LlavesSesion.ATRIBUTO, auth.get().llaves());
        sesion.setAttribute(Csrf.ATRIBUTO, Csrf.nuevo());
        return Response.ok(new ApiDTO.Sesion(usuario.getNombre(), usuario.getUsuario(),
                request.getContextPath() + "/accesos")).build();
    }

    @POST
    @Path("registro")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registro(ApiDTO.Registro r) throws ValidacionException {
        if (r == null) {
            throw new ValidacionException("El cuerpo debe ser un objeto JSON valido.");
        }
        authFacade.registrar(r.nombre(), r.usuario(), r.email(), r.password(), r.confirmar());
        return Response.status(Response.Status.CREATED)
                .entity(new ApiDTO.Mensaje("Cuenta creada. Ya puedes iniciar sesión.",
                        request.getContextPath() + "/login?registrado=1"))
                .build();
    }

    @POST
    @Path("logout")
    public Response logout(@HeaderParam(Csrf.CABECERA) String token) {
        HttpSession sesion = request.getSession(false);
        if (sesion != null) {
            if (!Csrf.valido(sesion, token)) {
                return ApiDTO.ErrorApi.respuesta(403, "Falta el token CSRF o no es valido.");
            }
            sesion.invalidate();
        }
        return Response.noContent().build();
    }
}
