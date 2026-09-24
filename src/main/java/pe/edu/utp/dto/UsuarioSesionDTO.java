package pe.edu.utp.dto;

import java.io.Serializable;

/**
 * Datos minimos del usuario logueado que se guardan en la sesion (sin hashes ni llaves).
 */
public class UsuarioSesionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int id;
    private final String nombre;
    private final String usuario;
    private final String email;

    public UsuarioSesionDTO(int id, String nombre, String usuario, String email) {
        this.id = id;
        this.nombre = nombre;
        this.usuario = usuario;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getEmail() {
        return email;
    }
}
