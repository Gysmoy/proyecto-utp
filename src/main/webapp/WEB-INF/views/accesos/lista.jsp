<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="es">
    <head>
        <%@ include file="/WEB-INF/views/includes/head.jspf" %>
        <title>Mis accesos - Gestor de Accesos</title>
    </head>
    <body>
        <%@ include file="/WEB-INF/views/includes/navbar.jspf" %>

        <main class="container pb-5">
            <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
                <div>
                    <h1 class="h3 mb-0">Mis accesos</h1>
                    <span class="text-muted small">${fn:length(accesos)} registro(s)</span>
                </div>
                <a href="${pageContext.request.contextPath}/accesos/nuevo" class="btn btn-primary">
                    <i class="bi bi-plus-lg"></i> Nuevo acceso
                </a>
            </div>

            <%@ include file="/WEB-INF/views/includes/flash.jspf" %>

            <form class="row g-2 mb-4" method="get" action="${pageContext.request.contextPath}/accesos">
                <div class="col-12 col-md-6">
                    <input type="search" name="q" class="form-control" placeholder="Buscar por título, usuario o URL"
                           value="<c:out value='${q}'/>">
                </div>
                <div class="col-8 col-md-4">
                    <select name="tipo" class="form-select">
                        <option value="">Todos los tipos</option>
                        <c:forEach var="t" items="${tipos}">
                            <option value="${t.id}" ${t.id == tipoSel ? 'selected' : ''}><c:out value="${t.nombre}"/></option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-4 col-md-2 d-grid">
                    <button type="submit" class="btn btn-outline-secondary"><i class="bi bi-search"></i> Filtrar</button>
                </div>
            </form>

            <c:choose>
                <c:when test="${empty accesos}">
                    <div class="text-center text-muted py-5">
                        <i class="bi bi-safe2 display-4"></i>
                        <p class="mt-3 mb-0">No hay accesos registrados.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="row g-3">
                        <c:forEach var="a" items="${accesos}">
                            <div class="col-12 col-md-6 col-xl-4">
                                <div class="card acceso-card shadow-sm h-100">
                                    <div class="card-body">
                                        <div class="d-flex justify-content-between align-items-start mb-2">
                                            <h2 class="h6 mb-0 text-truncate" title="<c:out value='${a.titulo}'/>">
                                                <c:out value="${a.titulo}"/>
                                            </h2>
                                            <span class="badge text-bg-secondary tipo-badge ms-2"><c:out value="${a.tipoNombre}"/></span>
                                        </div>

                                        <c:if test="${not empty a.url}">
                                            <a href="<c:out value='${a.url}'/>" target="_blank" rel="noopener noreferrer"
                                               class="small d-block text-truncate mb-2">
                                                <i class="bi bi-link-45deg"></i> <c:out value="${a.url}"/>
                                            </a>
                                        </c:if>

                                        <div class="small text-muted">Usuario</div>
                                        <div class="d-flex align-items-center gap-2 mb-2">
                                            <span class="text-truncate"><c:out value="${a.login}"/></span>
                                            <button type="button" class="btn btn-sm btn-link p-0 js-copiar-texto"
                                                    data-texto="<c:out value='${a.login}'/>" title="Copiar usuario">
                                                <i class="bi bi-clipboard"></i>
                                            </button>
                                        </div>

                                        <div class="small text-muted">Contraseña</div>
                                        <div class="d-flex align-items-center gap-2">
                                            <span class="password-mask" id="pwd-${a.id}">••••••••••</span>
                                            <button type="button" class="btn btn-sm btn-link p-0 js-revelar" data-id="${a.id}" title="Ver contraseña">
                                                <i class="bi bi-eye"></i>
                                            </button>
                                            <button type="button" class="btn btn-sm btn-link p-0 js-copiar-pwd" data-id="${a.id}" title="Copiar contraseña">
                                                <i class="bi bi-clipboard"></i>
                                            </button>
                                        </div>

                                        <c:if test="${not empty a.notas}">
                                            <p class="small text-muted mt-2 mb-0" style="white-space: pre-line"><c:out value="${a.notas}"/></p>
                                        </c:if>
                                    </div>
                                    <div class="card-footer bg-transparent border-0 d-flex justify-content-end gap-2 pb-3">
                                        <a href="${pageContext.request.contextPath}/accesos/editar?id=${a.id}" class="btn btn-sm btn-outline-primary">
                                            <i class="bi bi-pencil"></i> Editar
                                        </a>
                                        <button type="button" class="btn btn-sm btn-outline-danger js-eliminar"
                                                data-id="${a.id}" data-titulo="<c:out value='${a.titulo}'/>">
                                            <i class="bi bi-trash"></i> Eliminar
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </main>

        <!-- Modal de confirmacion para eliminar -->
        <div class="modal fade" id="modalEliminar" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered">
                <form class="modal-content" method="post" action="${pageContext.request.contextPath}/accesos/eliminar">
                    <input type="hidden" name="_csrf" value="${sessionScope.csrf}">
                    <input type="hidden" name="id" id="eliminarId">
                    <div class="modal-header">
                        <h5 class="modal-title">Eliminar acceso</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                    </div>
                    <div class="modal-body">
                        ¿Seguro que deseas eliminar <strong id="eliminarTitulo"></strong>? Esta acción no se puede deshacer.
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                        <button type="submit" class="btn btn-danger">Eliminar</button>
                    </div>
                </form>
            </div>
        </div>

        <!-- Aviso flotante -->
        <div class="toast-container position-fixed bottom-0 end-0 p-3">
            <div id="toast" class="toast text-bg-dark border-0" role="status">
                <div class="toast-body" id="toastTexto"></div>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js"></script>
        <script>
            const BASE = '${pageContext.request.contextPath}';
            const CSRF = '${sessionScope.csrf}';
            const MASCARA = '••••••••••';
            const OCULTAR_EN_MS = 20000;
            const toast = new bootstrap.Toast(document.getElementById('toast'), {delay: 2500});

            function avisar(texto) {
                document.getElementById('toastTexto').textContent = texto;
                toast.show();
            }

            // Pide al servidor la contrasena descifrada con la llave privada RSA
            async function obtenerPassword(id) {
                const resp = await fetch(BASE + '/accesos/revelar', {
                    method: 'POST',
                    headers: {'X-Requested-With': 'fetch'},
                    body: new URLSearchParams({id: id, _csrf: CSRF})
                });
                if (resp.status === 401 || resp.status === 403) {
                    window.location = BASE + '/login?expirado=1';
                    throw new Error('Sesion expirada');
                }
                const data = await resp.json();
                if (!resp.ok) {
                    throw new Error(data.error || 'Error');
                }
                return data.password;
            }

            async function copiar(texto, mensaje) {
                try {
                    await navigator.clipboard.writeText(texto);
                    avisar(mensaje);
                } catch (e) {
                    avisar('No se pudo copiar al portapapeles');
                }
            }

            const temporizadores = {};

            document.querySelectorAll('.js-revelar').forEach(btn => {
                btn.addEventListener('click', async () => {
                    const id = btn.dataset.id;
                    const span = document.getElementById('pwd-' + id);
                    const icono = btn.querySelector('i');
                    if (span.dataset.visible === '1') {
                        span.textContent = MASCARA;
                        span.dataset.visible = '0';
                        icono.className = 'bi bi-eye';
                        clearTimeout(temporizadores[id]);
                        return;
                    }
                    try {
                        span.textContent = await obtenerPassword(id);
                        span.dataset.visible = '1';
                        icono.className = 'bi bi-eye-slash';
                        // Se vuelve a ocultar sola despues de un rato
                        temporizadores[id] = setTimeout(() => {
                            span.textContent = MASCARA;
                            span.dataset.visible = '0';
                            icono.className = 'bi bi-eye';
                        }, OCULTAR_EN_MS);
                    } catch (e) {
                        avisar(e.message);
                    }
                });
            });

            document.querySelectorAll('.js-copiar-pwd').forEach(btn => {
                btn.addEventListener('click', async () => {
                    try {
                        await copiar(await obtenerPassword(btn.dataset.id), 'Contraseña copiada');
                    } catch (e) {
                        avisar(e.message);
                    }
                });
            });

            document.querySelectorAll('.js-copiar-texto').forEach(btn => {
                btn.addEventListener('click', () => copiar(btn.dataset.texto, 'Usuario copiado'));
            });

            const modalEliminar = new bootstrap.Modal(document.getElementById('modalEliminar'));
            document.querySelectorAll('.js-eliminar').forEach(btn => {
                btn.addEventListener('click', () => {
                    document.getElementById('eliminarId').value = btn.dataset.id;
                    document.getElementById('eliminarTitulo').textContent = btn.dataset.titulo;
                    modalEliminar.show();
                });
            });
        </script>
    </body>
</html>
