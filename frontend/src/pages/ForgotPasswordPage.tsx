import { useTranslation } from 'react-i18next';
import { api } from '@/api';
import { AuthPageLayout } from '@/components/layout/AuthPageLayout';
import { AuthCard } from '@/components/ui/AuthCard';
import { BrandedHeader } from '@/components/ui/BrandedHeader';
import { ForgotPasswordForm } from '@/components/form/ForgotPasswordForm';
import { usePageTitle } from '@/hooks/usePageTitle';

export function ForgotPasswordPage() {
  const { t } = useTranslation();
  usePageTitle(t('auth.forgotPasswordTitle'));

  return (
    <AuthPageLayout>
      <BrandedHeader />
      <AuthCard>
        <ForgotPasswordForm onSubmit={(data) => api.forgotPassword(data.email)} />
      </AuthCard>
    </AuthPageLayout>
  );
}