import httpClient, { setAccessToken } from '@/lib/axios';
import type { User, AuthPayload } from '@/types';

const authService = {
  register: async (email: string, password: string, displayName: string): Promise<AuthPayload> => {
    const res = await httpClient.post<AuthPayload>('/api/auth/register', { email, password, fullName: displayName });
    setAccessToken(res.data.accessToken);
    return res.data;
  },
  login: async (email: string, password: string): Promise<AuthPayload> => {
    const res = await httpClient.post<AuthPayload>('/api/auth/login', { email, password });
    setAccessToken(res.data.accessToken);
    return res.data;
  },
  forgotPassword: async (email: string): Promise<void> => {
    await httpClient.post('/api/auth/forgot-password', { email });
  },
  resetPassword: async (token: string, newPassword: string): Promise<void> => {
    await httpClient.post('/api/auth/reset-password', { token, newPassword });
  },
  me: async (): Promise<User> => {
    const res = await httpClient.get<User>('/api/auth/me');
    return res.data;
  },
  logout: async (): Promise<void> => {
    await httpClient.post('/api/auth/logout');
    setAccessToken(null);
  },
  refresh: async (): Promise<AuthPayload> => {
    const res = await httpClient.post<AuthPayload>('/api/auth/refresh');
    setAccessToken(res.data.accessToken);
    return res.data;
  },
};

export default authService;
