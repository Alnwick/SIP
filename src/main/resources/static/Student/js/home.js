const API_STATUS = '/students/process-status'; // GET
const API_LOGOUT = '/auth/logout'; // POST
const API_DOCS_STATUS = '/documents/my-status'; // GET

// Fases utilizadas exclusivamente para la representación visual en la interfaz de usuario
const PHASES = ["Registrado", "Doc Inicial", "Cartas", "Doc Término", "Liberación"];

document.addEventListener('DOMContentLoaded', () => {
    renderUniversalHeader('students');
    loadData();
    renderUniversalFooter();
});

async function loadData() {
    let stagesData = [];
    let docsData = [];
    let docsCarts = [];
    let docsTermino = [];

    try {
        const urlDocs = `${API_DOCS_STATUS}?processStatus=DOC_INICIAL`;
        const urlCarts = `${API_DOCS_STATUS}?processStatus=CARTAS`;
        const urlTermino = `${API_DOCS_STATUS}?processStatus=DOC_FINAL`;

        // Ejecución concurrente de peticiones para agilizar la carga del dashboard del estudiante
        const [respStatus, respDocs, respCarts, respTermino] = await Promise.all([
            fetch(API_STATUS),
            fetch(urlDocs),
            fetch(urlCarts),
            fetch(urlTermino)
        ]);

        if (respStatus.ok) stagesData = await respStatus.json();
        if (respDocs.ok) docsData = await respDocs.json();
        if (respCarts.ok) docsCarts = await respCarts.json();
        if (respTermino.ok) docsTermino = await respTermino.json();

        console.log("Docs inicio:", docsData);
        console.log("Docs Cartas:", docsCarts);
        console.log("Docs Término:", docsTermino);
    } catch (e) {
        console.warn("Error cargando datos", e);
    }

    renderProgress(stagesData, docsData, docsCarts, docsTermino);
}

function renderProgress(apiData, docsData, docsCarts, docsTermino) {
    const stepper = document.getElementById('main-stepper');
    if (!stepper) return;

    // Identificadores de documentos obligatorios parametrizados por fase según la normativa interna
    const docsObligatorios = ["CEDULA_REGISTRO", "CONSTANCIA_IMSS", "CAPTURA_EMPRESA", "CAPTURA_ALUMNO", "HORARIO"];
    const docsCartsFase2 = ["CARTA_ACEPTACION"];
    const docsTerminoFase3 = ["HOJAS_ASISTENCIA", "INFORMES_MENSUALES", "CARTA_TERMINO"];

    // Evaluación de banderas para identificar si el estudiante ha iniciado la carga en cada sección
    const haSubidoAlgo = docsData && docsData.some(doc => doc.fileName !== null && doc.status !== 'SIN_CARGA');
    const haSubidoAlgoCarts = docsCarts && docsCartsFase2.some(doc => doc.fileName !== null && doc.status !== 'SIN_CARGA');
    const haSubidoAlgoTermino = docsTermino && docsTerminoFase3.some(doc => doc.fileName !== null && doc.status !== 'SIN_CARGA');

    // Comprobación de aprobación unánime por fase (estatus "CORRECTO" en cada archivo requerido)
    const todoAprobadoReal = docsObligatorios.every(type => {
        const doc = docsData.find(d => d.typeCode === type);
        return doc && doc.status === 'CORRECTO';
    });

    const todoAprobadoRealCarts = docsCartsFase2.every(type => {
        const doc = docsCarts.find(d => d.typeCode === type);
        return doc && doc.status === 'CORRECTO';
    });

    const todoAprobadoRealTermino = docsTerminoFase3.every(type => {
        const doc = docsTermino.find(d => d.typeCode === type);
        return doc && doc.status === 'CORRECTO';
    });

    // Comprobación de archivos faltantes o en estado nulo por fase
    const faltaSubirArchivo = docsObligatorios.some(type => {
        const doc = docsData.find(d => d.typeCode === type);
        return !doc || doc.status === 'SIN_CARGA' || !doc.fileName;
    });
    const faltaSubirArchivoCarts = docsCartsFase2.some(type => {
        const doc = docsCarts.find(d => d.typeCode === type);
        return !doc || doc.status === 'SIN_CARGA' || !doc.fileName;
    });
    const faltaSubirArchivoTermino = docsTerminoFase3.some(type => {
        const doc = docsTermino.find(d => d.typeCode === type);
        return !doc || doc.status === 'SIN_CARGA' || !doc.fileName;
    });

    // Mapeo dinámico del Stepper basado en el estado cronológico de las fases del backend
    stepper.innerHTML = PHASES.map((name, idx) => {
        const data = apiData[idx] || {};
        let current = data.isCurrent || false;

        // Una fase se considera completada de forma nativa si tiene una fecha registrada válida
        let done = data.date && data.date !== "" && data.date !== "-";
        let displayDate = data.date;
        let customStatus = "";

        // Extracción explícita de la etapa activa reportada por el motor de procesos backend
        const etapaActivaBackend = apiData.find(d => d.isCurrent)?.stageName || "";

        // --- LÓGICA PARA FASE 0 (REGISTRADO) ---
        if (idx === 0) {
            if (etapaActivaBackend !== "REGISTRADO") {
                // Si el proceso ya avanzó de etapa, por defecto el registro inicial está concluido
                done = true;
                current = false;
            } else {
                done = false;
                current = true;
                customStatus = `Inició: ${fmt(displayDate)}`;
            }
        }

        // --- LÓGICA PARA FASE 1 (DOC_INICIAL) ---
        if (idx === 1) {
            if (todoAprobadoReal) {
                done = true;
                current = false;
            } else if (etapaActivaBackend === "DOC_INICIAL" || haSubidoAlgo) {
                done = false;
                current = true;
                customStatus = faltaSubirArchivo ? "Documentación incompleta" : "Revisando...";
            } else {
                done = false;
                current = false;
            }
        }

        // --- LÓGICA PARA FASE 2 (CARTAS) ---
        if (idx === 2) {
            if (todoAprobadoRealCarts) {
                done = true;
                current = false;
                // Fallback de contingencia visual en caso de requerir la fecha de la última actualización del archivo
                const ultimaCarta = docsCarts.find(d => d.typeCode === "CARTA_ACEPTACION");
                if (!displayDate || displayDate === "-") {
                    displayDate = ultimaCarta ? ultimaCarta.uploadDate : displayDate;
                }
            } else if (todoAprobadoReal || etapaActivaBackend === "CARTAS") {
                done = false;
                current = true;
                if (haSubidoAlgoCarts) {
                    customStatus = faltaSubirArchivoCarts ? "Documentación incompleta" : "Revisando...";
                } else {
                    customStatus = "Esperando documentos";
                }
            } else {
                done = false;
                current = false;
            }
        }

        // --- LÓGICA PARA FASE 3 (DOC_FINAL) ---
        if (idx === 3) {
            if (todoAprobadoRealTermino) {
                done = true;
                current = false;
                const ultimoTermino = docsTermino.find(d => d.typeCode === "CARTA_TERMINO");
                if (!displayDate || displayDate === "-") {
                    displayDate = ultimoTermino ? ultimoTermino.uploadDate : displayDate;
                }
            } else if (todoAprobadoRealCarts || etapaActivaBackend === "DOC_FINAL") {
                done = false;
                current = true;
                if (haSubidoAlgoTermino) {
                    customStatus = faltaSubirArchivoTermino ? "Documentación incompleta" : "Revisando...";
                } else {
                    customStatus = "Esperando reportes";
                }
            } else {
                done = false;
                current = false;
            }
        }

        // --- LÓGICA PARA FASE 4 (LIBERACIÓN) ---
        if (idx === 4) {
            if (todoAprobadoRealTermino || etapaActivaBackend === "LIBERADO") {
                // Si el backend reporta la fase activa de liberación o la anterior concluyó al 100%
                if (data.date && data.date !== "-") {
                    done = true;
                    current = false;
                } else {
                    done = false;
                    current = true;
                    customStatus = "Proceso de firmas final";
                }
            } else {
                done = false;
                current = false;
            }
        }

        // Determinación de la clase de estilo CSS según el estado de la iteración actual
        let statusClass = done ? 'completed' : (current ? 'active' : '');

        return `
            <div class="step ${statusClass}">
                <div class="dot">${done ? '✓' : idx + 1}</div>
                <div class="step-info">
                    <span class="label">${name}</span>
                    <div class="date-container">
                        <span class="date-badge">
                            ${done ? 'Terminó: ' + fmt(displayDate) :
            (current ? (customStatus || 'En progreso') : '—')}
                        </span>
                    </div>
                </div>
            </div>
        `;
    }).join('');

    // Actualización delegada de las tarjetas del Dashboard basadas en la sincronización de las fases del alumno
    actualizarTarjetas(todoAprobadoReal, todoAprobadoRealCarts, todoAprobadoRealTermino);

    console.log("Validaciones de flujo:", { todoAprobadoReal, todoAprobadoRealCarts, todoAprobadoRealTermino });
}

function actualizarTarjetas(docsInicialesOK, cartasOK, terminoOK) {
    const configuracion = [
        {
            id: 'card-cartas',
            link: 'registroCartas.html',
            tag: 'lock-tag-cartas',
            puedeAbrir: docsInicialesOK,
            mensaje: "Primero deben aceptar todos tus Documentos Iniciales."
        },
        {
            id: 'card-seguimiento',
            link: 'registroseguimiento.html',
            tag: 'lock-tag-seguimiento',
            puedeAbrir: cartasOK,
            mensaje: "Primero deben Aceptar tus Cartas."
        }
    ];

    configuracion.forEach(item => {
        const card = document.getElementById(item.id);
        const lock = document.getElementById(item.tag);
        if (!card) return;

        if (item.puedeAbrir) {
            // Configuración del elemento DOM en estado DESBLOQUEADO
            card.classList.remove('locked');
            if (lock) lock.style.display = 'none';
            card.onclick = () => window.location.href = item.link;
            card.style.cursor = "pointer";
            card.style.opacity = "1";
        } else {
            // Configuración del elemento DOM en estado BLOQUEADO
            card.classList.add('locked');
            if (lock) lock.style.display = 'flex';
            card.style.cursor = "not-allowed";
            card.onclick = (e) => {
                e.preventDefault();
                showModal("Aviso", item.mensaje, "info");
            };
        }
    });
}

function fmt(d) {
    try {
        if (!d || d === "-") return "—";

        // Validamos si la fecha viene en formato estricto YYYY-MM-DD (ej: "2026-04-22")
        const regexFechaCorta = /^\d{4}-\d{2}-\d{2}$/;

        let date;
        if (regexFechaCorta.test(d)) {
            // Dividimos los componentes numéricos de la cadena
            const [year, month, day] = d.split('-').map(Number);
            // El objeto Date de JS maneja los meses basados en índice 0 (Enero = 0, Abril = 3)
            date = new Date(year, month - 1, day);
        } else {
            // Si viene con timestamp completo o formato ISO complejo, usamos el parseo normal
            date = new Date(d);
        }

        if (isNaN(date.getTime())) return d;

        return date.toLocaleDateString('es-MX', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric'
        });
    } catch (e) {
        return d;
    }
}