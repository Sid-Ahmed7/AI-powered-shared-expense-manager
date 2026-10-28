import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { settlementService } from '@/services';
import type { CreateFromSuggestion, DeleteSettlement } from '@/types';

export function useBalances(groupId: string) {
  return useQuery({
    queryKey: ['balances', groupId],
    queryFn: () => settlementService.balances(groupId),
    enabled: !!groupId,
  });
}

export function useSettlements(groupId: string) {
  return useQuery({
    queryKey: ['settlements', groupId],
    queryFn: () => settlementService.settlements(groupId),
    enabled: !!groupId,
  });
}

export function useCreateFromSuggestion() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ groupId, transfer }: CreateFromSuggestion) =>
      settlementService.settleSuggestion(groupId, transfer),
    onSuccess: (_, { groupId }) => {
      queryClient.invalidateQueries({ queryKey: ['settlements', groupId] });
      queryClient.invalidateQueries({ queryKey: ['balances', groupId] });
    },
  });
}

export function useDeleteSettlement() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ groupId, settlementId }: DeleteSettlement) =>
      settlementService.deleteSettlement(groupId, settlementId),
    onSuccess: (_, { groupId }) => {
      queryClient.invalidateQueries({ queryKey: ['settlements', groupId] });
      queryClient.invalidateQueries({ queryKey: ['balances', groupId] });
    },
  });
}