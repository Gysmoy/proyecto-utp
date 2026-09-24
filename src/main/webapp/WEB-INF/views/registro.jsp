<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
    <head>
        <%@ include file="/WEB-INF/views/includes/head.jspf" %>
        <title>Registro - Gestor de Accesos</title>
    </head>
    <body>
        <div class="auth-wrapper">
            <div class="card auth-card shadow-lg">
                <div class="card-body p-4 p-md-5">
                    <div class="text-center mb-4">
                        <i class="bi bi-person-plus-fill auth-icon"></i>
                        <h1 class="h4 mt-2 mb-0">Crear cuenta</h1>
                        <p class="text-muted small">Tu contraseña protege tu llave privada: no la olvides, no se puede recuperar.</p>
                    </div>

                    <%@ include file="/WEB-INF/views/includes/flash.jspf" %>

                    <form action="${pageContext.request.contextPath}/registro" method="post">
                        <input type="hidden" name="_csrf" value="${sessionScope.csrf}">
                        <div class="mb-3">
                            <label for="nombre" class="form-label">Nombre completo</label>
                            <input type="text" class="form-control" id="nombre" name="nombre" required maxlength="100"
                                   autocomplete="name" value="<c:out value='${nombre}'/>">
                        </div>
                        <div class="mb-3">
                            <label for="usuario" class="form-label">Usuario</label>
                            <input type="text" class="form-control" id="usuario" name="usuario" required
                                   minlength="3" maxlength="50" pattern="[a-zA-Z0-9._\-]+" autocomplete="username"
                                   value="<c:out value='${usuarioIngresado}'/>">
                        </div>
                        <div class="mb-3">
                            <label for="email" class="form-label">Correo</label>
                            <input type="email" class="form-control" id="email" name="email" required maxlength="120"
                                   autocomplete="email" value="<c:out value='${email}'/>">
                        </div>
                        <div class="mb-3">
                            <label for="password" class="form-label">Contraseña</label>
                            <input type="password" class="form-control" id="password" name="password" required
                                   minlength="8" autocomplete="new-password">
                            <div class="form-text">Mínimo 8 caracteres.</div>
                        </div>
                        <div class="mb-4">
                            <label for="confirmar" class="form-label">Confirmar contraseña</label>
                            <input type="password" class="form-control" id="confirmar" name="confirmar" required
                                   minlength="8" autocomplete="new-password">
                        </div>
                        <button type="submit" class="btn btn-success w-100">
                            <i class="bi bi-check2-circle"></i> Registrarme
                        </button>
                    </form>

                    <p class="text-center mt-4 mb-0 small">
                        ¿Ya tienes cuenta? <a href="${pageContext.request.contextPath}/login">Inicia sesión</a>
                    </p>
                </div>
            </div>
        </div>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>
