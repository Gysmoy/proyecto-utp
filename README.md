# Gestor de Accesos

Sistema web para guardar de forma segura las cuentas y contraseñas de cada usuario.
Proyecto final del curso Desarrollo Web Integrado (UTP).

- Publicado en: https://gestor-de-accesos-utpbgn4.dcm.devex.pe/
- Autor: Carlos Manuel Gamboa Palomino (U24236879)

## Tecnologías

- JDK 21 y Jakarta EE 10 (Servlet 6.0, JSP 3.1, JSTL 3.0)
- MySQL 8 con JDBC (MySQL Connector/J)
- JSP + Bootstrap 5.3
- Patrones MVC, DAO, DTO, Facade e Intercepting Filter
- Seguridad: BCrypt, RSA-OAEP 2048, PBKDF2 y AES-256-GCM
- Servidor: Apache Tomcat 10.1 (desarrollo) y Eclipse Jetty 12 ee10 (servidor elegido)

## Estructura

```
database/gestor_accesos.sql      script de la base de datos
src/main/java/pe/edu/utp/
  controller/   servlets (Login, Registro, Logout, Acceso)
  facade/       reglas de negocio (AuthFacade, AccesoFacade)
  dao/          interfaces DAO y su implementación JDBC en dao/impl
  dto/          objetos de transferencia de datos
  filter/       filtros de codificación, CSRF y sesión
  security/     BCrypt, RSA y protección de la llave privada
  api/          API REST en JSON (/api/accesos)
src/main/webapp/WEB-INF/views/   vistas JSP
```

## Cómo ejecutar

1. Crear la base de datos: `mysql -u root -p < database/gestor_accesos.sql`
2. Revisar la conexión en `src/main/resources/db.properties`.
3. Ejecutar con una de estas opciones:
   - **NetBeans + Tomcat 10.1**: abrir el proyecto, elegir el servidor Tomcat y *Run Project*. Abre `http://localhost:8080/semana6/`.
   - **Jetty 12**: `mvn jetty:run-war` y abrir `http://localhost:8080/`.
   - **Docker**: `docker compose up -d --build` (aplicación + MySQL 8.4).

## API REST

Autenticación HTTP Basic con el usuario o correo y la contraseña del sistema.

| Método | Ruta | Respuesta |
|---|---|---|
| GET | `/api/accesos?q=&tipo=` | 200 |
| GET | `/api/accesos/{id}` | 200, 404 |
| GET | `/api/accesos/{id}/password` | 200, 404 |
| POST | `/api/accesos` | 201 + Location, 400, 415 |
| PUT | `/api/accesos/{id}` | 200, 400, 404 |
| DELETE | `/api/accesos/{id}` | 204, 404 |
| GET | `/api/tipos` | 200 |
