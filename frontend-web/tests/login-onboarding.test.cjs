const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const root = path.resolve(__dirname, '..');

function storage() {
  const data = new Map();
  return {
    getItem: key => data.has(key) ? data.get(key) : null,
    setItem: (key, value) => data.set(key, String(value)),
    removeItem: key => data.delete(key),
  };
}

function loadAuth() {
  const elements = new Map();
  const document = {
    addEventListener() {},
    querySelectorAll() { return []; },
    getElementById(id) {
      if (!elements.has(id)) elements.set(id, { id, value: '', textContent: '', style: {}, classList: { add() {}, remove() {}, toggle() {} }, setAttribute() {} });
      return elements.get(id);
    },
  };
  const localStorage = storage();
  const sessionStorage = storage();
  const window = { location: { href: '' }, clearAuthTokens() {} };
  const context = vm.createContext({ document, localStorage, sessionStorage, window, console, Date, setTimeout, clearTimeout, FileReader: class {} });
  const source = fs.readFileSync(path.join(root, 'js', 'auth.js'), 'utf8');
  vm.runInContext(source, context, { filename: 'auth.js' });
  return { context, document, elements, localStorage, sessionStorage, window };
}

function loadApi(fetch) {
  const window = {};
  const context = vm.createContext({ window, fetch, AbortController, setTimeout, clearTimeout, console });
  const source = fs.readFileSync(path.join(root, 'js', 'api.js'), 'utf8');
  vm.runInContext(source, context, { filename: 'api.js' });
  return window.NextagonApi;
}

test('atleta pode avançar, escolher nome e concluir com respostas persistidas', () => {
  const { context, elements, localStorage } = loadAuth();
  const content = elements.get('onboarding-content') || context.document.getElementById('onboarding-content');
  Object.defineProperty(content, 'innerHTML', {
    set(markup) {
      this.markup = markup;
      for (const match of markup.matchAll(/<(?:input|textarea)[^>]*id="([^"]+)"[^>]*value="([^"]*)"/g)) {
        context.document.getElementById(match[1]).value = match[2];
      }
      for (const match of markup.matchAll(/<textarea[^>]*id="([^"]+)"[^>]*>([\s\S]*?)<\/textarea>/g)) {
        context.document.getElementById(match[1]).value = match[2];
      }
    },
  });

  context.startProfileOnboarding({ id: 'athlete-1', name: 'Pessoa Atleta', role: 'atleta' });
  context.document.getElementById('onboarding-sport').value = 'Musculação';
  context.document.getElementById('onboarding-focus').value = 'Força';
  context.document.getElementById('onboarding-goal').value = 'Hipertrofia';
  context.advanceOnboarding();
  context.document.getElementById('onboarding-nickname').value = 'Atleta';
  context.finishProfileOnboarding();

  const saved = JSON.parse(localStorage.getItem('na_onboarding_profiles'))['athlete-1'];
  assert.equal(saved.sport, 'Musculação');
  assert.equal(saved.focus, 'Força');
  assert.equal(saved.goal, 'Hipertrofia');
  assert.equal(saved.nickname, 'Atleta');
  assert.equal(context.window.location.href, 'dashboard.html');
});

test('profissional pode marcar especialidades e pular etapas sem quebrar a conclusão', () => {
  const { context, elements, localStorage } = loadAuth();
  const content = context.document.getElementById('onboarding-content');
  Object.defineProperty(content, 'innerHTML', { set(markup) { this.markup = markup; } });
  context.startProfileOnboarding({ id: 'pro-1', name: 'Profissional', role: 'profissional' });
  const option = { textContent: 'Fisioterapia', classList: { toggle() {} }, setAttribute() {} };
  context.toggleOnboardingSpecialty(option);
  context.skipOnboardingStep();
  context.skipOnboardingStep();

  const saved = JSON.parse(localStorage.getItem('na_onboarding_profiles'))['pro-1'];
  assert.deepEqual(Array.from(saved.specialties), ['Fisioterapia']);
  assert.equal(saved.role, 'profissional');
  assert.equal(context.window.location.href, 'dashboard.html');
});

test('login envia e-mail normalizado e mapeia resposta da API', async () => {
  let observed;
  const api = loadApi(async (url, options) => {
    observed = { url, options };
    return { ok: true, text: async () => JSON.stringify({ accessToken: 'token', refreshToken: 'refresh', user: { id: '1', name: 'A', email: 'a@b.com', role: 'ATHLETE' } }) };
  });
  const auth = await api.login(' A@B.COM ', 'senha');
  assert.equal(observed.url, 'https://nextagon-backend.onrender.com/auth/login');
  assert.deepEqual(JSON.parse(observed.options.body), { email: 'A@B.COM', password: 'senha' });
  assert.equal(auth.user.role, 'atleta');
  assert.equal(auth.accessToken, 'token');
});

test('aquecimento falho não impede o login de continuar disponível', async () => {
  let calls = 0;
  const api = loadApi(async () => { calls += 1; throw new Error('offline'); });
  await assert.doesNotReject(api.warmup());
  assert.equal(calls, 1);
});

test('login bloqueia envios duplicados e libera o botão após resposta', async () => {
  const { context, document } = loadAuth();
  let resolveLogin;
  let calls = 0;
  context.NextagonApi = { login() { calls += 1; return new Promise(resolve => { resolveLogin = resolve; }); } };
  context.setAuthTokens = () => {};
  context.document.getElementById('login-email').value = 'atleta@exemplo.com';
  context.document.getElementById('login-pass').value = 'senha-segura';
  context.selectRole('atleta');

  const pending = context.doLogin();
  const button = document.getElementById('login-submit');
  assert.equal(button.disabled, true);
  assert.equal(button.textContent, 'Conectando…');
  await context.doLogin();
  assert.equal(calls, 1);

  resolveLogin({ user: { id: 'athlete-1', role: 'atleta', name: 'Atleta', email: 'atleta@exemplo.com' } });
  await pending;
  assert.equal(button.disabled, false);
  assert.equal(button.textContent, 'Entrar');
});
