package pe.edu.utp.dto;

/**
 * Tipo de acceso: correo, red social, banco, servidor, etc.
 */
public class TipoAccesoDTO {

    private int id;
    private String nombre;

    public TipoAccesoDTO() {
    }

    public TipoAccesoDTO(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

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
}
