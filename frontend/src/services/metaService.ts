import httpClient from '@/lib/axios';
import type { Category, AiStatus } from '@/types';

const metaService = {
  categories: async (): Promise<Category[]> => {
    const res = await httpClient.get<Category[]>('/api/meta/categories');
    return res.data;
  },
  aiStatus: async (): Promise<AiStatus> => {
    const res = await httpClient.get<AiStatus>('/api/ai/status');
    return res.data;
  },
};

export default metaService;
