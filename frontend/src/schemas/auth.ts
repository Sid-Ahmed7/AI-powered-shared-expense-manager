import { z } from 'zod';

export const loginSchema = z.object({
  email: z.email().max(100),
  password: z.string().min(12).max(72),
});

export const registerSchema = z.object({
  email: z.email().max(100),
  password: z.string().min(12).max(72),
  fullName: z.string().min(1).max(100),
});

export const forgotPasswordSchema = z.object({
  email: z.email().max(100),
});

export const changePasswordSchema = z.object({
  currentPassword: z.string().min(12).max(72),
  newPassword: z.string().min(12).max(72),
});

export const resetPasswordSchema = z.object({
  newPassword: z.string().min(12).max(72),
  confirmPassword: z.string(),
}).refine((d) => d.newPassword === d.confirmPassword, {
  path: ['confirmPassword'],
  message: 'Passwords do not match',
});

export const deleteAccountSchema = z.object({
  password: z.string().min(12).max(72),
});

export type LoginInput = z.infer<typeof loginSchema>;
export type RegisterInput = z.infer<typeof registerSchema>;
export type ForgotPasswordInput = z.infer<typeof forgotPasswordSchema>;
export type ResetPasswordInput  = z.infer<typeof resetPasswordSchema>;
export type ChangePasswordInput = z.infer<typeof changePasswordSchema>;
export type DeleteAccountInput = z.infer<typeof deleteAccountSchema>;
