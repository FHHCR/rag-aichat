import http from './http';
import type { ApiResponse } from '../types/api';
import type { LoginResult, UserInfo } from '../types/auth';

export function login(username: string, password: string) {
  return http.post<ApiResponse<LoginResult>>('/auth/login', { username, password }).then((response) => response.data.data);
}

export function register(input: { username: string; password: string; displayName: string; confirmPassword: string }) {
  return http.post<ApiResponse<LoginResult>>('/auth/register', input).then((response) => response.data.data);
}

export function fetchCurrentUser() {
  return http.get<ApiResponse<UserInfo>>('/auth/me').then((response) => response.data.data);
}
