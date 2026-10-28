import httpClient from '@/lib/axios';
import type { Balances, Settlement, Transfer } from '@/types';

const settlementService = {
  balances: async (groupId: string): Promise<Balances> => {
    const res = await httpClient.get<Balances>(`/api/groups/${groupId}/balances`);
    return res.data;
  },
  settlements: async (groupId: string): Promise<Settlement[]> => {
    const res = await httpClient.get<Settlement[]>(`/api/groups/${groupId}/settlements`);
    return res.data;
  },
  settleSuggestion: async (groupId: string, transfer: Transfer): Promise<Settlement> => {
    const res = await httpClient.post<Settlement>(`/api/groups/${groupId}/settlements/from-suggestion`, transfer);
    return res.data;
  },
  deleteSettlement: async (groupId: string, id: string): Promise<void> => {
    await httpClient.delete(`/api/groups/${groupId}/settlements/${id}`);
  },
};

export default settlementService;
