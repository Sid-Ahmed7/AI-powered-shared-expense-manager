import httpClient from '@/lib/axios';
import type { Expense, ExpenseRequest, Page } from '@/types';

const expenseService = {
  expenses: async (groupId: string, params: { q?: string; category?: string; memberId?: string; page?: number; size?: number }): Promise<Page<Expense>> => {
    const search = new URLSearchParams();
    Object.entries(params).forEach(([k, v]) => v !== undefined && v !== '' && search.set(k, String(v)));
    const res = await httpClient.get<Page<Expense>>(`/api/groups/${groupId}/expenses?${search}`);
    return res.data;
  },
  createExpense: async (groupId: string, body: ExpenseRequest): Promise<Expense> => {
    const res = await httpClient.post<Expense>(`/api/groups/${groupId}/expenses`, body);
    return res.data;
  },
  updateExpense: async (groupId: string, id: string, body: ExpenseRequest): Promise<Expense> => {
    const res = await httpClient.put<Expense>(`/api/groups/${groupId}/expenses/${id}`, body);
    return res.data;
  },
  deleteExpense: async (groupId: string, id: string): Promise<void> => {
    await httpClient.delete(`/api/groups/${groupId}/expenses/${id}`);
  },
};

export default expenseService;
