import { api } from "@/api";
import { cls } from "@/lib/cls";
import { changePasswordSchema, type ChangePasswordInput } from "@/schemas";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation } from "@tanstack/react-query";
import { useState } from "react";
import { Controller, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import { CheckCircle, Lock } from "lucide-react";
import { ICON_SIZE } from "@/constants/components";
import { FormLabel } from "@/components/ui/FormLabel";
import { PasswordInput } from "@/components/ui/PasswordInput";
import { PasswordStrength } from "@/components/PasswordStrength";
import { ErrorAlert } from "@/components/ui/ErrorAlert";
import { Button } from "@/components/ui/Button";

interface ChangePasswordFormProps {
  onSuccess?: () => void;
}

export function ChangePasswordForm({ onSuccess }: ChangePasswordFormProps) {
  const { t } = useTranslation();
  const [serverError, setServerError] = useState<string | null>(null);

  const { handleSubmit, control, watch, reset, formState: { errors, isSubmitting } } = useForm<ChangePasswordInput>({ resolver: zodResolver(changePasswordSchema) });

  const newPassword = watch('newPassword', '');

  const change = useMutation({
    mutationFn: (data: ChangePasswordInput) => api.changePassword(data.currentPassword, data.newPassword),
    onSuccess: () => { reset(); onSuccess?.(); },
    onError: (err) => setServerError((err as Error).message),
  });

  const submit = handleSubmit((data) => {
    setServerError(null);
    change.mutate(data);
  });

  return (
    <form className={`${cls.cardPad} flex flex-col gap-4`} onSubmit={submit}>
      <h3><Lock size={ICON_SIZE} />{t('account.changePassword')}</h3>

      <label className="flex flex-col gap-[7px]">
        <FormLabel>{t('account.currentPassword')}</FormLabel>
        <Controller name="currentPassword" control={control} render={({ field }) => (
          <PasswordInput value={field.value ?? ''} onChange={field.onChange} autoComplete="current-password" hasError={!!errors.currentPassword} />
        )} />
        {errors.currentPassword && <span className="text-[12px] text-[var(--neg)]">{errors.currentPassword.message}</span>}
      </label>

      <label className="flex flex-col gap-[7px]">
        <FormLabel>{t('account.newPassword')}</FormLabel>
        <Controller name="newPassword" control={control} render={({ field }) => (
          <PasswordInput value={field.value ?? ''} onChange={field.onChange}
            autoComplete="new-password" hasError={!!errors.newPassword} />
        )} />
        {errors.newPassword && <span className="text-[12px] text-[var(--neg)]">{errors.newPassword.message}</span>}
      </label>

      <PasswordStrength password={newPassword} />

      <ErrorAlert message={serverError} />

      {change.data && (
        <div className="flex items-center gap-2 text-[var(--pos)] text-sm">
          <CheckCircle size={ICON_SIZE} />
          {t('account.passwordChangedRevoked', { count: change.data.revokedSessions })}
        </div>
      )}

      <Button loading={isSubmitting} className="self-start">
        {t('common.save')}
      </Button>
    </form>
  );
}