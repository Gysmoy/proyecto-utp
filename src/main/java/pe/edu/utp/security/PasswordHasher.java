package pe.edu.utp.security;

import at.favre.lib.crypto.bcrypt.BCrypt;
import java.nio.charset.StandardCharsets;

/**
 * Hash de contrasenas de usuarios con BCrypt, mismo formato que Laravel
 * ($2y$, cost 12). Un hash generado aqui se puede validar con Hash::check()
 * de Laravel y viceversa.
 */
public final class PasswordHasher {

    private static final int COST = 12;
    // BCrypt solo considera los primeros 72 bytes
    public static final int MAX_BYTES = 72;

    // Hash valido de una clave cualquiera: se usa para igualar tiempos cuando el usuario no existe
    private static final String HASH_FICTICIO = hash("usuario-inexistente");

    private PasswordHasher() {
    }

    public static String hash(String password) {
        return BCrypt.with(BCrypt.Version.VERSION_2Y).hashToString(COST, password.toCharArray());
    }

    public static boolean verificar(String password, String hash) {
        if (password == null || hash == null) {
            return false;
        }
        return BCrypt.verifyer().verify(password.toCharArray(), hash).verified;
    }

    /** Consume el mismo tiempo que una verificacion real para no revelar si el usuario existe. */
    public static void verificarFicticio(String password) {
        verificar(password == null ? "" : password, HASH_FICTICIO);
    }

    public static boolean excedeLimite(String password) {
        return password.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES;
    }
}
