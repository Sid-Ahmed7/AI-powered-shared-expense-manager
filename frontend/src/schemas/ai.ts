import { z } from 'zod';

export const parseSchema = z.object({
  text: z.string().min(1).max(500),
});

export const askSchema = z.object({
  question: z.string().min(1).max(500),
  history: z
    .array(
      z.object({
        role: z.enum(['user', 'assistant']),
        content: z.string().min(1).max(2000),
      }),
    )
    .max(10)
    .optional(),
});

export const reminderSchema = z.object({
  fromMemberId: z.string().uuid(),
  toMemberId: z.string().uuid(),
  amountCents: z.number().positive(),
  tone: z.string().optional(),
});

export const chatInputSchema = z.object({
  question: z.string().min(1).max(500),
});

export type ParseInput = z.infer<typeof parseSchema>;
export type AskInput = z.infer<typeof askSchema>;
export type ReminderInput = z.infer<typeof reminderSchema>;
export type ChatInputData = z.infer<typeof chatInputSchema>;
