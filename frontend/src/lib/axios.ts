import axios, { type AxiosInstance } from 'axios';
import { ApiError } from '@/lib/ApiError';
import type { AuthPayload } from '@/types';
import { BASE, CLIENT_HEADER } from '@/constants/components';

let accessToken: string | null = null;
let refreshPromise: Promise<AuthPayload | null> | null = null;
let onSessionEnded: () => void = () => {};

export function setAccessToken(token: string | null): void {
  accessToken = token;
}

export function setSessionEndedHandler(handler: () => void): void {
  onSessionEnded = handler;
}

export function refreshSession(): Promise<AuthPayload | null> {
  if (!refreshPromise) {
    refreshPromise = doRefresh().finally(() => { refreshPromise = null; });
  }
  return refreshPromise;
}

const sleep = (ms: number) => new Promise((r) => setTimeout(r, ms));

const httpClient: AxiosInstance = axios.create({
  baseURL: BASE,
  withCredentials: true,
  headers: CLIENT_HEADER,
});

httpClient.interceptors.request.use((config) => {
  if (accessToken) config.headers.Authorization = `Bearer ${accessToken}`;
  return config;
});

httpClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config as typeof error.config & { _retry?: boolean };
    const status: number = error.response?.status ?? 0;

    if (status === 401 && !original._retry && !original.url?.startsWith('/api/auth/')) {
      original._retry = true;
      const payload = await refreshSession();
      if (payload) {
        original.headers.Authorization = `Bearer ${payload.accessToken}`;
        return httpClient(original);
      }
      onSessionEnded();
    }

    const body = error.response?.data as Record<string, unknown> | undefined;
    const retryAfter = error.response?.headers?.['retry-after'] as string | undefined;
    const fallback = status === 429 && retryAfter ? `Too many requests, retry in ${retryAfter}s` : 'Unexpected error';
    throw new ApiError(status, (body?.detail as string) ?? fallback, body);
  },
);

async function doRefresh(attempt = 0): Promise<AuthPayload | null> {
  try {
    const res = await axios.post<AuthPayload>(
      BASE + '/api/auth/refresh',
      null,
      { withCredentials: true, headers: CLIENT_HEADER },
    );
    setAccessToken(res.data.accessToken);
    return res.data;
  } catch (err) {
    if (axios.isAxiosError(err) && err.response?.status === 409 && attempt < 2) {
      await sleep(300 + Math.random() * 300);
      return doRefresh(attempt + 1);
    }
    setAccessToken(null);
    return null;
  }
}

export async function logout(): Promise<void> {
  try {
    await axios.post(BASE + '/api/auth/logout', null, { withCredentials: true, headers: CLIENT_HEADER });
  } finally {
    setAccessToken(null);
  }
}

export default httpClient;
