// Pantalla "Mis accesos". Todo el intercambio con el servidor es AJAX (fetch) en JSON:
//   GET    /api/accesos?q=&tipo=        listado y busqueda
//   GET    /api/accesos/{id}            datos para editar
//   GET    /api/accesos/{id}/password   contrasena descifrada
//   POST   /api/accesos                 crear
//   PUT    /api/accesos/{id}            actualizar
//   DELETE /api/accesos/{id}            eliminar
const MASCARA = '••••••••••';
const OCULTAR_EN_MS = 20000;
const POR_PAGINA = 10;
const BOTON = 'ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only';
const temporizadores = {};
const estado = {accesos: [], pagina: 0, editandoId: 0, porEliminar: null, consulta: 0, busqueda: null};

const $id = id => document.getElementById(id);

function avisar(texto, severidad = 'info') {
    PF('avisos').renderMessage({summary: texto, detail: '', severity: severidad});
}

// Un 401 de la API significa que la sesion vencio: vuelve al login
function sesionVencida(r) {
    if (r.status === 401) {
        location.href = Api.ruta('/login?expirado=1');
        return true;
    }
    return false;
}

// Crea elementos con textContent: los datos del usuario nunca se interpretan como HTML
function el(tag, atributos = {}, texto) {
    const e = document.createElement(tag);
    for (const [k, v] of Object.entries(atributos)) {
        e.setAttribute(k, v);
    }
    if (texto !== undefined && texto !== null) {
        e.textContent = texto;
    }
    return e;
}

function boton(icono, titulo, clases, accion) {
    const b = el('button', {type: 'button', title: titulo, 'aria-label': titulo, class: `${BOTON} ${clases}`});
    b.append(el('span', {class: `ui-button-icon-left ui-icon ui-c pi ${icono}`}), el('span', {class: 'ui-button-text ui-c'}, 'ui-button'));
    b.addEventListener('click', accion);
    return b;
}

// ---------- Listado ----------

async function cargar() {
    const consulta = ++estado.consulta;
    const params = new URLSearchParams();
    const q = $id('frmLista:q').value.trim();
    const tipo = $id('frmLista:tipoFiltro_input').value;
    if (q) {
        params.set('q', q);
    }
    if (tipo) {
        params.set('tipo', tipo);
    }
    const texto = params.toString();
    const r = await Api.get('/api/accesos' + (texto ? '?' + texto : ''));
    if (consulta !== estado.consulta || sesionVencida(r)) {
        return; // llego una busqueda mas nueva o se vencio la sesion
    }
    if (!r.ok) {
        avisar(r.datos.error || 'Ocurrió un error al listar los accesos.', 'error');
        return;
    }
    estado.accesos = r.datos;
    const paginas = Math.max(1, Math.ceil(r.datos.length / POR_PAGINA));
    estado.pagina = Math.min(estado.pagina, paginas - 1);
    pintar();
}

function pintar() {
    Object.values(temporizadores).forEach(clearTimeout);
    const total = estado.accesos.length;
    $id('contador').textContent = `${total} registro(s)`;
    const filas = $id('filas');
    filas.replaceChildren();
    if (!total) {
        const vacio = el('tr', {class: 'ui-widget-content ui-datatable-empty-message'});
        vacio.append(el('td', {colspan: '5'}, 'No hay accesos registrados.'));
        filas.append(vacio);
    }
    const inicio = estado.pagina * POR_PAGINA;
    estado.accesos.slice(inicio, inicio + POR_PAGINA).forEach((a, i) => filas.append(fila(a, i)));
    pintarPaginador(total);
}

function celda(titulo) {
    const td = el('td', {role: 'gridcell'});
    td.append(el('span', {class: 'ui-column-title'}, titulo));
    return td;
}

function fila(a, i) {
    const tr = el('tr', {role: 'row', 'data-id': a.id, class: `ui-widget-content ${i % 2 ? 'ui-datatable-odd' : 'ui-datatable-even'}`});

    const acceso = celda('Acceso');
    acceso.append(el('div', {class: 'acceso-titulo'}, a.titulo));
    if (a.url && /^https?:\/\//i.test(a.url)) {
        acceso.append(el('a', {href: a.url, target: '_blank', rel: 'noopener noreferrer', class: 'acceso-url'}, a.url));
    }
    if (a.notas) {
        acceso.append(el('div', {class: 'acceso-notas'}, a.notas));
    }

    const tipo = celda('Tipo');
    const tag = el('span', {class: 'ui-tag ui-widget ui-tag-info'});
    tag.append(el('span', {class: 'ui-tag-value'}, a.tipo));
    tipo.append(tag);

    const usuario = celda('Usuario');
    const datoUsuario = el('div', {class: 'dato-copiable'});
    const copiarUsuario = el('button', {type: 'button', class: 'boton-icono', title: 'Copiar usuario', 'aria-label': 'Copiar usuario'});
    copiarUsuario.append(el('i', {class: 'pi pi-copy'}));
    copiarUsuario.addEventListener('click', () => copiar(a.login, 'Usuario copiado'));
    datoUsuario.append(el('span', {class: 'dato-texto'}, a.login), copiarUsuario);
    usuario.append(datoUsuario);

    const password = celda('Contraseña');
    const datoPassword = el('div', {class: 'dato-copiable'});
    datoPassword.append(
        el('span', {class: 'password-mask', id: `pwd-${a.id}`}, MASCARA),
        boton('pi-eye', 'Ver contraseña', `rounded-button ui-button-flat ui-button-secondary js-ojo-${a.id}`, () => alternarPassword(a.id)),
        boton('pi-copy', 'Copiar contraseña', 'rounded-button ui-button-flat ui-button-secondary', () => copiarPassword(a.id)));
    password.append(datoPassword);

    const acciones = celda('Acciones');
    acciones.append(
        boton('pi-pencil', 'Editar', 'rounded-button ui-button-flat', () => editarAcceso(a.id)),
        boton('pi-trash', 'Eliminar', 'rounded-button ui-button-flat ui-button-danger', () => pedirEliminar(a)));

    tr.append(acceso, tipo, usuario, password, acciones);
    return tr;
}

// Paginador con el mismo HTML que el de p:dataTable
function pintarPaginador(total) {
    const contenedor = $id('paginador');
    contenedor.replaceChildren();
    const paginas = Math.ceil(total / POR_PAGINA);
    if (paginas <= 1) {
        return;
    }
    const barra = el('div', {class: 'ui-paginator ui-paginator-bottom ui-widget-header ui-corner-bottom', role: 'navigation', 'aria-label': 'Paginación'});
    const enlace = (clases, contenido, destino, deshabilitado, etiqueta) => {
        const a = el('a', {href: '#', class: `${clases} ui-state-default ui-corner-all${deshabilitado ? ' ui-state-disabled' : ''}`, 'aria-label': etiqueta});
        a.append(contenido);
        a.addEventListener('click', e => {
            e.preventDefault();
            if (!deshabilitado) {
                estado.pagina = destino;
                pintar();
            }
        });
        return a;
    };
    barra.append(enlace('ui-paginator-prev', el('span', {class: 'ui-icon ui-icon-seek-prev'}, 'P'), estado.pagina - 1, estado.pagina === 0, 'Página anterior'));
    const numeros = el('span', {class: 'ui-paginator-pages'});
    for (let p = 0; p < paginas; p++) {
        const n = enlace(`ui-paginator-page${p === estado.pagina ? ' ui-state-active' : ''}`, document.createTextNode(String(p + 1)), p, false, `Página ${p + 1}`);
        numeros.append(n);
    }
    barra.append(numeros);
    barra.append(enlace('ui-paginator-next', el('span', {class: 'ui-icon ui-icon-seek-next'}, 'N'), estado.pagina + 1, estado.pagina === paginas - 1, 'Página siguiente'));
    contenedor.append(barra);
}

// ---------- Filtros ----------

function filtrar() {
    estado.pagina = 0;
    cargar();
}

function limpiarFiltros() {
    $id('frmLista:q').value = '';
    PF('selFiltro').selectValue('');
    filtrar();
}

// ---------- Ver y copiar contrasena ----------

async function pedirPassword(id) {
    const r = await Api.get(`/api/accesos/${id}/password`);
    if (sesionVencida(r)) {
        return null;
    }
    if (!r.ok) {
        avisar(r.datos.error || 'No se pudo descifrar la contraseña.', 'error');
        return null;
    }
    return r.datos.password;
}

function iconoOjo(id, clase) {
    const icono = document.querySelector(`.js-ojo-${id} .ui-icon`);
    if (icono) {
        icono.classList.remove('pi-eye', 'pi-eye-slash');
        icono.classList.add(clase);
    }
}

function ponerMascara(id) {
    const span = $id(`pwd-${id}`);
    if (span) {
        span.textContent = MASCARA;
        span.dataset.visible = '0';
    }
    iconoOjo(id, 'pi-eye');
}

async function alternarPassword(id) {
    const span = $id(`pwd-${id}`);
    if (span.dataset.visible === '1') {
        clearTimeout(temporizadores[id]);
        ponerMascara(id);
        return;
    }
    const password = await pedirPassword(id);
    if (password === null) {
        return;
    }
    span.textContent = password;
    span.dataset.visible = '1';
    iconoOjo(id, 'pi-eye-slash');
    // Se vuelve a ocultar sola despues de un rato
    temporizadores[id] = setTimeout(() => ponerMascara(id), OCULTAR_EN_MS);
}

async function copiar(texto, mensaje) {
    try {
        await navigator.clipboard.writeText(texto);
        avisar(mensaje);
    } catch (e) {
        avisar('No se pudo copiar al portapapeles', 'error');
    }
}

async function copiarPassword(id) {
    const password = await pedirPassword(id);
    if (password !== null) {
        copiar(password, 'Contraseña copiada');
    }
}

// ---------- Dialogo de nuevo / editar ----------

const CAMPOS = ['titulo', 'url', 'login', 'clave', 'notas'];

function abrirDialogo(titulo, acceso) {
    estado.editandoId = acceso ? acceso.id : 0;
    mostrarMensajes('frmAcceso:erroresForm', []);
    for (const campo of CAMPOS) {
        const input = $id(`frmAcceso:${campo}`);
        input.value = acceso && campo !== 'clave' ? (acceso[campo] ?? '') : '';
        input.classList.remove('ui-state-error');
    }
    PF('selTipo').selectValue(acceso ? String(acceso.tipoId) : '');
    $id('frmAcceso:clave').placeholder = acceso ? 'Dejar vacío para no cambiar' : '';
    mostrarClave(false);
    $id('dlgAcceso_title').textContent = titulo;
    PF('dlgAcceso').show();
    setTimeout(() => $id('frmAcceso:titulo').focus(), 50);
}

function nuevoAcceso() {
    abrirDialogo('Nuevo acceso', null);
}

async function editarAcceso(id) {
    const r = await Api.get(`/api/accesos/${id}`);
    if (sesionVencida(r)) {
        return;
    }
    if (!r.ok) {
        avisar(r.datos.error || 'Ocurrió un error al abrir el acceso.', 'error');
        cargar();
        return;
    }
    abrirDialogo('Editar acceso', r.datos);
}

async function guardarAcceso() {
    const errores = camposVacios([
        ['frmAcceso:titulo', 'Ingresa un título.'],
        ['frmAcceso:tipo_input', 'Selecciona un tipo.'],
        ['frmAcceso:login', 'Ingresa el usuario o correo de la cuenta.']
    ]);
    mostrarMensajes('frmAcceso:erroresForm', errores);
    if (errores.length) {
        return;
    }
    const id = estado.editandoId;
    // El JSON que espera la API; password vacio al editar conserva la actual
    const cuerpo = {
        tipoId: Number($id('frmAcceso:tipo_input').value),
        titulo: $id('frmAcceso:titulo').value,
        url: $id('frmAcceso:url').value,
        login: $id('frmAcceso:login').value,
        password: $id('frmAcceso:clave').value || null,
        notas: $id('frmAcceso:notas').value
    };
    ocupado('frmAcceso:btnGuardar', true);
    const r = id === 0 ? await Api.post('/api/accesos', cuerpo) : await Api.put(`/api/accesos/${id}`, cuerpo);
    ocupado('frmAcceso:btnGuardar', false);
    if (sesionVencida(r)) {
        return;
    }
    if (!r.ok) {
        // 400: regla de negocio; el dialogo sigue abierto y conserva lo escrito
        mostrarMensajes('frmAcceso:erroresForm', [r.datos.error || 'Ocurrió un error al guardar el acceso.']);
        return;
    }
    PF('dlgAcceso').hide();
    avisar(id === 0 ? 'Acceso creado.' : 'Acceso actualizado.');
    cargar();
}

function mostrarClave(visible) {
    $id('frmAcceso:clave').type = visible ? 'text' : 'password';
    const icono = document.querySelector('[id="frmAcceso:verClave"] .ui-icon');
    icono.classList.toggle('pi-eye', !visible);
    icono.classList.toggle('pi-eye-slash', visible);
}

function alternarClave() {
    mostrarClave($id('frmAcceso:clave').type === 'password');
}

// Genera una contrasena aleatoria de 20 caracteres con crypto.getRandomValues
function generarClave() {
    const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%&*-_=+?';
    const valores = crypto.getRandomValues(new Uint32Array(20));
    $id('frmAcceso:clave').value = Array.from(valores, v => chars[v % chars.length]).join('');
    mostrarClave(true);
}

// ---------- Eliminar ----------

function pedirEliminar(acceso) {
    estado.porEliminar = acceso;
    document.querySelector('#dlgEliminar .ui-confirm-dialog-message').textContent =
        `¿Seguro que deseas eliminar ${acceso.titulo}? Esta acción no se puede deshacer.`;
    PF('dlgEliminar').show();
}

async function confirmarEliminar() {
    const acceso = estado.porEliminar;
    if (!acceso) {
        return;
    }
    ocupado('btnConfirmarEliminar', true);
    const r = await Api.del(`/api/accesos/${acceso.id}`);
    ocupado('btnConfirmarEliminar', false);
    PF('dlgEliminar').hide();
    estado.porEliminar = null;
    if (sesionVencida(r)) {
        return;
    }
    if (r.ok) {
        avisar('Acceso eliminado.');
    } else {
        avisar(r.datos.error || 'Ocurrió un error al eliminar el acceso.', 'error');
    }
    cargar();
}

// ---------- Inicio ----------

$(function () {
    // Busqueda mientras se escribe, con una pausa para no llamar en cada tecla
    $id('frmLista:q').addEventListener('input', () => {
        clearTimeout(estado.busqueda);
        estado.busqueda = setTimeout(filtrar, 300);
    });
    cargar();
});
