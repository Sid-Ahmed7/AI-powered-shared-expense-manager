import { useState } from "react";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { ArrowRight } from "lucide-react";
import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { resetPasswordSchema, type ResetPasswordInput } from "@/schemas";
import { cls } from "@/lib/cls";
import { Button } from "@/components/ui/Button";
import { PasswordInput } from "@/components/ui/PasswordInput";
import { ErrorAlert } from "@/components/ui/ErrorAlert";
import { FormLabel } from "@/components/ui/FormLabel";
import { PasswordStrength } from "@/components/PasswordStrength";
import { SuccessAlert } from "@/components/ui/SuccessAlert";

interface ResetPasswordFormProps {
  token: string;
  onSubmit: (token: string, newPassword: string) => Promise<void>;
}

export function ResetPasswordForm({ token, onSubmit }: ResetPasswordFormProps) {
  const { t } = useTranslation();
  const [serverError, setServerError] = useState<string | null>(null);
  const [done, setDone] = useState(false);

  const { handleSubmit, control, watch, formState: { errors, isSubmitting } } = useForm<ResetPasswordInput>({ resolver: zodResolver(resetPasswordSchema) });

  const newPassword = watch('newPassword', '');

  const submit = handleSubmit(async (data) => {
    setServerError(null);
    try {
      await onSubmit(token, data.newPassword);
      setDone(true);
    } catch (err) {
      setServerError((err as Error).message);
    }
  });

  if (!token) {
    return (
      <div className="flex flex-col items-center gap-4 py-4 text-center">
        <p className="text-[var(--neg)]">{t('auth.resetPasswordInvalidToken')}</p>
        <Link to="/forgot-password" className={cls.btnGhost}>{t('auth.forgotPasswordTitle')}</Link>
      </div>
    );
  }

  if (done) {
    return (
      <SuccessAlert
        title={t('auth.resetPasswordSuccess')}
        body={t('auth.resetPasswordSuccessBody')}
        action={
          <Link to="/login" className={`${cls.btnPrimary} mt-2`}>
            {t('auth.loginBtn')}<ArrowRight size={16} />
          </Link>
        }
      />
    );
  }

  return (
    <>
      <div className="mb-[22px]">
        <h2 className="mb-1">{t('auth.resetPasswordTitle')}</h2>
        <p className="text-[13px] text-[var(--muted)]">{t('auth.resetPasswordSubtitle')}</p>
      </div>

      <form className="flex flex-col gap-[14px]" onSubmit={submit}>
        <label className="flex flex-col gap-[7px]">
          <FormLabel>
            {t('auth.newPasswordLabel')}
            <span className="font-medium normal-case tracking-normal ml-1.5">{t('auth.passwordHint')}</span>
          </FormLabel>
          <Controller name="newPassword" control={control} render={({ field }) => (
            <PasswordInput value={field.value ?? ''} onChange={field.onChange} autoComplete="new-password" hasError={!!errors.newPassword} />
          )} />
          {errors.newPassword && <span className="text-[12px] text-[var(--neg)]">{errors.newPassword.message}</span>}
        </label>

        <PasswordStrength password={newPassword} />

        <label className="flex flex-col gap-[7px]">
          <FormLabel>{t('auth.confirmPasswordLabel')}</FormLabel>
          <Controller name="confirmPassword" control={control} render={({ field }) => (
            <PasswordInput value={field.value ?? ''} onChange={field.onChange} autoComplete="new-password" placeholder={t('auth.confirmPasswordPlaceholder')} hasError={!!errors.confirmPassword} />)} />
          {errors.confirmPassword && <span className="text-[12px] text-[var(--neg)]">{errors.confirmPassword.message}</span>}
        </label>

        <ErrorAlert message={serverError} />

        <Button fullWidth loading={isSubmitting} className="mt-1 !py-[13px]">
          {t('auth.resetPasswordBtn')}<ArrowRight size={16} />
        </Button>
      </form>

      <p className="mt-5 text-sm text-[var(--muted)] text-center">
        <Link to="/login" className="font-semibold">{t('auth.backToLogin')}</Link>
      </p>
    </>
  );
}
