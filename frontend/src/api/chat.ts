import http from './http';
import type { ApiResponse } from '../types/api';

export interface ChatSession {
  id: string;
  title: string;
  createdAt: string | null;
  updatedAt: string | null;
}

export interface ChatMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  createdAt: string | null;
}

export function fetchChatSessions() {
  return http.get<ApiResponse<ChatSession[]>>('/chat/sessions').then((response) => response.data.data);
}

export function createChatSession(title?: string) {
  return http
    .post<ApiResponse<ChatSession>>('/chat/sessions', title ? { title } : {})
    .then((response) => response.data.data);
}

export function deleteChatSession(sessionId: string) {
  return http.delete<ApiResponse<void>>(`/chat/sessions/${sessionId}`).then((response) => response.data.data);
}

export function fetchChatMessages(sessionId: string) {
  return http
    .get<ApiResponse<ChatMessage[]>>(`/chat/sessions/${sessionId}/messages`)
    .then((response) => response.data.data);
}

export function createChatMessage(sessionId: string, role: 'user' | 'assistant', content: string) {
  return http
    .post<ApiResponse<ChatMessage>>(`/chat/sessions/${sessionId}/messages`, { role, content })
    .then((response) => response.data.data);
}
