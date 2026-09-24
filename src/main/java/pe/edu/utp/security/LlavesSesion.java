package pe.edu.utp.security;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Llaves RSA del usuario logueado, guardadas en la sesion HTTP.
 * NO implementa Serializable a proposito: si Tomcat persiste las sesiones
 * a disco (al reiniciar) omite este atributo y la llave privada nunca se
 * escribe en texto plano. En ese caso el usuario debe volver a loguearse.
 */
public final class LlavesSesion {

    public static final String ATRIBUTO = "llaves";

    private final PublicKey publica;
    private final PrivateKey privada;

    public LlavesSesion(PublicKey publica, PrivateKey privada) {
        this.publica = publica;
        this.privada = privada;
    }

    public PublicKey getPublica() {
        return publica;
    }

    public PrivateKey getPrivada() {
        return privada;
    }
}
