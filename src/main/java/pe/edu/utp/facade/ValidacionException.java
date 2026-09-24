package pe.edu.utp.facade;

/**
 * Error de negocio con un mensaje apto para mostrarse al usuario.
 */
public class ValidacionException extends Exception {

    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
