export type ApiUser = { id: string; name: string; email: string; role: 'ATHLETE' | 'PROFESSIONAL' | 'ADMIN' };
export type AuthResponse = { accessToken: string; refreshToken: string; user: ApiUser };
export type Professional = { id: string; name: string; email: string; bio: string | null; specialties: string[]; averageRating: number; totalReviews: number; hourlyRate: number | null; available: boolean };

const API_URL = process.env.EXPO_PUBLIC_API_URL || 'http://10.0.2.2:8080';
let accessToken: string | null = null;

export function setAccessToken(token: string | null) { accessToken = token; }

export async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers: Record<string, string> = { Accept: 'application/json', ...(init.headers as Record<string, string> || {}) };
  if (accessToken) headers.Authorization = `Bearer ${accessToken}`;
  if (init.body !== undefined) headers['Content-Type'] = 'application/json';
  let response: Response;
  try { response = await fetch(`${API_URL}${path}`, { ...init, headers }); }
  catch { throw new Error('Não foi possível conectar ao servidor.'); }
  if (response.status === 204) return undefined as T;
  const body = await response.json().catch(() => null);
  if (!response.ok) throw new Error(body?.message || body?.error || 'Não foi possível concluir a solicitação.');
  return body as T;
}

export const api = {
  login: (email: string, password: string) => request<AuthResponse>('/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) }),
  register: (name: string, email: string, password: string, role: 'ATHLETE' | 'PROFESSIONAL') => request<AuthResponse>('/auth/register', { method: 'POST', body: JSON.stringify({ name, email, password, role }) }),
  currentUser: () => request<ApiUser>('/users/me'),
  professionals: (page = 0, size = 20) => request<{ content: Professional[] }>(`/marketplace/professionals?page=${page}&size=${size}`),
  athleteProfile: () => request('/profile/athlete'),
  saveAthleteProfile: (body: unknown) => request('/profile/athlete', { method: 'PUT', body: JSON.stringify(body) }),
  professionalProfile: (id: string) => request<Professional>(`/profile/professional/${encodeURIComponent(id)}`),
  saveProfessionalProfile: (body: unknown) => request('/profile/professional', { method: 'PUT', body: JSON.stringify(body) }),
  athleteContracts: (id: string) => request<unknown[]>(`/contracts/athlete/${encodeURIComponent(id)}`),
  professionalContracts: (id: string) => request<unknown[]>(`/contracts/professional/${encodeURIComponent(id)}`),
  chatHistory: (id: string) => request<unknown[]>(`/chat/contract/${encodeURIComponent(id)}`),
  sendMessage: (contractId: string, content: string, attachmentUrl?: string) => request('/chat/send', { method: 'POST', body: JSON.stringify({ contractId, content, attachmentUrl: attachmentUrl || null }) }),
};
