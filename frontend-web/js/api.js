const NEXTAGON_API_URL = window.NEXTAGON_API_URL || 'http://localhost:8080';
const NEXTAGON_TOKEN_KEY = 'na_access_token';
const NEXTAGON_REFRESH_TOKEN_KEY = 'na_refresh_token';

function getAccessToken() {
    return sessionStorage.getItem(NEXTAGON_TOKEN_KEY);
}

function setAuthTokens(auth) {
    if (auth.accessToken) sessionStorage.setItem(NEXTAGON_TOKEN_KEY, auth.accessToken);
    if (auth.refreshToken) sessionStorage.setItem(NEXTAGON_REFRESH_TOKEN_KEY, auth.refreshToken);
}

function clearAuthTokens() {
    sessionStorage.removeItem(NEXTAGON_TOKEN_KEY);
    sessionStorage.removeItem(NEXTAGON_REFRESH_TOKEN_KEY);
}

async function apiRequest(path, options = {}) {
    const headers = { Accept: 'application/json', ...(options.headers || {}) };
    const token = getAccessToken();
    if (token) headers.Authorization = `Bearer ${token}`;
    if (options.body !== undefined) headers['Content-Type'] = 'application/json';

    let response;
    try {
        response = await fetch(`${NEXTAGON_API_URL}${path}`, { ...options, headers });
    } catch {
        throw new Error('Não foi possível conectar ao servidor. Verifique se o backend está em execução.');
    }

    if (response.status === 204) return undefined;
    const payload = await response.json().catch(() => null);
    if (!response.ok) {
        const message = payload?.message || payload?.error || 'Não foi possível concluir a solicitação.';
        throw new Error(message);
    }
    return payload;
}

const ROLE_MAP = {
    atleta: 'ATHLETE',
    profissional: 'PROFESSIONAL',
    admin: 'ADMIN',
};

function mapRoleToBackend(role) {
    const key = String(role || '').trim().toLowerCase();
    return ROLE_MAP[key] || key.toUpperCase();
}

const NextagonApi = {
    login: (email, password) => apiRequest('/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) }),
    register: (name, email, password, role) => apiRequest('/auth/register', { method: 'POST', body: JSON.stringify({ name, email, password, role: mapRoleToBackend(role) }) }),
    currentUser: () => apiRequest('/users/me'),
    professionals: (page = 0, size = 20, minRating = 0) => apiRequest(`/marketplace/professionals?page=${page}&size=${size}&minRating=${minRating}`),
    athleteProfile: () => apiRequest('/profile/athlete'),
    saveAthleteProfile: (profile) => apiRequest('/profile/athlete', { method: 'PUT', body: JSON.stringify(profile) }),
    professionalProfile: (userId) => apiRequest(`/profile/professional/${encodeURIComponent(userId)}`),
    saveProfessionalProfile: (profile) => apiRequest('/profile/professional', { method: 'PUT', body: JSON.stringify(profile) }),
    athleteContracts: (userId) => apiRequest(`/contracts/athlete/${encodeURIComponent(userId)}`),
    professionalContracts: (userId) => apiRequest(`/contracts/professional/${encodeURIComponent(userId)}`),
    chatHistory: (contractId) => apiRequest(`/chat/contract/${encodeURIComponent(contractId)}`),
    sendMessage: (contractId, content, attachmentUrl = null) => apiRequest('/chat/send', { method: 'POST', body: JSON.stringify({ contractId, content, attachmentUrl }) }),
};