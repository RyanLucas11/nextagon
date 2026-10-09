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
let selectedLoginRole = 'atleta';

function normalizeRole(role) {
    const value = String(role || '').toLowerCase();
    if (value === 'athlete' || value === 'atleta') return 'atleta';
    if (value === 'professional' || value === 'profissional') return 'profissional';
    if (value === 'administrator' || value === 'admin') return 'admin';
    return value;
}

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
   PERMISSÕES POR PAPEL
   ───────────────────────────────────────────────────────────── */
const PERMISSIONS = {
    atleta: {
        verDashboard:            true,
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
        if (!saved) return [];
        const parsed = JSON.parse(saved);
        return Array.isArray(parsed) ? parsed : [];
    } catch (e) {
        console.warn('[NextAgon Auth] Falha ao ler usuários.', e);
        return [];
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
    try {
        const profiles = JSON.parse(localStorage.getItem('na_onboarding_profiles') || '{}');
        const profile = profiles[user.id] || profiles[user.email];
        if (profile?.nickname) user = { ...user, nome: profile.nickname };
    } catch (_) { /* Mantém o login disponível se os dados locais estiverem inválidos. */ }
    const role = normalizeRole(user.role);
    const session = {
        user:    { id: user.id, email: user.email, nome: user.nome || user.name, role, avatar: user.avatar || (user.nome || user.name || '').split(' ').map(word => word[0]).join('').slice(0, 2).toUpperCase() },
        perms:   PERMISSIONS[role] || {},
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
    clearAuthTokens();
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
    if (loginInFlight) return;
    const emailEl = document.getElementById('login-email');
    const passEl  = document.getElementById('login-pass');
    if (!emailEl || !passEl) return;

    const email = emailEl.value.trim().toLowerCase();
    const senha = passEl.value;

    if (!email || !senha) {
        showLoginError('Preencha e-mail e senha.');
        return;
    }

    loginInFlight = true;
    setLoginLoading(true);
    try {
        const auth = await NextagonApi.login(email, senha);
        const authenticatedRole = normalizeRole(auth.user?.role);
        if (authenticatedRole !== selectedLoginRole) {
            showLoginError(`Esta conta é ${authenticatedRole === 'profissional' ? 'Profissional' : authenticatedRole === 'admin' ? 'Admin' : 'Atleta'}. Selecione esse perfil para entrar.`);
            return;
        }
        setAuthTokens(auth);
        saveSession(auth.user);
        window.location.href = 'dashboard.html';
    } catch (error) {
        shakeInputs();
        showLoginError(error instanceof Error ? error.message : 'E-mail ou senha incorretos.');
    } finally {
        loginInFlight = false;
        setLoginLoading(false);
    }
}

let loginInFlight = false;

function setLoginLoading(loading) {
    const button = document.getElementById('login-submit');
    if (!button) return;
    button.disabled = loading;
    button.setAttribute('aria-busy', String(loading));
    button.textContent = loading ? 'Conectando…' : 'Entrar';
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
    if (senha.length < 8)           { setMsg(msg, 'Senha deve ter pelo menos 8 caracteres.', 'error'); return; }
    if (senha !== confirma)         { setMsg(msg, 'As senhas não coincidem.', 'error'); return; }

    try {
        const auth = await NextagonApi.register(nome, email, senha, role);
        setAuthTokens(auth);
        saveSession(auth.user);
        setMsg(msg, '✓ Conta criada com sucesso!', 'success');
        startProfileOnboarding(auth.user);
    } catch (error) {
        setMsg(msg, error instanceof Error ? error.message : 'Não foi possível criar a conta.', 'error');
    }
}

let onboardingStep = 1;
let onboardingUser = null;
let onboardingProfile = {};
const PROFESSIONAL_SPECIALTIES = ['Personal trainer', 'Nutrição esportiva', 'Fisioterapia', 'Preparação física', 'Psicologia do esporte', 'Educação física', 'Medicina esportiva', 'Outro'];

function startProfileOnboarding(user) {
    onboardingUser = user;
    onboardingStep = 1;
    onboardingProfile = { specialties: [] };
    const screen = document.getElementById('profile-onboarding');
    if (!screen) { window.location.href = 'dashboard.html'; return; }
    screen.hidden = false;
    renderOnboardingStep();
}

function renderOnboardingStep() {
    const role = normalizeRole(onboardingUser?.role);
    const professional = role === 'profissional';
    const content = document.getElementById('onboarding-content');
    const step = document.getElementById('onboarding-step');
    const progress = document.getElementById('onboarding-progress-fill');
    if (!content || !step || !progress) return;
    step.textContent = `PASSO ${onboardingStep} DE 2`;
    progress.style.width = onboardingStep === 1 ? '50%' : '100%';

    if (onboardingStep === 1 && !professional) {
        content.innerHTML = `
          <h1 class="onboarding-title" id="onboarding-title">Vamos personalizar sua jornada</h1>
          <p class="onboarding-description">Conte o que você pratica e como podemos ajudar. Você pode editar essas informações depois.</p>
          <label class="onboarding-label" for="onboarding-sport">Qual modalidade você pratica e quer ter assistência?</label>
          <input class="onboarding-input" id="onboarding-sport" placeholder="Ex.: musculação, corrida, natação" value="${escapeOnboarding(onboardingProfile.sport || '')}">
          <label class="onboarding-label" for="onboarding-focus">Como é o seu foco no treino?</label>
          <textarea class="onboarding-textarea" id="onboarding-focus" placeholder="Ex.: ganhar força, melhorar a técnica ou manter a constância">${escapeOnboarding(onboardingProfile.focus || '')}</textarea>
          <label class="onboarding-label" for="onboarding-goal">Qual é o seu principal objetivo?</label>
          <input class="onboarding-input" id="onboarding-goal" placeholder="Ex.: ganhar massa muscular, melhorar o condicionamento" value="${escapeOnboarding(onboardingProfile.goal || '')}">
          ${onboardingActions(false)}`;
    } else if (onboardingStep === 1) {
        content.innerHTML = `
          <h1 class="onboarding-title" id="onboarding-title">Apresente seu trabalho</h1>
          <p class="onboarding-description">Selecione suas especialidades e conte um pouco sobre o serviço que oferece e sua trajetória.</p>
          <span class="onboarding-label">Quais são suas especialidades?</span>
          <div class="specialty-options">${PROFESSIONAL_SPECIALTIES.map(item => `<button type="button" class="specialty-option${(onboardingProfile.specialties || []).includes(item) ? ' selected' : ''}" aria-pressed="${(onboardingProfile.specialties || []).includes(item)}" onclick="toggleOnboardingSpecialty(this)">${item}</button>`).join('')}</div>
          <label class="onboarding-label" for="onboarding-work">Descreva seu trabalho</label>
          <textarea class="onboarding-textarea" id="onboarding-work" placeholder="Como você ajuda seus clientes?">${escapeOnboarding(onboardingProfile.workDescription || '')}</textarea>
          <label class="onboarding-label" for="onboarding-experience">Conte sobre sua experiência</label>
          <textarea class="onboarding-textarea" id="onboarding-experience" placeholder="Tempo de carreira, formações e experiências relevantes">${escapeOnboarding(onboardingProfile.experience || '')}</textarea>
          ${onboardingActions(false)}`;
    } else {
        const initials = (onboardingUser?.nome || onboardingUser?.name || 'NA').split(/\s+/).map(part => part[0]).join('').slice(0, 2).toUpperCase();
        content.innerHTML = `
          <h1 class="onboarding-title" id="onboarding-title">Deixe seu perfil com a sua cara</h1>
          <p class="onboarding-description">Escolha como quer ser chamado e, se quiser, adicione uma foto ao seu perfil.</p>
          <label class="onboarding-label" for="onboarding-nickname">Como você quer ser chamado?</label>
          <input class="onboarding-input" id="onboarding-nickname" placeholder="Seu nome ou apelido" value="${escapeOnboarding(onboardingProfile.nickname || onboardingUser?.nome || onboardingUser?.name || '')}">
          <div class="profile-photo-row"><div class="profile-photo-preview" id="onboarding-photo-preview">${onboardingProfile.photo ? `<img src="${onboardingProfile.photo}" alt="Prévia da foto de perfil">` : initials}</div><div><label class="photo-picker-label" for="onboarding-photo">${onboardingProfile.photo ? 'Trocar foto' : 'Adicionar foto'}</label><input id="onboarding-photo" type="file" accept="image/*" hidden onchange="previewOnboardingPhoto(event)"></div></div>
          ${onboardingActions(true)}`;
    }
}

function onboardingActions(isFinalStep) {
    return `<div class="onboarding-actions">${onboardingStep === 2 ? '<button type="button" class="onboarding-back" onclick="goBackOnboarding()">Voltar</button>' : ''}<button type="button" class="btn-main" onclick="${isFinalStep ? 'finishProfileOnboarding()' : 'advanceOnboarding()'}">${isFinalStep ? 'Concluir' : 'Continuar'}</button></div>`;
}

function escapeOnboarding(value) {
    return String(value || '').replace(/[&<>"']/g, char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[char]);
}

function collectOnboardingStep() {
    const value = id => document.getElementById(id)?.value?.trim() || '';
    if (onboardingStep === 1 && normalizeRole(onboardingUser?.role) === 'profissional') {
        onboardingProfile.workDescription = value('onboarding-work');
        onboardingProfile.experience = value('onboarding-experience');
    } else if (onboardingStep === 1) {
        onboardingProfile.sport = value('onboarding-sport');
        onboardingProfile.focus = value('onboarding-focus');
        onboardingProfile.goal = value('onboarding-goal');
    } else {
        onboardingProfile.nickname = value('onboarding-nickname');
    }
}

function advanceOnboarding() {
    collectOnboardingStep();
    onboardingStep = 2;
    renderOnboardingStep();
}

function skipOnboardingStep() {
    collectOnboardingStep();
    if (onboardingStep === 1) {
        onboardingStep = 2;
        renderOnboardingStep();
    } else {
        finishProfileOnboarding();
    }
}

function goBackOnboarding() {
    collectOnboardingStep();
    onboardingStep = 1;
    renderOnboardingStep();
}

function toggleOnboardingSpecialty(button) {
    const specialty = button.textContent.trim();
    const selected = new Set(onboardingProfile.specialties || []);
    if (selected.has(specialty)) selected.delete(specialty); else selected.add(specialty);
    onboardingProfile.specialties = Array.from(selected);
    button.classList.toggle('selected', selected.has(specialty));
    button.setAttribute('aria-pressed', String(selected.has(specialty)));
}

function previewOnboardingPhoto(event) {
    const file = event.target.files?.[0];
    if (!file) return;
    if (!file.type.startsWith('image/')) return;
    const reader = new FileReader();
    reader.onload = () => {
        onboardingProfile.photo = String(reader.result || '');
        const preview = document.getElementById('onboarding-photo-preview');
        if (preview) preview.innerHTML = `<img src="${onboardingProfile.photo}" alt="Prévia da foto de perfil">`;
    };
    reader.readAsDataURL(file);
}

function finishProfileOnboarding() {
    collectOnboardingStep();
    try {
        const id = onboardingUser?.id || onboardingUser?.email || 'new-user';
        const key = 'na_onboarding_profiles';
        const saved = JSON.parse(localStorage.getItem(key) || '{}');
        saved[id] = { ...onboardingProfile, role: normalizeRole(onboardingUser?.role), completedAt: new Date().toISOString() };
        localStorage.setItem(key, JSON.stringify(saved));
        if (onboardingProfile.photo) localStorage.setItem('na_avatar_img', onboardingProfile.photo);
        const nickname = onboardingProfile.nickname;
        if (nickname && onboardingUser) saveSession({ ...onboardingUser, nome: nickname });
    } catch (error) {
        console.warn('[NextAgon Onboarding] Não foi possível salvar todos os dados do perfil.', error);
    }
    window.location.href = 'dashboard.html';
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
    atleta:       '🏃 <strong>Atleta:</strong> Acessa a rede fitness, a comunidade de treinos e profissionais.',
    profissional: '🩺 <strong>Profissional:</strong> Pode criar e gerenciar seu perfil na plataforma.',
    admin:        '🛡️ <strong>Admin:</strong> Acesso total — gerencia usuários, perfis e toda a plataforma.',
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
    if (!Object.prototype.hasOwnProperty.call(PERMISSIONS, role)) return;
    selectedLoginRole = role;
    document.querySelectorAll('.role-tab').forEach(t => t.classList.remove('active'));
    document.getElementById('tab-' + role)?.classList.add('active');
    const hint = document.getElementById('hint-box');
    if (hint) hint.innerHTML = HINTS[role] || '';
}

/* ─────────────────────────────────────────────────────────────
   INICIALIZAÇÃO
   ───────────────────────────────────────────────────────────── */
document.addEventListener('DOMContentLoaded', () => {
    // Inicia o backend em segundo plano enquanto a pessoa preenche os dados.
    NextagonApi.warmup();

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
