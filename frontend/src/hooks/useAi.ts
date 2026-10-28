import { useMutation, useQuery } from '@tanstack/react-query';
import { aiService, metaService } from '@/services';
import type { Ask,  ParseExpense, Reminder, ScanReceipt} from '@/types';

export function useAiStatus() {
  return useQuery({
    queryKey: ['aiStatus'],
    queryFn: metaService.aiStatus,
    staleTime: 60 * 1000,
  });
}

export function useParseExpense() {
  return useMutation({
    mutationFn: ({ groupId, text }: ParseExpense) =>
      aiService.parseExpense(groupId, text),
  });
}

export function useScanReceipt() {
  return useMutation({
    mutationFn: ({ groupId, file }: ScanReceipt) =>
      aiService.scanReceipt(groupId, file),
  });
}

export function useAsk() {
  return useMutation({
    mutationFn: ({ groupId, question, history }: Ask ) =>
      aiService.ask(groupId, question, history ?? []),
  });
}

export function useRecap(groupId: string, month?: string) {
  return useQuery({
    queryKey: ['recap', groupId, month],
    queryFn: () => aiService.recap(groupId, month!),
    enabled: !!groupId && month !== undefined,
  });
}

export function useReminder() {
  return useMutation({
    mutationFn: ({groupId, fromMemberId, toMemberId, amountCents, tone = 'SYMPA'}: Reminder ) =>
      aiService.reminder(groupId, { fromMemberId, toMemberId, amountCents, tone }),
  });
}
