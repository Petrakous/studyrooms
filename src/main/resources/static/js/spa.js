let token = null;

const tokenStatus = document.getElementById('token-status');
const loginError = document.getElementById('login-error');
const spacesList = document.getElementById('spaces-list');
const spacesError = document.getElementById('spaces-error');
const reservationError = document.getElementById('reservation-error');
const reservationStatus = document.getElementById('reservation-status');
const reservationsList = document.getElementById('reservations-list');
const reservationsError = document.getElementById('reservations-error');
const spaceSelect = document.getElementById('space-select');

function showError(el, message) {
    el.textContent = message || '';
    el.classList.toggle('hidden', !message);
}

function updateTokenStatus(hasToken) {
    tokenStatus.textContent = hasToken ? 'Token ready' : 'No token';
    tokenStatus.className = hasToken ? 'pill pill-success' : 'pill pill-muted';
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
        const details = payload?.message || payload?.error || body || response.statusText;
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
            const msg = body.message || 'Login failed. Check your credentials.';
            throw new Error(msg);
        }
        const data = await res.json();
        token = data.token;
        updateTokenStatus(true);
        await Promise.all([loadSpaces(), loadReservations()]);
    } catch (e) {
        token = null;
        updateTokenStatus(false);
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

document.getElementById('login-form').addEventListener('submit', handleLogin);
document.getElementById('reload-spaces').addEventListener('click', loadSpaces);
document.getElementById('reservation-form').addEventListener('submit', handleReservationSubmit);
document.getElementById('reload-reservations').addEventListener('click', loadReservations);

updateTokenStatus(false);
