import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { Flame, Users, LogOut, User, ArrowRight, Plus, Hash } from 'lucide-react';
import { api } from '@/api';
import { useAuth } from '@/context/auth';
import { euros } from '@/utils/money';
import { Pagination } from '@/components/Pagination';
import { usePagination } from '@/hooks/usePagination';
import { cls } from '@/lib/cls';
import { Button } from '@/components/ui/Button';
import { usePageTitle } from '@/hooks/usePageTitle';
import { Sidebar } from '@/components/ui/Sidebar';
import { SidebarLink } from '@/components/ui/SidebarLink';
import { BalanceBadge } from '@/components/ui/BalanceBadge';
import { EmptyState } from '@/components/ui/EmptyState';
import { ICON_SIZE } from '@/constants/components';

export function GroupsPage() {
  const { t } = useTranslation();
  usePageTitle(t('groups.groupCount', { count: 2 }));
  const { user, signOut } = useAuth();
  const navigate = useNavigate();
  const groups = useQuery({ queryKey: ['groups'], queryFn: api.groups });
  const [code, setCode] = useState('');

  const totalSpentCents = groups.data?.reduce((sum, g) => sum + g.totalSpentCents, 0) ?? 0;
  const groupPagination = usePagination(groups.data ?? [], 9);

  return (
    <div className="flex min-h-screen">
      <Sidebar
        nav={
          <>
            <p className="text-[10.5px] font-bold tracking-[0.1em] uppercase text-[var(--muted)] px-2.5 pt-2.5 pb-1.5 m-0">
              {t('groups.title')}
            </p>

            {groups.isLoading && (
              <div className="px-2.5 py-1.5">
                <div className={`${cls.skeleton} h-8 rounded-lg`} />
              </div>
            )}

            {groups.data?.length === 0 && (
              <p className="text-[var(--muted)] text-[13px] px-2.5 m-0">{t('groups.empty')}</p>
            )}

            {groups.data?.map((group) => (
              <Link key={group.id} to={`/groups/${group.id}`}
                className="flex items-center gap-2.5 px-2.5 py-1.5 rounded-[9px] text-[13px] font-medium text-[var(--text-2)] no-underline transition-all hover:bg-[var(--surface-2)] hover:text-[var(--text)] hover:opacity-100">
                <div className="w-[26px] h-[26px] rounded-[7px] bg-gradient-to-br from-[var(--primary-soft)] to-[rgba(249,115,22,0.08)] border border-[rgba(249,115,22,0.2)] grid place-items-center text-[11px] font-black text-[var(--primary)] shrink-0">
                  {group.name.charAt(0).toUpperCase()}
                </div>
                <span className="grow overflow-hidden text-ellipsis whitespace-nowrap">{group.name}</span>
                {group.myBalanceCents !== 0 && (
                  <span className={`text-[11px] font-bold shrink-0 ${group.myBalanceCents > 0 ? 'text-[var(--pos)]' : 'text-[var(--neg)]'}`}>
                    {euros(Math.abs(group.myBalanceCents))}
                  </span>
                )}
              </Link>
            ))}

            <div className="h-px bg-[var(--border)] mx-2.5 my-1.5" />

            <SidebarLink as={Link} to="/groups/new" icon={Plus}>
              {t('groups.new')}
            </SidebarLink>
          </>
        }
        footer={
          <>
            <SidebarLink as={Link} to="/account" icon={User}>
              {t('nav.account')}
            </SidebarLink>
            <SidebarLink icon={LogOut} onClick={() => void signOut()}>
              {t('auth.logout')}
            </SidebarLink>
          </>
        }
      />

      <main className="flex-1 min-w-0 min-h-screen overflow-y-auto p-10 pb-16">
        <div className="mb-7">
          <h1>{t('groups.greeting', { name: user?.displayName })}</h1>
          {!groups.isLoading && groups.data && groups.data.length > 0 && (
            <p className="text-[var(--muted)] mt-1">{groups.data.length} {t('groups.groupCount', { count: groups.data.length })}</p>
          )}
        </div>

        {!groups.isLoading && groups.data && groups.data.length > 0 && totalSpentCents > 0 && (
          <div className="relative overflow-hidden mb-8 max-w-sm bg-[linear-gradient(145deg,#161210_0%,#1E1610_60%,#161210_100%)] border border-[rgba(249,115,22,0.15)] rounded-[var(--radius-lg)] p-[24px_22px_20px] before:content-[''] before:absolute before:-top-[80px] before:-right-[80px] before:w-[220px] before:h-[220px] before:rounded-full before:bg-[radial-gradient(circle,rgba(249,115,22,0.12)_0%,transparent_65%)] before:pointer-events-none after:content-[''] after:absolute after:-bottom-[60px] after:-left-[40px] after:w-[160px] after:h-[160px] after:rounded-full after:bg-[radial-gradient(circle,rgba(251,191,36,0.06)_0%,transparent_65%)] after:pointer-events-none">
            <div className="text-[11px] font-bold tracking-[0.1em] uppercase text-[var(--muted)] mb-2">{t('groups.totalSpent')}</div>
            <div className="text-[42px] font-black tracking-[-0.05em] [font-variant-numeric:tabular-nums] leading-none text-[var(--text)]">{euros(totalSpentCents)}</div>
            <div className="mt-[10px] text-[13px] text-[var(--muted)]">
              {groups.data.length} {t('groups.groupCount', { count: groups.data.length })}
            </div>
          </div>
        )}

        {groups.isLoading && (
          <div className="grid grid-cols-[repeat(auto-fill,minmax(280px,1fr))] gap-3.5 mb-7">
            {[1, 2, 3].map((i) => <div key={i} className={`${cls.skeleton} h-32 rounded-[var(--radius)]`} />)}
          </div>
        )}

        {groups.data?.length === 0 && (
          <EmptyState icon={Flame} title={t('groups.empty')} description={t('groups.emptyHint')} />
        )}

        {groups.data && groups.data.length > 0 && (
          <>
            <div className="grid grid-cols-[repeat(auto-fill,minmax(280px,1fr))] gap-3.5 mb-4">
              {groupPagination.paginated.map((g) => (
                <Link key={g.id} to={`/groups/${g.id}`}
                  className="flex flex-col gap-3.5 p-[18px_20px] no-underline text-inherit bg-[var(--card)] border border-[var(--border)] rounded-[var(--radius)] transition-all hover:border-[rgba(249,115,22,0.4)] hover:bg-[var(--primary-dim)] hover:-translate-y-px hover:opacity-100">
                  <div className="flex items-center gap-3">
                    <div className="w-11 h-11 rounded-[13px] bg-[linear-gradient(145deg,var(--primary-soft),rgba(249,115,22,0.08))] border border-[rgba(249,115,22,0.2)] grid place-items-center text-[18px] font-black text-[var(--primary)] shrink-0 tracking-[-0.02em]">
                      {g.name.charAt(0).toUpperCase()}
                    </div>
                    <div className="grow min-w-0">
                      <strong className="text-[15px] tracking-[-0.01em]">{g.name}</strong>
                      <div className="flex items-center gap-1 text-xs text-[var(--muted)] mt-0.5">
                        <Users size={11} />
                        {t('groups.memberCount', { count: g.memberCount })}
                        &nbsp;·&nbsp;
                        {t('groups.expenseCount', { count: g.expenseCount })}
                      </div>
                    </div>
                  </div>
                  <div className="flex items-center justify-between pt-2.5 border-t border-[var(--border)]">
                    <BalanceBadge cents={g.myBalanceCents} />
                    <ArrowRight size={15} className="text-[var(--muted)]" />
                  </div>
                </Link>
              ))}
            </div>
            <Pagination
              page={groupPagination.page}
              totalPages={groupPagination.totalPages}
              onPage={groupPagination.setPage}
              from={groupPagination.from}
              to={groupPagination.to}
              total={groupPagination.total}
            />
          </>
        )}

        <form
          className="max-w-sm mt-6"
          onSubmit={(e) => {
            e.preventDefault();
            const clean = code.trim().toUpperCase().replace(/[^A-Z0-9]/g, '');
            if (clean) navigate(`/join/${encodeURIComponent(clean)}`);
          }}
        >
          <div className="flex gap-2 bg-[var(--surface)] border border-[var(--border)] rounded-[var(--radius)] p-[6px_6px_6px_14px] items-center transition-all focus-within:border-[var(--border-focus)] focus-within:shadow-[0_0_0_3px_var(--primary-dim)]">
            <Hash size={ICON_SIZE} className="text-[var(--muted)] shrink-0" />
            <input
              className="flex-1 border-0 bg-transparent font-[inherit] text-[var(--text)] py-2 min-w-0 text-[14px] tracking-[0.06em] placeholder:text-[var(--muted)] focus:outline-none"
              placeholder={t('groups.inviteCodePlaceholder')}
              value={code}
              onChange={(e) => setCode(e.target.value)}
              maxLength={8}
            />
            <Button type="submit" variant="ghost" size="sm">{t('groups.join')}</Button>
          </div>
        </form>
      </main>
    </div>
  );
}
