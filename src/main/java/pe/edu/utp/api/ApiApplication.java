package pe.edu.utp.api;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Activa Jakarta REST (JAX-RS) bajo /api. WildFly descubre solo los recursos
 * (@Path) y los providers (@Provider) del paquete; JSON-B convierte los DTO a JSON.
 */
@ApplicationPath("api")
public class ApiApplication extends Application {
}
