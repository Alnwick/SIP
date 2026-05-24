const API_REGISTER_OPERATOR = '/admin/registerOperator';

document.addEventListener('DOMContentLoaded', () => {
    renderUniversalHeader('users');
    renderUniversalFooter();
    const form = document.getElementById('form-register-operator');

    if (form) {
        form.addEventListener('submit', handleRegisterOperator);
    }
});

async function handleRegisterOperator(e) {
    e.preventDefault(); // Evita que la página se recargue

    const btnSubmit = document.getElementById('btn-submit-operator');
    const name = document.getElementById('name').value.trim();
    const fLastName = document.getElementById('fLastName').value.trim();
    const mLastName = document.getElementById('mLastName').value.trim();
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value

    //Validar todos los campo esten llenos
    if (!name || !fLastName || !mLastName || !email || !password || !confirmPassword) {
        showModal('Campos Incompletos', 'Por favor, rellene todos los campos obligatorios.', 'error');
        return;
    }

    //QUe las contraseñas coincidan
    if (password !== confirmPassword) {
        showModal('Error de Contraseña', 'Las contraseñas ingresadas no coinciden. Verifíquelas.', 'error');
        return;
    }

    //subir en json al enpoint
    const operatorPayload = {
        name: name,
        fLastName: fLastName,
        mLastName: mLastName,
        email: email,
        password: password,
        confirmPassword: confirmPassword
    };

    btnSubmit.disabled = true;
    btnSubmit.textContent = 'Registrando...';

    try{
        const response = await fetch(API_REGISTER_OPERATOR, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'},
            body: JSON.stringify(operatorPayload)
        });

        if (response.ok) {
            showModal('¡Éxito!', 'El operador ha sido registrado correctamente en el sistema.', 'success');
            document.getElementById('form-register-operator').reset();
        } else if (response.status === 403) {
            showModal('Acceso Denegado', 'No tienes los permisos de ADMINISTRADOR necesarios para esta acción.', 'error');
        } else {
            const errorText = await response.text();
            console.error('Error en el servidor:', errorText);
            showModal('Error', 'No se pudo completar el registro. Intente más tarde.', 'error');
        }

    } catch (error) {
        console.error('Error de red/conexión:', error);
        showModal('Error de Conexión', 'No se pudo establecer comunicación con el servidor de la UPIICSA.', 'error');
    } finally {
        //Desbloquear el botón al terminar todo el flujo
        btnSubmit.disabled = false;
        btnSubmit.textContent = 'Registrar Operador';
    }
}