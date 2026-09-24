package pe.edu.utp.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

/**
 * Cifrado asimetrico RSA-2048 con OAEP (SHA-256).
 * La llave publica cifra las contrasenas de los accesos y solo la privada
 * puede descifrarlas.
 */
public final class RsaCipher {

    private static final int KEY_SIZE = 2048;
    private static final String TRANSFORMACION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final OAEPParameterSpec OAEP = new OAEPParameterSpec(
            "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
    // 2048 bits = 256 bytes; OAEP SHA-256 reserva 2*32+2 bytes
    public static final int MAX_BYTES = 190;

    private RsaCipher() {
    }

    public static KeyPair generarPar() throws GeneralSecurityException {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(KEY_SIZE);
        return gen.generateKeyPair();
    }

    public static String cifrar(String texto, PublicKey publica) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(TRANSFORMACION);
        cipher.init(Cipher.ENCRYPT_MODE, publica, OAEP);
        byte[] cifrado = cipher.doFinal(texto.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(cifrado);
    }

    public static String descifrar(String cifradoBase64, PrivateKey privada) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(TRANSFORMACION);
        cipher.init(Cipher.DECRYPT_MODE, privada, OAEP);
        byte[] plano = cipher.doFinal(Base64.getDecoder().decode(cifradoBase64));
        return new String(plano, StandardCharsets.UTF_8);
    }

    public static String publicaABase64(PublicKey publica) {
        return Base64.getEncoder().encodeToString(publica.getEncoded());
    }

    public static PublicKey publicaDesdeBase64(String base64) throws GeneralSecurityException {
        byte[] bytes = Base64.getDecoder().decode(base64);
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(bytes));
    }

    public static PrivateKey privadaDesdeBytes(byte[] pkcs8) throws GeneralSecurityException {
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(pkcs8));
    }

    public static boolean excedeLimite(String texto) {
        return texto.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES;
    }
}
