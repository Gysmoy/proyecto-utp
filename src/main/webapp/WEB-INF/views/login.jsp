<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
    <head>
        <%@ include file="/WEB-INF/views/includes/head.jspf" %>
        <title>Iniciar sesión - Gestor de Accesos</title>
    </head>
    <body>
        <div class="auth-wrapper">
            <div class="card auth-card shadow-lg">
                <div class="card-body p-4 p-md-5">
                    <div class="text-center mb-4">
                        <i class="bi bi-shield-lock-fill auth-icon"></i>
                        <h1 class="h4 mt-2 mb-0">Acceso al Sistema</h1>
                        <p class="text-muted small">Gestor de contraseñas y accesos</p>
                    </div>

                    <%@ include file="/WEB-INF/views/includes/flash.jspf" %>

                    <form action="${pageContext.request.contextPath}/login" method="post" autocomplete="on">
                        <input type="hidden" name="_csrf" value="${sessionScope.csrf}">
                        <div class="mb-3">
                            <label for="usuario" class="form-label">Usuario o correo</label>
                            <input type="text" class="form-control" id="usuario" name="usuario" required autofocus
                                   autocomplete="username" value="<c:out value='${usuarioIngresado}'/>">
                        </div>
                        <div class="mb-4">
                            <label for="password" class="form-label">Contraseña</label>
                            <input type="password" class="form-control" id="password" name="password" required
                                   autocomplete="current-password">
                        </div>
                        <button type="submit" class="btn btn-primary w-100">
                            <i class="bi bi-box-arrow-in-right"></i> Ingresar
                        </button>
                    </form>

                    <p class="text-center mt-4 mb-0 small">
                        ¿No tienes cuenta? <a href="${pageContext.request.contextPath}/registro">Regístrate</a>
                    </p>
                </div>
            </div>
        </div>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>
