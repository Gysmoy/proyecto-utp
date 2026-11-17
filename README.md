# Gestor de Accesos

Sistema web para guardar de forma segura las cuentas y contraseñas de cada usuario.
Proyecto final del curso Desarrollo Web Integrado (UTP).

- Publicado en: https://gestor-de-accesos-utpbgn4.dcm.devex.pe/
- Autor: Carlos Manuel Gamboa Palomino (U24236879)

## Tecnologías

- JDK 21 y Jakarta EE 10
- Jakarta Faces 4.0 con PrimeFaces 15 y CDI
- AJAX con `fetch` y JSON contra una API REST (Jakarta REST 3.1 + JSON-B)
- MySQL 8 con JDBC (MySQL Connector/J)
- Patrones MVC, DAO, DTO, Facade e Intercepting Filter
- Seguridad: BCrypt, RSA-OAEP 2048, PBKDF2 y AES-256-GCM
- Maven (empaquetado `app.war`) y WildFly 39

## Estructura

```
database/gestor_accesos.sql      script de la base de datos
src/main/java/pe/edu/utp/
  controller/   managed beans que arman las vistas (LoginBean, SesionBean, TipoBean)
  facade/       reglas de negocio (AuthFacade, AccesoFacade)
  dao/          interfaces DAO y su implementación JDBC en dao/impl
  dto/          objetos de transferencia de datos
  filter/       filtros de codificación y sesión
  security/     BCrypt, RSA, protección de la llave privada y token CSRF
  api/          API REST en JSON con Jakarta REST (auth, accesos, tipos)
src/main/webapp/
  login.xhtml, registro.xhtml, accesos.xhtml   vistas Facelets con PrimeFaces
  WEB-INF/templates/                           plantillas
  resources/js/                                AJAX: api.js, auth.js, accesos.js
```

## Cómo ejecutar

1. Crear la base de datos: `mysql -u root -p < database/gestor_accesos.sql`
2. Revisar la conexión en `src/main/resources/db.properties`.
3. Ejecutar con una de estas opciones:
   - **NetBeans + WildFly**: registrar WildFly en *Tools → Servers*, abrir el proyecto y *Run Project*. Abre `http://localhost:8080/`.
   - **Maven**: `mvn package wildfly:run` y abrir `http://localhost:8080/`.
   - **Docker**: `docker compose up -d --build` (WildFly + MySQL 8.4).

## API REST

Las pantallas no envían formularios de Faces: login, registro, salir y el CRUD de accesos
viajan por AJAX en JSON contra esta API.

- Desde la web: sesión + cabecera `X-CSRF-Token` en POST, PUT y DELETE.
- Desde otro sistema: HTTP Basic con el usuario o correo y la contraseña.
- Errores: `{"status": 400, "error": "mensaje"}` con el código HTTP correspondiente.

| Método | Ruta | Respuesta |
|---|---|---|
| POST | `/api/auth/login` | 200, 401 |
| POST | `/api/auth/registro` | 201, 400 |
| POST | `/api/auth/logout` | 204, 403 |
| GET | `/api/accesos?q=&tipo=` | 200 |
| GET | `/api/accesos/{id}` | 200, 404 |
| GET | `/api/accesos/{id}/password` | 200, 404 |
| POST | `/api/accesos` | 201 + Location, 400, 415 |
| PUT | `/api/accesos/{id}` | 200, 400, 404 |
| DELETE | `/api/accesos/{id}` | 204, 404 |
| GET | `/api/tipos` | 200 |
