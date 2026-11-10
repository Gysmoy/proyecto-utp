// Acciones de cliente del listado y del formulario de accesos
const MASCARA = '••••••••••';
const OCULTAR_EN_MS = 20000;
const temporizadores = {};

function avisar(texto, severidad = 'info') {
    PF('avisos').renderMessage({summary: texto, detail: '', severity: severidad});
}

function iconoOjo(id, clase) {
    const icono = document.querySelector('.js-ojo-' + id + ' .ui-icon');
    if (icono) {
        icono.classList.remove('pi-eye', 'pi-eye-slash');
        icono.classList.add(clase);
    }
}

function ponerMascara(id) {
    const span = document.getElementById('pwd-' + id);
    if (span) {
        span.textContent = MASCARA;
        span.dataset.visible = '0';
    }
    iconoOjo(id, 'pi-eye');
}

// onstart del boton ver: si ya esta visible solo la oculta y cancela la peticion
function ocultarSiVisible(id) {
    const span = document.getElementById('pwd-' + id);
    if (!span || span.dataset.visible !== '1') {
        return false;
    }
    clearTimeout(temporizadores[id]);
    ponerMascara(id);
    return true;
}

// oncomplete: el servidor entrega la contrasena descifrada en args.password
function mostrarPassword(args, id) {
    if (!args || args.password === undefined) {
        return;
    }
    const span = document.getElementById('pwd-' + id);
    span.textContent = args.password;
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

function copiarPassword(args) {
    if (args && args.password !== undefined) {
        copiar(args.password, 'Contraseña copiada');
    }
}

function copiarTexto(boton) {
    copiar(boton.dataset.texto, 'Usuario copiado');
}

function mostrarClave(visible) {
    document.getElementById('frmAcceso:clave').type = visible ? 'text' : 'password';
    const icono = document.querySelector('[id="frmAcceso:verClave"] .ui-icon');
    icono.classList.toggle('pi-eye', !visible);
    icono.classList.toggle('pi-eye-slash', visible);
}

function alternarClave() {
    mostrarClave(document.getElementById('frmAcceso:clave').type === 'password');
}

// Genera una contrasena aleatoria de 20 caracteres con crypto.getRandomValues
function generarClave() {
    const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%&*-_=+?';
    const valores = crypto.getRandomValues(new Uint32Array(20));
    document.getElementById('frmAcceso:clave').value = Array.from(valores, v => chars[v % chars.length]).join('');
    mostrarClave(true);
}
