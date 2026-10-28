import { loginSchema, type LoginInput } from "@/schemas";
import { zodResolver } from "@hookform/resolvers/zod";
import { useState } from "react";
import { Controller, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import { FormLabel } from "@/components/ui/FormLabel";
import { InputWithIcon } from "@/components/ui/InputWithIcon";
import { ArrowRight, Mail } from "lucide-react";
import { Link } from "react-router-dom";
import { PasswordInput } from "@/components/ui/PasswordInput";
import { ErrorAlert } from "@/components/ui/ErrorAlert";
import { Button } from "@/components/ui/Button";
import { ICON_SIZE } from "@/constants/components";

interface LoginFormProps {
  onSubmit: (data: LoginInput) => Promise<void>;
  from?: string;
}

export function LoginForm({ onSubmit, from }: LoginFormProps) {
  const { t } = useTranslation();
  const [serverError, setServerError] = useState<string | null>(null);

  const { register, handleSubmit, control, formState: { errors, isSubmitting } } = useForm<LoginInput>({ resolver: zodResolver(loginSchema) });

  const submit = handleSubmit(async (data) => {
    setServerError(null);
    try {
      await onSubmit(data);
    } catch (err) {
      setServerError((err as Error).message);
    }
  });

  return (
    <form className="flex flex-col gap-[14px]" onSubmit={submit}>
      <label className="flex flex-col gap-[7px]">
        <FormLabel>{t('auth.email')}</FormLabel>
        <InputWithIcon icon={Mail} type="email" autoComplete="email" placeholder={t('auth.emailPlaceholder')} {...register('email')} />
        {errors.email && <span className="text-[12px] text-[var(--neg)]">{errors.email.message}</span>}
      </label>

      <label className="flex flex-col gap-[7px]">
        <FormLabel right={
          <Link to="/forgot-password" className="font-medium normal-case tracking-normal text-[var(--muted)] hover:text-[var(--primary)] transition-colors">
            {t('auth.forgotPassword')}
          </Link>
        }>
          {t('auth.password')}
        </FormLabel>
        <Controller name="password" control={control} render={({ field }) => (
          <PasswordInput value={field.value ?? ''} onChange={field.onChange}
            autoComplete="current-password" hasError={!!errors.password} />
        )} />
        {errors.password && <span className="text-[12px] text-[var(--neg)]">{errors.password.message}</span>}
      </label>

      <ErrorAlert message={serverError} />

      <Button fullWidth loading={isSubmitting} className="mt-1 !py-[13px]">
        {t('auth.loginBtn')}<ArrowRight size={ICON_SIZE} />
      </Button>

      <p className="mt-2 text-sm text-[var(--muted)] text-center">
        {t('auth.noAccount')}{' '}
        <Link to="/register" state={{ from }} className="font-semibold">{t('auth.noAccountLink')}</Link>
      </p>
    </form>
  );
}