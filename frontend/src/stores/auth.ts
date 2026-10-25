import { defineStore } from 'pinia';

export type UserRole = 'USER' | 'ADMIN';

export interface UserInfo {
  id: string;
  username: string;
  displayName: string;
  role: UserRole;
}

const TOKEN_KEY = 'rag_aichat_token';
const USER_KEY = 'rag_aichat_user';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY),
    user: localStorage.getItem(USER_KEY) ? JSON.parse(localStorage.getItem(USER_KEY) || 'null') as UserInfo : null
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
    isAdmin: (state) => state.user?.role === 'ADMIN'
  },
  actions: {
    setAuth(token: string, user: UserInfo) {
      this.token = token;
      this.user = user;
      localStorage.setItem(TOKEN_KEY, token);
      localStorage.setItem(USER_KEY, JSON.stringify(user));
    },
    clearAuth() {
      this.token = null;
      this.user = null;
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
    }
  }
});
