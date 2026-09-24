package pe.edu.utp.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Entrega conexiones JDBC a MySQL. Usa las variables de entorno DB_HOST,
 * DB_PORT, DB_NAME, DB_USER y DB_PASSWORD si existen (Docker); si no,
 * lee db.properties (desarrollo local).
 */
public final class Conexion {

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        Properties props = new Properties();
        try (InputStream in = Conexion.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new IllegalStateException("No se encontro db.properties en el classpath");
            }
            props.load(in);
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (IOException | ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
        String host = System.getenv("DB_HOST");
        if (host != null && !host.isBlank()) {
            URL = "jdbc:mysql://" + host + ":" + env("DB_PORT", "3306") + "/" + env("DB_NAME", "gestor_accesos")
                    + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Lima&characterEncoding=UTF-8";
            USER = env("DB_USER", "root");
            PASSWORD = env("DB_PASSWORD", "");
        } else {
            URL = props.getProperty("db.url");
            USER = props.getProperty("db.user");
            PASSWORD = props.getProperty("db.password", "");
        }
    }

    private static String env(String nombre, String porDefecto) {
        String valor = System.getenv(nombre);
        return valor == null || valor.isBlank() ? porDefecto : valor;
    }

    private Conexion() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
