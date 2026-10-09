(function () {
    const kind = document.body.dataset.feedKind;
    const isTraining = kind === 'training';
    const form = document.getElementById('postForm');
    const feed = document.getElementById('feedList');
    const textField = document.getElementById('postText');
    const titleField = document.getElementById('postTitle');
    const typeField = document.getElementById('postType');
    const opportunityFields = document.getElementById('opportunityFields');
    const user = typeof getCurrentUser === 'function' ? getCurrentUser() : null;
    let activeFilter = 'all';
    let posts = [];

    if (!form || !feed || !user) return;

    const authorName = user.nome || user.name || 'Membro Next Agon';
    const roleLabels = { atleta: 'Atleta', athlete: 'Atleta', profissional: 'Profissional', professional: 'Profissional', admin: 'Administrador', administrator: 'Administrador' };
    const authorRole = roleLabels[String(user.role || '').toLowerCase()] || 'Membro Next Agon';
    const initials = authorName.trim().split(/\s+/).slice(0, 2).map(part => part[0]).join('').toUpperCase() || 'NA';

    function escapeHtml(value) {
        return String(value ?? '').replace(/[&<>"']/g, char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[char]);
    }

    function formatDate(timestamp) {
        const date = new Date(timestamp);
        if (Number.isNaN(date.getTime())) return 'agora';
        return new Intl.DateTimeFormat('pt-BR', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' }).format(date);
    }

    function render() {
        const filtered = posts.filter(post => activeFilter === 'all' || (isTraining ? post.category === activeFilter : post.type === activeFilter));
        if (!filtered.length) {
            const message = isTraining
                ? 'Seja a primeira pessoa a compartilhar um treino.'
                : 'Compartilhe uma ideia ou publique uma oportunidade para sua rede.';
            feed.innerHTML = `<div class="feed-empty"><i class="fa-solid ${isTraining ? 'fa-dumbbell' : 'fa-users'}"></i><strong>A conversa começa com você</strong><p>${message}</p></div>`;
            return;
        }
        feed.innerHTML = filtered.map(post => {
            const opportunity = !isTraining && post.type === 'opportunity';
            const liked = Boolean(post.likedByMe);
            const name = escapeHtml(post.author || 'Membro Next Agon');
            const avatar = escapeHtml(post.avatar || String(post.author || 'N').trim().split(/\s+/).slice(0, 2).map(part => part[0]).join('').toUpperCase());
            const title = post.title ? `<h3 class="post-title">${escapeHtml(post.title)}</h3>` : '';
            const details = opportunity ? `<div class="opportunity-details"><span><i class="fa-solid fa-location-dot"></i>${escapeHtml(post.location || 'Local a combinar')}</span><span><i class="fa-solid fa-briefcase"></i>${escapeHtml(post.employmentType || 'Oportunidade')}</span></div>` : '';
            const label = isTraining ? escapeHtml(post.category || 'Treino') : opportunity ? 'Contratação' : 'Rede fitness';
            const role = roleLabels[String(post.role || '').toLowerCase()] || escapeHtml(post.role || 'Membro');
            return `<article class="feed-post">
                <div class="post-head"><div class="post-avatar">${avatar}</div><div class="post-author"><strong>${name}</strong><span>${role} · ${formatDate(post.createdAt)}</span></div><span class="post-kind ${opportunity ? 'opportunity' : ''}">${label}</span></div>
                ${title}${details}<p class="post-text">${escapeHtml(post.text)}</p>
                <div class="post-actions"><button type="button" class="post-action ${liked ? 'liked' : ''}" data-like="${escapeHtml(post.id)}" aria-pressed="${liked}"><i class="fa-${liked ? 'solid' : 'regular'} fa-thumbs-up"></i>${liked ? 'Gostei' : 'Curtir'} · ${Number(post.likeCount) || 0}</button><span class="post-category">${isTraining ? 'TREINO' : opportunity ? 'OPORTUNIDADE' : 'COMUNIDADE'}</span></div>
            </article>`;
        }).join('');
    }

    async function loadPosts() {
        feed.innerHTML = '<div class="feed-empty"><i class="fa-solid fa-spinner fa-spin"></i><strong>Carregando publicações</strong><p>Buscando atualizações da comunidade.</p></div>';
        try {
            posts = await NextagonApi.listCommunityPosts(kind);
            if (!Array.isArray(posts)) posts = [];
            render();
        } catch (error) {
            feed.innerHTML = `<div class="feed-empty"><i class="fa-solid fa-triangle-exclamation"></i><strong>Não foi possível carregar o feed</strong><p>${escapeHtml(error.message || 'Verifique a conexão e tente novamente.')}</p><button class="aside-action" type="button" data-retry-feed>Tentar novamente</button></div>`;
        }
    }

    function setComposerIdentity() {
        document.querySelectorAll('#composerAvatar,#networkAvatar').forEach(element => element.textContent = initials);
        const composerName = document.getElementById('composerName');
        const networkName = document.getElementById('networkName');
        if (composerName) composerName.textContent = isTraining ? `Compartilhe seu treino, ${authorName.split(' ')[0]}` : `Compartilhe com a rede, ${authorName.split(' ')[0]}`;
        if (networkName) networkName.textContent = authorName;
    }

    function toggleOpportunityFields() {
        const isOpportunity = typeField?.value === 'opportunity';
        if (opportunityFields) opportunityFields.hidden = !isOpportunity;
        if (titleField) titleField.required = isTraining || isOpportunity;
        if (textField) textField.placeholder = isTraining
            ? 'Como foi o treino? Compartilhe exercícios, dicas ou aprendizados...'
            : isOpportunity
                ? 'Descreva a oportunidade, requisitos e como se candidatar...'
                : 'Compartilhe uma experiência, conhecimento ou novidade sobre exercícios e saúde...';
    }

    form.addEventListener('submit', async event => {
        event.preventDefault();
        const type = isTraining ? 'training' : typeField.value;
        const title = titleField?.value.trim() || '';
        const text = textField.value.trim();
        const location = document.getElementById('postLocation')?.value.trim() || '';
        if (!text || (isTraining && !title) || (type === 'opportunity' && (!title || !location))) {
            if (typeof showToast === 'function') showToast('Preencha os campos obrigatórios.', 'warn');
            return;
        }

        const submit = form.querySelector('[type="submit"]');
        if (submit) { submit.disabled = true; submit.dataset.label = submit.innerHTML; submit.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Publicando'; }
        try {
            const post = await NextagonApi.createCommunityPost({
                feed: kind,
                type,
                title,
                text,
                category: isTraining ? document.getElementById('workoutDiscipline').value : '',
                location,
                employmentType: document.getElementById('postEmployment')?.value || ''
            });
            posts = [post, ...posts];
            form.reset();
            toggleOpportunityFields();
            render();
            if (typeof showToast === 'function') showToast(isTraining ? 'Treino publicado para a comunidade.' : 'Publicação compartilhada com a rede.', 'success');
        } catch (error) {
            if (typeof showToast === 'function') showToast(error.message || 'Não foi possível publicar.', 'err');
        } finally {
            if (submit) { submit.disabled = false; submit.innerHTML = submit.dataset.label || 'Publicar'; delete submit.dataset.label; }
        }
    });

    feed.addEventListener('click', async event => {
        if (event.target.closest('[data-retry-feed]')) { loadPosts(); return; }
        const button = event.target.closest('[data-like]');
        if (!button) return;
        button.disabled = true;
        try {
            const updated = await NextagonApi.toggleCommunityLike(button.dataset.like);
            posts = posts.map(post => post.id === updated.id ? updated : post);
            render();
        } catch (error) {
            button.disabled = false;
            if (typeof showToast === 'function') showToast(error.message || 'Não foi possível registrar sua curtida.', 'err');
        }
    });

    document.querySelectorAll('.feed-filters [data-filter]').forEach(button => button.addEventListener('click', () => {
        activeFilter = button.dataset.filter;
        button.parentElement.querySelectorAll('.filter-chip').forEach(chip => chip.classList.toggle('active', chip === button));
        render();
    }));

    typeField?.addEventListener('change', toggleOpportunityFields);
    document.getElementById('createOpportunity')?.addEventListener('click', () => {
        typeField.value = 'opportunity';
        toggleOpportunityFields();
        form.scrollIntoView({ behavior: 'smooth', block: 'center' });
        titleField?.focus({ preventScroll: true });
    });
    setComposerIdentity();
    toggleOpportunityFields();
    loadPosts();
})();
