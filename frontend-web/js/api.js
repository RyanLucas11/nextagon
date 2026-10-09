(function () {
    const DEFAULT_API_URL = 'https://nextagon-backend.onrender.com';
    const baseUrl = (window.NEXTAGON_API_URL || DEFAULT_API_URL).replace(/\/+$/, '');
    const REQUEST_TIMEOUT_MS = 30000;
    const LOGIN_TIMEOUT_MS = 65000;
    const RETRY_DELAYS_MS = [1000, 2500, 5000, 8000];

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

    async function request(path, options = {}, config = {}) {
        const safePath = path.startsWith('/') ? path : `/${path}`;
        const url = `${baseUrl}${safePath}`;
        const timeoutMs = config.timeoutMs || REQUEST_TIMEOUT_MS, retries = config.retries ?? 0;
        for (let attempt = 0; attempt <= retries; attempt++) {
            const controller = new AbortController(), timeout = setTimeout(() => controller.abort(), timeoutMs);
            try {
                const response = await fetch(url, { headers: { 'Content-Type': 'application/json', ...(options.headers || {}) }, credentials: 'omit', ...options, signal: controller.signal });
                const text = await response.text(); let payload = null;
                if (text) { try { payload = JSON.parse(text); } catch (_) { payload = { message: text }; } }
                if (!response.ok) {
                    const error = new Error(payload?.message || payload?.error || payload?.details || 'Erro ao comunicar com o servidor.'); error.status = response.status;
                    if (attempt < retries && [408,425,429,502,503,504].includes(response.status)) {
                        await new Promise(r => setTimeout(r, RETRY_DELAYS_MS[attempt] || 8000));
                        continue;
                    }
                    throw error;
                }
                return payload;
            } catch (error) {
                const retry = error?.name === 'AbortError' || [408,425,429,502,503,504].includes(error?.status);
                if (attempt < retries && retry) {
                    console.warn('Falha temporária na conexão com a API; nova tentativa.', { url, attempt: attempt + 1, error });
                    await new Promise(r => setTimeout(r, RETRY_DELAYS_MS[attempt] || 8000));
                    continue;
                }
                if (error?.name === 'AbortError') throw new Error('O servidor demorou para responder. Verifique sua conexão e tente novamente.');
                if (error instanceof TypeError) throw new Error('Não foi possível alcançar o servidor. Verifique a conexão e tente novamente.');
                throw error instanceof Error && error.message ? error : new Error('Nao foi possivel conectar com o servidor. Tente novamente.');
            } finally { clearTimeout(timeout); }
        }
        throw new Error('N�o foi poss�vel conectar com o servidor. Tente novamente.');
    }

    const NextagonApi = {
        async warmup() {
            try {
                await request('/health', { method: 'GET' }, { timeoutMs: 55000, retries: 1 });
            } catch (_) {
                // O aquecimento é opcional; o login continua disponível se falhar.
            }
        },
        async login(email, senha) {
            const payload = await request('/auth/login', {
                method: 'POST',
                body: JSON.stringify({
                    email: String(email || '').trim(),
                    password: String(senha || '')
                })
            }, { timeoutMs: LOGIN_TIMEOUT_MS, retries: 0 });

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
