package pe.edu.utp.controller;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

/**
 * Atajos para publicar mensajes de Faces desde los managed beans.
 */
final class Mensajes {

    private Mensajes() {
    }

    static void info(String texto) {
        agregar(null, FacesMessage.SEVERITY_INFO, texto);
    }

    static void error(String texto) {
        agregar(null, FacesMessage.SEVERITY_ERROR, texto);
    }

    /** Error ligado a un componente: no aparece en los avisos globales. */
    static void error(String clientId, String texto) {
        agregar(clientId, FacesMessage.SEVERITY_ERROR, texto);
    }

    private static void agregar(String clientId, FacesMessage.Severity severidad, String texto) {
        FacesContext.getCurrentInstance().addMessage(clientId, new FacesMessage(severidad, texto, null));
    }
}
