import { z } from 'zod';

export const settlementSchema = z.object({
  fromMemberId: z.string().uuid(),
  toMemberId: z.string().uuid(),
  amount: z.number().min(0.01).max(1000000),
  date: z.string().optional(),
  note: z.string().max(200).optional(),
});

export type SettlementInput = z.infer<typeof settlementSchema>;
