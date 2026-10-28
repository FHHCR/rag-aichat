import http from './http';
import type { ApiResponse } from '../types/api';

export interface AdminUser {
  id: string;
  username: string;
  displayName: string | null;
  role: 'USER' | 'ADMIN';
  status: 'ACTIVE' | 'DISABLED';
  createdAt: string;
  updatedAt: string;
}

export interface UpdateUserPayload {
  displayName?: string;
  role?: 'USER' | 'ADMIN';
  status?: 'ACTIVE' | 'DISABLED';
  password?: string;
}

export function listAdminUsers() {
  return http.get<ApiResponse<AdminUser[]>>('/admin/users').then((response) => response.data.data);
}

export function updateAdminUser(userId: string, payload: UpdateUserPayload) {
  return http
    .patch<ApiResponse<AdminUser>>(`/admin/users/${userId}`, payload)
    .then((response) => response.data.data);
}
