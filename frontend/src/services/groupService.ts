import httpClient from '@/lib/axios';
import type { GroupSummary, GroupDetail, JoinPreview } from '@/types';

const groupService = {
  groups: async (): Promise<GroupSummary[]> => {
    const res = await httpClient.get<GroupSummary[]>('/api/groups');
    return res.data;
  },
  group: async (id: string): Promise<GroupDetail> => {
    const res = await httpClient.get<GroupDetail>(`/api/groups/${id}`);
    return res.data;
  },
  createGroup: async (body: { name: string; currency?: string; members: string[] }): Promise<GroupDetail> => {
    const res = await httpClient.post<GroupDetail>('/api/groups', body);
    return res.data;
  },
  deleteGroup: async (groupId: string): Promise<void> => {
    await httpClient.delete(`/api/groups/${groupId}`);
  },
  addMember: async (groupId: string, name: string): Promise<GroupDetail> => {
    const res = await httpClient.post<GroupDetail>(`/api/groups/${groupId}/members`, { name });
    return res.data;
  },
  regenerateInvite: async (groupId: string): Promise<GroupDetail> => {
    const res = await httpClient.post<GroupDetail>(`/api/groups/${groupId}/invite/regenerate`);
    return res.data;
  },
  joinPreview: async (code: string): Promise<JoinPreview> => {
    const res = await httpClient.get<JoinPreview>(`/api/join/${code}`);
    return res.data;
  },
  join: async (code: string, memberId?: string): Promise<GroupDetail> => {
    const res = await httpClient.post<GroupDetail>(`/api/join/${code}`, { memberId });
    return res.data;
  },
};

export default groupService;
