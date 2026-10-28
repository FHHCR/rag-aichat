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

export interface DocumentFile {
  id: string;
  knowledgeBaseId: string;
  fileName: string;
  originalPath: string | null;
  mimeType: string | null;
  fileSize: number | null;
  parser: string | null;
  status: string;
  parseError: string | null;
  chunkCount: number;
  uploadedById: string;
  createdAt: string;
  updatedAt: string;
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

export function listKnowledgeBaseFiles(knowledgeBaseId: string) {
  return http
    .get<ApiResponse<DocumentFile[]>>(`/admin/knowledge-bases/${knowledgeBaseId}/files`)
    .then((response) => response.data.data);
}

export function uploadKnowledgeBaseFile(knowledgeBaseId: string, file: File) {
  const formData = new FormData();
  formData.append('file', file);

  return http
    .post<ApiResponse<DocumentFile>>(`/admin/knowledge-bases/${knowledgeBaseId}/files`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 180000
    })
    .then((response) => response.data.data);
}

export function reindexKnowledgeBaseFile(knowledgeBaseId: string, fileId: string) {
  return http
    .post<ApiResponse<DocumentFile>>(`/admin/knowledge-bases/${knowledgeBaseId}/files/${fileId}/reindex`)
    .then((response) => response.data.data);
}

export function deleteKnowledgeBaseFile(knowledgeBaseId: string, fileId: string) {
  return http
    .delete<ApiResponse<void>>(`/admin/knowledge-bases/${knowledgeBaseId}/files/${fileId}`)
    .then((response) => response.data.data);
}
