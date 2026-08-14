/* =============================================================
   admin.js — Next Agon · Painel Administrativo
   ============================================================= */

// ── DADOS BASE ──────────────────────────────────────────────────

const USERS_BASE = [
    { email: 'atleta@nextagon.com',       senha: '123456', role: 'atleta',       nome: 'Lucas Atleta',     avatar: 'LA', ativo: true },
    { email: 'profissional@nextagon.com', senha: '123456', role: 'profissional', nome: 'Ana Profissional', avatar: 'AP', ativo: true },
    { email: 'admin@nextagon.com',        senha: '123456', role: 'admin',        nome: 'Carlos Admin',     avatar: 'CA', ativo: true },
];

const PROFS_BASE = [
    { id:1, nome:'Carlos Lima',    iniciais:'CL', foto:'https://randomuser.me/api/portraits/men/32.jpg',   area:'Personal Trainer',  modalidade:'Presencial', disponivel:true,  rating:4.9, reviews:127, exp:8,  preco:280, cidade:'São Paulo, SP',      specs:['Hipertrofia','Força','Emagrecimento'],                registro:'CREF 048721-G/SP' },
    { id:2, nome:'Fernanda Ramos', iniciais:'FR', foto:'https://randomuser.me/api/portraits/women/55.jpg', area:'Nutricionista',     modalidade:'Online',     disponivel:true,  rating:4.9, reviews:189, exp:9,  preco:200, cidade:'Fortaleza, CE',      specs:['Nutrição','Emagrecimento','Reeducação Alimentar'],    registro:'CRN 19384' },
    { id:3, nome:'Lucas Andrade',  iniciais:'LA', foto:'https://randomuser.me/api/portraits/men/75.jpg',   area:'Personal Trainer',  modalidade:'Ambos',      disponivel:true,  rating:4.8, reviews:96,  exp:6,  preco:220, cidade:'Brasília, DF',       specs:['Funcional','Emagrecimento','Força'],                  registro:'CREF 032118-G/DF' },
    { id:4, nome:'Marina Souza',   iniciais:'MS', foto:'https://randomuser.me/api/portraits/women/44.jpg', area:'Nutricionista',     modalidade:'Presencial', disponivel:true,  rating:4.7, reviews:74,  exp:5,  preco:180, cidade:'Rio de Janeiro, RJ', specs:['Nutrição','Reeducação Alimentar','Emagrecimento'],    registro:'CRN 22710' },
    { id:5, nome:'Rafael Torres',  iniciais:'RT', foto:'https://randomuser.me/api/portraits/men/54.jpg',   area:'Personal Trainer',  modalidade:'Online',     disponivel:false, rating:4.6, reviews:58,  exp:4,  preco:160, cidade:'Recife, PE',         specs:['Hipertrofia','Treino em Casa','Emagrecimento'],       registro:'CREF 061455-G/PE' },
    { id:6, nome:'Patrícia Alves', iniciais:'PA', foto:'https://randomuser.me/api/portraits/women/68.jpg', area:'Nutricionista',     modalidade:'Ambos',      disponivel:true,  rating:5.0, reviews:211, exp:11, preco:260, cidade:'Curitiba, PR',       specs:['Nutrição','Saúde da Mulher','Performance Esportiva'], registro:'CRN 14560' },
];

const TREINOS_BASE = [
    {
        id: 1,
        nome: 'Hipertrofia Superior A',
        desc: 'Foco em peito, tríceps e ombros com progressão de carga.',
        freq: '4x/semana',
        dur: '60–70 min',
        nivel: 'Intermediário',
        exercicios: [
            { name:'Supino Reto com Barra',             sets:'4×10' },
            { name:'Supino Inclinado com Halteres',     sets:'4×12' },
            { name:'Crucifixo com Halteres',            sets:'3×15' },
            { name:'Desenvolvimento Militar com Barra', sets:'4×10' },
            { name:'Tríceps Pulley Barra Reta',         sets:'4×12' },
        ]
    }
];

const EXERCISES_BASE = [
    { id:1,  name:'Supino Reto com Barra',       muscle:'peito',    equip:'Barra',           diff:'Médio',  tipo:'Força',     emoji:'🏋️', icon:'fa-solid fa-person', sets:[{reps:'4×10'},{reps:'3×8'}] },
    { id:2,  name:'Agachamento Livre',            muscle:'pernas',   equip:'Barra',           diff:'Médio',  tipo:'Força',     emoji:'🦵', icon:'fa-solid fa-person', sets:[{reps:'4×10'},{reps:'3×12'}] },
    { id:3,  name:'Levantamento Terra',           muscle:'costas',   equip:'Barra',           diff:'Difícil',tipo:'Força',     emoji:'💪', icon:'fa-solid fa-person', sets:[{reps:'4×5'},{reps:'3×6'}] },
    { id:4,  name:'Puxada Frontal',               muscle:'costas',   equip:'Máquina',         diff:'Médio',  tipo:'Força',     emoji:'🔙', icon:'fa-solid fa-person', sets:[{reps:'4×12'},{reps:'3×10'}] },
    { id:5,  name:'Desenvolvimento com Halteres', muscle:'ombros',   equip:'Halteres',        diff:'Médio',  tipo:'Força',     emoji:'🏋️', icon:'fa-solid fa-person', sets:[{reps:'4×12'}] },
    { id:6,  name:'Rosca Direta com Barra',       muscle:'bíceps',   equip:'Barra',           diff:'Fácil',  tipo:'Força',     emoji:'💪', icon:'fa-solid fa-person', sets:[{reps:'3×12'}] },
    { id:7,  name:'Tríceps Pulley Barra Reta',    muscle:'tríceps',  equip:'Máquina',         diff:'Fácil',  tipo:'Força',     emoji:'💪', icon:'fa-solid fa-person', sets:[{reps:'4×12'}] },
    { id:8,  name:'Stiff com Halteres',           muscle:'glúteos',  equip:'Halteres',        diff:'Médio',  tipo:'Força',     emoji:'🍑', icon:'fa-solid fa-person', sets:[{reps:'4×12'}] },
    { id:9,  name:'Prancha Abdominal',            muscle:'abdômen',  equip:'Sem equipamento', diff:'Fácil',  tipo:'Funcional', emoji:'🧘', icon:'fa-solid fa-person', sets:[{reps:'3×45s'}] },
    { id:10, name:'Corrida na Esteira',           muscle:'full body',equip:'Esteira',          diff:'Fácil',  tipo:'Cardio',    emoji:'🏃', icon:'fa-solid fa-person', sets:[{reps:'30 min'}] },
];

// ── ESTADO ──────────────────────────────────────────────────────

let usuarios  = JSON.parse(localStorage.getItem('na_admin_users')   || 'null') || USERS_BASE;
let profs     = JSON.parse(localStorage.getItem('na_admin_profs')   || 'null') || PROFS_BASE;
let treinos   = JSON.parse(localStorage.getItem('na_admin_treinos') || 'null') || TREINOS_BASE;
let exercises = JSON.parse(localStorage.getItem('na_admin_exs')    || 'null') || EXERCISES_BASE;
let auditLog  = JSON.parse(localStorage.getItem('na_admin_log')    || '[]');

let logFilter      = 'todos';
let logCurrentPage = 0;
const LOG_PER_PAGE = 20;

// ── SAVE ────────────────────────────────────────────────────────

function save() {
    localStorage.setItem('na_admin_users',   JSON.stringify(usuarios));
    localStorage.setItem('na_admin_profs',   JSON.stringify(profs));
    localStorage.setItem('na_admin_treinos', JSON.stringify(treinos));
    localStorage.setItem('na_admin_exs',     JSON.stringify(exercises));
    try {
        const extras = profs.filter(p => p.criadoPeloAdmin).map(p => ({
            id: p.id, nome: p.nome,
            iniciais: p.iniciais || p.nome.split(' ').slice(0,2).map(w => w[0]).join('').toUpperCase(),
            foto: p.foto || '', area: p.area, cidade: p.cidade || '', rating: p.rating || 5.0, specs: p.specs || []
        }));
        localStorage.setItem('na_extra_profs', JSON.stringify(extras));
    } catch(e) {}
    if (treinos.length) localStorage.setItem('na_treino_rec', JSON.stringify(treinos[0]));
}

function saveLog() { localStorage.setItem('na_admin_log', JSON.stringify(auditLog)); }

// ── AUDIT LOG ───────────────────────────────────────────────────

function log(tipo, acao, detalhe) {
    const u = getAdminUser();
    auditLog.unshift({ tipo, acao, detalhe, usuario: u ? u.nome : 'Admin', ts: new Date().toISOString() });
    if (auditLog.length > 500) auditLog = auditLog.slice(0, 500);
    saveLog();
    atualizarStatAcoes();
}

function atualizarStatAcoes() {
    const el = document.getElementById('statAcoes');
    if (el) el.textContent = auditLog.length;
}

function renderLog(containerId, limit) {
    const container = document.getElementById(containerId);
    if (!container) return;
    let filtrado = logFilter === 'todos' ? auditLog : auditLog.filter(e => e.tipo === logFilter);
    if (limit) {
        filtrado = filtrado.slice(0, limit);
    } else {
        const start = logCurrentPage * LOG_PER_PAGE;
        const total = filtrado.length;
        const pag     = document.getElementById('logPag');
        const pagInfo = document.getElementById('logPagInfo');
        if (pag) pag.style.display = total > LOG_PER_PAGE ? 'flex' : 'none';
        if (pagInfo) pagInfo.textContent = `${start + 1}–${Math.min(start + LOG_PER_PAGE, total)} de ${total}`;
        document.getElementById('logPrev').disabled = logCurrentPage === 0;
        document.getElementById('logNext').disabled = start + LOG_PER_PAGE >= total;
        filtrado = filtrado.slice(start, start + LOG_PER_PAGE);
    }
    if (!filtrado.length) {
        container.innerHTML = '<div class="empty"><i class="fa-solid fa-clock-rotate-left"></i><p>Nenhuma ação registrada ainda.</p></div>';
        return;
    }
    const iconMap = { create:'fa-plus', edit:'fa-pen', delete:'fa-trash', login:'fa-right-to-bracket', system:'fa-gear' };
    container.innerHTML = filtrado.map(e => {
        const d = new Date(e.ts);
        return `<div class="log-item">
            <div class="log-icon ${e.tipo}"><i class="fa-solid ${iconMap[e.tipo] || 'fa-circle'}"></i></div>
            <div class="log-content">
                <div class="log-action">${e.acao}</div>
                <div class="log-detail">${e.detalhe} &mdash; por <strong>${e.usuario}</strong></div>
            </div>
            <div class="log-meta">${d.toLocaleTimeString('pt-BR',{hour:'2-digit',minute:'2-digit'})}<br>${d.toLocaleDateString('pt-BR')}</div>
        </div>`;
    }).join('');
}

function setLogFilter(f, btn) {
    logFilter = f; logCurrentPage = 0;
    document.querySelectorAll('.log-filter-btn').forEach(b => b.classList.remove('active'));
    if (btn) btn.classList.add('active');
    renderLog('logList');
}

function logPage(dir) {
    const filtrado = logFilter === 'todos' ? auditLog : auditLog.filter(e => e.tipo === logFilter);
    logCurrentPage = Math.max(0, Math.min(Math.ceil(filtrado.length / LOG_PER_PAGE) - 1, logCurrentPage + dir));
    renderLog('logList');
}

function confirmarLimparLog() {
    abrirConfirm('🗑️','Limpar todo o histórico?',`Todas as ${auditLog.length} ações serão apagadas permanentemente.`, () => {
        auditLog = []; saveLog();
        renderLog('logList'); renderLog('dashLogList', 8);
        atualizarStatAcoes(); toast('Histórico limpo.', 'warn');
    });
}

// ── NAVEGAÇÃO ───────────────────────────────────────────────────

function showPage(id, btn) {
    document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
    document.querySelectorAll('.nav-item').forEach(b => b.classList.remove('active'));
    document.getElementById('page-' + id).classList.add('active');
    if (btn) btn.classList.add('active');
    renderPage(id);
}

function renderPage(id) {
    if (id === 'dashboard')     renderDashboard();
    if (id === 'historico')     { logCurrentPage = 0; renderLog('logList'); }
    if (id === 'usuarios')      renderUsuarios();
    if (id === 'profissionais') renderProfs();
    if (id === 'treinos')       renderTreinos();
    if (id === 'exercicios')    renderExercicios();
    if (id === 'auditoria')     renderAuditoria();
}

function renderDashboard() {
    document.getElementById('statUsuarios').textContent = usuarios.length;
    document.getElementById('statProfs').textContent    = profs.length;
    document.getElementById('statTreinos').textContent  = treinos.length;
    atualizarStatAcoes();
    renderLog('dashLogList', 8);
}

function voltarDashboard() { window.location.href = 'dashboard.html'; }

// ── USUÁRIOS ────────────────────────────────────────────────────

function renderUsuarios() {
    const roleLabel = { atleta:'Atleta', profissional:'Profissional', admin:'Admin' };
    const roleBadge = { atleta:'badge-blue', profissional:'badge-green', admin:'badge-purple' };
    document.getElementById('tbodyUsuarios').innerHTML = usuarios.map((u, i) => `
        <tr>
            <td><div class="td-user"><div class="avatar-sm">${u.avatar || u.nome[0]}</div><div>${u.nome}</div></div></td>
            <td>${u.email}</td>
            <td><span class="badge ${roleBadge[u.role]||'badge-gray'}">${roleLabel[u.role]||u.role}</span></td>
            <td><span class="badge ${u.ativo!==false?'badge-green':'badge-red'}">${u.ativo!==false?'Ativo':'Inativo'}</span></td>
            <td><div class="td-actions">
                <button class="btn-edit"   onclick="editarUsuario(${i})"><i class="fa-solid fa-pen"></i> Editar</button>
                <button class="btn-danger" onclick="confirmarExcluirUsuario(${i})"><i class="fa-solid fa-trash"></i></button>
            </div></td>
        </tr>`).join('');
}

function abrirModalUsuario(idx) {
    limparModal('modalUsuario');
    document.getElementById('uIdx').value = idx !== undefined ? idx : '';
    if (idx !== undefined) {
        const u = usuarios[idx];
        document.getElementById('modalUsuarioTitulo').textContent = 'Editar Usuário';
        document.getElementById('uNome').value   = u.nome;
        document.getElementById('uEmail').value  = u.email;
        document.getElementById('uAvatar').value = u.avatar || '';
        document.getElementById('uSenha').value  = u.senha  || '';
        document.getElementById('uRole').value   = u.role;
    } else {
        document.getElementById('modalUsuarioTitulo').textContent = 'Novo Usuário';
    }
    abrir('modalUsuario');
}

function editarUsuario(i) { abrirModalUsuario(i); }

function salvarUsuario() {
    const idx    = document.getElementById('uIdx').value;
    const nome   = document.getElementById('uNome').value.trim();
    const email  = document.getElementById('uEmail').value.trim().toLowerCase();
    const avatar = document.getElementById('uAvatar').value.trim().toUpperCase() || nome.split(' ').slice(0,2).map(w=>w[0]).join('').toUpperCase();
    const senha  = document.getElementById('uSenha').value;
    const role   = document.getElementById('uRole').value;
    if (!nome || !email) return toast('Preencha nome e e-mail.', 'err');
    if (idx === '') {
        if (senha.length < 6) return toast('Senha deve ter no mínimo 6 caracteres.', 'err');
        usuarios.push({ nome, email, avatar, senha, role, ativo: true });
        log('create', 'Usuário criado', `${nome} (${role})`);
        toast(`Usuário "${nome}" criado!`);
    } else {
        const i = parseInt(idx);
        usuarios[i] = { ...usuarios[i], nome, email, avatar, role, ...(senha.length>=6?{senha}:{}) };
        log('edit', 'Usuário editado', `${nome} (${role})`);
        toast(`Usuário "${nome}" atualizado.`);
    }
    save(); fechar('modalUsuario'); renderUsuarios(); renderDashboard();
}

function confirmarExcluirUsuario(i) {
    abrirConfirm('⚠️','Excluir usuário?',`"${usuarios[i].nome}" será removido permanentemente.`, () => {
        const nome = usuarios[i].nome;
        usuarios.splice(i, 1); save();
        log('delete','Usuário excluído', nome);
        renderUsuarios(); renderDashboard(); toast(`Usuário "${nome}" excluído.`,'warn');
    });
}

// ── PROFISSIONAIS ───────────────────────────────────────────────

function renderProfs() {
    document.getElementById('tbodyProfs').innerHTML = profs.map((p, i) => `
        <tr>
            <td><div class="td-user">
                <div class="avatar-sm">${p.foto?`<img src="${p.foto}" onerror="this.parentNode.textContent='${p.iniciais||p.nome[0]}'">`:(p.iniciais||p.nome[0])}</div>
                <div>${p.nome}</div>
            </div></td>
            <td>${p.area}</td><td>${p.cidade||'—'}</td><td>${p.modalidade}</td>
            <td><span class="badge ${p.disponivel?'badge-green':'badge-red'}">${p.disponivel?'Disponível':'Indisponível'}</span></td>
            <td><strong style="color:var(--accent)">R$ ${p.preco}</strong></td>
            <td><div class="td-actions">
                <button class="btn-edit"   onclick="editarProfissional(${i})"><i class="fa-solid fa-pen"></i> Editar</button>
                <button class="btn-danger" onclick="confirmarExcluirProf(${i})"><i class="fa-solid fa-trash"></i></button>
            </div></td>
        </tr>`).join('');
}

function abrirModalProfissional(idx) {
    limparModal('modalProfissional');
    document.getElementById('pIdx').value = idx !== undefined ? idx : '';
    if (idx !== undefined) {
        const p = profs[idx];
        document.getElementById('modalProfTitulo').textContent = 'Editar Profissional';
        document.getElementById('pNome').value       = p.nome;
        document.getElementById('pArea').value       = p.area;
        document.getElementById('pCidade').value     = p.cidade     || '';
        document.getElementById('pRegistro').value   = p.registro   || '';
        document.getElementById('pPreco').value      = p.preco      || '';
        document.getElementById('pExp').value        = p.exp        || '';
        document.getElementById('pModalidade').value = p.modalidade;
        document.getElementById('pDisponivel').value = String(p.disponivel);
        document.getElementById('pSpecs').value      = (p.specs||[]).join(', ');
        document.getElementById('pFoto').value       = p.foto       || '';
    } else {
        document.getElementById('modalProfTitulo').textContent = 'Novo Profissional';
    }
    abrir('modalProfissional');
}

function editarProfissional(i) { abrirModalProfissional(i); }

function salvarProfissional() {
    const idx        = document.getElementById('pIdx').value;
    const nome       = document.getElementById('pNome').value.trim();
    const area       = document.getElementById('pArea').value;
    const cidade     = document.getElementById('pCidade').value.trim();
    const registro   = document.getElementById('pRegistro').value.trim();
    const preco      = parseFloat(document.getElementById('pPreco').value) || 0;
    const exp        = parseInt(document.getElementById('pExp').value)     || 1;
    const modalidade = document.getElementById('pModalidade').value;
    const disponivel = document.getElementById('pDisponivel').value === 'true';
    const specs      = document.getElementById('pSpecs').value.split(',').map(s=>s.trim()).filter(Boolean);
    const foto       = document.getElementById('pFoto').value.trim();
    const iniciais   = nome.split(' ').slice(0,2).map(w=>w[0]).join('').toUpperCase();
    if (!nome) return toast('Preencha o nome do profissional.', 'err');
    if (idx === '') {
        const newId = profs.reduce((mx,p)=>Math.max(mx,p.id||0),0)+1;
        profs.unshift({ id:newId, nome, iniciais, foto, area, modalidade, disponivel, rating:5.0, reviews:0, exp, preco, cidade, specs, registro, isNovo:true, criadoPeloAdmin:true });
        log('create','Profissional cadastrado',`${nome} — ${area}`);
        toast(`Profissional "${nome}" adicionado!`);
    } else {
        const i = parseInt(idx);
        profs[i] = { ...profs[i], nome, iniciais, foto, area, modalidade, disponivel, exp, preco, cidade, specs, registro };
        log('edit','Profissional editado',`${nome} — ${area}`);
        toast(`Perfil de "${nome}" atualizado.`);
    }
    save(); fechar('modalProfissional'); renderProfs(); renderDashboard();
}

function confirmarExcluirProf(i) {
    abrirConfirm('⚠️','Excluir profissional?',`"${profs[i].nome}" será removido permanentemente.`, () => {
        const nome = profs[i].nome;
        profs.splice(i, 1); save();
        log('delete','Profissional excluído', nome);
        renderProfs(); renderDashboard(); toast(`Profissional "${nome}" excluído.`,'warn');
    });
}

// ── TREINOS ─────────────────────────────────────────────────────

function renderTreinos() {
    const container = document.getElementById('treinoAdminList');
    if (!treinos.length) { container.innerHTML = '<div class="empty"><i class="fa-solid fa-dumbbell"></i><p>Nenhum treino cadastrado.</p></div>'; return; }
    container.innerHTML = treinos.map((t, i) => `
        <div class="treino-admin-card">
            <div style="width:48px;height:48px;border-radius:14px;background:var(--admin-dim);border:1px solid rgba(124,58,237,.3);display:flex;align-items:center;justify-content:center;font-size:22px;flex-shrink:0">🏋️</div>
            <div class="treino-admin-info">
                <div class="treino-admin-name">${t.nome}</div>
                <div class="treino-admin-meta">
                    <span><i class="fa-solid fa-repeat"  style="color:var(--admin);margin-right:4px"></i>${t.freq||'—'}</span>
                    <span><i class="fa-regular fa-clock" style="color:var(--admin);margin-right:4px"></i>${t.dur||'—'}</span>
                    <span><i class="fa-solid fa-signal"  style="color:var(--admin);margin-right:4px"></i>${t.nivel||'—'}</span>
                    <span><i class="fa-solid fa-list"    style="color:var(--admin);margin-right:4px"></i>${(t.exercicios||[]).length} exercícios</span>
                </div>
                <div style="font-size:12px;color:var(--text3);margin-top:4px">${t.desc||''}</div>
            </div>
            <div class="treino-admin-actions">
                <button class="btn-edit"   onclick="editarTreino(${i})"><i class="fa-solid fa-pen"></i> Editar</button>
                <button class="btn-danger" onclick="confirmarExcluirTreino(${i})"><i class="fa-solid fa-trash"></i></button>
            </div>
        </div>`).join('');
}

function abrirModalTreino(idx) {
    document.getElementById('tIdx').value = idx !== undefined ? idx : '';
    document.getElementById('tExList').innerHTML = '';
    if (idx !== undefined) {
        const t = treinos[idx];
        document.getElementById('modalTreinoTitulo').textContent = 'Editar Treino';
        document.getElementById('tNome').value  = t.nome;
        document.getElementById('tDesc').value  = t.desc  || '';
        document.getElementById('tFreq').value  = t.freq  || '';
        document.getElementById('tDur').value   = t.dur   || '';
        document.getElementById('tNivel').value = t.nivel || 'Intermediário';
        (t.exercicios||[]).forEach(ex => addExRow(ex.name, ex.sets));
    } else {
        document.getElementById('modalTreinoTitulo').textContent = 'Novo Treino';
        ['tNome','tDesc','tFreq','tDur'].forEach(id => document.getElementById(id).value = '');
        document.getElementById('tNivel').value = 'Intermediário';
    }
    abrir('modalTreino');
}

function editarTreino(i) { abrirModalTreino(i); }

function addExRow(name, sets) {
    const div = document.createElement('div');
    div.style.cssText = 'display:flex;gap:8px;align-items:center';
    div.innerHTML = `
        <input placeholder="Nome do exercício" value="${name||''}" style="flex:1;background:var(--bg);border:1px solid var(--border);border-radius:8px;color:var(--text);font-size:13px;padding:8px 11px;outline:none;font-family:inherit">
        <input placeholder="Séries" value="${sets||''}" style="width:90px;background:var(--bg);border:1px solid var(--border);border-radius:8px;color:var(--text);font-size:13px;padding:8px 11px;outline:none;font-family:inherit">
        <button type="button" onclick="this.parentNode.remove()" style="background:rgba(248,113,113,.12);border:1px solid rgba(248,113,113,.25);color:#f87171;width:32px;height:32px;border-radius:7px;cursor:pointer;font-size:14px;flex-shrink:0">×</button>`;
    document.getElementById('tExList').appendChild(div);
}

function salvarTreino() {
    const idx   = document.getElementById('tIdx').value;
    const nome  = document.getElementById('tNome').value.trim();
    const desc  = document.getElementById('tDesc').value.trim();
    const freq  = document.getElementById('tFreq').value.trim();
    const dur   = document.getElementById('tDur').value.trim();
    const nivel = document.getElementById('tNivel').value;
    if (!nome) return toast('Preencha o nome do treino.', 'err');
    const exercicios = Array.from(document.querySelectorAll('#tExList > div')).map(r => {
        const inp = r.querySelectorAll('input');
        return { name: inp[0].value.trim(), sets: inp[1].value.trim() };
    }).filter(e => e.name);
    const treino = { nome, desc, freq, dur, nivel, exercicios };
    if (idx === '') {
        treino.id = treinos.reduce((mx,t)=>Math.max(mx,t.id||0),0)+1;
        treinos.push(treino); log('create','Treino criado', nome); toast(`Treino "${nome}" criado!`);
    } else {
        const i = parseInt(idx);
        treinos[i] = { ...treinos[i], ...treino }; log('edit','Treino editado', nome); toast(`Treino "${nome}" atualizado.`);
    }
    save(); fechar('modalTreino'); renderTreinos(); renderDashboard();
}

function confirmarExcluirTreino(i) {
    abrirConfirm('🗑️','Excluir treino?',`"${treinos[i].nome}" será removido permanentemente.`, () => {
        const nome = treinos[i].nome;
        treinos.splice(i, 1); save();
        log('delete','Treino excluído', nome);
        renderTreinos(); renderDashboard(); toast(`Treino "${nome}" excluído.`,'warn');
    });
}

// ── EXERCÍCIOS ──────────────────────────────────────────────────

function renderExercicios() {
    const diffBadge = { 'Fácil':'badge-green','Médio':'badge-yellow','Difícil':'badge-red' };
    document.getElementById('tbodyEx').innerHTML = exercises.map((ex, i) => `
        <tr>
            <td><div class="td-user">
                <div style="width:34px;height:34px;border-radius:9px;background:rgba(26,107,222,.12);border:1px solid rgba(26,107,222,.2);display:flex;align-items:center;justify-content:center;font-size:16px;flex-shrink:0">${ex.emoji||'💪'}</div>
                <div>${ex.name}</div>
            </div></td>
            <td style="text-transform:capitalize">${ex.muscle}</td>
            <td>${ex.equip}</td>
            <td><span class="badge ${diffBadge[ex.diff]||'badge-gray'}">${ex.diff}</span></td>
            <td>${ex.tipo}</td>
            <td><div class="td-actions">
                <button class="btn-edit"   onclick="editarExercicio(${i})"><i class="fa-solid fa-pen"></i> Editar</button>
                <button class="btn-danger" onclick="confirmarExcluirEx(${i})"><i class="fa-solid fa-trash"></i></button>
            </div></td>
        </tr>`).join('');
}

function abrirModalExercicio(idx) {
    document.getElementById('exIdx').value = idx !== undefined ? idx : '';
    document.getElementById('exSeriesList').innerHTML = '';
    if (idx !== undefined) {
        const ex = exercises[idx];
        document.getElementById('modalExTitulo').textContent = 'Editar Exercício';
        document.getElementById('exNome').value    = ex.name;
        document.getElementById('exMusculo').value = ex.muscle;
        document.getElementById('exEquip').value   = ex.equip;
        document.getElementById('exDiff').value    = ex.diff;
        document.getElementById('exTipo').value    = ex.tipo;
        document.getElementById('exEmoji').value   = ex.emoji || '';
        (ex.sets||[]).forEach(s => addSerieRow(s.reps));
    } else {
        document.getElementById('modalExTitulo').textContent = 'Novo Exercício';
        ['exNome','exEquip','exEmoji'].forEach(id => document.getElementById(id).value = '');
        document.getElementById('exMusculo').value = 'peito';
        document.getElementById('exDiff').value    = 'Médio';
        document.getElementById('exTipo').value    = 'Força';
    }
    abrir('modalExercicio');
}

function editarExercicio(i) { abrirModalExercicio(i); }

function addSerieRow(reps) {
    const div = document.createElement('div');
    div.style.cssText = 'display:flex;gap:8px;align-items:center';
    div.innerHTML = `
        <input placeholder="Ex: 4×10" value="${reps||''}" style="flex:1;background:var(--bg);border:1px solid var(--border);border-radius:8px;color:var(--text);font-size:13px;padding:8px 11px;outline:none;font-family:inherit">
        <button type="button" onclick="this.parentNode.remove()" style="background:rgba(248,113,113,.12);border:1px solid rgba(248,113,113,.25);color:#f87171;width:32px;height:32px;border-radius:7px;cursor:pointer;font-size:14px;flex-shrink:0">×</button>`;
    document.getElementById('exSeriesList').appendChild(div);
}

function salvarExercicio() {
    const idx    = document.getElementById('exIdx').value;
    const name   = document.getElementById('exNome').value.trim();
    const muscle = document.getElementById('exMusculo').value;
    const equip  = document.getElementById('exEquip').value.trim();
    const diff   = document.getElementById('exDiff').value;
    const tipo   = document.getElementById('exTipo').value;
    const emoji  = document.getElementById('exEmoji').value.trim() || '💪';
    if (!name) return toast('Preencha o nome do exercício.', 'err');
    const sets = Array.from(document.querySelectorAll('#exSeriesList > div'))
        .map(r => ({ reps: r.querySelector('input').value.trim() })).filter(s => s.reps);
    if (!sets.length) sets.push({ reps: '3×12' });
    const exObj = { name, muscle, equip, diff, tipo, emoji, icon:'fa-solid fa-person', sets };
    if (idx === '') {
        exObj.id = exercises.reduce((mx,e)=>Math.max(mx,e.id||0),0)+1;
        exercises.push(exObj); log('create','Exercício criado', name); toast(`Exercício "${name}" adicionado!`);
    } else {
        const i = parseInt(idx);
        exercises[i] = { ...exercises[i], ...exObj }; log('edit','Exercício editado', name); toast(`Exercício "${name}" atualizado.`);
    }
    save(); fechar('modalExercicio'); renderExercicios();
}

function confirmarExcluirEx(i) {
    abrirConfirm('🗑️','Excluir exercício?',`"${exercises[i].name}" será removido permanentemente.`, () => {
        const name = exercises[i].name;
        exercises.splice(i, 1); save();
        log('delete','Exercício excluído', name);
        renderExercicios(); toast(`Exercício "${name}" excluído.`,'warn');
    });
}

// ── FILTRAR TABELA ──────────────────────────────────────────────

function filtrarTabela(tableId, query) {
    const q = query.toLowerCase();
    document.querySelectorAll(`#${tableId} tbody tr`).forEach(tr => {
        tr.style.display = tr.textContent.toLowerCase().includes(q) ? '' : 'none';
    });
}

// ── MODAL HELPERS ───────────────────────────────────────────────

let _confirmCallback = null;

function abrirConfirm(icon, title, sub, cb) {
    document.getElementById('confirmIcon').textContent  = icon;
    document.getElementById('confirmTitle').textContent = title;
    document.getElementById('confirmSub').textContent   = sub;
    _confirmCallback = cb;
    document.getElementById('confirmBtn').onclick = () => { fechar('modalConfirm'); if (_confirmCallback) _confirmCallback(); };
    abrir('modalConfirm');
}

function abrir(id)  { document.getElementById(id).classList.add('open');    document.body.style.overflow = 'hidden'; }
function fechar(id) { document.getElementById(id).classList.remove('open'); document.body.style.overflow = ''; }
function fecharModalSeOverlay(e, id) { if (e.target === document.getElementById(id)) fechar(id); }
function limparModal(id) {
    document.getElementById(id).querySelectorAll('input, select, textarea').forEach(el => {
        if (el.tagName === 'SELECT') el.selectedIndex = 0;
        else if (el.type !== 'hidden') el.value = '';
    });
}

// ── TOAST ───────────────────────────────────────────────────────

let _toastTimer;
function toast(msg, type) {
    const el   = document.getElementById('toast');
    const icon = document.getElementById('toastIcon');
    el.className   = 'toast ' + (type || '');
    icon.className = 'fa-solid ' + (type==='warn'?'fa-triangle-exclamation':type==='err'?'fa-circle-xmark':'fa-circle-check');
    document.getElementById('toastMsg').textContent = msg;
    el.classList.add('show');
    clearTimeout(_toastTimer);
    _toastTimer = setTimeout(() => el.classList.remove('show'), 3500);
}

// ── INIT ────────────────────────────────────────────────────────

function getAdminUser() {
    try { return JSON.parse(sessionStorage.getItem('naUser') || 'null'); } catch(e) { return null; }
}

document.addEventListener('DOMContentLoaded', () => {
    const u = getAdminUser();
    if (!u || u.role !== 'admin') {
        document.body.innerHTML = `
            <div style="display:flex;align-items:center;justify-content:center;height:100vh;flex-direction:column;gap:16px;background:#0a1120;color:#f1f5f9;font-family:system-ui">
                <i class="fa-solid fa-shield-halved" style="font-size:48px;color:#f87171"></i>
                <div style="font-size:20px;font-weight:700">Acesso Negado</div>
                <div style="color:#7a90aa">Você precisa ser administrador para acessar esta página.</div>
                <a href="index.html" style="background:#1a6bde;color:#fff;padding:10px 24px;border-radius:10px;text-decoration:none;font-weight:700;margin-top:8px">Ir para o Login</a>
            </div>`;
        return;
    }
    document.getElementById('sideNome').textContent   = u.nome   || 'Admin';
    document.getElementById('sideAvatar').textContent = u.avatar || u.nome[0] || 'A';
    const dateLabel = document.getElementById('adminDateLabel');
    if (dateLabel) {
        dateLabel.textContent = new Date().toLocaleDateString('pt-BR', { weekday:'long', day:'numeric', month:'long', year:'numeric' });
    }
    log('login', 'Acesso ao painel admin', `Login como ${u.nome}`);
    renderDashboard();
    atualizarStatAcoes();
});
// ── AUDITORIA DO SISTEMA ─────────────────────────────────────────

let auditTypeFilter = 'todos';
let auditCurrentPage = 0;
const AUDIT_PER_PAGE = 25;

const AUDIT_TIPOS = [
    { key: 'todos',  label: 'Todos',    icon: 'fa-list',               color: '#7a90aa' },
    { key: 'create', label: 'Criação',  icon: 'fa-plus',               color: '#34d399' },
    { key: 'edit',   label: 'Edição',   icon: 'fa-pen',                color: '#60a5fa' },
    { key: 'delete', label: 'Exclusão', icon: 'fa-trash',              color: '#f87171' },
    { key: 'login',  label: 'Login',    icon: 'fa-right-to-bracket',   color: '#a78bfa' },
    { key: 'system', label: 'Sistema',  icon: 'fa-gear',               color: '#fbbf24' },
];

function renderAuditoria() {
    renderAuditKPIs();
    renderAuditBarChart();
    renderAuditHourChart();
    renderAuditTypeFilter();
    auditCurrentPage = 0;
    renderAuditTimeline();

    const subtitle = document.getElementById('auditSubtitle');
    if (subtitle) {
        const last = auditLog[0];
        subtitle.textContent = last
            ? `Última atividade: ${new Date(last.ts).toLocaleString('pt-BR')} — ${auditLog.length} ação(ões) registrada(s)`
            : 'Nenhuma ação registrada ainda.';
    }
}

function renderAuditKPIs() {
    const counts = {};
    AUDIT_TIPOS.filter(t => t.key !== 'todos').forEach(t => counts[t.key] = 0);
    auditLog.forEach(e => { if (counts[e.tipo] !== undefined) counts[e.tipo]++; });

    // Unique users
    const uniqueUsers = new Set(auditLog.map(e => e.usuario)).size;

    // Most active day
    const byDay = {};
    auditLog.forEach(e => {
        const d = new Date(e.ts).toLocaleDateString('pt-BR');
        byDay[d] = (byDay[d] || 0) + 1;
    });
    const topDay = Object.entries(byDay).sort((a,b)=>b[1]-a[1])[0];

    const kpis = [
        { icon:'fa-bolt', color:'#fbbf24', bg:'rgba(251,191,36,.12)', val: auditLog.length, lbl:'Total de Ações', sub:'No histórico completo' },
        { icon:'fa-plus',               color:'#34d399', bg:'rgba(52,211,153,.12)',  val: counts.create, lbl:'Criações',    sub:'Registros adicionados' },
        { icon:'fa-pen',                color:'#60a5fa', bg:'rgba(96,165,250,.12)',  val: counts.edit,   lbl:'Edições',     sub:'Alterações realizadas' },
        { icon:'fa-trash',              color:'#f87171', bg:'rgba(248,113,113,.12)', val: counts.delete, lbl:'Exclusões',   sub:'Registros removidos' },
        { icon:'fa-right-to-bracket',   color:'#a78bfa', bg:'rgba(167,139,250,.12)', val: counts.login,  lbl:'Acessos',    sub:'Logins no painel' },
        { icon:'fa-users',              color:'#38bdf8', bg:'rgba(56,189,248,.12)',  val: uniqueUsers,   lbl:'Usuários Ativos', sub:'Realizaram ações' },
    ];
    if (topDay) kpis.push({ icon:'fa-calendar-day', color:'#fb923c', bg:'rgba(251,146,60,.12)', val: topDay[1], lbl:'Pico Diário', sub:topDay[0] });

    document.getElementById('auditKpis').innerHTML = kpis.map(k => `
        <div class="audit-kpi">
            <div class="audit-kpi-icon" style="background:${k.bg};color:${k.color}"><i class="fa-solid ${k.icon}"></i></div>
            <div class="audit-kpi-val">${k.val}</div>
            <div class="audit-kpi-lbl">${k.lbl}</div>
            <div class="audit-kpi-sub">${k.sub}</div>
        </div>`).join('');
}

function renderAuditBarChart() {
    const tipos = AUDIT_TIPOS.filter(t => t.key !== 'todos');
    const counts = {};
    tipos.forEach(t => counts[t.key] = 0);
    auditLog.forEach(e => { if (counts[e.tipo] !== undefined) counts[e.tipo]++; });
    const max = Math.max(1, ...Object.values(counts));

    const colorMap = { create:'#34d399', edit:'#60a5fa', delete:'#f87171', login:'#a78bfa', system:'#fbbf24' };

    document.getElementById('auditBarChart').innerHTML = tipos.map(t => `
        <div class="audit-bar-row">
            <div class="audit-bar-label">
                <i class="fa-solid ${t.icon}" style="color:${t.color};width:14px;text-align:center"></i>
                ${t.label}
            </div>
            <div class="audit-bar-track">
                <div class="audit-bar-fill" style="width:${Math.round(counts[t.key]/max*100)}%;background:${colorMap[t.key]}"></div>
            </div>
            <div class="audit-bar-count">${counts[t.key]}</div>
        </div>`).join('');
}

function renderAuditHourChart() {
    const hourCounts = Array(24).fill(0);
    auditLog.forEach(e => { hourCounts[new Date(e.ts).getHours()]++; });
    const max = Math.max(1, ...hourCounts);

    document.getElementById('auditHourChart').innerHTML = hourCounts.map((c, h) => `
        <div class="audit-hour-col" title="${h}h: ${c} ações">
            <div class="audit-hour-bar" style="height:${Math.max(4, Math.round(c/max*70))}px" data-tip="${h}h: ${c}"></div>
            ${h % 6 === 0 ? `<div class="audit-hour-lbl">${String(h).padStart(2,'0')}h</div>` : '<div class="audit-hour-lbl"></div>'}
        </div>`).join('');
}

function renderAuditTypeFilter() {
    document.getElementById('auditTypeFilter').innerHTML = AUDIT_TIPOS.map(t => `
        <button class="audit-pill ${auditTypeFilter === t.key ? 'active' : ''}"
                onclick="setAuditFilter('${t.key}', this)">
            <i class="fa-solid ${t.icon}"></i> ${t.label}
        </button>`).join('');
}

function setAuditFilter(tipo, btn) {
    auditTypeFilter = tipo;
    auditCurrentPage = 0;
    document.querySelectorAll('.audit-pill').forEach(b => b.classList.remove('active'));
    if (btn) btn.classList.add('active');
    renderAuditTimeline();
}

function auditChangePage(dir) {
    const filtered = getAuditFiltered();
    const total = filtered.length;
    auditCurrentPage = Math.max(0, Math.min(Math.ceil(total / AUDIT_PER_PAGE) - 1, auditCurrentPage + dir));
    renderAuditTimeline();
}

function getAuditFiltered() {
    const query = (document.getElementById('auditSearch')?.value || '').toLowerCase();
    return auditLog.filter(e => {
        const matchTipo = auditTypeFilter === 'todos' || e.tipo === auditTypeFilter;
        const matchQuery = !query || e.acao.toLowerCase().includes(query)
            || e.detalhe.toLowerCase().includes(query)
            || e.usuario.toLowerCase().includes(query);
        return matchTipo && matchQuery;
    });
}

function renderAuditTimeline() {
    const filtered = getAuditFiltered();
    const total = filtered.length;
    const start = auditCurrentPage * AUDIT_PER_PAGE;
    const page  = filtered.slice(start, start + AUDIT_PER_PAGE);

    // Paginação
    const pag     = document.getElementById('auditPag');
    const pagInfo = document.getElementById('auditPagInfo');
    if (pag) pag.style.display = total > AUDIT_PER_PAGE ? 'flex' : 'none';
    if (pagInfo) pagInfo.textContent = `${start + 1}–${Math.min(start + AUDIT_PER_PAGE, total)} de ${total}`;
    if (document.getElementById('auditPrev')) document.getElementById('auditPrev').disabled = auditCurrentPage === 0;
    if (document.getElementById('auditNext')) document.getElementById('auditNext').disabled = start + AUDIT_PER_PAGE >= total;

    const tl = document.getElementById('auditTimeline');
    if (!page.length) {
        tl.innerHTML = `<div class="audit-empty"><i class="fa-solid fa-magnifying-glass"></i><p>Nenhuma ação encontrada.</p></div>`;
        return;
    }

    const iconMap = { create:'fa-plus', edit:'fa-pen', delete:'fa-trash', login:'fa-right-to-bracket', system:'fa-gear' };
    const labelMap = { create:'Criação', edit:'Edição', delete:'Exclusão', login:'Login', system:'Sistema' };

    // Agrupar por data
    const groups = {};
    page.forEach(e => {
        const d = new Date(e.ts).toLocaleDateString('pt-BR', { weekday:'long', day:'numeric', month:'long', year:'numeric' });
        if (!groups[d]) groups[d] = [];
        groups[d].push(e);
    });

    tl.innerHTML = Object.entries(groups).map(([date, items]) => `
        <div class="audit-timeline-group">
            <div class="audit-tl-date"><i class="fa-solid fa-calendar-days" style="margin-right:6px"></i>${date}</div>
            ${items.map(e => {
        const d = new Date(e.ts);
        const time = d.toLocaleTimeString('pt-BR', {hour:'2-digit', minute:'2-digit', second:'2-digit'});
        return `<div class="audit-tl-item">
                    <div class="audit-tl-dot ${e.tipo}"></div>
                    <div class="audit-tl-body">
                        <div class="audit-tl-action">${e.acao}</div>
                        <div class="audit-tl-detail">${e.detalhe} — por <strong>${e.usuario}</strong></div>
                    </div>
                    <div class="audit-tl-meta">
                        <span class="audit-tl-badge ${e.tipo}">
                            <i class="fa-solid ${iconMap[e.tipo] || 'fa-circle'}"></i> ${labelMap[e.tipo] || e.tipo}
                        </span>
                        <span>${time}</span>
                    </div>
                </div>`;
    }).join('')}
        </div>`).join('');
}

function exportarAuditCSV() {
    const filtered = getAuditFiltered();
    if (!filtered.length) { toast('Nenhum dado para exportar.', 'warn'); return; }
    const header = ['Data/Hora', 'Tipo', 'Ação', 'Detalhe', 'Usuário'];
    const rows = filtered.map(e => [
        new Date(e.ts).toLocaleString('pt-BR'),
        e.tipo, e.acao, e.detalhe, e.usuario
    ].map(v => `"${String(v).replace(/"/g,'""')}"`).join(','));
    const csv = [header.join(','), ...rows].join('\r\n');
    const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url; a.download = `auditoria_next_agon_${new Date().toISOString().slice(0,10)}.csv`;
    a.click(); URL.revokeObjectURL(url);
    toast('CSV exportado com sucesso!');
    log('system', 'Exportação de auditoria', `${filtered.length} registros exportados`);
}