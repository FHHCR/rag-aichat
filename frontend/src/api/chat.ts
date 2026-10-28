import http from './http';
import type { ApiResponse } from '../types/api';
import { useAuthStore } from '../stores/auth';

export interface ChatSession {
  id: string;
  title: string;
  knowledgeBaseScope?: string | null;
  createdAt: string | null;
  updatedAt: string | null;
}

export interface ChatMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  sourceJson?: string | null;
  createdAt: string | null;
}

export interface ChatAskPayload {
  content: string;
  knowledgeBaseIds?: string[];
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

export function askChat(sessionId: string, content: string, knowledgeBaseIds?: string[]) {
  return http
    .post<ApiResponse<ChatMessage>>(`/chat/sessions/${sessionId}/ask`, {
      content,
      knowledgeBaseIds
    } satisfies ChatAskPayload)
    .then((response) => response.data.data);
}

export interface AskStreamHandlers {
  onChunk?: (chunk: string) => void;
  onDone?: (message: ChatMessage) => void;
  onError?: (message: string) => void;
}

export async function askChatStream(
  sessionId: string,
  content: string,
  knowledgeBaseIds: string[] | undefined,
  handlers: AskStreamHandlers
): Promise<void> {
  const authStore = useAuthStore();
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
  const url = `${baseUrl.replace(/\/+$/, '')}/chat/sessions/${sessionId}/ask`;
  const payload: ChatAskPayload = { content, knowledgeBaseIds };

  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'text/event-stream',
      ...(authStore.token ? { Authorization: `Bearer ${authStore.token}` } : {})
    },
    body: JSON.stringify(payload)
  });

  if (!response.ok) {
    let message = '发送问题失败';
    try {
      const data = await response.json();
      if (data?.message) message = data.message;
    } catch {
      // fall back to the default message
    }
    throw new Error(message);
  }

  if (!response.body) {
    throw new Error('当前浏览器不支持流式输出');
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let buffer = '';
  let eventName = 'message';
  let eventData = '';

  const dispatch = () => {
    const name = eventName;
    const data = eventData;
    eventName = 'message';
    eventData = '';
    if (!data) return;

    if (name === 'chunk') {
      try {
        const parsed = JSON.parse(data);
        if (parsed.content) handlers.onChunk?.(parsed.content);
      } catch {
        // ignore malformed chunk
      }
    } else if (name === 'done') {
      try {
        const parsed = JSON.parse(data);
        if (parsed.message) handlers.onDone?.(parsed.message);
      } catch {
        // ignore malformed done payload
      }
    } else if (name === 'error') {
      try {
        const parsed = JSON.parse(data);
        handlers.onError?.(parsed.message || '生成回答失败');
      } catch {
        handlers.onError?.('生成回答失败');
      }
    }
  };

  while (true) {
    const { done, value } = await reader.read();
    if (done) break;
    buffer += decoder.decode(value, { stream: true });

    let newlineIndex = buffer.indexOf('\n');
    while (newlineIndex >= 0) {
      const line = buffer.slice(0, newlineIndex).replace(/\r$/, '');
      buffer = buffer.slice(newlineIndex + 1);
      if (line.startsWith('event:')) {
        eventName = line.slice(6).trim();
      } else if (line.startsWith('data:')) {
        eventData += line.slice(5).trim();
      } else if (line === '') {
        dispatch();
      }
      newlineIndex = buffer.indexOf('\n');
    }
  }

  if (eventData) {
    dispatch();
  }
}
