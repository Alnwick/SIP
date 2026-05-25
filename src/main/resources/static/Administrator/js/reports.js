const API_CATALOGS = '/catalogs';
const API_STUDENTS = '/students';
const API_LOGOUT = '/auth/logout';
const API_REPORTS = '/admin/generate-report';

let selectedCareers = [];
let selectedPlan = 'all';
let selectedFilter = 'total';
let currentPage = 0;

document.addEventListener('DOMContentLoaded', () => {
    renderUniversalHeader('users');
    init();
    renderUniversalFooter();
});

async function init() {
    setupListeners();
    await fetchCareers();
    await fetchSyllabus();
    await updateDashboard();
}

function setupListeners() {
    document.getElementById('searchInput').addEventListener('input', debounce(() => {
        currentPage = 0;
        renderTable();
    }, 300));

    document.getElementById('btnApplyFilters')?.addEventListener('click', async () => {
        currentPage = 0;
        await updateDashboard();
    });

    document.getElementById('btnDescargarReporte')?.addEventListener('click', downloadReportExcel);
}

async function fetchCareers() {
    const careers = await apiRequest(`${API_CATALOGS}/careers?SchoolName=UPIICSA`);
    if (!careers) return;

    const nameMapping = {
        "Ingeniería en Informática": "IIn",
        "Ingeniería en Transporte": "IT",
        "Licenciatura en Administracion Industrial": "LAI",
        "Ingeniería Ferroviaria": "IF",
        "Ingeniería Industrial": "II",
        "Licenciatura en Ciencias de la Informatica": "LCI",
        "Ingeniería en Sistemas Automotrices": "ISA"
    };

    const container = document.getElementById('careerContainer');
    if (!container) return;

    let html = `
        <label class="career-checkbox-label">
            <input type="checkbox" id="chkAllCareers" checked> Todas
        </label>
    `;

    careers.forEach(c => {
        const nombreVisual = nameMapping[c.name] || c.acronym;
        html += `
            <label class="career-checkbox-label">
                <input type="checkbox" class="career-item-checkbox" data-acronym="${c.acronym}"> ${nombreVisual}
            </label>
        `;
    });

    container.innerHTML = html;

    const chkAll = document.getElementById('chkAllCareers');
    const itemCheckboxes = container.querySelectorAll('.career-item-checkbox');

    chkAll.onchange = async () => {
        if (chkAll.checked) {
            itemCheckboxes.forEach(cb => cb.checked = false);
            selectedCareers = [];
        }
        currentPage = 0;
        await updateDashboard();
    };

    itemCheckboxes.forEach(cb => {
        cb.onchange = async () => {
            if (cb.checked) {
                chkAll.checked = false;
            }

            selectedCareers = Array.from(itemCheckboxes)
                .filter(item => item.checked)
                .map(item => item.dataset.acronym);

            if (selectedCareers.length === 0) {
                chkAll.checked = true;
            }

            currentPage = 0;
            await updateDashboard();
        };
    });
}

async function fetchSyllabus() {
    const container = document.getElementById('planContainer');
    if (!container) return;

    const careerFiltro = selectedCareers.length > 0 ? selectedCareers[0] : 'all';
    let url;

    if (careerFiltro === 'all') {
        url = `${API_CATALOGS}/allSyllabus?schoolAcronym=UPIICSA`;
    } else {
        url = `${API_CATALOGS}/syllabus?schoolAcronym=UPIICSA&careerAcronym=${careerFiltro}`;
    }

    const syllabus = await apiRequest(url);

    container.innerHTML = '<div class="selectable-item active" data-code="all">Todos los planes</div>' +
        (syllabus ? syllabus.map(s => `<div class="selectable-item" data-code="${s.code}">${s.code}</div>`).join('') : '');

    container.querySelectorAll('.selectable-item').forEach(item => {
        item.onclick = async () => {
            container.querySelector('.active').classList.remove('active');
            item.classList.add('active');
            selectedPlan = item.dataset.code;
            currentPage = 0;

            await renderTable();
            await fetchStats();
        };
    });
}

async function updateDashboard() {
    await fetchStats();
    await renderTable();
}

async function fetchStats() {
    const careerFiltro = selectedCareers.length > 0 ? selectedCareers[0] : 'all';
    const url = `${API_STUDENTS}/stats?careerAcronym=${careerFiltro}&planCode=${selectedPlan}`;
    const stats = await apiRequest(url);
    const grid = document.getElementById('statsGrid');

    const labels = [
        { k: 'total', l: 'Total Alumnos' },
        { k: 'registered', l: 'Registrados' },
        { k: 'docInitial', l: 'Doc. Inicial' },
        { k: 'letterAccep', l: 'Aceptación' },
        { k: 'docFinal', l: 'Doc. Final' }
    ];

    if (grid) {
        grid.innerHTML = labels.map(item => `
            <div class="stat-card ${selectedFilter === item.k ? 'active' : ''}" onclick="filterByStat('${item.k}')">
                <div class="stat-val">${stats ? stats[item.k] : 0}</div>
                <div class="stat-lab">${item.l}</div>
            </div>
        `).join('');
    }
}

window.filterByStat = function(key) {
    selectedFilter = key;
    currentPage = 0;
    updateDashboard();
};

async function renderTable() {
    const container = document.getElementById('studentTableBody');
    const searchTerm = document.getElementById('searchInput').value.trim();

    const careerFiltro = selectedCareers.length > 0 ? selectedCareers[0] : 'all';
    let url = `${API_STUDENTS}/filtered?page=${currentPage}&career=${careerFiltro}&plan=${selectedPlan}`;

    if (searchTerm) {
        url += `&search=${encodeURIComponent(searchTerm)}`;
    }

    const response = await fetch(url);
    const data = await response.json();
    const students = data.content || [];

    try {
        if (students.length === 0) {
            container.innerHTML = '<tr><td colspan="4" style="text-align:center; padding:3rem; color:var(--text-muted)">No hay alumnos con estos filtros.</td></tr>';
            updatePaginationUI(0, 0, 0, true, true);
            return;
        }

        container.innerHTML = students.map(s => `
            <tr onclick="window.location.href='documentosInicio.html?enrollment=${s.enrollment}'" style="cursor:pointer;">
                <td><strong>${s.syllabusCode || 'N/A'}</strong></td>
                <td>${s.name} ${s.fLastName} ${s.mLastName}</td>
                <td>${s.enrollment}</td>
                <td><span class="visual-status ${s.processStatus?.toLowerCase() || ''}">${s.processStatus || 'N/A'}</span></td>
            </tr>
        `).join('');

        const p = data.page;
        const start = (p.number * p.size) + 1;
        const end = (p.number * p.size) + students.length;

        const isFirst = p.number === 0;
        const isLast = p.number >= p.totalPages - 1;

        updatePaginationUI(start, end, p.totalElements, isFirst, isLast);

    } catch (e) { console.error("Error:", e); }
}

function updatePaginationUI(start, end, total, isFirst, isLast) {
    const legend = document.getElementById('paginationLegend');
    if (legend) {
        legend.innerText = `Mostrando ${start} a ${end} de ${total} alumnos`;
    }

    const btnPrev = document.getElementById('btnPrev');
    const btnNext = document.getElementById('btnNext');

    if (btnPrev) btnPrev.disabled = isFirst;
    if (btnNext) btnNext.disabled = isLast;
}

window.changePage = function(step) {
    currentPage += step;
    renderTable();
};

async function apiRequest(url) {
    try {
        const resp = await fetch(url);
        return resp.ok ? await resp.json() : null;
    } catch (e) { return null; }
}

function debounce(func, wait) {
    let timeout;
    return (...args) => {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), wait);
    };
}

/* ==========================================================================
   FUNCIONALIDAD DE DESCARGA DE EXCEL (MANTENIDA EXACTAMENTE IGUAL)
   ========================================================================== */
async function downloadReportExcel() {
    const btnDownload = document.getElementById('btnDescargarReporte');

    // 1. Obtener valores de fecha de los inputs reales del HTML
    const startDate = document.getElementById('startDateInput')?.value || "2026-01-01";
    const endDate = document.getElementById('endDateInput')?.value || "2026-05-31";

    // 2. Mapear la carrera seleccionada al array que espera el RequestBody de Java
    const careersPayload = selectedCareers;
    try {
        if (btnDownload) {
            btnDownload.disabled = true;
            btnDownload.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Generando...';
        }

        // 3. Petición POST al backend enviando el cuerpo con la lista
        const response = await fetch(`${API_REPORTS}?startDate=${startDate}&endDate=${endDate}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(careersPayload)
        });

        if (!response.ok) {
            throw new Error(`Error en el servidor: ${response.status}`);
        }

        // 4. Interpretar la respuesta binaria como un BLOB
        const blob = await response.blob();

        // 5. Crear el enlace temporal en el navegador e iniciar la descarga automática
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;

        a.download = `Reporte SIP(${startDate} : ${endDate}).xlsx`;

        document.body.appendChild(a);
        a.click();

        window.URL.revokeObjectURL(url);
        a.remove();

        if (typeof showModal === "function") {
            showModal('¡Éxito!', 'El reporte en Excel se ha generado y descargado correctamente.', 'success');
        }

    } catch (error) {
        console.error('Error detallado descargando el archivo:', error);
        if (typeof showModal === "function") {
            showModal('Error de Descarga', 'No se pudo obtener el archivo. Revisa los permisos o contacta a soporte.', 'error');
        }
    } finally {
        if (btnDownload) {
            btnDownload.disabled = false;
            btnDownload.innerHTML = '<i class="fas fa-file-excel"></i> Descargar';
        }
    }
}