import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { setAccessToken } from '@/lib/axios';
import { authService } from '@/services';
import type { Login, Register } from '@/types';

export function useMe() {
  return useQuery({
    queryKey: ['me'],
    queryFn: authService.me,
    retry: false,
    staleTime: 5 * 60 * 1000,
  });
}

export function useLogin() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ email, password }: Login) =>
      authService.login(email, password),
    onSuccess: data => {
      queryClient.setQueryData(['me'], data.user);
    },
  });
}

export function useRegister() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ email, password, fullName }: Register ) =>
      authService.register(email, password, fullName),
    onSuccess: data => {
      queryClient.setQueryData(['me'], data.user);
    },
  });
}

export function useLogout() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: authService.logout,
    onSettled: () => {
      setAccessToken(null);
      queryClient.clear();
    },
  });
}

export function useRefresh() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: authService.refresh,
    onSuccess: data => {
      queryClient.setQueryData(['me'], data.user);
    },
    onError: () => {
      setAccessToken(null);
    },
  });
}
