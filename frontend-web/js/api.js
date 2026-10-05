(function () {
    const DEFAULT_API_URL = 'https://nextagon-backend.onrender.com';
    const baseUrl = (window.NEXTAGON_API_URL || DEFAULT_API_URL).replace(/\/+$/, '');

    function normalizeRole(role) {
        const value = String(role || '').trim().toLowerCase();
        if (value === 'athlete' || value === 'atleta') return 'atleta';
        if (value === 'professional' || value === 'profissional') return 'profissional';
        if (value === 'administrator' || value === 'admin' || value === 'administrador') return 'admin';
        return value || 'atleta';
    }

    function buildUser(payload = {}) {
        const normalizedRole = normalizeRole(payload.role);
        return {
            id: payload.id || payload._id || null,
            nome: payload.name || payload.nome || '',
            name: payload.name || payload.nome || '',
            email: payload.email || '',
            role: normalizedRole,
            avatar: payload.avatar || ''
        };
    }

    async function request(path, options = {}) {
        const safePath = path.startsWith('/') ? path : `/${path}`;
        const url = `${baseUrl}${safePath}`;

        try {
            const response = await fetch(url, {
                headers: {
                    'Content-Type': 'application/json',
                    ...(options.headers || {})
                },
                credentials: 'omit',
                ...options,
            });

            let payload = null;
            const text = await response.text();
            if (text) {
                try {
                    payload = JSON.parse(text);
                } catch (error) {
                    payload = { message: text };
                }
            }

            if (!response.ok) {
                const message = payload?.message || payload?.error || payload?.details || 'Erro ao comunicar com o servidor.';
                throw new Error(message);
            }

            return payload;
        } catch (error) {
            if (error instanceof Error && error.message) {
                throw error;
            }
            throw new Error('Não foi possível conectar com o servidor. Tente novamente.');
        }
    }

    const NextagonApi = {
        async login(email, senha) {
            const payload = await request('/auth/login', {
                method: 'POST',
                body: JSON.stringify({
                    email: String(email || '').trim(),
                    password: String(senha || '')
                })
            });

            return {
                accessToken: payload.accessToken || '',
                refreshToken: payload.refreshToken || '',
                user: buildUser(payload.user || {})
            };
        },

        async register(nome, email, senha, role = 'atleta') {
            const mappedRole = normalizeRole(role);
            const payloadRole = mappedRole === 'profissional' ? 'PROFESSIONAL' : mappedRole === 'admin' ? 'ADMIN' : 'ATHLETE';

            const payload = await request('/auth/register', {
                method: 'POST',
                body: JSON.stringify({
                    name: String(nome || '').trim(),
                    email: String(email || '').trim(),
                    password: String(senha || ''),
                    role: payloadRole
                })
            });

            return {
                accessToken: payload.accessToken || '',
                refreshToken: payload.refreshToken || '',
                user: buildUser(payload.user || {})
            };
        }
    };

    window.NEXTAGON_API_URL = baseUrl;
    window.NextagonApi = NextagonApi;

    window.setAuthTokens = function (auth) {
        if (!auth) return;
        localStorage.setItem('na_access_token', auth.accessToken || '');
        localStorage.setItem('na_refresh_token', auth.refreshToken || '');
    };

    window.clearAuthTokens = function () {
        localStorage.removeItem('na_access_token');
        localStorage.removeItem('na_refresh_token');
    };

    window.getAuthToken = function () {
        return localStorage.getItem('na_access_token') || '';
    };
})();