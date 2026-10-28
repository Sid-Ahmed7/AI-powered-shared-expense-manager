import { registerSchema, type RegisterInput } from "@/schemas";
import { zodResolver } from "@hookform/resolvers/zod";
import { useState } from "react";
import { Controller, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import { FormLabel } from "@/components/ui/FormLabel";
import { InputWithIcon } from "@/components/ui/InputWithIcon";
import { ArrowRight, Mail, User } from "lucide-react";
import { PasswordInput } from "@/components/ui/PasswordInput";
import { PasswordStrength } from "@/components/PasswordStrength";
import { ErrorAlert } from "@/components/ui/ErrorAlert";
import { Button } from "@/components/ui/Button";
import { Link } from "react-router-dom";
import { ICON_SIZE } from "@/constants/components";

interface RegisterFormProps {
  onSubmit: (data: RegisterInput) => Promise<void>;
  from?: string;
}

export function RegisterForm({ onSubmit, from }: RegisterFormProps) {
  const { t } = useTranslation();
  const [serverError, setServerError] = useState<string | null>(null);

  const { register, handleSubmit, control, watch, formState: { errors, isSubmitting } } = useForm<RegisterInput>({ resolver: zodResolver(registerSchema) });

  const password = watch('password', '');

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
        <FormLabel>{t('auth.displayNameLabel')}</FormLabel>
        <InputWithIcon icon={User} autoComplete="given-name" placeholder={t('auth.displayNamePlaceholder')} {...register('fullName')} />
        {errors.fullName && <span className="text-[12px] text-[var(--neg)]">{errors.fullName.message}</span>}
      </label>

      <label className="flex flex-col gap-[7px]">
        <FormLabel>{t('auth.email')}</FormLabel>
        <InputWithIcon icon={Mail} type="email" autoComplete="email" placeholder={t('auth.emailPlaceholder')} {...register('email')} />
        {errors.email && <span className="text-[12px] text-[var(--neg)]">{errors.email.message}</span>}
      </label>

      <label className="flex flex-col gap-[7px]">
        <FormLabel>
          {t('auth.password')}
          <span className="font-medium normal-case tracking-normal ml-1.5 text-[var(--muted)]">
            {t('auth.passwordHint')}
          </span>
        </FormLabel>
        <Controller name="password" control={control} render={({ field }) => (
          <PasswordInput value={field.value ?? ''} onChange={field.onChange}
            autoComplete="new-password" hasError={!!errors.password} />
        )} />
        {errors.password && <span className="text-[12px] text-[var(--neg)]">{errors.password.message}</span>}
      </label>

      <PasswordStrength password={password} />

      <ErrorAlert message={serverError} />

      <Button fullWidth loading={isSubmitting} className="mt-1 !py-[13px]">
        {t('auth.registerBtn')}<ArrowRight size={ICON_SIZE} />
      </Button>

      <p className="mt-2 text-sm text-[var(--muted)] text-center">
        {t('auth.alreadyAccount')}{' '}
        <Link to="/login" state={{ from }} className="font-semibold">{t('auth.alreadyAccountLink')}</Link>
      </p>
    </form>
  );
}