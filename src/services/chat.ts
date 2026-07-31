import { Platform } from 'react-native';

export type ChatProfile = 'atleta' | 'profissional';

export type ChatMessage = {
  id: string;
  from: ChatProfile;
  text: string;
  createdAt: string;
};

const STORAGE_KEY = 'nextagon-chat-main';

const seedMessages: ChatMessage[] = [
  {
    id: 'seed-1',
    from: 'profissional',
    text: 'Seu treino B foi atualizado para esta semana.',
    createdAt: new Date(Date.now() - 1000 * 60 * 12).toISOString(),
  },
  {
    id: 'seed-2',
    from: 'atleta',
    text: 'Perfeito, vou iniciar hoje.',
    createdAt: new Date(Date.now() - 1000 * 60 * 8).toISOString(),
  },
  {
    id: 'seed-3',
    from: 'profissional',
    text: 'Me avise se sentir desconforto no ombro.',
    createdAt: new Date(Date.now() - 1000 * 60 * 4).toISOString(),
  },
];

let memoryMessages = seedMessages;

export function getChatMessages() {
  if (Platform.OS !== 'web' || typeof localStorage === 'undefined') {
    return memoryMessages;
  }

  const stored = localStorage.getItem(STORAGE_KEY);
  if (!stored) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(seedMessages));
    return seedMessages;
  }

  try {
    return JSON.parse(stored) as ChatMessage[];
  } catch {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(seedMessages));
    return seedMessages;
  }
}

export function saveChatMessages(messages: ChatMessage[]) {
  memoryMessages = messages;

  if (Platform.OS === 'web' && typeof localStorage !== 'undefined') {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(messages));
  }
}

export function createMessage(from: ChatProfile, text: string): ChatMessage {
  return {
    id: `${from}-${Date.now()}`,
    from,
    text: text.trim(),
    createdAt: new Date().toISOString(),
  };
}

export function clearChatMessages() {
  saveChatMessages(seedMessages);
  return seedMessages;
}
