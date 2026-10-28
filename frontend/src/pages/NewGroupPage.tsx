import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { ChevronLeft } from 'lucide-react';
import { CreateGroupForm } from '@/components/form/CreateGroupForm';
import { usePageTitle } from '@/hooks/usePageTitle';
import { BACK_LINK } from '@/constants/components';

export function NewGroupPage() {
  const { t } = useTranslation();
  usePageTitle(t('groups.newTitle'));

  return (
    <div className="max-w-xl mx-auto px-4 pb-[72px]">
      <header className="flex items-center justify-between gap-3 py-5 pb-4 sticky top-0 bg-[var(--bg)] z-10">
        <Link to="/" className={BACK_LINK}>
          <ChevronLeft size={18} />
          {t('nav.backToGroups')}
        </Link>
      </header>

      <div className="mb-5">
        <h1 className="mb-1.5">{t('groups.newTitle')}</h1>
        <p className="text-[var(--muted)] text-[13px]">{t('groups.newSubtitle')}</p>
      </div>

      <CreateGroupForm />
    </div>
  );
}