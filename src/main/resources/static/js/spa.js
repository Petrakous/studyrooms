/**
 * Study Rooms SPA (Single Page Application)
 * 
 * A client-side application for managing study room reservations.
 * Uses JWT (JSON Web Token) authentication stored in sessionStorage.
 * 
 * Features:
 * - User authentication via JWT
 * - View available study spaces
 * - Create new reservations
 * - View and cancel existing reservations
 */

// ============================================================================
// STATE & DOM REFERENCES
// ============================================================================

/** JWT token retrieved from sessionStorage (persists across page reloads within session) */
let token = sessionStorage.getItem('spaToken');

// DOM element references for UI updates
const tokenStatus = document.getElementById('token-status');       // Displays token state (ready/no token)
const loginError = document.getElementById('login-error');         // Login error message container
const spacesList = document.getElementById('spaces-list');         // Container for study spaces list
const spacesError = document.getElementById('spaces-error');       // Spaces loading error container
const reservationError = document.getElementById('reservation-error');   // Reservation form error container
const reservationStatus = document.getElementById('reservation-status'); // Reservation submission status pill
const reservationsList = document.getElementById('reservations-list');   // Container for user's reservations
const reservationsError = document.getElementById('reservations-error'); // Reservations loading error container
const spaceSelect = document.getElementById('space-select');       // Dropdown for selecting a space
const logoutButton = document.getElementById('logout');            // Logout button
const staffCard = document.getElementById('staff-card') || document.getElementById('staff-spaces-card');
const staffSpacesCard = document.getElementById('staff-spaces-card');
const staffReservationsCard = document.getElementById('staff-reservations-card');
const staffOccupancyCard = document.getElementById('staff-occupancy-card');

const staffSpaceForm = document.getElementById('staff-space-form');
const staffSpaceIdInput = document.getElementById('staff-space-id');
const staffSpaceNameInput = document.getElementById('staff-space-name');
const staffSpaceDescriptionInput = document.getElementById('staff-space-description');
const staffSpaceCapacityInput = document.getElementById('staff-space-capacity');
const staffSpaceOpenInput = document.getElementById('staff-space-open');
const staffSpaceCloseInput = document.getElementById('staff-space-close');
const staffSpaceFullDayInput = document.getElementById('staff-space-fullday');
const staffSpaceStatus = document.getElementById('staff-space-status');
const staffSpacesList = document.getElementById('staff-spaces-list');
const staffSpacesError = document.getElementById('staff-spaces-error');

const staffReservationsForm = document.getElementById('staff-reservations-form');
const staffReservationsDateInput = document.getElementById('staff-reservations-date');
const staffReservationsList = document.getElementById('staff-reservations-list');
const staffReservationsError = document.getElementById('staff-reservations-error');

const staffOccupancyForm = document.getElementById('staff-occupancy-form');
const staffOccupancySpaceSelect = document.getElementById('staff-occupancy-space');
const staffOccupancyStartInput = document.getElementById('staff-occupancy-start');
const staffOccupancyEndInput = document.getElementById('staff-occupancy-end');
const staffOccupancySummary = document.getElementById('staff-occupancy-summary');
const staffOccupancyList = document.getElementById('staff-occupancy-list');
const staffOccupancyError = document.getElementById('staff-occupancy-error');

const reloadStaffSpacesButton = document.getElementById('reload-staff-spaces');
const reloadStaffReservationsButton = document.getElementById('reload-staff-reservations');
const reloadStaffOccupancyButton = document.getElementById('reload-staff-occupancy');

const staffSections = [staffCard, staffSpacesCard, staffReservationsCard, staffOccupancyCard].filter(Boolean);


let cachedSpaces = [];

// ============================================================================
// UTILITY FUNCTIONS
// ============================================================================

/**
 * Shows or hides an error message in the specified element.
 * @param {HTMLElement} el - The error container element
 * @param {string} message - The error message (empty string or null to hide)
 */
function showError(el, message) {
    el.textContent = message || '';
    el.classList.toggle('hidden', !message);
}

/**
 * Updates the JWT token in memory and sessionStorage.
 * Also updates the UI to reflect token status.
 * @param {string|null} value - The JWT token string, or null to clear
 */
function setToken(value) {
    token = value;
    if (value) {
        sessionStorage.setItem('spaToken', value);
    } else {
        sessionStorage.removeItem('spaToken');
    }
    updateTokenStatus(!!value);
}

/**
 * Updates the token status indicator pill in the UI.
 * @param {boolean} hasToken - Whether a valid token exists
 */
function updateTokenStatus(hasToken) {
    tokenStatus.textContent = hasToken ? 'Token ready' : 'No token';
    tokenStatus.className = hasToken ? 'pill pill-success' : 'pill pill-muted';
}

/**
 * Resets the UI to its logged-out state.
 * Clears all data displays and error messages.
 */
function resetUiForLogout() {
    spacesList.innerHTML = 'Sign in first.';
    reservationsList.innerHTML = 'Sign in first.';
    spaceSelect.innerHTML = '<option value="" disabled selected>Select a space</option>';
    reservationStatus.textContent = 'Waiting';
    reservationStatus.className = 'pill pill-muted';
    staffSections.forEach(section => section?.classList.add('hidden'));
    staffSpacesList.textContent = 'Sign in as staff to manage spaces.';
    staffReservationsList.textContent = 'Sign in as staff to view reservations.';
    staffOccupancyList.textContent = 'Sign in as staff to view occupancy.';
    staffOccupancySummary.classList.add('hidden');
    staffOccupancySummary.textContent = '';
    staffOccupancySpaceSelect.innerHTML = '<option value="" disabled selected>Select a space</option>';
    if (staffSpaceForm) {
        staffSpaceForm.reset();
        staffSpaceIdInput.value = '';
        staffSpaceStatus.textContent = 'Waiting';
        staffSpaceStatus.className = 'pill pill-muted';
    }
    if (staffReservationsForm) {
        staffReservationsForm.reset();
    }
    if (staffOccupancyForm) {
        staffOccupancyForm.reset();
    }
    showError(loginError, '');
    showError(spacesError, '');
    showError(reservationError, '');
    showError(reservationsError, '');
    showError(staffSpacesError, '');
    showError(staffReservationsError, '');
    showError(staffOccupancyError, '');
}

/**
 * Toggles staff-only UI elements.
 * @param {boolean} isStaff - Whether the current user has staff access
 */
function setStaffVisibility(isStaff) {
    staffSections.forEach(section => {
        if (!section) return;
        section.classList.toggle('hidden', !isStaff);
    });
}

/**
 * Checks whether the current token grants access to staff endpoints.
 * Uses a lightweight staff-only API request to detect role.
 */
async function checkStaffAccess() {
    if (!token) {
        setStaffVisibility(false);
        return false;
    }

    const today = new Date().toISOString().slice(0, 10);
    try {
        const response = await fetch(`/api/staff/reservations?date=${today}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            setStaffVisibility(true);
            return true;
        }

        if (response.status === 401) {
            setToken(null);
            resetUiForLogout();
        }

        setStaffVisibility(false);
        return false;
    } catch {
        setStaffVisibility(false);
        return false;
    }
}

/**
 * Formats an API error response into a user-friendly message.
 * Handles various error response formats from the backend.
 * @param {Object} payload - Parsed JSON error response body
 * @param {string} fallback - Fallback message if payload is empty
 * @param {string} statusText - HTTP status text
 * @returns {string} Formatted error message
 */
function formatApiError(payload, fallback, statusText) {
    // Try to extract message from common API error formats
    let message = payload?.message || payload?.error || fallback || statusText;
    
    // If the API returned field-specific validation errors, append them
    const errors = payload?.errors;
    if (errors && typeof errors === 'object') {
        const detailed = Object.entries(errors).map(([field, msg]) => `${field}: ${msg}`).join('; ');
        if (detailed) {
            message = `${message} (${detailed})`;
        }
    }
    return message;
}

// ============================================================================
// API COMMUNICATION
// ============================================================================

/**
 * Makes an authenticated API request with the JWT token.
 * Automatically handles 401 responses by clearing the token and resetting UI.
 * 
 * @param {string} path - API endpoint path (e.g., '/api/spaces')
 * @param {Object} options - Fetch options (method, body, headers, etc.)
 * @returns {Promise<Object|null>} Parsed JSON response, or null for 204 No Content
 * @throws {Error} If not authenticated or if the API returns an error
 */
async function apiFetch(path, options = {}) {
    // Require authentication before making API calls
    if (!token) {
        throw new Error('Please authenticate first to obtain a JWT.');
    }
    
    // Merge default headers with provided options
    const opts = {
        ...options,
        headers: {
            'Content-Type': 'application/json',
            ...(options.headers || {}),
            'Authorization': `Bearer ${token}`  // Add JWT Bearer token
        }
    };
    
    const response = await fetch(path, opts);
    
    // Handle error responses
    if (!response.ok) {
        const body = await response.text();
        // Safely try to parse JSON error body
        const payload = (() => { try { return JSON.parse(body); } catch { return null; }})();
        
        // If unauthorized (token expired/invalid), clear auth state
        if (response.status === 401) {
            setToken(null);
            resetUiForLogout();
        }
        
        const details = formatApiError(payload, body, response.statusText);
        throw new Error(details);
    }
    
    // Handle 204 No Content (e.g., successful DELETE)
    if (response.status === 204) {
        return null;
    }
    
    return response.json();
}

// ============================================================================
// AUTHENTICATION
// ============================================================================

/**
 * Handles the login form submission.
 * Authenticates the user and fetches initial data on success.
 * @param {Event} event - Form submit event
 */
async function handleLogin(event) {
    event.preventDefault();
    showError(loginError, '');
    
    // Extract credentials from the login form
    const formData = new FormData(event.target);
    const username = formData.get('username');
    const password = formData.get('password');
    
    try {
        // Authenticate via the login API endpoint
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
        
        // Store the JWT token from the response
        const data = await res.json();
        setToken(data.token);
        
        // Load spaces and reservations in parallel after successful login
        const [_, __, isStaff] = await Promise.all([loadSpaces(), loadReservations(), checkStaffAccess()]);
        if (isStaff) {
            await Promise.all([loadStaffSpaces(), loadStaffReservations()]);
        }
    } catch (e) {
        // On failure, clear any partial auth state and show error
        setToken(null);
        resetUiForLogout();
        showError(loginError, e.message);
    }
}

// ============================================================================
// STUDY SPACES
// ============================================================================

/**
 * Renders the list of available study spaces to the UI.
 * Populates both the spaces list display and the reservation form dropdown.
 * @param {Array} spaces - Array of space objects from the API
 */
function renderSpaces(spaces) {
    cachedSpaces = Array.isArray(spaces) ? spaces : [];
    // Reset the space selection dropdown
    spaceSelect.innerHTML = '<option value="" disabled selected>Select a space</option>';
    
    // Handle empty spaces list
    if (!spaces.length) {
        spacesList.innerHTML = '<p class="empty">No spaces found.</p>';
        return;
    }
    
    // Use DocumentFragment for efficient DOM manipulation
    const fragment = document.createDocumentFragment();
    
    spaces.forEach(space => {
        // Create a list item for each space
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

        // Also add an option to the reservation form dropdown
        const option = document.createElement('option');
        option.value = space.id;
        option.textContent = `${space.name} (cap. ${space.capacity})`;
        spaceSelect.appendChild(option);
    });
    
    // Replace the spaces list content with the new fragment
    spacesList.innerHTML = '';
    spacesList.appendChild(fragment);

    updateStaffSpaceOptions();
    if (!staffSpacesCard.classList.contains('hidden')) {
        renderStaffSpaces(cachedSpaces);
    }
}

function updateStaffSpaceOptions() {
    if (!staffOccupancySpaceSelect) return;
    staffOccupancySpaceSelect.innerHTML = '<option value="" disabled selected>Select a space</option>';
    cachedSpaces.forEach(space => {
        const option = document.createElement('option');
        option.value = space.id;
        option.textContent = `${space.name} (cap. ${space.capacity})`;
        staffOccupancySpaceSelect.appendChild(option);
    });
}

/**
 * Fetches available study spaces from the API and renders them.
 * Called on login success and when the refresh button is clicked.
 */
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

// ============================================================================
// STAFF - MANAGE SPACES
// ============================================================================

function formatSpaceHours(space) {
    if (space.fullDay) {
        return 'Open 24 hours';
    }
    return `${space.openTime || '--:--'} to ${space.closeTime || '--:--'}`;
}

function resetStaffSpaceForm() {
    staffSpaceForm.reset();
    staffSpaceIdInput.value = '';
    staffSpaceStatus.textContent = 'Waiting';
    staffSpaceStatus.className = 'pill pill-muted';
    updateStaffFullDayInputs();
}

function updateStaffFullDayInputs() {
    const isFullDay = staffSpaceFullDayInput.checked;
    staffSpaceOpenInput.disabled = isFullDay;
    staffSpaceCloseInput.disabled = isFullDay;
    staffSpaceOpenInput.required = !isFullDay;
    staffSpaceCloseInput.required = !isFullDay;
    if (isFullDay) {
        staffSpaceOpenInput.value = '';
        staffSpaceCloseInput.value = '';
    }
}

function renderStaffSpaces(spaces) {
    if (!spaces.length) {
        staffSpacesList.innerHTML = '<p class="empty">No spaces found.</p>';
        return;
    }

    const fragment = document.createDocumentFragment();
    spaces.forEach(space => {
        const item = document.createElement('div');
        item.className = 'list-item';

        const meta = document.createElement('div');
        meta.innerHTML = `
            <strong>${space.name}</strong><br>
            <span class="muted">Capacity: ${space.capacity} — ${formatSpaceHours(space)}</span>
        `;

        const actions = document.createElement('div');

        const editButton = document.createElement('button');
        editButton.type = 'button';
        editButton.textContent = 'Edit';
        editButton.onclick = () => {
            staffSpaceIdInput.value = space.id;
            staffSpaceNameInput.value = space.name;
            staffSpaceDescriptionInput.value = space.description || '';
            staffSpaceCapacityInput.value = space.capacity;
            staffSpaceFullDayInput.checked = !!space.fullDay;
            staffSpaceOpenInput.value = (space.openTime || '').slice(0, 5);
            staffSpaceCloseInput.value = (space.closeTime || '').slice(0, 5);
            staffSpaceStatus.textContent = 'Editing';
            staffSpaceStatus.className = 'pill pill-muted';
            updateStaffFullDayInputs();
        };

        const deleteButton = document.createElement('button');
        deleteButton.type = 'button';
        deleteButton.textContent = 'Delete';
        deleteButton.onclick = async () => {
            if (!confirm(`Delete "${space.name}"?`)) return;
            try {
                await apiFetch(`/api/spaces/${space.id}`, { method: 'DELETE' });
                await loadSpaces();
                showError(staffSpacesError, '');
            } catch (e) {
                showError(staffSpacesError, e.message);
            }
        };

        actions.appendChild(editButton);
        actions.appendChild(deleteButton);

        item.appendChild(meta);
        item.appendChild(actions);
        fragment.appendChild(item);
    });

    staffSpacesList.innerHTML = '';
    staffSpacesList.appendChild(fragment);
}

async function loadStaffSpaces() {
    showError(staffSpacesError, '');
    try {
        const spaces = await apiFetch('/api/spaces');
        cachedSpaces = spaces;
        renderStaffSpaces(spaces);
        updateStaffSpaceOptions();
    } catch (e) {
        showError(staffSpacesError, e.message);
        staffSpacesList.textContent = 'Sign in as staff to manage spaces.';
    }
}

async function handleStaffSpaceSubmit(event) {
    event.preventDefault();
    showError(staffSpacesError, '');

    const payload = {
        name: staffSpaceNameInput.value.trim(),
        description: staffSpaceDescriptionInput.value.trim() || null,
        capacity: parseInt(staffSpaceCapacityInput.value, 10),
        fullDay: staffSpaceFullDayInput.checked
    };

    if (!payload.fullDay) {
        payload.openTime = staffSpaceOpenInput.value;
        payload.closeTime = staffSpaceCloseInput.value;
    }

    const spaceId = staffSpaceIdInput.value;
    staffSpaceStatus.textContent = spaceId ? 'Updating...' : 'Creating...';
    staffSpaceStatus.className = 'pill pill-muted';

    try {
        if (spaceId) {
            await apiFetch(`/api/spaces/${spaceId}`, {
                method: 'PUT',
                body: JSON.stringify(payload)
            });
        } else {
            await apiFetch('/api/spaces', {
                method: 'POST',
                body: JSON.stringify(payload)
            });
        }
        staffSpaceStatus.textContent = 'Saved';
        staffSpaceStatus.className = 'pill pill-success';
        resetStaffSpaceForm();
        await loadSpaces();
    } catch (e) {
        staffSpaceStatus.textContent = 'Failed';
        staffSpaceStatus.className = 'pill pill-danger';
        showError(staffSpacesError, e.message);
    }
}

// ============================================================================
// STAFF - ALL RESERVATIONS
// ============================================================================

function renderStaffReservations(reservations) {
    if (!reservations.length) {
        staffReservationsList.innerHTML = '<p class="empty">No reservations found.</p>';
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

        const cancelButton = document.createElement('button');
        cancelButton.type = 'button';
        cancelButton.textContent = 'Cancel';
        cancelButton.disabled = ['CANCELLED', 'CANCELLED_BY_STAFF'].includes((res.status || '').toUpperCase());
        cancelButton.onclick = async () => {
            if (!confirm('Cancel this reservation as staff?')) return;
            try {
                await apiFetch(`/api/staff/reservations/${res.id}/cancel`, { method: 'POST' });
                await loadStaffReservations();
            } catch (e) {
                showError(staffReservationsError, e.message);
            }
        };

        item.appendChild(meta);
        item.appendChild(cancelButton);
        fragment.appendChild(item);
    });

    staffReservationsList.innerHTML = '';
    staffReservationsList.appendChild(fragment);
}

async function loadStaffReservations() {
    showError(staffReservationsError, '');
    const dateValue = staffReservationsDateInput.value;
    const query = dateValue ? `?date=${dateValue}` : '';
    try {
        const reservations = await apiFetch(`/api/staff/reservations${query}`);
        renderStaffReservations(reservations);
    } catch (e) {
        showError(staffReservationsError, e.message);
        staffReservationsList.textContent = 'Sign in as staff to view reservations.';
    }
}

function handleStaffReservationsFilter(event) {
    event.preventDefault();
    loadStaffReservations();
}

// ============================================================================
// STAFF - OCCUPANCY
// ============================================================================

function renderStaffOccupancy(stats) {
    if (!stats.length) {
        staffOccupancyList.innerHTML = '<p class="empty">No data for the selected range.</p>';
        return;
    }

    const fragment = document.createDocumentFragment();
    stats.forEach(entry => {
        const percent = Number(entry.occupancyPercentage ?? 0).toFixed(1);
        const item = document.createElement('div');
        item.className = 'list-item';
        item.innerHTML = `
            <div>
                <strong>${entry.date}</strong><br>
                <span class="muted">Reservations: ${entry.reservationsCount}</span>
            </div>
            <span class="tag">${percent}%</span>
        `;
        fragment.appendChild(item);
    });

    staffOccupancyList.innerHTML = '';
    staffOccupancyList.appendChild(fragment);
}

async function loadStaffOccupancy(event) {
    if (event) {
        event.preventDefault();
    }
    showError(staffOccupancyError, '');

    const spaceId = staffOccupancySpaceSelect.value;
    const startDate = staffOccupancyStartInput.value;
    const endDate = staffOccupancyEndInput.value;
    if (!spaceId || !startDate || !endDate) {
        showError(staffOccupancyError, 'Please select a space and date range.');
        return;
    }

    try {
        const stats = await apiFetch(`/api/stats/occupancy?spaceId=${spaceId}&startDate=${startDate}&endDate=${endDate}`);
        const space = cachedSpaces.find(item => `${item.id}` === `${spaceId}`);
        staffOccupancySummary.textContent = `${space?.name || 'Selected space'} (${startDate} to ${endDate})`;
        staffOccupancySummary.classList.remove('hidden');
        renderStaffOccupancy(stats);
    } catch (e) {
        showError(staffOccupancyError, e.message);
        staffOccupancyList.textContent = 'Sign in as staff to view occupancy.';
    }
}

// ============================================================================
// RESERVATIONS
// ============================================================================

/**
 * Renders the user's reservations to the UI.
 * Each reservation includes a cancel button (disabled if already cancelled).
 * @param {Array} reservations - Array of reservation objects from the API
 */
function renderReservations(reservations) {
    // Handle empty reservations list
    if (!reservations.length) {
        reservationsList.innerHTML = '<p class="empty">No reservations yet.</p>';
        return;
    }
    
    // Use DocumentFragment for efficient DOM manipulation
    const fragment = document.createDocumentFragment();
    
    reservations.forEach(res => {
        // Create container for each reservation
        const item = document.createElement('div');
        item.className = 'list-item';
        
        // Create metadata display (space name, date/time, status)
        const meta = document.createElement('div');
        meta.className = 'reservation-meta';
        meta.innerHTML = `
            <strong>${res.studySpace?.name || 'Space #' + res.studySpace?.id}</strong>
            <span class="muted">${res.date} — ${res.startTime} to ${res.endTime}</span>
            <span class="tag">${res.status || 'ACTIVE'}</span>
        `;
        
        // Create cancel button with click handler
        const cancelBtn = document.createElement('button');
        cancelBtn.type = 'button';
        cancelBtn.textContent = 'Cancel';
        cancelBtn.onclick = () => cancelReservation(res.id);
        
        // Disable cancel button for already-cancelled reservations
        const normalizedStatus = (res.status || '').toUpperCase();
        // Allow cancellation while the reservation is still active (e.g. CONFIRMED)
        cancelBtn.disabled = normalizedStatus === 'CANCELLED' || normalizedStatus === 'CANCELLED_BY_STAFF';
        
        item.appendChild(meta);
        item.appendChild(cancelBtn);
        fragment.appendChild(item);
    });
    
    // Replace the reservations list content with the new fragment
    reservationsList.innerHTML = '';
    reservationsList.appendChild(fragment);
}

/**
 * Fetches the current user's reservations from the API and renders them.
 * Called on login success and when the refresh button is clicked.
 */
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

/**
 * Cancels a reservation by ID after user confirmation.
 * Refreshes the reservations list on success.
 * @param {number} id - The reservation ID to cancel
 */
async function cancelReservation(id) {
    // Confirm before cancelling
    if (!confirm('Cancel this reservation?')) return;
    
    try {
        await apiFetch(`/api/reservations/${id}`, { method: 'DELETE' });
        // Refresh the list to show updated status
        await loadReservations();
    } catch (e) {
        showError(reservationsError, e.message);
    }
}

/**
 * Handles the reservation form submission.
 * Creates a new reservation and refreshes the list on success.
 * @param {Event} event - Form submit event
 */
async function handleReservationSubmit(event) {
    event.preventDefault();
    showError(reservationError, '');
    
    // Build the reservation payload from form data
    const formData = new FormData(event.target);
    const payload = {
        studySpaceId: parseInt(formData.get('studySpaceId'), 10),
        date: formData.get('date'),
        startTime: formData.get('startTime'),
        endTime: formData.get('endTime')
    };
    
    // Update status indicator to show submission in progress
    reservationStatus.textContent = 'Submitting...';
    reservationStatus.className = 'pill pill-muted';
    
    try {
        // Send the reservation request
        await apiFetch('/api/reservations', { method: 'POST', body: JSON.stringify(payload) });
        
        // Update status to success
        reservationStatus.textContent = 'Created';
        reservationStatus.className = 'pill pill-success';
        
        // Reset the form for the next reservation
        event.target.reset();
        spaceSelect.selectedIndex = 0;
        
        // Refresh reservations to show the new one
        await loadReservations();
    } catch (e) {
        // Update status to failure and show error
        reservationStatus.textContent = 'Failed';
        reservationStatus.className = 'pill pill-danger';
        showError(reservationError, e.message);
    }
}

// ============================================================================
// LOGOUT
// ============================================================================

/**
 * Handles user logout.
 * Clears the JWT token and resets the UI to logged-out state.
 */
function handleLogout() {
    setToken(null);
    resetUiForLogout();
}

// ============================================================================
// EVENT LISTENERS & INITIALIZATION
// ============================================================================

// Attach form and button event handlers
document.getElementById('login-form').addEventListener('submit', handleLogin);
document.getElementById('reload-spaces').addEventListener('click', loadSpaces);
document.getElementById('reservation-form').addEventListener('submit', handleReservationSubmit);
document.getElementById('reload-reservations').addEventListener('click', loadReservations);
logoutButton.addEventListener('click', handleLogout);
staffSpaceForm.addEventListener('submit', handleStaffSpaceSubmit);
staffSpaceFullDayInput.addEventListener('change', updateStaffFullDayInputs);
reloadStaffSpacesButton.addEventListener('click', loadStaffSpaces);
staffReservationsForm.addEventListener('submit', handleStaffReservationsFilter);
reloadStaffReservationsButton.addEventListener('click', loadStaffReservations);
staffOccupancyForm.addEventListener('submit', loadStaffOccupancy);
reloadStaffOccupancyButton.addEventListener('click', loadStaffOccupancy);

// Initialize UI based on existing token state
updateTokenStatus(!!token);
updateStaffFullDayInputs();

// If a token exists (e.g., from a previous session), auto-load data
// On failure (e.g., expired token), automatically log out
if (token) {
    Promise.all([loadSpaces(), loadReservations(), checkStaffAccess()])
        .then((results) => {
            if (results[2]) {
                return Promise.all([loadStaffSpaces(), loadStaffReservations()]);
            }
            return null;
        })
        .catch(() => handleLogout());
}
