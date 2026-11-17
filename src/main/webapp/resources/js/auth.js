// Login y registro por AJAX: el formulario viaja como JSON a la API REST
// y la respuesta JSON indica a donde ir o que error mostrar.

const valor = id => document.getElementById(id).value;

async function ingresar() {
    const errores = camposVacios([
        ['frmLogin:usuario', 'Ingresa tu usuario o correo.'],
        ['frmLogin:password', 'Ingresa tu contraseña.']
    ]);
    mostrarMensajes('frmLogin:mensajes', errores);
    if (errores.length) {
        return;
    }
    ocupado('frmLogin:ingresar', true);
    // POST /api/auth/login  {"usuario": "...", "password": "..."}
    const r = await Api.post('/api/auth/login', {
        usuario: valor('frmLogin:usuario'),
        password: valor('frmLogin:password')
    });
    if (r.ok) {
        location.href = r.datos.destino;
        return;
    }
    ocupado('frmLogin:ingresar', false);
    document.getElementById('frmLogin:password').value = '';
    mostrarMensajes('frmLogin:mensajes', [r.datos.error || 'Ocurrió un error, intenta nuevamente.']);
}

async function registrarme() {
    const errores = camposVacios([
        ['frmRegistro:nombre', 'Ingresa tu nombre.'],
        ['frmRegistro:usuario', 'Ingresa un usuario.'],
        ['frmRegistro:email', 'Ingresa tu correo.'],
        ['frmRegistro:password', 'Ingresa una contraseña.'],
        ['frmRegistro:confirmar', 'Confirma tu contraseña.']
    ]);
    mostrarMensajes('frmRegistro:mensajes', errores);
    if (errores.length) {
        return;
    }
    ocupado('frmRegistro:registrarme', true);
    // POST /api/auth/registro: 201 si se creo la cuenta, 400 con el error de validacion
    const r = await Api.post('/api/auth/registro', {
        nombre: valor('frmRegistro:nombre'),
        usuario: valor('frmRegistro:usuario'),
        email: valor('frmRegistro:email'),
        password: valor('frmRegistro:password'),
        confirmar: valor('frmRegistro:confirmar')
    });
    if (r.ok) {
        location.href = r.datos.destino;
        return;
    }
    ocupado('frmRegistro:registrarme', false);
    mostrarMensajes('frmRegistro:mensajes', [r.datos.error || 'Ocurrió un error, intenta nuevamente.']);
}
