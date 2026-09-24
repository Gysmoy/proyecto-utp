package pe.edu.utp.dto;

import java.time.LocalDateTime;

/**
 * Datos de un usuario del sistema tal como se guardan en la tabla usuarios.
 */
public class UsuarioDTO {

    private int id;
    private String nombre;
    private String usuario;
    private String email;
    private String password;        // hash BCrypt
    private String publicKey;       // RSA publica en Base64
    private String privateKeyEnc;   // RSA privada cifrada con AES-GCM (Base64)
    private String kdfSalt;         // salt de PBKDF2 (Base64)
    private LocalDateTime createdAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKeyEnc() {
        return privateKeyEnc;
    }

    public void setPrivateKeyEnc(String privateKeyEnc) {
        this.privateKeyEnc = privateKeyEnc;
    }

    public String getKdfSalt() {
        return kdfSalt;
    }

    public void setKdfSalt(String kdfSalt) {
        this.kdfSalt = kdfSalt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
