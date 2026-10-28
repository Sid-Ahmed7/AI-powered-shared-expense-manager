import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Share2, RefreshCw, Trash2, CheckCircle, Users } from 'lucide-react';
import { api, type GroupDetail } from '@/api';
import { cls } from '@/lib/cls';
import { Button } from '@/components/ui/Button';
import { AddMemberForm } from '@/components/form/AddMemberForm';
import { ICON_SIZE } from '@/constants/components';

interface MembersTabProps {
  group: GroupDetail;

}

export function MembersTab({ group }: MembersTabProps) {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const [copied, setCopied] = useState(false);
  const link = `${location.origin}/join/${group.inviteCode}`;

  const update = (g: typeof group) => queryClient.setQueryData(['group', group.id], g);
  const add = useMutation({
    mutationFn: (name: string) => api.addMember(group.id, name),
    onSuccess: update,
  });
  const regenerate = useMutation({ mutationFn: () => api.regenerateInvite(group.id), onSuccess: update });
  const navigate = useNavigate();
  const removeGroup = useMutation({
    mutationFn: () => api.deleteGroup(group.id),
    onSuccess: () => {
      queryClient.removeQueries({ queryKey: ['group', group.id] });
      void queryClient.invalidateQueries({ queryKey: ['groups'] });
      navigate('/');
    },
  });

  const share = async () => {
    const text = t('members.shareText', { name: group.name, link });
    if (navigator.share) {
      await navigator.share({ text }).catch(() => {});
    } else {
      await navigator.clipboard.writeText(link);
      setCopied(true);
    }
  };

  return (
    <div className="flex flex-col gap-4">
      <section className={`${cls.cardPad} flex flex-col gap-4`}>
        <h3>{t('members.invite')}</h3>
        <div className="font-mono text-[26px] tracking-[0.22em] text-center p-4 bg-[linear-gradient(135deg,var(--accent-soft),rgba(58,45,11,0.4))] border border-[rgba(251,191,36,0.2)] rounded-[var(--radius-sm)] font-extrabold text-[var(--accent)]">
          {group.inviteCode}
        </div>
        <div className="flex items-center gap-[10px]">
          <Button className="grow min-w-0" onClick={share}>
            {copied ? <CheckCircle size={ICON_SIZE} /> : <Share2 size={ICON_SIZE} />}
            {copied ? t('members.linkCopied') : t('members.shareLink')}
          </Button>
          {group.owner && (
            <Button
              variant="ghost"
              title={t('members.newCode')}
              disabled={regenerate.isPending}
              loading={regenerate.isPending}
              onClick={() => confirm(t('members.newCodeConfirm')) && regenerate.mutate()}
            >
              <RefreshCw size={ICON_SIZE} />
            </Button>
          )}
        </div>
      </section>

      <section className={cls.cardPad}>
        <h3>
          <Users size={13} className="text-[var(--muted)]" />
          {t('tabs.members')}
          <span className="text-[var(--muted)] ml-auto text-xs font-normal normal-case tracking-normal">
            {group.members.length}
          </span>
        </h3>
        <ul className="list-none m-0 mb-4 p-0">
          {group.members.map((member) => (
            <li key={member.id} className="flex items-center gap-3 py-[9px] border-b border-[var(--border)] last:border-b-0">
              <span className="w-9 h-9 rounded-full grid place-items-center bg-[linear-gradient(145deg,var(--primary-soft),rgba(249,115,22,0.08))] border border-[rgba(249,115,22,0.2)] text-[var(--primary)] font-extrabold text-[13px] shrink-0">
                {member.name.slice(0, 1).toUpperCase()}
              </span>
              <span className="grow min-w-0">
                {member.name}{member.me && <span className="text-[var(--muted)] ml-1.5">({t('common.me')})</span>}
              </span>
              <span className={`inline-flex items-center text-[11px] py-[2px] px-2 rounded-full font-bold tracking-[0.02em] ${member.hasAccount ? 'bg-[var(--pos-soft)] text-[var(--pos)]' : 'bg-[var(--primary-soft)] text-[var(--primary)]'}`}>
                {member.hasAccount ? t('members.hasAccount') : t('members.noAccount')}
              </span>
            </li>
          ))}
        </ul>

        <AddMemberForm onSubmit={(name) => add.mutate(name)} disabled={add.isPending} />
        {add.error && <p className="text-[var(--neg)] text-[13px] mt-1.5">{add.error.message}</p>}
      </section>

      {group.owner && (
        <section className={`${cls.cardPad} flex flex-col gap-4 !border-[rgba(248,113,113,0.25)] !bg-[rgba(248,113,113,0.03)]`}>
          <h3 className="text-[var(--neg)] after:bg-[rgba(248,113,113,0.2)]">{t('members.deleteGroup')}</h3>
          <p className="text-[13px] text-[var(--muted)]">{t('members.deleteGroupHint')}</p>
          <Button
            variant="danger"
            className="self-start"
            disabled={removeGroup.isPending}
            loading={removeGroup.isPending}
            onClick={() => confirm(t('members.deleteGroupConfirm', { name: group.name })) && removeGroup.mutate()}
          >
            <Trash2 size={ICON_SIZE} />
            {t('members.deleteGroup')}
          </Button>
          {removeGroup.error && <p className="text-[var(--neg)] text-[13px]">{removeGroup.error.message}</p>}
        </section>
      )}
    </div>
  );
}