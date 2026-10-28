import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { expenseService } from '@/services';
import type { ExpenseListParams, CreateExpense, UpdateExpense, DeleteExpense } from '@/types';

export function useExpenses(groupId: string, params?: ExpenseListParams) {
  return useQuery({
    queryKey: ['expenses', groupId, params],
    queryFn: () => expenseService.expenses(groupId, params ?? {}),
    enabled: !!groupId,
  });
}

export function useCreateExpense() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ groupId, data }: CreateExpense) =>
      expenseService.createExpense(groupId, data),
    onSuccess: (_, { groupId }) => {
      queryClient.invalidateQueries({ queryKey: ['expenses', groupId] });
      queryClient.invalidateQueries({ queryKey: ['balances', groupId] });
    },
  });
}

export function useUpdateExpense() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ groupId, expenseId, data }: UpdateExpense) =>
      expenseService.updateExpense(groupId, expenseId, data),
    onSuccess: (_, { groupId }) => {
      queryClient.invalidateQueries({ queryKey: ['expenses', groupId] });
      queryClient.invalidateQueries({ queryKey: ['balances', groupId] });
    },
  });
}

export function useDeleteExpense() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ groupId, expenseId }: DeleteExpense) =>
      expenseService.deleteExpense(groupId, expenseId),
    onSuccess: (_, { groupId }) => {
      queryClient.invalidateQueries({ queryKey: ['expenses', groupId] });
      queryClient.invalidateQueries({ queryKey: ['balances', groupId] });
    },
  });
}