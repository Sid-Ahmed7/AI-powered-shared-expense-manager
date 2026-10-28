import { useLocation, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { api } from '@/api';
import { safeRedirect, useAuth } from '@/context/auth';
import { AuthPageLayout } from '@/components/layout/AuthPageLayout';
import { AuthCard } from '@/components/ui/AuthCard';
import { BrandedHeader } from '@/components/ui/BrandedHeader';
import { LoginForm } from '@/components/form/LoginForm';
import { RegisterForm } from '@/components/form/RegisterForm';
import { usePageTitle } from '@/hooks/usePageTitle';
import type { LoginInput } from '@/schemas';
import type { RegisterInput } from '@/schemas';
import type { AuthMode } from '@/types/components';

export function AuthPage({ mode }: AuthMode) {
  const { t } = useTranslation();
  usePageTitle(mode === 'login' ? t('auth.login') : t('auth.register'));
  const { signIn } = useAuth();
  const navigate = useNavigate();
  const from = safeRedirect((useLocation().state as { from?: any } | null)?.from);

  const handleLogin = async (data: LoginInput) => {
    const res = await api.login(data.email, data.password);
    signIn(res);
    navigate(from, { replace: true });
  };

  const handleRegister = async (data: RegisterInput) => {
    const res = await api.register(data.email, data.password, data.fullName);
    signIn(res);
    navigate(from, { replace: true });
  };

  return (
    <AuthPageLayout>
      <BrandedHeader />
      <AuthCard>
        <div className="mb-[22px]">
          <h2 className="mb-1">
            {mode === 'login' ? t('auth.login') : t('auth.register')}
          </h2>
          <p className="text-[13px] text-[var(--muted)]">
            {mode === 'login' ? t('auth.loginSubtitle') : t('auth.registerSubtitle')}
          </p>
        </div>
        {mode === 'login' ? <LoginForm onSubmit={handleLogin} from={from} /> : <RegisterForm onSubmit={handleRegister} from={from} />}
      </AuthCard>
    </AuthPageLayout>
  );
}