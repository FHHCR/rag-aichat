export interface UserInfo {
  id: string;
  username: string;
  displayName: string;
  role: 'USER' | 'ADMIN';
}

export interface LoginResult {
  token: string;
  user: UserInfo;
}
