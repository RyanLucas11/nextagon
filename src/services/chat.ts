import { Platform } from 'react-native';

export type AppUser = {
  id: string;
  name: string;
  email: string;
  password: string;
  role: 'Atleta' | 'Profissional';
  headline: string;
  location: string;
  bio: string;
};

export type Conversation = {
  id: string;
  participantIds: [string, string];
  updatedAt: string;
};

export type ChatMessage = {
  id: string;
  conversationId: string;
  senderId: string;
  text: string;
  createdAt: string;
  readAt?: string;
};

type Database = { users: AppUser[]; conversations: Conversation[]; messages: ChatMessage[] };
const KEY = 'nextagon-community-db-v1';
let memory: Database = { users: [], conversations: [], messages: [] };
let activeUser: AppUser | null = null;

function database(): Database {
  if (Platform.OS !== 'web' || typeof localStorage === 'undefined') return memory;
  try {
    const value = localStorage.getItem(KEY);
    return value ? JSON.parse(value) as Database : memory;
  } catch { return memory; }
}
function save(value: Database) {
  memory = value;
  if (Platform.OS === 'web' && typeof localStorage !== 'undefined') localStorage.setItem(KEY, JSON.stringify(value));
}

export const AuthService = {
  register(input: Omit<AppUser, 'id' | 'headline' | 'location' | 'bio'>) {
    const db = database();
    if (db.users.some((user) => user.email.toLowerCase() === input.email.toLowerCase())) throw new Error('Este e-mail já está cadastrado.');
    const user: AppUser = { ...input, id: `user-${Date.now()}`, headline: input.role === 'Profissional' ? 'Profissional da comunidade fitness' : 'Membro da comunidade fitness', location: '', bio: '' };
    save({ ...db, users: [...db.users, user] });
    return user;
  },
  login(email: string, password: string) {
    const user = database().users.find((item) => item.email.toLowerCase() === email.toLowerCase() && item.password === password);
    if (!user) throw new Error('E-mail ou senha inválidos.');
    activeUser = user;
    return user;
  },
  active: () => activeUser,
  logout: () => { activeUser = null; },
  users: () => database().users,
  updateProfile(userId: string, patch: Partial<Pick<AppUser, 'name' | 'headline' | 'location' | 'bio'>>) {
    const db = database();
    const users = db.users.map((user) => user.id === userId ? { ...user, ...patch } : user);
    save({ ...db, users });
    const updated = users.find((user) => user.id === userId)!;
    if (activeUser?.id === userId) activeUser = updated;
    return updated;
  },
};

export const ConversationService = {
  list(userId: string) { return database().conversations.filter((item) => item.participantIds.includes(userId)).sort((a, b) => b.updatedAt.localeCompare(a.updatedAt)); },
  findOrCreate(userId: string, otherUserId: string) {
    const db = database();
    const found = db.conversations.find((item) => item.participantIds.includes(userId) && item.participantIds.includes(otherUserId));
    if (found) return found;
    const conversation = { id: `conversation-${Date.now()}`, participantIds: [userId, otherUserId] as [string, string], updatedAt: new Date().toISOString() };
    save({ ...db, conversations: [...db.conversations, conversation] });
    return conversation;
  },
};

export const MessageService = {
  list(conversationId: string) { return database().messages.filter((item) => item.conversationId === conversationId).sort((a, b) => a.createdAt.localeCompare(b.createdAt)); },
  send(conversationId: string, senderId: string, text: string) {
    const db = database();
    const message: ChatMessage = { id: `message-${Date.now()}`, conversationId, senderId, text: text.trim(), createdAt: new Date().toISOString() };
    save({ ...db, messages: [...db.messages, message], conversations: db.conversations.map((item) => item.id === conversationId ? { ...item, updatedAt: message.createdAt } : item) });
    return message;
  },
};
