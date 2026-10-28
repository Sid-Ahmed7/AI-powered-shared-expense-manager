import httpClient from '@/lib/axios';
import type { ExpenseDraft, ReceiptDraft, ChatTurn, RecapResponse, Tone } from '@/types';

const aiService = {
  parseExpense: async (groupId: string, text: string): Promise<ExpenseDraft> => {
    const res = await httpClient.post<ExpenseDraft>(`/api/groups/${groupId}/ai/parse-expense`, { text });
    return res.data;
  },
  scanReceipt: async (groupId: string, file: File): Promise<ReceiptDraft> => {
    const form = new FormData();
    form.append('file', file);
    const res = await httpClient.post<ReceiptDraft>(`/api/groups/${groupId}/ai/scan-receipt`, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return res.data;
  },
  ask: async (groupId: string, question: string, history: ChatTurn[]): Promise<{ answer: string; toolsUsed: string[] }> => {
    const res = await httpClient.post<{ answer: string; toolsUsed: string[] }>(`/api/groups/${groupId}/ai/ask`, { question, history });
    return res.data;
  },
  recap: async (groupId: string, month: string): Promise<RecapResponse> => {
    const res = await httpClient.get<RecapResponse>(`/api/groups/${groupId}/ai/recap?month=${month}`);
    return res.data;
  },
  reminder: async (groupId: string, body: { fromMemberId: string; toMemberId: string; amountCents: number; tone: Tone }): Promise<{ message: string }> => {
    const res = await httpClient.post<{ message: string }>(`/api/groups/${groupId}/ai/reminder`, body);
    return res.data;
  },
};

export default aiService;
