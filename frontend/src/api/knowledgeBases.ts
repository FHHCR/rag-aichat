import http from './http';
import type { ApiResponse } from '../types/api';

export interface KnowledgeBase {
  id: string;
  name: string;
  description: string | null;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface KnowledgeBaseInput {
  name: string;
  description?: string;
}

export function listKnowledgeBases() {
  return http.get<ApiResponse<KnowledgeBase[]>>('/admin/knowledge-bases').then((response) => response.data.data);
}

export function createKnowledgeBase(input: KnowledgeBaseInput) {
  return http.post<ApiResponse<KnowledgeBase>>('/admin/knowledge-bases', input).then((response) => response.data.data);
}

export function updateKnowledgeBase(id: string, input: KnowledgeBaseInput) {
  return http.put<ApiResponse<KnowledgeBase>>(`/admin/knowledge-bases/${id}`, input).then((response) => response.data.data);
}

export function deleteKnowledgeBase(id: string) {
  return http.delete<ApiResponse<null>>(`/admin/knowledge-bases/${id}`).then((response) => response.data.data);
}
