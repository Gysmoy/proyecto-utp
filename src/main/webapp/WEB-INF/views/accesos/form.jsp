<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="esNuevo" value="${acceso.id == 0}"/>
<!DOCTYPE html>
<html lang="es">
    <head>
        <%@ include file="/WEB-INF/views/includes/head.jspf" %>
        <title>${esNuevo ? 'Nuevo' : 'Editar'} acceso - Gestor de Accesos</title>
    </head>
    <body>
        <%@ include file="/WEB-INF/views/includes/navbar.jspf" %>

        <main class="container pb-5" style="max-width: 720px">
            <a href="${pageContext.request.contextPath}/accesos" class="small text-decoration-none">
                <i class="bi bi-arrow-left"></i> Volver
            </a>
            <h1 class="h3 mt-2 mb-3">${esNuevo ? 'Nuevo acceso' : 'Editar acceso'}</h1>

            <%@ include file="/WEB-INF/views/includes/flash.jspf" %>

            <div class="card shadow-sm border-0">
                <div class="card-body p-4">
                    <form method="post" action="${pageContext.request.contextPath}/accesos/guardar" autocomplete="off">
                        <input type="hidden" name="_csrf" value="${sessionScope.csrf}">
                        <input type="hidden" name="id" value="${acceso.id}">

                        <div class="row g-3">
                            <div class="col-md-8">
                                <label for="titulo" class="form-label">Título</label>
                                <input type="text" class="form-control" id="titulo" name="titulo" required maxlength="100"
                                       placeholder="Ej: Gmail personal" value="<c:out value='${acceso.titulo}'/>">
                            </div>
                            <div class="col-md-4">
                                <label for="tipoId" class="form-label">Tipo</label>
                                <select class="form-select" id="tipoId" name="tipoId" required>
                                    <option value="">Selecciona...</option>
                                    <c:forEach var="t" items="${tipos}">
                                        <option value="${t.id}" ${t.id == acceso.tipoId ? 'selected' : ''}><c:out value="${t.nombre}"/></option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-12">
                                <label for="url" class="form-label">URL <span class="text-muted small">(opcional)</span></label>
                                <input type="url" class="form-control" id="url" name="url" maxlength="255"
                                       placeholder="https://..." value="<c:out value='${acceso.url}'/>">
                            </div>
                            <div class="col-md-6">
                                <label for="login" class="form-label">Usuario / correo de la cuenta</label>
                                <input type="text" class="form-control" id="login" name="login" required maxlength="150"
                                       value="<c:out value='${acceso.login}'/>">
                            </div>
                            <div class="col-md-6">
                                <label for="password" class="form-label">Contraseña</label>
                                <div class="input-group">
                                    <input type="password" class="form-control" id="password" name="password"
                                           autocomplete="new-password" ${esNuevo ? 'required' : ''}
                                           placeholder="${esNuevo ? '' : 'Dejar vacío para no cambiar'}">
                                    <button type="button" class="btn btn-outline-secondary" id="verPwd" title="Mostrar">
                                        <i class="bi bi-eye"></i>
                                    </button>
                                    <button type="button" class="btn btn-outline-secondary" id="generarPwd" title="Generar contraseña segura">
                                        <i class="bi bi-magic"></i>
                                    </button>
                                </div>
                                <div class="form-text">Se guarda cifrada con tu llave pública RSA.</div>
                            </div>
                            <div class="col-12">
                                <label for="notas" class="form-label">Notas <span class="text-muted small">(opcional)</span></label>
                                <textarea class="form-control" id="notas" name="notas" rows="3" maxlength="2000"><c:out value="${acceso.notas}"/></textarea>
                            </div>
                        </div>

                        <div class="d-flex justify-content-end gap-2 mt-4">
                            <a href="${pageContext.request.contextPath}/accesos" class="btn btn-secondary">Cancelar</a>
                            <button type="submit" class="btn btn-primary"><i class="bi bi-save"></i> Guardar</button>
                        </div>
                    </form>
                </div>
            </div>
        </main>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js"></script>
        <script>
            const inputPwd = document.getElementById('password');

            document.getElementById('verPwd').addEventListener('click', e => {
                const visible = inputPwd.type === 'text';
                inputPwd.type = visible ? 'password' : 'text';
                e.currentTarget.querySelector('i').className = visible ? 'bi bi-eye' : 'bi bi-eye-slash';
            });

            // Genera una contrasena aleatoria de 20 caracteres con crypto.getRandomValues
            document.getElementById('generarPwd').addEventListener('click', () => {
                const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%&*-_=+?';
                const valores = crypto.getRandomValues(new Uint32Array(20));
                inputPwd.value = Array.from(valores, v => chars[v % chars.length]).join('');
                inputPwd.type = 'text';
                document.querySelector('#verPwd i').className = 'bi bi-eye-slash';
            });
        </script>
    </body>
</html>
