import { z } from 'zod';

export const createGroupSchema = z.object({
  name: z.string().min(1).max(80),
  currency: z.string().optional(),
  members: z.array(z.string()).max(30).optional(),
});

export const updateGroupSchema = z.object({
  name: z.string().max(80).optional(),
});

export const addMemberSchema = z.object({
  name: z.string().min(1).max(40),
});

export const joinSchema = z.object({
  memberId: z.string().uuid().optional(),
});

export type CreateGroupInput = z.infer<typeof createGroupSchema>;
export type UpdateGroupInput = z.infer<typeof updateGroupSchema>;
export type AddMemberInput = z.infer<typeof addMemberSchema>;
export type JoinInput = z.infer<typeof joinSchema>;
