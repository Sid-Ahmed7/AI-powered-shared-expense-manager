import { z } from 'zod';

const splitValueSchema = z.object({
  memberId: z.string().uuid(),
  value: z.number().min(0).max(1000000),
});

const itemSchema = z.object({
  label: z.string().min(1).max(120),
  amount: z.number().min(0).max(1000000),
  memberIds: z.array(z.string().uuid()).min(1).max(50),
});

export const expenseSchema = z.object({
  description: z.string().min(1).max(200),
  amount: z.number().min(0.01).max(1000000),
  payerMemberId: z.string().uuid(),
  category: z.string().optional(),
  splitMode: z.string(),
  date: z.string().optional(),
  splits: z.array(splitValueSchema).max(50).optional(),
  items: z.array(itemSchema).max(200).optional(),
  source: z.string().optional(),
  version: z.number().optional(),
  force: z.boolean().optional(),
});

export const expenseFormSchema = z.object({
  description: z.string().min(1).max(200),
  amount: z.string().min(1),
  payerMemberId: z.string().min(1),
  category: z.string().min(1),
  date: z.string().min(1),
});

export type ExpenseInput = z.infer<typeof expenseSchema>;
export type ExpenseFormInput = z.infer<typeof expenseFormSchema>;
