package pe.edu.utp.facade;

/**
 * Error tecnico (BD, criptografia). El detalle va al log, no al usuario.
 */
public class AppException extends RuntimeException {

    public AppException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
