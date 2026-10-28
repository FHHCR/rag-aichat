import http from './http';
import type { ApiResponse } from '../types/api';

export interface ComponentStatus {
  name: string;
  status: string;
  detail: string | null;
}

export interface SystemStatus {
  appVersion: string;
  timestamp: string | null;
  components: ComponentStatus[];
}

export function getSystemStatus() {
  return http.get<ApiResponse<SystemStatus>>('/admin/system/status').then((response) => response.data.data);
}
