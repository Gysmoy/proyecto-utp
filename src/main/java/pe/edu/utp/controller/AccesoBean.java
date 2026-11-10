package pe.edu.utp.controller;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.primefaces.PrimeFaces;
import pe.edu.utp.dto.AccesoDTO;
import pe.edu.utp.dto.TipoAccesoDTO;
import pe.edu.utp.dto.UsuarioSesionDTO;
import pe.edu.utp.facade.AccesoFacade;
import pe.edu.utp.facade.AppException;
import pe.edu.utp.facade.ValidacionException;
import pe.edu.utp.security.LlavesSesion;

/**
 * Controlador de accesos.xhtml: listado con filtros y CRUD en un dialogo.
 * Las llaves RSA se leen de la sesion en cada operacion, nunca se guardan aqui:
 * este bean es serializable y LlavesSesion no debe serlo.
 */
@Named
@ViewScoped
public class AccesoBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(AccesoBean.class.getName());
    // Los errores del dialogo se muestran dentro de el, no en los avisos globales
    private static final String ERRORES_FORM = "frmAcceso:erroresForm";

    @Inject
    private AccesoFacade accesoFacade;

    private String q;
    private Integer tipoFiltro;
    private List<AccesoDTO> accesos;
    private List<TipoAccesoDTO> tipos;
    private AccesoDTO formulario = new AccesoDTO();
    private String password;

    @PostConstruct
    public void iniciar() {
        tipos = accesoFacade.listarTipos();
        cargar();
    }

    public void buscar() {
        try {
            cargar();
        } catch (AppException e) {
            LOG.log(Level.SEVERE, "Error al listar accesos", e);
            Mensajes.error("Ocurrió un error al listar los accesos.");
        }
    }

    public void limpiarFiltros() {
        q = null;
        tipoFiltro = null;
        buscar();
    }

    public void nuevo() {
        formulario = new AccesoDTO();
        password = null;
    }

    public void editar(int id) {
        try {
            Optional<AccesoDTO> acceso = accesoFacade.obtener(id, usuario().getId());
            if (acceso.isEmpty()) {
                Mensajes.error("El acceso no existe.");
                return;
            }
            formulario = acceso.get();
            password = null;
            PrimeFaces.current().executeScript("PF('dlgAcceso').show()");
        } catch (AppException e) {
            LOG.log(Level.SEVERE, "Error al obtener acceso", e);
            Mensajes.error("Ocurrió un error al abrir el acceso.");
        }
    }

    public void guardar() {
        boolean esNuevo = formulario.getId() == 0;
        try {
            accesoFacade.guardar(usuario().getId(), llaves(), formulario.getId(), formulario.getTipoId(),
                    formulario.getTitulo(), formulario.getUrl(), formulario.getLogin(), password,
                    formulario.getNotas());
        } catch (ValidacionException e) {
            Mensajes.error(ERRORES_FORM, e.getMessage());
            return;
        } catch (AppException e) {
            LOG.log(Level.SEVERE, "Error al guardar acceso", e);
            Mensajes.error(ERRORES_FORM, "Ocurrió un error al guardar el acceso.");
            return;
        }
        password = null;
        Mensajes.info(esNuevo ? "Acceso creado." : "Acceso actualizado.");
        buscar();
        PrimeFaces.current().executeScript("PF('dlgAcceso').hide()");
    }

    public void eliminar(int id) {
        try {
            accesoFacade.eliminar(id, usuario().getId());
            Mensajes.info("Acceso eliminado.");
            buscar();
        } catch (ValidacionException e) {
            Mensajes.error(e.getMessage());
        } catch (AppException e) {
            LOG.log(Level.SEVERE, "Error al eliminar acceso", e);
            Mensajes.error("Ocurrió un error al eliminar el acceso.");
        }
    }

    /** Descifra la contrasena y la entrega al navegador como parametro del oncomplete. */
    public void revelar(int id) {
        try {
            String texto = accesoFacade.revelarPassword(id, usuario().getId(), llaves());
            PrimeFaces.current().ajax().addCallbackParam("password", texto);
        } catch (ValidacionException e) {
            Mensajes.error(e.getMessage());
        } catch (AppException e) {
            LOG.log(Level.SEVERE, "Error al revelar contrasena", e);
            Mensajes.error("No se pudo descifrar la contraseña.");
        }
    }

    private void cargar() {
        accesos = accesoFacade.listar(usuario().getId(), q, tipoFiltro);
    }

    private static Map<String, Object> sesion() {
        return FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
    }

    private static UsuarioSesionDTO usuario() {
        return (UsuarioSesionDTO) sesion().get(UsuarioSesionDTO.ATRIBUTO);
    }

    private static LlavesSesion llaves() {
        return (LlavesSesion) sesion().get(LlavesSesion.ATRIBUTO);
    }

    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    public Integer getTipoFiltro() {
        return tipoFiltro;
    }

    public void setTipoFiltro(Integer tipoFiltro) {
        this.tipoFiltro = tipoFiltro;
    }

    public List<AccesoDTO> getAccesos() {
        return accesos;
    }

    public List<TipoAccesoDTO> getTipos() {
        return tipos;
    }

    public AccesoDTO getFormulario() {
        return formulario;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
