import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { groupService } from '@/services';
import type { AddMember, JoinGroup } from '@/types';

export function useGroups() {
  return useQuery({
    queryKey: ['groups'],
    queryFn: groupService.groups,
  });
}

export function useGroup(groupId: string) {
  return useQuery({
    queryKey: ['groups', groupId],
    queryFn: () => groupService.group(groupId),
    enabled: !!groupId,
  });
}

export function useCreateGroup() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: groupService.createGroup,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['groups'] });
    },
  });
}

export function useDeleteGroup() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (groupId: string) => groupService.deleteGroup(groupId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['groups'] });
    },
  });
}

export function useAddMember() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ groupId, name }: AddMember) =>
      groupService.addMember(groupId, name),
    onSuccess: (_, { groupId }) => {
      queryClient.invalidateQueries({ queryKey: ['groups', groupId] });
    },
  });
}

export function useRegenerateInvite() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (groupId: string) => groupService.regenerateInvite(groupId),
    onSuccess: (_, groupId) => {
      queryClient.invalidateQueries({ queryKey: ['groups', groupId] });
    },
  });
}

export function useJoinPreview(code: string) {
  return useQuery({
    queryKey: ['joinPreview', code],
    queryFn: () => groupService.joinPreview(code),
    enabled: !!code,
  });
}

export function useJoin() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ code, memberId }: JoinGroup) =>
      groupService.join(code, memberId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['groups'] });
    },
  });
}