# 🏋️ Nextagon Mobile

Aplicativo mobile desenvolvido em **React Native** com **TypeScript** utilizando **Expo**. O projeto tem como objetivo conectar atletas e profissionais da área fitness, permitindo autenticação, gerenciamento de treinos, chat e acompanhamento de informações da plataforma.

---

## 📱 Funcionalidades

- Login de usuários
- Login de profissionais
- Área administrativa
- Dashboard
- Gerenciamento de treinos
- Lista de profissionais
- Lista de academias
- Chat entre usuários
- Interface moderna e responsiva
- Navegação utilizando Expo Router

---

## 🛠️ Tecnologias Utilizadas

- React Native
- TypeScript
- Expo SDK 57
- Expo Router
- React Native Safe Area Context
- Expo Image
- Expo Status Bar

---

## 📂 Estrutura do Projeto

```
src/
│
├── app/
│   ├── index.tsx
│   ├── Dashboard.tsx
│   ├── LoginUsuario.tsx
│   ├── LoginProfissional.tsx
│   ├── Admin.tsx
│   ├── Chat.tsx
│   ├── Treinos.tsx
│   ├── Profissionais.tsx
│   ├── Academia.tsx
│   └── _layout.tsx
│
├── components/
│   ├── Button.tsx
│   ├── Card.tsx
│   ├── Header.tsx
│   ├── Input.tsx
│   └── Loading.tsx
│
├── constants/
│   └── theme.ts
│
└── services/
    ├── api.ts
    ├── auth.ts
    ├── dashboard.ts
    ├── admin.ts
    └── chat.ts
```

---

## 🚀 Como executar o projeto

### 1. Clone o repositório

```bash
git clone https://github.com/RyanLucas11/nextagon.git
```

### 2. Entre na pasta

```bash
cd nextagon
```

### 3. Instale as dependências

```bash
npm install
```

ou

```bash
yarn
```

### 4. Execute o projeto

```bash
npx expo start
```

Depois disso, escolha uma das opções:

- Android Emulator
- iOS Simulator
- Expo Go
- Navegador Web

---

## 📌 Organização

O projeto segue uma arquitetura simples e organizada, separando:

- **app** → telas da aplicação
- **components** → componentes reutilizáveis
- **services** → comunicação com API e regras de negócio
- **constants** → cores, temas e constantes globais

Essa organização facilita a manutenção e a escalabilidade do sistema.

---

## 🎯 Objetivo

O Nextagon foi desenvolvido como projeto acadêmico com o objetivo de aplicar conceitos de desenvolvimento mobile utilizando React Native, consumo de APIs, componentização, organização de código e boas práticas de desenvolvimento.

---

## 👨‍💻 Desenvolvedor

**Ryan Lucas Fontenele Mendonça**

GitHub:
https://github.com/RyanLucas11

---