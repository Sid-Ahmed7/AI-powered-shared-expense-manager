import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { accountService } from '@/services';
import type { ChangePassword, DeleteAccount } from '@/types';

export function useSessions() {
  return useQuery({
    queryKey: ['sessions'],
    queryFn: accountService.sessions,
  });
}

export function useRevokeOthers() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: accountService.revokeOtherSessions,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['sessions'] });
    },
  });
}

export function useChangePassword() {
  return useMutation({
    mutationFn: ({ currentPassword, newPassword }: ChangePassword) =>
      accountService.changePassword(currentPassword, newPassword),
  });
}

export function useDeleteAccount() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ password }: DeleteAccount) => accountService.deleteAccount(password),
    onSuccess: () => {
      queryClient.clear();
    },
  });
}

export function useRevokeSession() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (sessionId: string) => accountService.revokeSession(sessionId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['sessions'] });
    },
  });
}
