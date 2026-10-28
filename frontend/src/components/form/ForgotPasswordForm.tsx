import { useState } from "react";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Mail, ArrowRight } from "lucide-react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { forgotPasswordSchema, type ForgotPasswordInput } from "@/schemas";
import { cls } from "@/lib/cls";
import { Button } from "@/components/ui/Button";
import { InputWithIcon } from "@/components/ui/InputWithIcon";
import { ErrorAlert } from "@/components/ui/ErrorAlert";
import { FormLabel } from "@/components/ui/FormLabel";
import { SuccessAlert } from "@/components/ui/SuccessAlert";

interface ForgotPasswordFormProps {
  onSubmit: (data: ForgotPasswordInput) => Promise<void>;
}

export function ForgotPasswordForm({ onSubmit }: ForgotPasswordFormProps) {
  const { t } = useTranslation();
  const [serverError, setServerError] = useState<string | null>(null);
  const [sent, setSent] = useState(false);

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<ForgotPasswordInput>({ resolver: zodResolver(forgotPasswordSchema) });

  const submit = handleSubmit(async (data) => {
    setServerError(null);
    try {
      await onSubmit(data);
      setSent(true);
    } catch (err) {
      setServerError((err as Error).message);
    }
  });

  if (sent) {
    return (
      <SuccessAlert
        title={t('auth.forgotPasswordSuccess')}
        body={t('auth.forgotPasswordSuccessBody')}
        action={<Link to="/login" className={`${cls.btnGhost} mt-2`}>{t('auth.backToLogin')}</Link>}
      />
    );
  }

  return (
    <>
      <div className="mb-[22px]">
        <h2 className="mb-1">{t('auth.forgotPasswordTitle')}</h2>
        <p className="text-[13px] text-[var(--muted)]">{t('auth.forgotPasswordSubtitle')}</p>
      </div>

      <form className="flex flex-col gap-[14px]" onSubmit={submit}>
        <label className="flex flex-col gap-[7px]">
          <FormLabel>{t('auth.email')}</FormLabel>
          <InputWithIcon icon={Mail} type="email" autoComplete="email"
            placeholder="toi@exemple.fr" {...register('email')} />
          {errors.email && <span className="text-[12px] text-[var(--neg)]">{errors.email.message}</span>}
        </label>

        <ErrorAlert message={serverError} />

        <Button fullWidth loading={isSubmitting} className="mt-1 !py-[13px]">
          {t('auth.forgotPasswordBtn')}<ArrowRight size={16} />
        </Button>
      </form>

      <p className="mt-5 text-sm text-[var(--muted)] text-center">
        <Link to="/login" className="font-semibold">{t('auth.backToLogin')}</Link>
      </p>
    </>
  );
}
