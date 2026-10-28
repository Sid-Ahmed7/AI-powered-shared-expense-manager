import { Link, NavLink, Route, Routes, useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { ChevronLeft, Receipt, Scale, Bot, BarChart2, Users, Sparkles, User, LogOut } from 'lucide-react';
import { api } from '@/api';
import { useAuth } from '@/context/auth';
import { cls } from '@/lib/cls';
import { usePageTitle } from '@/hooks/usePageTitle';
import { ExpensesTab } from '@/components/ExpensesTab';
import { BalancesTab } from '@/components/BalancesTab';
import { AssistantTab } from '@/components/AssistantTab';
import { RecapTab } from '@/components/RecapTab';
import { MembersTab } from '@/components/MembersTab';
import { Sidebar } from '@/components/ui/Sidebar';
import { SidebarLink } from '@/components/ui/SidebarLink';
import { ICON_SIZE, SIDEBAR_LINK, SIDEBAR_LINK_ACTIVE } from '@/constants/components';

export function GroupPage() {
  const { t } = useTranslation();
  const { signOut } = useAuth();
  const groupId = useParams().groupId!;
  const group = useQuery({ queryKey: ['group', groupId], queryFn: () => api.group(groupId) });
  usePageTitle(group.data?.name ?? '');
  const ai = useQuery({ queryKey: ['ai-status'], queryFn: api.aiStatus, staleTime: Infinity });
  const categories = useQuery({ queryKey: ['categories'], queryFn: api.categories, staleTime: Infinity });

  const footer = (
    <>
      <SidebarLink as={Link} to="/account" icon={User}>{t('nav.account')}</SidebarLink>
      <SidebarLink icon={LogOut} onClick={() => void signOut()}>{t('auth.logout')}</SidebarLink>
    </>
  );

  if (group.error) {
    return (
      <div className="max-w-xl mx-auto px-4 pb-[72px]">
        <p className="text-[var(--neg)]">{t('groups.notFound')}</p>
        <Link to="/" className={SIDEBAR_LINK}>
          <ChevronLeft size={ICON_SIZE} />{t('common.back')}
        </Link>
      </div>
    );
  }

  if (!group.data || !categories.data) {
    return (
      <div className="flex min-h-screen">
        <Sidebar nav={null} footer={footer} />
        <main className="flex-1 min-w-0 p-10">
          <div className={`${cls.skeleton} h-14 rounded-[var(--radius)] max-w-sm`} />
        </main>
      </div>
    );
  }

  const tabProps = { group: group.data, categories: categories.data, aiEnabled: !!ai.data?.enabled };
  const initial = group.data.name.charAt(0).toUpperCase();

  const subheader = (
    <>
      <SidebarLink as={Link} to="/" icon={ChevronLeft} className="text-[var(--muted)] text-[13px] mb-2">
        {t('nav.backToGroups')}
      </SidebarLink>
      <div className="flex items-center gap-2.5 px-2.5 py-1">
        <div className="w-[34px] h-[34px] rounded-[10px] shrink-0 grid place-items-center font-black text-sm text-[var(--primary)] bg-[linear-gradient(145deg,var(--primary-soft),rgba(249,115,22,0.06))] border-[1.5px] border-[rgba(249,115,22,0.25)]">
          {initial}
        </div>
        <div className="min-w-0">
          <div className="font-bold text-sm tracking-tight overflow-hidden text-ellipsis whitespace-nowrap">
            {group.data.name}
          </div>
          <div className="text-[11px] text-[var(--muted)] mt-0.5">
            {t('groups.memberCount', { count: group.data.members.length })}
          </div>
        </div>
      </div>
    </>
  );

  const nav = (
    <>
      <NavLink to={`/groups/${groupId}`} end className={({ isActive }) => `${SIDEBAR_LINK}${isActive ? ` ${SIDEBAR_LINK_ACTIVE}` : ''}`}>
        <Receipt size={ICON_SIZE} />{t('tabs.expenses')}
      </NavLink>
      <NavLink to={`/groups/${groupId}/soldes`} className={({ isActive }) => `${SIDEBAR_LINK}${isActive ? ` ${SIDEBAR_LINK_ACTIVE}` : ''}`}>
        <Scale size={ICON_SIZE} />{t('tabs.balances')}
      </NavLink>
      <NavLink to={`/groups/${groupId}/assistant`} className={({ isActive }) => `${SIDEBAR_LINK}${isActive ? ` ${SIDEBAR_LINK_ACTIVE}` : ''}`}>
        {tabProps.aiEnabled ? <Sparkles size={ICON_SIZE} /> : <Bot size={ICON_SIZE} />}{t('tabs.assistant')}
      </NavLink>
      <NavLink to={`/groups/${groupId}/bilan`} className={({ isActive }) => `${SIDEBAR_LINK}${isActive ? ` ${SIDEBAR_LINK_ACTIVE}` : ''}`}>
        <BarChart2 size={ICON_SIZE} />{t('tabs.recap')}
      </NavLink>
      <NavLink to={`/groups/${groupId}/membres`} className={({ isActive }) => `${SIDEBAR_LINK}${isActive ? ` ${SIDEBAR_LINK_ACTIVE}` : ''}`}>
        <Users size={ICON_SIZE} />{t('tabs.members')}
      </NavLink>
    </>
  );

  return (
    <div className="flex min-h-screen">
      <Sidebar subheader={subheader} nav={nav} footer={footer} />
      <main className="flex-1 min-w-0 min-h-screen overflow-y-auto p-10 pb-16">
        <Routes>
          <Route index element={<ExpensesTab {...tabProps} />} />
          <Route path="soldes" element={<BalancesTab {...tabProps} />} />
          <Route path="assistant" element={<AssistantTab {...tabProps} />} />
          <Route path="bilan" element={<RecapTab {...tabProps} />} />
          <Route path="membres" element={<MembersTab {...tabProps} />} />
        </Routes>
      </main>
    </div>
  );
}