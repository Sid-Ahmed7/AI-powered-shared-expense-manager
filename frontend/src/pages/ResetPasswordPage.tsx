import { useTranslation } from 'react-i18next';
import { useSearchParams } from 'react-router-dom';
import { api } from '@/api';
import { AuthPageLayout } from '@/components/layout/AuthPageLayout';
import { AuthCard } from '@/components/ui/AuthCard';
import { BrandedHeader } from '@/components/ui/BrandedHeader';
import { ResetPasswordForm } from '@/components/form/ResetPasswordForm';
import { usePageTitle } from '@/hooks/usePageTitle';

export function ResetPasswordPage() {
  const { t } = useTranslation();
  usePageTitle(t('auth.resetPasswordTitle'));
  const [params] = useSearchParams();
  const token = params.get('token') ?? '';

  return (
    <AuthPageLayout>
      <BrandedHeader />
      <AuthCard>
        <ResetPasswordForm
          token={token}
          onSubmit={(tok, newPassword) => api.resetPassword(tok, newPassword)}
        />
      </AuthCard>
    </AuthPageLayout>
  );
}