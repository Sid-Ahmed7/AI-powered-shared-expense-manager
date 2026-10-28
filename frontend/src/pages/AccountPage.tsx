import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { ChevronLeft, Monitor, Shield, Download, Trash2, LogOut, CheckCircle, AlertTriangle, User, KeyRound, Activity, HardDrive, Settings2, Languages, SunMoon } from 'lucide-react';
import { DeviceIcon } from '@/components/ui/DeviceIcon';
import { describeDevice } from '@/utils/device';
import { api } from '@/api';
import { useAuth } from '@/context/auth';
import { Pagination } from '@/components/Pagination';
import { usePagination } from '@/hooks/usePagination';
import { cls } from '@/lib/cls';
import { usePageTitle } from '@/hooks/usePageTitle';
import { useTheme } from '@/hooks/useTheme';
import { PasswordInput } from '@/components/ui/PasswordInput';
import { ChangePasswordForm } from '@/components/form/ChangePasswordForm';
import { Button } from '@/components/ui/Button';
import { Sidebar } from '@/components/ui/Sidebar';
import { SidebarLink } from '@/components/ui/SidebarLink';
import type { AccountNavItem, Section } from '@/types/components';
import { ICON_SIZE, LANGUAGES } from '@/constants/components';



export function AccountPage() {
  const { t, i18n } = useTranslation();
  usePageTitle(t('nav.account'));
  const { theme, setTheme } = useTheme();
  const { user, endSession } = useAuth();
  const queryClient = useQueryClient();
  const [section, setSection] = useState<Section>('profile');

  const sessions = useQuery({ queryKey: ['sessions'], queryFn: api.sessions });
  const events = useQuery({ queryKey: ['security-events'], queryFn: api.securityEvents });

  const sessionPagination = usePagination(sessions.data ?? [], 5);
  const eventPagination = usePagination(events.data ?? [], 10);

  const dateTime = (iso: string) => new Date(iso).toLocaleString(i18n.language, { dateStyle: 'medium', timeStyle: 'short' });

  const refreshLists = () => {
    void queryClient.invalidateQueries({ queryKey: ['sessions'] });
    void queryClient.invalidateQueries({ queryKey: ['security-events'] });
  };
  const revoke = useMutation({ mutationFn: api.revokeSession, onSuccess: refreshLists });
  const revokeOthers = useMutation({ mutationFn: api.revokeOtherSessions, onSuccess: refreshLists });

  const exportData = useMutation({
    mutationFn: api.exportData,
    onSuccess: (data) => {
      const url = URL.createObjectURL(new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' }));
      const a = document.createElement('a');
      a.href = url; a.download = 'pactole-mes-donnees.json'; a.click();
      URL.revokeObjectURL(url);
      refreshLists();
    },
  });

  const [deletePassword, setDeletePassword] = useState('');
  const [confirmDelete, setConfirmDelete] = useState(false);
  const deleteAccount = useMutation({ mutationFn: () => api.deleteAccount(deletePassword), onSuccess: endSession });

  const navItems: AccountNavItem[] = [
    { key: 'profile', label: t('account.navProfile'), icon: <User size={ICON_SIZE} /> },
    { key: 'security', label: t('account.navSecurity'), icon: <KeyRound size={ICON_SIZE} /> },
    { key: 'activity', label: t('account.navActivity'), icon: <Activity size={ICON_SIZE} /> },
    { key: 'data', label: t('account.navData'),  icon: <HardDrive size={ICON_SIZE} /> },
    { key: 'preferences', label: t('account.navPreferences'), icon: <Settings2 size={ICON_SIZE} /> }
  ];

  return (
    <div className="flex min-h-screen">
      <Sidebar
        nav={
          <SidebarLink as={Link} to="/" icon={ChevronLeft}>
            {t('nav.backToGroups')}
          </SidebarLink>
        }
        footer={
          <SidebarLink icon={LogOut} onClick={() => void endSession()}>
            {t('auth.logout')}
          </SidebarLink>
        }
      />

      <main className="flex-1 min-w-0 min-h-screen overflow-y-auto p-10 pb-16">
        <div className="max-w-[860px]">
          <div className="mb-7">
            <h1>{t('account.title')}</h1>
          </div>

          <div className="flex items-start gap-0">
            <nav className="w-[200px] shrink-0 sticky top-0 pr-6 flex flex-col gap-0.5">
              {navItems.map((item) => (
                <Button
                  key={item.key}
                  variant="ghost"
                  className={`w-full !justify-start !gap-2.5 !px-2.5 !py-2 !rounded-[9px] !text-sm !border-0 [&:hover:not(:disabled)]:!bg-[var(--surface-2)] [&:hover:not(:disabled)]:!text-[var(--text)] [&:hover:not(:disabled)]:!border-0${section === item.key ? ' !bg-[var(--primary-dim)] !text-[var(--primary)] !font-semibold' : ' !text-[var(--text-2)]'}`}
                  onClick={() => setSection(item.key)}
                >
                  {item.icon}
                  {item.label}
                </Button>
              ))}
            </nav>

            <div className="flex-1 min-w-0 flex flex-col gap-5">

              {section === 'profile' && (
                <>
                  <div className="text-xl font-extrabold tracking-tight text-[var(--text)] mb-1">{t('account.navProfile')}</div>
                  <div className="flex items-center gap-5 p-6 bg-[linear-gradient(145deg,var(--card),var(--surface))] border border-[var(--border)] rounded-[var(--radius-lg)]">
                    <div className="w-16 h-16 rounded-full bg-[linear-gradient(145deg,var(--primary-soft),rgba(249,115,22,0.08))] border-2 border-[rgba(249,115,22,0.2)] grid place-items-center text-[var(--primary)] font-black text-[26px] shrink-0 shadow-[0_0_20px_rgba(249,115,22,0.15)]">
                      {user?.displayName?.charAt(0).toUpperCase() ?? '?'}
                    </div>
                    <div className="grow min-w-0">
                      <div className="font-extrabold text-[20px] tracking-[-0.02em]">{user?.displayName}</div>
                      <div className="text-[var(--muted)] text-sm mt-1 overflow-hidden text-ellipsis whitespace-nowrap">{user?.email}</div>
                    </div>
                  </div>
                </>
              )}

              {section === 'security' && (
                <>
                  <div className="text-xl font-extrabold tracking-tight text-[var(--text)] mb-1">{t('account.navSecurity')}</div>

                  <section className={`${cls.cardPad} flex flex-col gap-4`}>
                    <h3><Monitor size={12} />{t('account.sessions')}</h3>
                    <ul className="list-none m-0 p-0">
                      {sessionPagination.paginated.map((session) => (
                        <li key={session.sessionId} className="flex flex-wrap items-center gap-[8px_12px] py-[11px] border-b border-[var(--border)] last:border-b-0">
                          <div className="text-[var(--muted)] shrink-0">
                            <DeviceIcon userAgent={session.userAgent} />
                          </div>
                          <div className="grow min-w-0">
                            <div className="flex items-center gap-2 flex-wrap">
                              <strong className="text-sm">{describeDevice(session.userAgent, t('account.unknownDevice'))}</strong>
                              {session.current && (
                                <span className="inline-flex items-center text-[11px] py-[2px] px-2 rounded-full bg-[var(--primary-soft)] text-[var(--primary)] font-bold tracking-[0.02em]">
                                  {t('account.currentSession')}
                                </span>
                              )}
                            </div>
                            <div className="text-[var(--muted)] text-[13px] mt-0.5">
                              {t('account.connectedAt', { date: dateTime(session.startedAt) })}
                              {session.ipMasked && ` · ${session.ipMasked}`}
                            </div>
                          </div>
                          {!session.current ? (
                            <Button variant="ghost" size="sm" loading={revoke.isPending} onClick={() => revoke.mutate(session.sessionId)}>
                              <LogOut size={13} />
                              {t('account.disconnect')}
                            </Button>
                          ) : (
                            <span className="text-[var(--pos)] flex items-center gap-1 text-xs">
                              <CheckCircle size={13} />
                            </span>
                          )}
                        </li>
                      ))}
                    </ul>
                    <Pagination
                      page={sessionPagination.page}
                      totalPages={sessionPagination.totalPages}
                      onPage={sessionPagination.setPage}
                      from={sessionPagination.from}
                      to={sessionPagination.to}
                      total={sessionPagination.total}
                    />
                    {sessions.data && sessions.data.length > 1 && (
                      <Button variant="ghost" size="sm" className="self-start" loading={revokeOthers.isPending} onClick={() => revokeOthers.mutate()}>
                        <LogOut size={13} />
                        {t('account.disconnectOthers')}
                      </Button>
                    )}
                  </section>

                  <ChangePasswordForm onSuccess={refreshLists} />
                </>
              )}

              {section === 'activity' && (
                <>
                  <div className="text-xl font-extrabold tracking-tight text-[var(--text)] mb-1">{t('account.navActivity')}</div>
                  <section className={cls.cardPad}>
                    <h3><Shield size={12} />{t('account.recentActivity')}</h3>
                    <p className="text-[var(--muted)] text-[13px] mb-[14px]">{t('account.securityHint')}</p>
                    <ul className="list-none m-0 p-0">
                      {eventPagination.paginated.map((event, i) => {
                        const isAlert = event.type === 'REFRESH_REUSE_DETECTED' || event.type === 'ACCOUNT_LOCKED';
                        return (
                          <li key={i} className="flex flex-wrap items-center gap-[8px_12px] py-[11px] border-b border-[var(--border)] last:border-b-0">
                            <span className={`shrink-0 ${isAlert ? 'text-[var(--neg)]' : 'text-[var(--muted)]'}`}>
                              {isAlert ? <AlertTriangle size={14} /> : <CheckCircle size={14} />}
                            </span>
                            <span className={`grow min-w-0 text-[13px] ${isAlert ? 'text-[var(--neg)] font-semibold' : ''}`}>
                              {t(`events.${event.type}`, { defaultValue: event.type })}
                            </span>
                            <span className="text-[var(--muted)] text-xs">
                              {dateTime(event.date)}
                              <span className="ml-1.5 opacity-70">
                                {describeDevice(event.userAgent, t('account.unknownDevice'))}
                              </span>
                            </span>
                          </li>
                        );
                      })}
                    </ul>
                    <Pagination
                      page={eventPagination.page}
                      totalPages={eventPagination.totalPages}
                      onPage={eventPagination.setPage}
                      from={eventPagination.from}
                      to={eventPagination.to}
                      total={eventPagination.total}
                    />
                  </section>
                </>
              )}

              {section === 'preferences' && (
                <>
                  <div className="text-xl font-extrabold tracking-tight text-[var(--text)] mb-1">{t('account.navPreferences')}</div>

                  <section className={`${cls.cardPad} flex flex-col gap-4`}>
                    <h3><Languages size={12} />{t('account.language')}</h3>
                    <div className={cls.segmented}>
                      {LANGUAGES.map((language) => (
                        <Button
                          key={language}
                          variant="seg"
                          data-active={i18n.language.startsWith(language) || undefined}
                          className={i18n.language.startsWith(language) ? cls.segBtnActive : ''}
                          onClick={() => void i18n.changeLanguage(language)}
                        >
                          {language === 'fr' ? '🇫🇷 Français' : '🇬🇧 English'}
                        </Button>
                      ))}
                    </div>
                  </section>

                  <section className={`${cls.cardPad} flex flex-col gap-4`}>
                    <h3><SunMoon size={12} />{t('account.appearance')}</h3>
                    <div className={cls.segmented}>
                      <Button
                        variant="seg"
                        data-active={theme === 'dark' || undefined}
                        className={theme === 'dark' ? cls.segBtnActive : ''}
                        onClick={() => setTheme('dark')}
                      >
                        🌙 {t('account.darkMode')}
                      </Button>
                      <Button
                        variant="seg"
                        data-active={theme === 'light' || undefined}
                        className={theme === 'light' ? cls.segBtnActive : ''}
                        onClick={() => setTheme('light')}
                      >
                        ☀️ {t('account.lightMode')}
                      </Button>
                    </div>
                  </section>
                </>
              )}

              
              {section === 'data' && (
                <>
                  <div className="text-xl font-extrabold tracking-tight text-[var(--text)] mb-1">{t('account.navData')}</div>

                  <section className={`${cls.cardPad} flex items-center gap-4`}>
                    <div className="w-11 h-11 rounded-xl bg-[var(--surface)] border border-[var(--border)] grid place-items-center shrink-0">
                      <Download size={20} className="text-[var(--muted)]" />
                    </div>
                    <div className="grow min-w-0">
                      <div className="font-semibold text-sm">{t('account.myData')}</div>
                      <div className="text-[var(--muted)] text-[13px]">{t('account.exportHint')}</div>
                    </div>
                    <Button variant="ghost" size="sm" loading={exportData.isPending} onClick={() => exportData.mutate()}>
                      {t('account.exportBtn')}
                    </Button>
                  </section>

                  <section className={`${cls.cardPad} flex flex-col gap-4 !border-[rgba(248,113,113,0.25)] !bg-[rgba(248,113,113,0.03)]`}>
                    <h3 className="text-[var(--neg)] after:bg-[rgba(248,113,113,0.2)]">{t('account.deleteTitle')}</h3>
                    <p className="text-[13px] text-[var(--text-2)]">{t('account.deleteDescription')}</p>
                    {!confirmDelete ? (
                      <Button variant="danger" className="self-start" onClick={() => setConfirmDelete(true)}>
                        <Trash2 size={14} />
                        {t('account.deleteBtn')}
                      </Button>
                    ) : (
                      <form className="flex flex-col gap-4" onSubmit={(e) => { e.preventDefault(); deleteAccount.mutate(); }}>
                        <label className="flex flex-col gap-[7px]">
                          <span className="text-[12px] font-bold tracking-[0.04em] uppercase text-[var(--muted)]">{t('account.confirmWithPassword')}</span>
                          <PasswordInput value={deletePassword} onChange={setDeletePassword} autoComplete="current-password" />
                        </label>
                        {deleteAccount.error && <p className="text-[var(--neg)] text-[13px]">{deleteAccount.error.message}</p>}
                        <div className="flex items-center gap-[10px]">
                          <Button type="button" variant="ghost" onClick={() => setConfirmDelete(false)}>
                            {t('common.cancel')}
                          </Button>
                          <Button variant="danger" disabled={!deletePassword} loading={deleteAccount.isPending}>
                            <Trash2 size={14} />
                            {t('account.deletePermanently')}
                          </Button>
                        </div>
                      </form>
                    )}
                  </section>
                </>
              )}

            </div>
          </div>
        </div>
      </main>
    </div>
  );
}