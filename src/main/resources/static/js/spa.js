let token = sessionStorage.getItem('spaToken');

const tokenStatus = document.getElementById('token-status');
const loginError = document.getElementById('login-error');
const spacesList = document.getElementById('spaces-list');
const spacesError = document.getElementById('spaces-error');
const reservationError = document.getElementById('reservation-error');
const reservationStatus = document.getElementById('reservation-status');
const reservationsList = document.getElementById('reservations-list');
const reservationsError = document.getElementById('reservations-error');
const spaceSelect = document.getElementById('space-select');
const logoutButton = document.getElementById('logout');

function showError(el, message) {
    el.textContent = message || '';
    el.classList.toggle('hidden', !message);
}

function setToken(value) {
    token = value;
    if (value) {
        sessionStorage.setItem('spaToken', value);
    } else {
        sessionStorage.removeItem('spaToken');
    }
    updateTokenStatus(!!value);
}

function updateTokenStatus(hasToken) {
    tokenStatus.textContent = hasToken ? 'Token ready' : 'No token';
    tokenStatus.className = hasToken ? 'pill pill-success' : 'pill pill-muted';
}

function resetUiForLogout() {
    spacesList.innerHTML = 'Sign in first.';
    reservationsList.innerHTML = 'Sign in first.';
    spaceSelect.innerHTML = '<option value="" disabled selected>Select a space</option>';
    reservationStatus.textContent = 'Waiting';
    reservationStatus.className = 'pill pill-muted';
    showError(loginError, '');
    showError(spacesError, '');
    showError(reservationError, '');
    showError(reservationsError, '');
}

function formatApiError(payload, fallback, statusText) {
    let message = payload?.message || payload?.error || fallback || statusText;
    const errors = payload?.errors;
    if (errors && typeof errors === 'object') {
        const detailed = Object.entries(errors).map(([field, msg]) => `${field}: ${msg}`).join('; ');
        if (detailed) {
            message = `${message} (${detailed})`;
        }
    }
    return message;
}

async function apiFetch(path, options = {}) {
    if (!token) {
        throw new Error('Please authenticate first to obtain a JWT.');
    }
    const opts = {
        ...options,
        headers: {
            'Content-Type': 'application/json',
            ...(options.headers || {}),
            'Authorization': `Bearer ${token}`
        }
    };
    const response = await fetch(path, opts);
    if (!response.ok) {
        const body = await response.text();
        const payload = (() => { try { return JSON.parse(body); } catch { return null; }})();
        if (response.status === 401) {
            setToken(null);
            resetUiForLogout();
        }
        const details = formatApiError(payload, body, response.statusText);
        throw new Error(details);
    }
    if (response.status === 204) {
        return null;
    }
    return response.json();
}

async function handleLogin(event) {
    event.preventDefault();
    showError(loginError, '');
    const formData = new FormData(event.target);
    const username = formData.get('username');
    const password = formData.get('password');
    try {
        const res = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        if (!res.ok) {
            const body = await res.json().catch(() => ({}));
            const msg = formatApiError(body, 'Login failed. Check your credentials.', res.statusText);
            throw new Error(msg);
        }
        const data = await res.json();
        setToken(data.token);
        await Promise.all([loadSpaces(), loadReservations()]);
    } catch (e) {
        setToken(null);
        resetUiForLogout();
        showError(loginError, e.message);
    }
}

function renderSpaces(spaces) {
    spaceSelect.innerHTML = '<option value="" disabled selected>Select a space</option>';
    if (!spaces.length) {
        spacesList.innerHTML = '<p class="empty">No spaces found.</p>';
        return;
    }
    const fragment = document.createDocumentFragment();
    spaces.forEach(space => {
        const item = document.createElement('div');
        item.className = 'list-item';
        item.innerHTML = `
            <div>
                <strong>${space.name}</strong><br>
                <span class="muted">Capacity: ${space.capacity} — ${space.openTime} to ${space.closeTime}</span>
            </div>
            <span class="tag">ID ${space.id}</span>
        `;
        fragment.appendChild(item);

        const option = document.createElement('option');
        option.value = space.id;
        option.textContent = `${space.name} (cap. ${space.capacity})`;
        spaceSelect.appendChild(option);
    });
    spacesList.innerHTML = '';
    spacesList.appendChild(fragment);
}

async function loadSpaces() {
    showError(spacesError, '');
    try {
        const spaces = await apiFetch('/api/spaces');
        renderSpaces(spaces);
    } catch (e) {
        showError(spacesError, e.message);
        spacesList.innerHTML = 'Sign in first.';
        spaceSelect.innerHTML = '<option value="" disabled selected>Select a space</option>';
    }
}

function renderReservations(reservations) {
    if (!reservations.length) {
        reservationsList.innerHTML = '<p class="empty">No reservations yet.</p>';
        return;
    }
    const fragment = document.createDocumentFragment();
    reservations.forEach(res => {
        const item = document.createElement('div');
        item.className = 'list-item';
        const meta = document.createElement('div');
        meta.className = 'reservation-meta';
        meta.innerHTML = `
            <strong>${res.studySpace?.name || 'Space #' + res.studySpace?.id}</strong>
            <span class="muted">${res.date} — ${res.startTime} to ${res.endTime}</span>
            <span class="tag">${res.status || 'ACTIVE'}</span>
        `;
        const cancelBtn = document.createElement('button');
        cancelBtn.type = 'button';
        cancelBtn.textContent = 'Cancel';
        cancelBtn.onclick = () => cancelReservation(res.id);
        const normalizedStatus = (res.status || '').toUpperCase();
        // Allow cancellation while the reservation is still active (e.g. CONFIRMED/PENDING)
        cancelBtn.disabled = normalizedStatus === 'CANCELLED' || normalizedStatus === 'CANCELLED_BY_STAFF';
        item.appendChild(meta);
        item.appendChild(cancelBtn);
        fragment.appendChild(item);
    });
    reservationsList.innerHTML = '';
    reservationsList.appendChild(fragment);
}

async function loadReservations() {
    showError(reservationsError, '');
    try {
        const reservations = await apiFetch('/api/reservations/my');
        renderReservations(reservations);
    } catch (e) {
        showError(reservationsError, e.message);
        reservationsList.innerHTML = 'Sign in first.';
    }
}

async function cancelReservation(id) {
    if (!confirm('Cancel this reservation?')) return;
    try {
        await apiFetch(`/api/reservations/${id}`, { method: 'DELETE' });
        await loadReservations();
    } catch (e) {
        showError(reservationsError, e.message);
    }
}

async function handleReservationSubmit(event) {
    event.preventDefault();
    showError(reservationError, '');
    const formData = new FormData(event.target);
    const payload = {
        studySpaceId: parseInt(formData.get('studySpaceId'), 10),
        date: formData.get('date'),
        startTime: formData.get('startTime'),
        endTime: formData.get('endTime')
    };
    reservationStatus.textContent = 'Submitting...';
    reservationStatus.className = 'pill pill-muted';
    try {
        await apiFetch('/api/reservations', { method: 'POST', body: JSON.stringify(payload) });
        reservationStatus.textContent = 'Created';
        reservationStatus.className = 'pill pill-success';
        event.target.reset();
        spaceSelect.selectedIndex = 0;
        await loadReservations();
    } catch (e) {
        reservationStatus.textContent = 'Failed';
        reservationStatus.className = 'pill pill-danger';
        showError(reservationError, e.message);
    }
}

function handleLogout() {
    setToken(null);
    resetUiForLogout();
}

document.getElementById('login-form').addEventListener('submit', handleLogin);
document.getElementById('reload-spaces').addEventListener('click', loadSpaces);
document.getElementById('reservation-form').addEventListener('submit', handleReservationSubmit);
document.getElementById('reload-reservations').addEventListener('click', loadReservations);
logoutButton.addEventListener('click', handleLogout);

updateTokenStatus(!!token);
if (token) {
    Promise.all([loadSpaces(), loadReservations()]).catch(() => handleLogout());
}
