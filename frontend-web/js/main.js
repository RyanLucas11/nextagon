/* =============================================================
   main.js — Next Agon · Funções Compartilhadas
   ============================================================= */

function initProfile() {
    const user = requireAuth();
    if (!user) return;

    const roleLabel = { atleta: 'Atleta', profissional: 'Profissional', admin: 'Administrador' };
    const roleIcon  = { atleta: 'fa-person-running', profissional: 'fa-user-doctor', admin: 'fa-shield-halved' };
    const roleColor = { atleta: '', profissional: '', admin: '#a78bfa' };

    function setAvatar(el) {
        if (!el) return;
        const saved = localStorage.getItem('na_avatar_img');
        if (saved) {
            el.innerHTML = `<img src="${saved}" alt="avatar">`;
        } else {
            el.textContent = user.avatar || user.nome[0] || '?';
            if (user.role === 'admin') {
                el.style.background = 'linear-gradient(135deg, #a78bfa, #7c3aed)';
                el.style.color = '#fff';
            }
        }
    }

    const sideAvatar = document.getElementById('sidebar-avatar');
    const sideNome   = document.getElementById('sidebar-nome');
    const sideRole   = document.getElementById('sidebar-role');
    setAvatar(sideAvatar);
    if (sideNome) sideNome.textContent = user.nome;
    if (sideRole) {
        sideRole.innerHTML = `<i class="fa-solid ${roleIcon[user.role] || 'fa-user'}"></i> ${roleLabel[user.role] || user.role}`;
        if (roleColor[user.role]) sideRole.style.color = roleColor[user.role];
    }

    const ppAvatar = document.getElementById('pp-avatar');
    const ppNome   = document.getElementById('pp-nome');
    const ppRole   = document.getElementById('pp-role');
    const ppEmail  = document.getElementById('pp-email');
    setAvatar(ppAvatar);
    if (ppNome)  ppNome.textContent = user.nome;
    if (ppRole) {
        ppRole.innerHTML = `<i class="fa-solid ${roleIcon[user.role] || 'fa-user'}"></i> ${roleLabel[user.role] || user.role}`;
        if (roleColor[user.role]) ppRole.style.color = roleColor[user.role];
    }
    if (ppEmail) ppEmail.textContent = user.email;

    const userNomeEl = document.getElementById('user-nome');
    if (userNomeEl) userNomeEl.textContent = user.nome.split(' ')[0];

    const dataHoje = document.getElementById('data-hoje');
    if (dataHoje) {
        dataHoje.textContent = new Date().toLocaleDateString('pt-BR', {
            weekday: 'long', day: 'numeric', month: 'long', year: 'numeric'
        });
    }

    const adminSec = document.getElementById('admin-section');
    if (adminSec) adminSec.style.display = user.role === 'admin' ? 'block' : 'none';

    const temaSalvo = localStorage.getItem('na_tema') || 'dark';
    applyTema(temaSalvo, false);

    /* Atualiza subtítulo do idioma */
    _updateIdiomaSubLabel();
    /* Atualiza ícone de tema */
    _updateTemaIcon();
}

function openPanel() {
    const panel   = document.getElementById('profile-panel');
    const overlay = document.getElementById('panel-overlay');
    if (panel)   panel.classList.add('pp-open');
    if (overlay) { overlay.classList.add('ov-open'); overlay.onclick = closePanel; }
    document.body.style.overflow = 'hidden';
}

function closePanel() {
    const panel   = document.getElementById('profile-panel');
    const overlay = document.getElementById('panel-overlay');
    if (panel)   panel.classList.remove('pp-open');
    if (overlay) overlay.classList.remove('ov-open');
    document.body.style.overflow = '';
    _closeAllModals();
}

const PAGE_MAP = {
    dashboard:     'dashboard.html',
    treinos:       'treinos.html',
    academia:      'academia.html',
    profissionais: 'profissionais.html',
    chat:          'chat.html',
    admin:         'Admin.html',
};

function showPage(name) {
    const dest = PAGE_MAP[name];
    if (dest && !window.location.pathname.endsWith(dest)) {
        window.location.href = dest;
    }
}

/* ══════════════════════════════════════════════════════════
   MODAL ENGINE — base para todos os modais de perfil
   ══════════════════════════════════════════════════════════ */

function _injectModalStyles() {
    if (document.getElementById('na-modal-styles')) return;
    const s = document.createElement('style');
    s.id = 'na-modal-styles';
    s.textContent = `
        .na-modal-overlay {
            position: fixed; inset: 0;
            background: rgba(0,0,0,0.65);
            backdrop-filter: blur(4px);
            z-index: 2000;
            display: flex; align-items: center; justify-content: center;
            padding: 20px;
            animation: naFadeIn .2s ease;
        }
        @keyframes naFadeIn { from { opacity:0 } to { opacity:1 } }
        @keyframes naSlideUp { from { opacity:0; transform:translateY(20px) } to { opacity:1; transform:translateY(0) } }

        .na-modal {
            background: var(--card);
            border: 1px solid var(--border);
            border-radius: 20px;
            width: 100%; max-width: 420px;
            max-height: 90vh;
            overflow-y: auto;
            box-shadow: 0 24px 64px rgba(0,0,0,.5);
            animation: naSlideUp .25s cubic-bezier(0.4,0,0.2,1);
        }
        .na-modal-header {
            display: flex; align-items: center; justify-content: space-between;
            padding: 20px 20px 0;
            margin-bottom: 16px;
        }
        .na-modal-title {
            font-size: 17px; font-weight: 700; color: var(--text);
            display: flex; align-items: center; gap: 10px;
        }
        .na-modal-title i {
            width: 32px; height: 32px; border-radius: 9px;
            display: flex; align-items: center; justify-content: center;
            font-size: 15px;
        }
        .na-modal-close {
            background: var(--card2); border: 1px solid var(--border);
            color: var(--text2); width: 32px; height: 32px; border-radius: 50%;
            font-size: 18px; cursor: pointer; display: flex; align-items: center;
            justify-content: center; transition: all .2s; flex-shrink: 0;
        }
        .na-modal-close:hover { background: var(--card); color: var(--text); }
        .na-modal-body { padding: 0 20px 20px; }

        /* ── Foto Modal ── */
        .foto-drop-zone {
            border: 2px dashed var(--border);
            border-radius: 14px;
            padding: 28px 20px;
            text-align: center;
            cursor: pointer;
            transition: all .2s;
            background: var(--card2);
            margin-bottom: 16px;
        }
        .foto-drop-zone:hover, .foto-drop-zone.drag-over {
            border-color: var(--accent);
            background: var(--accent-dim);
        }
        .foto-drop-zone i { font-size: 32px; color: var(--text3); margin-bottom: 10px; display: block; }
        .foto-drop-zone p { font-size: 14px; color: var(--text2); margin-bottom: 4px; }
        .foto-drop-zone small { font-size: 11px; color: var(--text3); }
        .foto-preview-wrap {
            display: flex; align-items: center; gap: 16px;
            background: var(--card2); border: 1px solid var(--border);
            border-radius: 14px; padding: 16px; margin-bottom: 16px;
        }
        .foto-preview-img {
            width: 72px; height: 72px; border-radius: 50%;
            object-fit: cover; border: 3px solid var(--accent);
            flex-shrink: 0;
        }
        .foto-preview-txt { flex: 1; }
        .foto-preview-txt strong { display: block; font-size: 14px; color: var(--text); margin-bottom: 4px; }
        .foto-preview-txt span { font-size: 12px; color: var(--text3); }
        .foto-remove-btn {
            background: rgba(239,68,68,.1); border: 1px solid rgba(239,68,68,.2);
            color: var(--red); border-radius: 8px; padding: 6px 12px;
            font-size: 12px; font-weight: 600; cursor: pointer; transition: all .2s;
            font-family: inherit;
        }
        .foto-remove-btn:hover { background: rgba(239,68,68,.2); }
        .foto-initials-row {
            display: flex; gap: 10px; margin-bottom: 16px;
        }
        .foto-initials-row input {
            flex: 1; background: var(--card2); border: 1px solid var(--border);
            color: var(--text); padding: 10px 14px; border-radius: 10px;
            font-size: 14px; font-family: inherit;
            transition: border-color .2s;
        }
        .foto-initials-row input:focus { outline: none; border-color: var(--accent); }
        .foto-initials-row button {
            background: var(--accent-dim); border: 1px solid rgba(26,107,222,.3);
            color: var(--accent); padding: 10px 16px; border-radius: 10px;
            font-size: 13px; font-weight: 600; cursor: pointer; transition: all .2s;
            font-family: inherit; white-space: nowrap;
        }
        .foto-initials-row button:hover { background: var(--accent); color: #fff; }

        /* ── Idioma Modal ── */
        .idioma-grid {
            display: grid; grid-template-columns: 1fr 1fr;
            gap: 10px; margin-bottom: 16px;
        }
        .idioma-opt {
            background: var(--card2); border: 2px solid var(--border);
            border-radius: 12px; padding: 14px 12px;
            cursor: pointer; transition: all .18s;
            display: flex; align-items: center; gap: 10px;
        }
        .idioma-opt:hover { border-color: var(--accent); background: var(--accent-dim); }
        .idioma-opt.selected { border-color: var(--accent); background: var(--accent-dim); }
        .idioma-flag { font-size: 22px; flex-shrink: 0; }
        .idioma-info strong { display: block; font-size: 13px; color: var(--text); font-weight: 600; }
        .idioma-info span { font-size: 11px; color: var(--text3); }
        .idioma-check {
            width: 18px; height: 18px; border-radius: 50%;
            border: 2px solid var(--border); margin-left: auto; flex-shrink: 0;
            transition: all .18s; background: transparent;
        }
        .idioma-opt.selected .idioma-check {
            background: var(--accent); border-color: var(--accent);
            box-shadow: inset 0 0 0 3px var(--card2);
        }
        .idioma-note {
            background: var(--card2); border: 1px solid var(--border);
            border-radius: 10px; padding: 12px 14px;
            font-size: 12px; color: var(--text3);
            display: flex; gap: 8px; align-items: flex-start;
            margin-bottom: 16px;
        }
        .idioma-note i { color: var(--accent); margin-top: 1px; flex-shrink: 0; }

        /* ── Tema Modal ── */
        .tema-options {
            display: flex; gap: 12px; margin-bottom: 20px;
        }
        .tema-card {
            flex: 1; border: 2px solid var(--border); border-radius: 14px;
            overflow: hidden; cursor: pointer; transition: all .2s;
        }
        .tema-card:hover { border-color: var(--accent); transform: translateY(-2px); }
        .tema-card.selected { border-color: var(--accent); box-shadow: 0 0 0 3px var(--accent-glow); }
        .tema-preview {
            height: 80px; display: flex; align-items: flex-end; padding: 8px;
            position: relative; overflow: hidden;
        }
        .tema-preview-dark { background: linear-gradient(160deg, #0a1120 0%, #111927 100%); }
        .tema-preview-light { background: linear-gradient(160deg, #f0f4f8 0%, #ffffff 100%); }
        .tema-preview-bar {
            height: 20px; border-radius: 6px; width: 60%;
            background: rgba(26,107,222,.4);
        }
        .tema-preview-dot {
            position: absolute; top: 10px; right: 10px;
            width: 24px; height: 24px; border-radius: 50%;
        }
        .tema-preview-dark .tema-preview-dot { background: #1a6bde; }
        .tema-preview-light .tema-preview-dot { background: #1a6bde; }
        .tema-card-label {
            padding: 10px 12px;
            background: var(--card2);
            display: flex; align-items: center; justify-content: space-between;
        }
        .tema-card-label strong { font-size: 13px; color: var(--text); }
        .tema-card-label i { font-size: 14px; color: var(--text3); }
        .tema-card.selected .tema-card-label i { color: var(--accent); }
        .tema-auto-toggle {
            background: var(--card2); border: 1px solid var(--border);
            border-radius: 12px; padding: 14px 16px;
            display: flex; align-items: center; gap: 14px;
            margin-bottom: 16px;
        }
        .tema-auto-toggle-info { flex: 1; }
        .tema-auto-toggle-info strong { display: block; font-size: 14px; color: var(--text); margin-bottom: 2px; }
        .tema-auto-toggle-info span { font-size: 12px; color: var(--text3); }

        /* Toggle Switch */
        .na-toggle {
            position: relative; width: 44px; height: 24px; flex-shrink: 0;
        }
        .na-toggle input { opacity: 0; width: 0; height: 0; }
        .na-toggle-slider {
            position: absolute; inset: 0; border-radius: 24px;
            background: var(--border); cursor: pointer; transition: .25s;
        }
        .na-toggle-slider:before {
            content: ''; position: absolute;
            height: 18px; width: 18px; left: 3px; bottom: 3px;
            border-radius: 50%; background: #fff;
            transition: .25s; box-shadow: 0 1px 4px rgba(0,0,0,.3);
        }
        .na-toggle input:checked + .na-toggle-slider { background: var(--accent); }
        .na-toggle input:checked + .na-toggle-slider:before { transform: translateX(20px); }

        /* ── Notificações Modal ── */
        .notif-section-label {
            font-size: 10px; letter-spacing: .1em; text-transform: uppercase;
            color: var(--text3); font-weight: 700; margin: 16px 0 8px;
        }
        .notif-row {
            background: var(--card2); border: 1px solid var(--border);
            border-radius: 12px; padding: 14px 16px;
            display: flex; align-items: center; gap: 14px;
            margin-bottom: 8px; transition: border-color .18s;
        }
        .notif-row:hover { border-color: rgba(26,107,222,.3); }
        .notif-icon {
            width: 36px; height: 36px; border-radius: 10px;
            display: flex; align-items: center; justify-content: center;
            font-size: 16px; flex-shrink: 0;
        }
        .notif-row-info { flex: 1; }
        .notif-row-info strong { display: block; font-size: 13px; color: var(--text); font-weight: 600; margin-bottom: 2px; }
        .notif-row-info span { font-size: 11px; color: var(--text3); }
        .notif-master {
            background: var(--accent-dim); border: 1px solid rgba(26,107,222,.25);
            border-radius: 12px; padding: 14px 16px;
            display: flex; align-items: center; gap: 14px;
            margin-bottom: 16px;
        }
        .notif-master-info { flex: 1; }
        .notif-master-info strong { display: block; font-size: 14px; color: var(--text); margin-bottom: 2px; font-weight: 700; }
        .notif-master-info span { font-size: 12px; color: var(--text2); }

        /* Botão salvar / confirmar */
        .na-modal-save-btn {
            width: 100%; background: var(--accent);
            border: none; color: #fff;
            padding: 13px; border-radius: 12px;
            font-size: 14px; font-weight: 700; cursor: pointer;
            display: flex; align-items: center; justify-content: center; gap: 8px;
            transition: opacity .2s, transform .15s;
            font-family: inherit; margin-top: 4px;
        }
        .na-modal-save-btn:hover { opacity: .9; transform: translateY(-1px); }
        .na-modal-save-btn:active { transform: translateY(0); }
    `;
    document.head.appendChild(s);
}

function _closeAllModals() {
    document.querySelectorAll('.na-modal-overlay').forEach(m => m.remove());
}

function _createModal(titleHtml, bodyHtml) {
    _injectModalStyles();
    const overlay = document.createElement('div');
    overlay.className = 'na-modal-overlay';
    overlay.innerHTML = `
        <div class="na-modal" role="dialog" aria-modal="true">
            <div class="na-modal-header">
                <div class="na-modal-title">${titleHtml}</div>
                <button class="na-modal-close" onclick="this.closest('.na-modal-overlay').remove()" aria-label="Fechar">×</button>
            </div>
            <div class="na-modal-body">${bodyHtml}</div>
        </div>
    `;
    overlay.addEventListener('click', e => { if (e.target === overlay) overlay.remove(); });
    document.body.appendChild(overlay);
    return overlay;
}

/* ══════════════════════════════════════════════════════════
   ALTERAR FOTO DE PERFIL — upload real + fallback iniciais
   ══════════════════════════════════════════════════════════ */
function optFoto() {
    const user = getCurrentUser();
    if (!user) return;

    const savedImg = localStorage.getItem('na_avatar_img');
    const initials = user.avatar || user.nome.split(' ').slice(0,2).map(w=>w[0]).join('').toUpperCase();

    const body = `
        ${savedImg ? `
        <div class="foto-preview-wrap">
            <img class="foto-preview-img" src="${savedImg}" id="fotoCurrentImg" alt="Foto atual">
            <div class="foto-preview-txt">
                <strong>Foto atual</strong>
                <span>Clique em remover para usar iniciais</span>
            </div>
            <button class="foto-remove-btn" id="fotoBtnRemove"><i class="fa-solid fa-trash"></i> Remover</button>
        </div>` : ''}
        <div class="foto-drop-zone" id="fotoDropZone">
            <i class="fa-solid fa-cloud-arrow-up"></i>
            <p>Arraste uma foto ou clique para selecionar</p>
            <small>JPG, PNG ou GIF — máx. 5 MB</small>
        </div>
        <input type="file" id="fotoFileInput" accept="image/*" style="display:none">
        <div id="fotoNewPreview" style="display:none" class="foto-preview-wrap">
            <img class="foto-preview-img" id="fotoNewImg" alt="Nova foto">
            <div class="foto-preview-txt">
                <strong>Nova foto selecionada</strong>
                <span id="fotoNewName">—</span>
            </div>
        </div>
        <div style="display:flex;align-items:center;gap:10px;margin:12px 0 8px">
            <div style="flex:1;height:1px;background:var(--border)"></div>
            <span style="font-size:11px;color:var(--text3);white-space:nowrap">ou use iniciais</span>
            <div style="flex:1;height:1px;background:var(--border)"></div>
        </div>
        <div class="foto-initials-row">
            <input type="text" id="fotoInitialsInput" maxlength="2" placeholder="Ex: JR" value="${initials}">
            <button id="fotoBtnInitials"><i class="fa-solid fa-text-width"></i> Usar Iniciais</button>
        </div>
        <button class="na-modal-save-btn" id="fotoBtnSave"><i class="fa-solid fa-check"></i> Salvar Foto</button>
    `;

    const modal = _createModal(
        `<i style="background:var(--accent-dim);color:var(--accent)" class="fa-solid fa-camera"></i> Alterar Foto de Perfil`,
        body
    );

    let selectedFile = null;

    const dropZone = modal.querySelector('#fotoDropZone');
    const fileInput = modal.querySelector('#fotoFileInput');
    const newPreview = modal.querySelector('#fotoNewPreview');
    const newImg = modal.querySelector('#fotoNewImg');
    const newName = modal.querySelector('#fotoNewName');

    dropZone.addEventListener('click', () => fileInput.click());
    dropZone.addEventListener('dragover', e => { e.preventDefault(); dropZone.classList.add('drag-over'); });
    dropZone.addEventListener('dragleave', () => dropZone.classList.remove('drag-over'));
    dropZone.addEventListener('drop', e => {
        e.preventDefault(); dropZone.classList.remove('drag-over');
        const f = e.dataTransfer.files[0];
        if (f) _loadFotoFile(f);
    });
    fileInput.addEventListener('change', e => { if (e.target.files[0]) _loadFotoFile(e.target.files[0]); });

    function _loadFotoFile(f) {
        if (f.size > 5 * 1024 * 1024) { showToast('Arquivo muito grande (máx. 5 MB)', 'err'); return; }
        selectedFile = f;
        const reader = new FileReader();
        reader.onload = ev => {
            newImg.src = ev.target.result;
            newName.textContent = f.name;
            newPreview.style.display = 'flex';
        };
        reader.readAsDataURL(f);
    }

    const removBtn = modal.querySelector('#fotoBtnRemove');
    if (removBtn) {
        removBtn.addEventListener('click', () => {
            localStorage.removeItem('na_avatar_img');
            selectedFile = null;
            const user2 = getCurrentUser();
            const init2 = user2 ? (user2.avatar || user2.nome[0] || '?') : '?';
            _applyAvatarToAll(null, init2);
            showToast('Foto removida!');
            modal.remove();
        });
    }

    modal.querySelector('#fotoBtnInitials').addEventListener('click', () => {
        const val = modal.querySelector('#fotoInitialsInput').value.trim().toUpperCase().slice(0,2);
        if (!val) { showToast('Digite pelo menos 1 letra', 'warn'); return; }
        const u = getCurrentUser();
        u.avatar = val;
        sessionStorage.setItem('naUser', JSON.stringify(u));
        localStorage.removeItem('na_avatar_img');
        selectedFile = null;
        _applyAvatarToAll(null, val);
        showToast('Iniciais atualizadas: ' + val);
        modal.remove();
    });

    modal.querySelector('#fotoBtnSave').addEventListener('click', () => {
        if (!selectedFile && !newImg.src.startsWith('data:')) {
            showToast('Selecione uma foto primeiro', 'warn'); return;
        }
        if (selectedFile) {
            const reader = new FileReader();
            reader.onload = ev => {
                const dataUrl = ev.target.result;
                localStorage.setItem('na_avatar_img', dataUrl);
                _applyAvatarToAll(dataUrl, null);
                showToast('Foto de perfil atualizada!');
                modal.remove();
            };
            reader.readAsDataURL(selectedFile);
        }
    });
}

function _applyAvatarToAll(imgSrc, text) {
    ['sidebar-avatar', 'pp-avatar'].forEach(id => {
        const el = document.getElementById(id);
        if (!el) return;
        if (imgSrc) {
            el.innerHTML = `<img src="${imgSrc}" alt="avatar" style="width:100%;height:100%;object-fit:cover;border-radius:50%">`;
        } else {
            el.innerHTML = '';
            el.textContent = text || '?';
        }
    });
    if (window._syncMobAvatar) window._syncMobAvatar();
}

/* ══════════════════════════════════════════════════════════
   ALTERAR IDIOMA — seletor completo
   ══════════════════════════════════════════════════════════ */
const IDIOMAS = [
    { code: 'pt-BR', flag: '🇧🇷', nome: 'Português', regiao: 'Brasil' },
    { code: 'en-US', flag: '🇺🇸', nome: 'English', regiao: 'United States' },
    { code: 'es-ES', flag: '🇪🇸', nome: 'Español', regiao: 'España' },
    { code: 'fr-FR', flag: '🇫🇷', nome: 'Français', regiao: 'France' },
    { code: 'de-DE', flag: '🇩🇪', nome: 'Deutsch', regiao: 'Deutschland' },
    { code: 'it-IT', flag: '🇮🇹', nome: 'Italiano', regiao: 'Italia' },
];

function _updateIdiomaSubLabel() {
    const saved = localStorage.getItem('na_idioma') || 'pt-BR';
    const lang = IDIOMAS.find(l => l.code === saved) || IDIOMAS[0];
    const sub = document.querySelector('[onclick="optIdioma()"] .pp-opt-sub');
    if (sub) sub.textContent = lang.flag + ' ' + lang.nome + ' (' + lang.regiao + ')';
}

function optIdioma() {
    const saved = localStorage.getItem('na_idioma') || 'pt-BR';
    let selected = saved;

    const optsHtml = IDIOMAS.map(l => `
        <div class="idioma-opt ${l.code === saved ? 'selected' : ''}" data-code="${l.code}">
            <span class="idioma-flag">${l.flag}</span>
            <div class="idioma-info">
                <strong>${l.nome}</strong>
                <span>${l.regiao}</span>
            </div>
            <div class="idioma-check"></div>
        </div>
    `).join('');

    const body = `
        <div class="idioma-grid">${optsHtml}</div>
        <div class="idioma-note">
            <i class="fa-solid fa-circle-info"></i>
            <span>O idioma afeta datas, formatos numéricos e a interface. Alguns textos podem continuar em português.</span>
        </div>
        <button class="na-modal-save-btn" id="idiomaBtnSave"><i class="fa-solid fa-globe"></i> Confirmar Idioma</button>
    `;

    const modal = _createModal(
        `<i style="background:rgba(52,211,153,.12);color:#34d399" class="fa-solid fa-globe"></i> Alterar Idioma`,
        body
    );

    modal.querySelectorAll('.idioma-opt').forEach(opt => {
        opt.addEventListener('click', () => {
            modal.querySelectorAll('.idioma-opt').forEach(o => o.classList.remove('selected'));
            opt.classList.add('selected');
            selected = opt.dataset.code;
        });
    });

    modal.querySelector('#idiomaBtnSave').addEventListener('click', () => {
        localStorage.setItem('na_idioma', selected);
        const lang = IDIOMAS.find(l => l.code === selected) || IDIOMAS[0];
        _updateIdiomaSubLabel();
        showToast('Idioma alterado: ' + lang.flag + ' ' + lang.nome);
        modal.remove();
    });
}

/* ══════════════════════════════════════════════════════════
   ALTERAR TEMA — visual picker
   ══════════════════════════════════════════════════════════ */
function _updateTemaIcon() {
    const tema = localStorage.getItem('na_tema') || 'dark';
    const icon = document.querySelector('[onclick="optTema()"] .pp-icon i');
    if (icon) {
        icon.className = tema === 'dark' ? 'fa-solid fa-moon' : 'fa-solid fa-sun';
        icon.style.color = tema === 'dark' ? '#94a3b8' : '#fbbf24';
    }
    const sub = document.getElementById('tema-sub');
    if (sub) sub.textContent = tema === 'dark' ? 'Escuro ativo' : 'Claro ativo';
}

function optTema() {
    const atual = localStorage.getItem('na_tema') || 'dark';
    let selected = atual;

    const body = `
        <div class="tema-options">
            <div class="tema-card ${atual === 'dark' ? 'selected' : ''}" data-tema="dark" id="temaCardDark">
                <div class="tema-preview tema-preview-dark">
                    <div class="tema-preview-dot"></div>
                    <div class="tema-preview-bar"></div>
                </div>
                <div class="tema-card-label">
                    <strong>🌙 Escuro</strong>
                    <i class="fa-solid fa-check-circle"></i>
                </div>
            </div>
            <div class="tema-card ${atual === 'light' ? 'selected' : ''}" data-tema="light" id="temaCardLight">
                <div class="tema-preview tema-preview-light">
                    <div class="tema-preview-dot"></div>
                    <div class="tema-preview-bar" style="background:rgba(26,107,222,.3)"></div>
                </div>
                <div class="tema-card-label">
                    <strong>☀️ Claro</strong>
                    <i class="fa-solid fa-check-circle"></i>
                </div>
            </div>
        </div>
        <div class="tema-auto-toggle">
            <div class="tema-auto-toggle-info">
                <strong>Seguir sistema</strong>
                <span>Adapta ao tema do seu dispositivo</span>
            </div>
            <label class="na-toggle">
                <input type="checkbox" id="temaAutoCheck" ${localStorage.getItem('na_tema_auto') === '1' ? 'checked' : ''}>
                <span class="na-toggle-slider"></span>
            </label>
        </div>
        <button class="na-modal-save-btn" id="temaBtnSave"><i class="fa-solid fa-palette"></i> Aplicar Tema</button>
    `;

    const modal = _createModal(
        `<i style="background:rgba(100,116,139,.12);color:#94a3b8" class="fa-solid fa-moon"></i> Alterar Tema`,
        body
    );

    modal.querySelectorAll('.tema-card').forEach(card => {
        card.addEventListener('click', () => {
            modal.querySelectorAll('.tema-card').forEach(c => c.classList.remove('selected'));
            card.classList.add('selected');
            selected = card.dataset.tema;
            modal.querySelector('#temaAutoCheck').checked = false;
            // Preview live
            applyTema(selected, false);
        });
    });

    modal.querySelector('#temaAutoCheck').addEventListener('change', e => {
        if (e.target.checked) {
            const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
            selected = prefersDark ? 'dark' : 'light';
            modal.querySelectorAll('.tema-card').forEach(c => c.classList.remove('selected'));
            modal.querySelector(`[data-tema="${selected}"]`).classList.add('selected');
            applyTema(selected, false);
        }
    });

    modal.querySelector('#temaBtnSave').addEventListener('click', () => {
        const auto = modal.querySelector('#temaAutoCheck').checked;
        localStorage.setItem('na_tema_auto', auto ? '1' : '0');
        applyTema(selected, true);
        _updateTemaIcon();
        showToast('Tema ' + (selected === 'dark' ? '🌙 escuro' : '☀️ claro') + ' aplicado!');
        modal.remove();
    });
}

function applyTema(tema, salvar) {
    if (salvar) localStorage.setItem('na_tema', tema);
    document.documentElement.setAttribute('data-tema', tema);
    const sub = document.getElementById('tema-sub');
    if (sub) sub.textContent = tema === 'dark' ? 'Escuro ativo' : 'Claro ativo';
    if (salvar) showToast('Tema ' + (tema === 'dark' ? 'escuro' : 'claro') + ' ativado.');
}

/* ══════════════════════════════════════════════════════════
   NOTIFICAÇÕES — painel completo de toggles
   ══════════════════════════════════════════════════════════ */
const NOTIF_DEFAULTS = {
    master: true,
    chat_msg: true,
    chat_novo: true,
    treino_lembrete: true,
    treino_concluido: false,
    profissional_resposta: true,
    profissional_agenda: true,
    sistema_atualizacao: false,
    sistema_seguranca: true,
};

function _getNotifPrefs() {
    try { return { ...NOTIF_DEFAULTS, ...JSON.parse(localStorage.getItem('na_notif_prefs') || '{}') }; }
    catch(e) { return { ...NOTIF_DEFAULTS }; }
}

function optNotif() {
    const prefs = _getNotifPrefs();

    function row(key, icon, iconBg, iconColor, label, desc) {
        const checked = prefs[key] ? 'checked' : '';
        return `
        <div class="notif-row">
            <div class="notif-icon" style="background:${iconBg};color:${iconColor}"><i class="fa-solid ${icon}"></i></div>
            <div class="notif-row-info"><strong>${label}</strong><span>${desc}</span></div>
            <label class="na-toggle">
                <input type="checkbox" data-key="${key}" ${checked} ${!prefs.master && key !== 'master' ? 'disabled' : ''}>
                <span class="na-toggle-slider"></span>
            </label>
        </div>`;
    }

    const body = `
        <div class="notif-master">
            <div class="notif-icon" style="background:var(--accent-dim);color:var(--accent)"><i class="fa-solid fa-bell"></i></div>
            <div class="notif-master-info">
                <strong>Notificações ativas</strong>
                <span>Desative para silenciar tudo</span>
            </div>
            <label class="na-toggle">
                <input type="checkbox" id="notifMasterToggle" data-key="master" ${prefs.master ? 'checked' : ''}>
                <span class="na-toggle-slider"></span>
            </label>
        </div>

        <div id="notifSubSection">
            <div class="notif-section-label">Chat</div>
            ${row('chat_msg',    'fa-comment-dots', 'rgba(96,165,250,.12)', '#60a5fa', 'Novas mensagens', 'Alertas de mensagens recebidas')}
            ${row('chat_novo',   'fa-user-plus',    'rgba(52,211,153,.12)', '#34d399', 'Novo contato', 'Quando alguém inicia uma conversa')}

            <div class="notif-section-label">Academia</div>
            ${row('treino_lembrete',  'fa-dumbbell',      'rgba(251,191,36,.12)', '#fbbf24', 'Lembrete de treino', 'Avisos do horário do treino')}
            ${row('treino_concluido', 'fa-trophy',        'rgba(52,211,153,.12)', '#34d399', 'Treino concluído', 'Confirmação ao marcar treino')}

            <div class="notif-section-label">Profissionais</div>
            ${row('profissional_resposta', 'fa-user-doctor', 'rgba(167,139,250,.12)', '#a78bfa', 'Resposta de profissional', 'Quando um profissional responder')}
            ${row('profissional_agenda',   'fa-calendar',    'rgba(248,113,113,.12)', '#f87171', 'Agendamento',           'Confirmações e lembretes de consulta')}

            <div class="notif-section-label">Sistema</div>
            ${row('sistema_atualizacao', 'fa-rotate',       'rgba(100,116,139,.12)', '#94a3b8', 'Atualizações', 'Novidades e melhorias do app')}
            ${row('sistema_seguranca',   'fa-shield-halved','rgba(239,68,68,.12)',   '#f87171', 'Segurança',    'Atividades suspeitas na conta')}
        </div>

        <button class="na-modal-save-btn" id="notifBtnSave" style="margin-top:16px"><i class="fa-solid fa-bell"></i> Salvar Preferências</button>
    `;

    const modal = _createModal(
        `<i style="background:var(--accent-dim);color:var(--accent)" class="fa-solid fa-bell"></i> Notificações`,
        body
    );

    /* Master toggle habilita/desabilita os outros */
    const masterCheck = modal.querySelector('#notifMasterToggle');
    const subSection  = modal.querySelector('#notifSubSection');

    function updateSubState(enabled) {
        subSection.querySelectorAll('input[type=checkbox]').forEach(inp => {
            inp.disabled = !enabled;
        });
        subSection.style.opacity = enabled ? '1' : '0.45';
    }
    updateSubState(prefs.master);

    masterCheck.addEventListener('change', () => updateSubState(masterCheck.checked));

    modal.querySelector('#notifBtnSave').addEventListener('click', () => {
        const newPrefs = {};
        modal.querySelectorAll('input[type=checkbox][data-key]').forEach(inp => {
            newPrefs[inp.dataset.key] = inp.checked;
        });
        localStorage.setItem('na_notif_prefs', JSON.stringify(newPrefs));
        const totalOn = Object.values(newPrefs).filter(Boolean).length - (newPrefs.master ? 1 : 0);
        showToast(newPrefs.master ? `Notificações salvas (${totalOn} ativas)` : 'Notificações desativadas');
        modal.remove();
    });
}

/* ══════════════════════════════════════════════════════════
   OUTRAS OPÇÕES (mantidas)
   ══════════════════════════════════════════════════════════ */
function optSenha() {
    closePanel();
    sessionStorage.setItem('na_redirect_panel', 'senha');
    window.location.href = 'index.html';
}

function optLogin() {
    const user = getCurrentUser();
    if (!user) return;
    showToast('Logado como: ' + user.email + ' (' + user.role + ')');
}

function optFavoritos() {
    const favs = JSON.parse(localStorage.getItem('na_favoritos') || '[]');
    showToast(favs.length ? 'Você tem ' + favs.length + ' favorito(s) salvo(s).' : 'Nenhum favorito salvo ainda.');
}

function optSair() {
    if (confirm('Deseja sair da conta?')) { doLogout(); }
}

/* ══════════════════════════════════════════════════════════
   TOAST GLOBAL
   ══════════════════════════════════════════════════════════ */
let _mainToastTimer;
function showToast(msg, tipo) {
    let el   = document.getElementById('toast');
    let icon = document.getElementById('toastIcon');
    let txt  = document.getElementById('toastMsg');
    if (!el) {
        el = document.createElement('div');
        el.id = 'toast'; el.className = 'toast';
        el.style.cssText = 'position:fixed;bottom:24px;left:50%;transform:translateX(-50%) translateY(20px);background:#1e293b;color:#f1f5f9;padding:12px 20px;border-radius:10px;font-size:14px;display:flex;gap:8px;align-items:center;opacity:0;transition:all .3s;z-index:9999;pointer-events:none';
        el.innerHTML = '<span id="toastIcon"></span><span id="toastMsg"></span>';
        document.body.appendChild(el);
        const style = document.createElement('style');
        style.textContent = '.toast.show{opacity:1!important;transform:translateX(-50%) translateY(0)!important}';
        document.head.appendChild(style);
        icon = document.getElementById('toastIcon');
        txt  = document.getElementById('toastMsg');
    }
    const iconClass = tipo === 'warn' ? 'fa-triangle-exclamation' : tipo === 'err' ? 'fa-circle-xmark' : 'fa-circle-check';
    el.className = 'toast ' + (tipo || '');
    if (icon) icon.className = 'fa-solid ' + iconClass;
    if (txt)  txt.textContent = msg;
    el.classList.add('show');
    clearTimeout(_mainToastTimer);
    _mainToastTimer = setTimeout(() => el.classList.remove('show'), 3500);
}

/* ══════════════════════════════════════════════════════════
   INIT
   ══════════════════════════════════════════════════════════ */
document.addEventListener('DOMContentLoaded', () => {
    if (document.getElementById('sidebar-avatar') || document.getElementById('pp-avatar')) {
        initProfile();
    }
    /* Reaplicar tema salvo ao carregar */
    const temaAuto = localStorage.getItem('na_tema_auto') === '1';
    if (temaAuto) {
        const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
        applyTema(prefersDark ? 'dark' : 'light', false);
    } else {
        const temaSalvo = localStorage.getItem('na_tema') || 'dark';
        applyTema(temaSalvo, false);
    }
    document.addEventListener('keydown', e => { if (e.key === 'Escape') { _closeAllModals() || closePanel(); } });
});