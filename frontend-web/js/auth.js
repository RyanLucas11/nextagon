/* =============================================================
   auth.js — Next Agon · Sistema de Autenticação v2.0
   Reescrito para sessão estável, sem erros no chat e seguro.
   ============================================================= */

/* ─────────────────────────────────────────────────────────────
   CHAVES DE ARMAZENAMENTO
   ───────────────────────────────────────────────────────────── */
const NA_SESSION_KEY = 'na_session_v2';   // localStorage (persistente entre abas)
const NA_USERS_KEY   = 'na_admin_users';  // localStorage (base de usuários)
const SESSION_TTL_MS = 8 * 60 * 60 * 1000; // 8 horas

/* ─────────────────────────────────────────────────────────────
   HASH SIMPLES (SHA-256 via Web Crypto — async)
   Usado apenas para novos cadastros e troca de senha.
   Usuários antigos com senha em texto plano continuam funcionando
   via comparação direta (migração transparente).
   ───────────────────────────────────────────────────────────── */
async function hashSenha(senha) {
    const enc  = new TextEncoder().encode(senha);
    const buf  = await crypto.subtle.digest('SHA-256', enc);
    return Array.from(new Uint8Array(buf)).map(b => b.toString(16).padStart(2, '0')).join('');
}

/* Verifica senha aceitando texto plano (legado) ou hash */
async function verificarSenha(senhaDigitada, senhaArmazenada) {
    // Se já é um hash SHA-256 (64 chars hex), compara como hash
    if (/^[0-9a-f]{64}$/.test(senhaArmazenada)) {
        const hash = await hashSenha(senhaDigitada);
        return hash === senhaArmazenada;
    }
    // Legado: senha em texto plano
    return senhaDigitada === senhaArmazenada;
}

/* ─────────────────────────────────────────────────────────────
   USUÁRIOS PADRÃO (demo)
   ───────────────────────────────────────────────────────────── */
const USERS_DEFAULT = [
    { email: 'atleta@nextagon.com',       senha: '123456', role: 'atleta',       nome: 'Lucas Atleta',     avatar: 'LA', ativo: true },
    { email: 'profissional@nextagon.com', senha: '123456', role: 'profissional', nome: 'Ana Profissional', avatar: 'AP', ativo: true },
    { email: 'admin@nextagon.com',        senha: '123456', role: 'admin',        nome: 'Carlos Admin',     avatar: 'CA', ativo: true },
];

/* ─────────────────────────────────────────────────────────────
   PERMISSÕES POR PAPEL
   ───────────────────────────────────────────────────────────── */
const PERMISSIONS = {
    atleta: {
        verDashboard:            true,
        verAcademia:             true,
        verEsportes:             true,
        verProfissionais:        true,
        criarPerfilProfissional: false,
        acessoAdmin:             false,
        editarTreinoRecomendado: false,
        gerenciarUsuarios:       false,
        gerenciarProfissionais:  false,
        gerenciarTreinos:        false,
        gerenciarExercicios:     false,
    },
    profissional: {
        verDashboard:            true,
        verAcademia:             true,
        verEsportes:             true,
        verProfissionais:        true,
        criarPerfilProfissional: true,
        acessoAdmin:             false,
        editarTreinoRecomendado: false,
        gerenciarUsuarios:       false,
        gerenciarProfissionais:  false,
        gerenciarTreinos:        false,
        gerenciarExercicios:     false,
    },
    admin: {
        verDashboard:            true,
        verAcademia:             true,
        verEsportes:             true,
        verProfissionais:        true,
        criarPerfilProfissional: true,
        acessoAdmin:             true,
        editarTreinoRecomendado: true,
        gerenciarUsuarios:       true,
        gerenciarProfissionais:  true,
        gerenciarTreinos:        true,
        gerenciarExercicios:     true,
    },
};

/* ─────────────────────────────────────────────────────────────
   GERENCIAMENTO DE USUÁRIOS
   ───────────────────────────────────────────────────────────── */
function getUsers() {
    try {
        const saved = localStorage.getItem(NA_USERS_KEY);
        if (!saved) return [...USERS_DEFAULT];
        const parsed = JSON.parse(saved);
        return Array.isArray(parsed) && parsed.length ? parsed : [...USERS_DEFAULT];
    } catch (e) {
        console.warn('[NextAgon Auth] Falha ao ler usuários, usando padrão.', e);
        return [...USERS_DEFAULT];
    }
}

function saveUsers(users) {
    try {
        localStorage.setItem(NA_USERS_KEY, JSON.stringify(users));
    } catch (e) {
        console.error('[NextAgon Auth] Falha ao salvar usuários.', e);
    }
}

/* ─────────────────────────────────────────────────────────────
   GERENCIAMENTO DE SESSÃO
   Usa localStorage com TTL para persistir entre abas/recargas.
   ───────────────────────────────────────────────────────────── */
function saveSession(user) {
    const session = {
        user:    { email: user.email, nome: user.nome, role: user.role, avatar: user.avatar },
        perms:   PERMISSIONS[user.role] || {},
        loginAt: Date.now(),
        expAt:   Date.now() + SESSION_TTL_MS,
    };
    try {
        localStorage.setItem(NA_SESSION_KEY, JSON.stringify(session));
        // Mantém compatibilidade com páginas que ainda leem sessionStorage
        sessionStorage.setItem('naUser',  JSON.stringify(session.user));
        sessionStorage.setItem('naPerms', JSON.stringify(session.perms));
    } catch (e) {
        console.error('[NextAgon Auth] Falha ao salvar sessão.', e);
    }
}

function loadSession() {
    try {
        // 1. Tenta localStorage (persistente)
        const raw = localStorage.getItem(NA_SESSION_KEY);
        if (raw) {
            const session = JSON.parse(raw);
            if (session && session.expAt && Date.now() < session.expAt) {
                // Refresca sessionStorage para compatibilidade
                sessionStorage.setItem('naUser',  JSON.stringify(session.user));
                sessionStorage.setItem('naPerms', JSON.stringify(session.perms));
                return session;
            }
            // Sessão expirada
            clearSession();
            return null;
        }
        // 2. Fallback: sessionStorage (aba atual)
        const rawUser  = sessionStorage.getItem('naUser');
        const rawPerms = sessionStorage.getItem('naPerms');
        if (rawUser) {
            return { user: JSON.parse(rawUser), perms: rawPerms ? JSON.parse(rawPerms) : {} };
        }
    } catch (e) {
        console.warn('[NextAgon Auth] Sessão corrompida, limpando.', e);
        clearSession();
    }
    return null;
}

function clearSession() {
    localStorage.removeItem(NA_SESSION_KEY);
    sessionStorage.removeItem('naUser');
    sessionStorage.removeItem('naPerms');
}

/* ─────────────────────────────────────────────────────────────
   API PÚBLICA — funções usadas pelas outras páginas
   ───────────────────────────────────────────────────────────── */

/** Retorna o usuário logado ou null */
function getCurrentUser() {
    const session = loadSession();
    return session ? session.user : null;
}

/** Retorna as permissões do usuário logado ou null */
function getPermissions() {
    const session = loadSession();
    return session ? session.perms : null;
}

/** Verifica se o usuário tem determinada permissão */
function hasPermission(perm) {
    const perms = getPermissions();
    return perms ? !!perms[perm] : false;
}

/**
 * Exige autenticação. Se não logado, redireciona para index.html.
 * Retorna o usuário ou null (nunca cria usuário "fantasma").
 */
function requireAuth() {
    const user = getCurrentUser();
    if (!user) {
        window.location.href = 'index.html';
        return null;
    }
    return user;
}

/** Encerra a sessão e redireciona */
function doLogout() {
    clearSession();
    window.location.href = 'index.html';
}

/* ─────────────────────────────────────────────────────────────
   AÇÕES DO FORMULÁRIO
   ───────────────────────────────────────────────────────────── */

/** Login principal (chamado pelo botão Entrar) */
async function doLogin() {
    const emailEl = document.getElementById('login-email');
    const passEl  = document.getElementById('login-pass');
    if (!emailEl || !passEl) return;

    const email = emailEl.value.trim().toLowerCase();
    const senha = passEl.value;

    if (!email || !senha) {
        showLoginError('Preencha e-mail e senha.');
        return;
    }

    const users = getUsers();
    const user  = users.find(u => u.email === email && u.ativo !== false);

    if (!user || !(await verificarSenha(senha, user.senha))) {
        shakeInputs();
        showLoginError('E-mail ou senha incorretos.');
        return;
    }

    saveSession(user);
    window.location.href = 'dashboard.html';
}

/** Criação de conta */
async function criarConta() {
    const nome     = document.getElementById('c-nome')?.value.trim()     || '';
    const email    = document.getElementById('c-email')?.value.trim().toLowerCase() || '';
    const role     = document.getElementById('c-role')?.value            || 'atleta';
    const senha    = document.getElementById('c-senha')?.value           || '';
    const confirma = document.getElementById('c-confirma')?.value        || '';
    const msg      = document.getElementById('criar-msg');

    if (!nome || !email || !senha)  { setMsg(msg, 'Preencha todos os campos.', 'error'); return; }
    if (!/\S+@\S+\.\S+/.test(email)){ setMsg(msg, 'E-mail inválido.', 'error'); return; }
    if (senha.length < 6)           { setMsg(msg, 'Senha deve ter pelo menos 6 caracteres.', 'error'); return; }
    if (senha !== confirma)         { setMsg(msg, 'As senhas não coincidem.', 'error'); return; }

    const users = getUsers();
    if (users.find(u => u.email === email)) {
        setMsg(msg, 'Este e-mail já está cadastrado.', 'error');
        return;
    }

    const senhaHash = await hashSenha(senha);
    const avatar    = nome.split(' ').slice(0, 2).map(w => w[0]).join('').toUpperCase();
    users.push({ email, senha: senhaHash, role, nome, avatar, ativo: true });
    saveUsers(users);

    setMsg(msg, '✓ Conta criada com sucesso! Redirecionando…', 'success');

    setTimeout(() => {
        const emailLogin = document.getElementById('login-email');
        const passLogin  = document.getElementById('login-pass');
        if (emailLogin) emailLogin.value = email;
        // Não pré-preenche a senha (boas práticas)
        showPanel('login');
    }, 1800);
}

/** Troca de senha */
async function trocarSenha() {
    const email    = document.getElementById('s-email')?.value.trim().toLowerCase()   || '';
    const atual    = document.getElementById('s-atual')?.value                        || '';
    const nova     = document.getElementById('s-nova')?.value                         || '';
    const confirma = document.getElementById('s-confirma')?.value                     || '';
    const msg      = document.getElementById('senha-msg');

    if (!email || !atual || !nova) { setMsg(msg, 'Preencha todos os campos.', 'error'); return; }

    const users = getUsers();
    const user  = users.find(u => u.email === email);

    if (!user || !(await verificarSenha(atual, user.senha))) {
        setMsg(msg, 'E-mail ou senha atual incorretos.', 'error');
        return;
    }
    if (nova.length < 6)   { setMsg(msg, 'Nova senha deve ter pelo menos 6 caracteres.', 'error'); return; }
    if (nova !== confirma) { setMsg(msg, 'As novas senhas não coincidem.', 'error'); return; }

    user.senha = await hashSenha(nova);
    saveUsers(users);

    // Se o usuário logado trocou a própria senha, atualiza a sessão
    const current = getCurrentUser();
    if (current && current.email === email) {
        saveSession(user);
    }

    setMsg(msg, '✓ Senha alterada com sucesso!', 'success');
    setTimeout(() => {
        ['s-email', 's-atual', 's-nova', 's-confirma'].forEach(id => {
            const el = document.getElementById(id);
            if (el) el.value = '';
        });
        showPanel('login');
    }, 1800);
}

/* ─────────────────────────────────────────────────────────────
   UI HELPERS
   ───────────────────────────────────────────────────────────── */
function shakeInputs() {
    document.querySelectorAll('input').forEach(input => {
        input.style.borderColor = '#f87171';
        input.style.animation   = 'shake 0.4s ease';
        setTimeout(() => {
            input.style.borderColor = '';
            input.style.animation   = '';
        }, 800);
    });
}

function showLoginError(msgText) {
    const el = document.getElementById('login-error');
    if (!el) return;
    el.textContent = msgText;
    clearTimeout(el._timer);
    el._timer = setTimeout(() => { el.textContent = ''; }, 3500);
}

function setMsg(el, txt, type) {
    if (!el) return;
    el.textContent = txt;
    el.className   = 'msg ' + type;
    clearTimeout(el._timer);
    el._timer = setTimeout(() => {
        el.textContent = '';
        el.className   = 'msg';
    }, 3500);
}

/* ─────────────────────────────────────────────────────────────
   FORÇA DE SENHA
   ───────────────────────────────────────────────────────────── */
function calcStrength(v) {
    let score = 0;
    if (v.length >= 6)          score++;
    if (/[0-9]/.test(v))        score++;
    if (/[A-Z]/.test(v))        score++;
    if (/[^a-zA-Z0-9]/.test(v)) score++;
    return score;
}

function _applyStrengthUI(score, fillId, labelId) {
    const pct    = [0, 25, 50, 75, 100][score];
    const colors = ['', '#f87171', '#fb923c', '#fbbf24', '#34d399'];
    const texts  = ['—', 'Fraca', 'Razoável', 'Boa', 'Forte'];
    const fill   = document.getElementById(fillId);
    const label  = document.getElementById(labelId);
    if (fill)  { fill.style.width = pct + '%'; fill.style.background = colors[score]; }
    if (label) { label.textContent = texts[score]; label.style.color = colors[score] || 'var(--text3)'; }
}

function checkStrength(v) {
    const score = calcStrength(v);
    _applyStrengthUI(score, 'strength-fill', 'strength-label');
    const toggle = (id, cond) => document.getElementById(id)?.classList.toggle('ok', cond);
    toggle('req-len',     v.length >= 6);
    toggle('req-num',     /[0-9]/.test(v));
    toggle('req-upper',   /[A-Z]/.test(v));
    toggle('req-special', /[^a-zA-Z0-9]/.test(v));
}

function checkStrengthTrocar(v) {
    _applyStrengthUI(calcStrength(v), 'strength-fill-t', 'strength-label-t');
}

/* ─────────────────────────────────────────────────────────────
   PAINEL / ABAS DE LOGIN
   ───────────────────────────────────────────────────────────── */
const HINTS = {
    atleta:       '🏃 <strong>Atleta:</strong> Acessa treinos, esportes, profissionais e seu dashboard pessoal.',
    profissional: '🩺 <strong>Profissional:</strong> Pode criar e gerenciar seu perfil na plataforma.',
    admin:        '🛡️ <strong>Admin:</strong> Acesso total — gerencia usuários, perfis e toda a plataforma.',
};

const PREFILL = {
    atleta:       { email: 'atleta@nextagon.com',       senha: '123456' },
    profissional: { email: 'profissional@nextagon.com', senha: '123456' },
    admin:        { email: 'admin@nextagon.com',        senha: '123456' },
};

function showPanel(name) {
    const order = ['login', 'criar', 'senha'];
    document.querySelectorAll('.panel').forEach(p   => p.classList.remove('active'));
    document.querySelectorAll('.main-tab').forEach(t => t.classList.remove('active'));
    document.getElementById('panel-' + name)?.classList.add('active');
    const idx = order.indexOf(name);
    if (idx >= 0) document.querySelectorAll('.main-tab')[idx]?.classList.add('active');
}

function selectRole(role) {
    document.querySelectorAll('.role-tab').forEach(t => t.classList.remove('active'));
    document.getElementById('tab-' + role)?.classList.add('active');
    const hint = document.getElementById('hint-box');
    if (hint) hint.innerHTML = HINTS[role] || '';
    const prefill = PREFILL[role];
    if (prefill) {
        const emailEl = document.getElementById('login-email');
        const passEl  = document.getElementById('login-pass');
        if (emailEl) emailEl.value = prefill.email;
        if (passEl)  passEl.value  = prefill.senha;
    }
}

/* ─────────────────────────────────────────────────────────────
   INICIALIZAÇÃO
   ───────────────────────────────────────────────────────────── */
document.addEventListener('DOMContentLoaded', () => {
    // Redireciona para painel de senha se veio do perfil
    const redirect = sessionStorage.getItem('na_redirect_panel');
    if (redirect) {
        sessionStorage.removeItem('na_redirect_panel');
        showPanel(redirect);
    }

    // Enter no painel de login executa doLogin
    document.addEventListener('keydown', e => {
        if (e.key !== 'Enter') return;
        const loginAtivo = document.getElementById('panel-login')?.classList.contains('active');
        if (loginAtivo) doLogin();
    });
});