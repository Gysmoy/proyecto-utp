// Cliente AJAX de la API REST: envia y recibe JSON con fetch.
// En POST, PUT y DELETE agrega el token CSRF de la sesion (etiqueta meta de la plantilla).
const Api = (() => {
    const meta = nombre => document.querySelector(`meta[name="${nombre}"]`)?.content ?? '';
    const base = meta('ctx');

    // Devuelve {ok, status, datos}; datos es el JSON de la respuesta ({} si no trae cuerpo)
    async function llamar(metodo, ruta, cuerpo) {
        const cabeceras = {'Accept': 'application/json', 'X-Requested-With': 'XMLHttpRequest'};
        const opciones = {method: metodo, headers: cabeceras, credentials: 'same-origin'};
        if (cuerpo !== undefined) {
            cabeceras['Content-Type'] = 'application/json';
            opciones.body = JSON.stringify(cuerpo);
        }
        if (metodo !== 'GET') {
            const token = meta('csrf-token');
            if (token) {
                cabeceras['X-CSRF-Token'] = token;
            }
        }
        let respuesta;
        try {
            respuesta = await fetch(base + ruta, opciones);
        } catch (e) {
            return {ok: false, status: 0, datos: {error: 'No se pudo conectar con el servidor.'}};
        }
        let datos = {};
        if (respuesta.status !== 204) {
            try {
                datos = await respuesta.json();
            } catch (e) {
                datos = {};
            }
        }
        return {ok: respuesta.ok, status: respuesta.status, datos};
    }

    return {
        ruta: r => base + r,
        get: r => llamar('GET', r),
        post: (r, cuerpo) => llamar('POST', r, cuerpo),
        put: (r, cuerpo) => llamar('PUT', r, cuerpo),
        del: r => llamar('DELETE', r)
    };
})();

// Muestra mensajes con el mismo HTML que genera p:messages de PrimeFaces
function mostrarMensajes(contenedorId, textos, severidad = 'error') {
    const contenedor = document.getElementById(contenedorId);
    contenedor.replaceChildren();
    if (!textos.length) {
        return;
    }
    const caja = document.createElement('div');
    caja.className = `ui-messages-${severidad} ui-corner-all`;
    const icono = document.createElement('span');
    icono.className = `ui-messages-${severidad}-icon`;
    const lista = document.createElement('ul');
    for (const texto of textos) {
        const item = document.createElement('li');
        item.setAttribute('role', 'alert');
        const resumen = document.createElement('span');
        resumen.className = `ui-messages-${severidad}-summary`;
        resumen.textContent = texto;
        item.append(resumen);
        lista.append(item);
    }
    caja.append(icono, lista);
    contenedor.append(caja);
}

// Revisa los campos obligatorios en el navegador; el servidor vuelve a validar todo
function camposVacios(campos) {
    const errores = [];
    for (const [id, mensaje] of campos) {
        const input = document.getElementById(id);
        const vacio = !input.value.trim();
        input.classList.toggle('ui-state-error', vacio);
        if (vacio) {
            errores.push(mensaje);
        }
    }
    return errores;
}

// Deshabilita un boton de PrimeFaces mientras dura la peticion
function ocupado(botonId, valor) {
    const boton = document.getElementById(botonId);
    if (boton) {
        boton.disabled = valor;
        boton.classList.toggle('ui-state-disabled', valor);
    }
}

async function salir() {
    ocupado('frmSesion:btnSalir', true);
    await Api.post('/api/auth/logout');
    location.href = Api.ruta('/login');
}
