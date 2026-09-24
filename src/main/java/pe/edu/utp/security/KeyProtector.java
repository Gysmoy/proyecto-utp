package pe.edu.utp.security;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Protege la llave privada RSA de cada usuario antes de guardarla en la BD.
 * Se deriva una llave AES-256 de la contrasena del usuario con PBKDF2 y se
 * cifra la llave privada con AES-GCM. Asi, sin la contrasena del usuario,
 * la llave privada guardada en la BD no sirve.
 */
public final class KeyProtector {

    private static final int ITERACIONES = 210_000;
    private static final int SALT_BYTES = 16;
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;
    private static final SecureRandom RANDOM = new SecureRandom();

    private KeyProtector() {
    }

    public static String nuevoSalt() {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /** Devuelve Base64(iv || cifrado) de la llave privada en formato PKCS#8. */
    public static String proteger(PrivateKey privada, String password, String saltBase64)
            throws GeneralSecurityException {
        byte[] iv = new byte[IV_BYTES];
        RANDOM.nextBytes(iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, derivar(password, saltBase64), new GCMParameterSpec(TAG_BITS, iv));
        byte[] cifrado = cipher.doFinal(privada.getEncoded());
        byte[] salida = ByteBuffer.allocate(iv.length + cifrado.length).put(iv).put(cifrado).array();
        return Base64.getEncoder().encodeToString(salida);
    }

    /** Falla con AEADBadTagException si la contrasena no es la correcta. */
    public static PrivateKey recuperar(String protegidaBase64, String password, String saltBase64)
            throws GeneralSecurityException {
        byte[] datos = Base64.getDecoder().decode(protegidaBase64);
        byte[] iv = Arrays.copyOfRange(datos, 0, IV_BYTES);
        byte[] cifrado = Arrays.copyOfRange(datos, IV_BYTES, datos.length);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, derivar(password, saltBase64), new GCMParameterSpec(TAG_BITS, iv));
        byte[] pkcs8 = cipher.doFinal(cifrado);
        try {
            return RsaCipher.privadaDesdeBytes(pkcs8);
        } finally {
            Arrays.fill(pkcs8, (byte) 0);
        }
    }

    private static SecretKey derivar(String password, String saltBase64) throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), Base64.getDecoder().decode(saltBase64),
                ITERACIONES, 256);
        try {
            byte[] llave = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return new SecretKeySpec(llave, "AES");
        } finally {
            spec.clearPassword();
        }
    }
}
