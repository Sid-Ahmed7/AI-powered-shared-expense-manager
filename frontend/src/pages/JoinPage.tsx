import { Link, useNavigate, useParams } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { ChevronLeft, UserPlus, Users, AlertTriangle } from 'lucide-react';
import { api } from '@/api';
import { cls } from '@/lib/cls';
import { usePageTitle } from '@/hooks/usePageTitle';
import { Button } from '@/components/ui/Button';
import { BACK_LINK } from '@/constants/components';

export function JoinPage() {
  const { t } = useTranslation();
  const code = useParams().code!;
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const preview = useQuery({ queryKey: ['join', code], queryFn: () => api.joinPreview(code), retry: false });
  usePageTitle(preview.data?.groupName ?? '');

  const join = useMutation({
    mutationFn: (memberId?: string) => api.join(code, memberId),
    onSuccess: (group) => {
      void queryClient.invalidateQueries({ queryKey: ['groups'] });
      navigate(`/groups/${group.id}`);
    },
  });

  return (
    <div className="min-h-screen flex flex-col items-center justify-center px-4 py-6 bg-[radial-gradient(ellipse_60%_40%_at_50%_0%,rgba(249,115,22,0.07)_0%,transparent_65%),var(--bg)] relative">
      <div className="absolute top-5 left-5">
        <Link to="/" className={BACK_LINK}>
          <ChevronLeft size={18} />
          {t('nav.backToGroups')}
        </Link>
      </div>

      {preview.isLoading && (
        <div className={`${cls.skeleton} w-[340px] h-[220px] rounded-[20px]`} />
      )}

      {preview.error && (
        <div className="max-w-[360px] w-full px-6 py-7 bg-[var(--card)] border border-[rgba(248,113,113,0.25)] rounded-[20px] text-center flex flex-col items-center gap-3">
          <AlertTriangle size={32} className="text-[var(--neg)]" />
          <p className="text-[var(--muted)] text-[13px]">{t('groups.invalidCode')}</p>
          <Button variant="ghost" size="sm" onClick={() => navigate('/')}><ChevronLeft size={14} />{t('common.back')}</Button>
        </div>
      )}

      {preview.data && (
        <div className="max-w-[400px] w-full flex flex-col gap-4">
          {/* Group hero */}
          <div className="text-center mb-1">
            <div className="w-[72px] h-[72px] rounded-[22px] bg-[linear-gradient(145deg,var(--primary-soft),rgba(249,115,22,0.08))] border-2 border-[rgba(249,115,22,0.2)] grid place-items-center mx-auto mb-[14px] text-[28px] font-black text-[var(--primary)] shadow-[0_0_28px_rgba(249,115,22,0.15)]">
              {preview.data.groupName.charAt(0).toUpperCase()}
            </div>
            <p className="text-[var(--muted)] text-[13px] mb-1.5">{t('groups.joinSubtitle')}</p>
            <h1 className="text-[24px]">{preview.data.groupName}</h1>
          </div>

          {preview.data.unclaimedMembers.length > 0 && (
            <div className="bg-[var(--card)] border border-[var(--border)] rounded-[16px] py-[18px] px-4">
              <div className="flex items-center gap-2 mb-[14px]">
                <Users size={15} className="text-[var(--muted)] shrink-0" />
                <span className="text-[13px] font-bold text-[var(--muted)] uppercase tracking-[0.06em]">
                  {t('groups.claimHint')}
                </span>
              </div>
              <div className="grid grid-cols-[repeat(auto-fill,minmax(110px,1fr))] gap-2 mb-[14px]">
                {preview.data.unclaimedMembers.map((m) => (
                  <Button
                    key={m.id}
                    variant="ghost"
                    disabled={join.isPending}
                    onClick={() => join.mutate(m.id)}
                    className="!flex-col !py-[14px] !px-2 !bg-[var(--surface)] !rounded-xl !text-[13px] !font-semibold !text-[var(--text)] [&:hover:not(:disabled)]:!border-[rgba(249,115,22,0.5)] [&:hover:not(:disabled)]:!bg-[var(--primary-dim)]"
                  >
                    <div className="w-9 h-9 rounded-full bg-[linear-gradient(145deg,var(--primary-soft),rgba(249,115,22,0.08))] border border-[rgba(249,115,22,0.2)] grid place-items-center font-extrabold text-sm text-[var(--primary)]">
                      {m.name.charAt(0).toUpperCase()}
                    </div>
                    {t('groups.iam', { name: m.name })}
                  </Button>
                ))}
              </div>
              <p className="text-[var(--muted)] text-[13px]">{t('groups.otherwise')}</p>
            </div>
          )}

          <Button fullWidth className="!py-[13px]" loading={join.isPending} onClick={() => join.mutate(undefined)}>
            <UserPlus size={16} />
            {t('groups.joinNew')}
          </Button>
          {join.error && (
            <p className="text-[var(--neg)] text-[13px] text-center">{join.error.message}</p>
          )}
        </div>
      )}
    </div>
  );
}
