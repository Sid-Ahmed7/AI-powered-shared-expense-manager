import type { GroupDetail } from '@/api';


export function memberName(group: GroupDetail, id: string): string {
  return group.members.find((member) => member.id === id)?.name ?? '?';
}
