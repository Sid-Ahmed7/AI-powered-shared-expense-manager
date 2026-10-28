import httpClient from '@/lib/axios';
import type { SessionInfo, SecurityEventInfo } from '@/types';

const accountService = {
  sessions: async (): Promise<SessionInfo[]> => {
    const res = await httpClient.get<SessionInfo[]>('/api/account/sessions');
    return res.data;
  },
  revokeSession: async (id: string): Promise<void> => {
    await httpClient.delete(`/api/account/sessions/${id}`);
  },
  revokeOtherSessions: async (): Promise<{ revoked: number }> => {
    const res = await httpClient.post<{ revoked: number }>('/api/account/sessions/revoke-others');
    return res.data;
  },
  changePassword: async (currentPassword: string, newPassword: string): Promise<{ revokedSessions: number }> => {
    const res = await httpClient.post<{ revokedSessions: number }>('/api/account/password', { currentPassword, newPassword });
    return res.data;
  },
  securityEvents: async (): Promise<SecurityEventInfo[]> => {
    const res = await httpClient.get<SecurityEventInfo[]>('/api/account/security-events');
    return res.data;
  },
  exportData: async (): Promise<unknown> => {
    const res = await httpClient.get<unknown>('/api/account/export');
    return res.data;
  },
  deleteAccount: async (password: string): Promise<void> => {
    await httpClient.post('/api/account/delete', { password });
  },
};

export default accountService;
