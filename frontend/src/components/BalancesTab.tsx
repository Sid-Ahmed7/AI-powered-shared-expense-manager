import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { ArrowRight, RotateCcw, CheckCircle, Share2, TrendingUp } from 'lucide-react';
import { api, type Category, type GroupDetail, type Transfer } from '@/api';
import { euros, longDate } from '@/utils/money';
import { memberName } from '@/utils/members';
import { cls } from '@/lib/cls';
import { Button } from '@/components/ui/Button';
import { ReminderDialog } from '@/components/ReminderDialog';
import { ICON_SIZE } from '@/constants/components';

interface BalancesTabProps {
  group: GroupDetail;
  categories: Category[];
  aiEnabled: boolean;
}

export function BalancesTab(props: BalancesTabProps) {
  const { t } = useTranslation();
  const { group } = props;
  const queryClient = useQueryClient();
  const balances = useQuery({ queryKey: ['balances', group.id], queryFn: () => api.balances(group.id) });
  const settlements = useQuery({ queryKey: ['settlements', group.id], queryFn: () => api.settlements(group.id) });
  const [reminding, setReminding] = useState<Transfer | null>(null);

  const refresh = () => {
    void queryClient.invalidateQueries({ queryKey: ['balances', group.id] });
    void queryClient.invalidateQueries({ queryKey: ['settlements', group.id] });
    void queryClient.invalidateQueries({ queryKey: ['groups'] });
  };
  const settle = useMutation({ mutationFn: (tr: Transfer) => api.settleSuggestion(group.id, tr), onSuccess: refresh });
  const undo = useMutation({ mutationFn: (id: string) => api.deleteSettlement(group.id, id), onSuccess: refresh });

  if (!balances.data) return <div className={`${cls.skeleton} h-[120px] rounded-[var(--radius)]`} />;
  const max = Math.max(1, ...balances.data.members.map((m) => Math.abs(m.netCents)));

  return (
    <div className="flex flex-col gap-4">
      <section className={cls.cardPad}>
        <h3>{t('balance.title')}</h3>
        <div className="flex flex-col gap-3">
          {balances.data.members.map((member) => (
            <div key={member.memberId} className="grid grid-cols-[90px_1fr_88px] items-center gap-[10px]">
              <span className="overflow-hidden text-ellipsis whitespace-nowrap font-medium text-[13px]">{member.name}</span>
              <div className="relative h-[10px] bg-[var(--surface)] rounded-[5px] after:content-[''] after:absolute after:left-[50%] after:-top-[3px] after:-bottom-[3px] after:w-px after:bg-[var(--border)]">
                <div
                  className={`absolute top-0 bottom-0 rounded-[5px] transition-[width] duration-300 ${member.netCents >= 0 ? 'bg-[linear-gradient(90deg,rgba(74,222,128,0.6),var(--pos))]' : 'bg-[linear-gradient(270deg,rgba(248,113,113,0.6),var(--neg))]'}`}
                  style={{ width: `${(Math.abs(member.netCents) / max) * 50}%`, [member.netCents >= 0 ? 'left' : 'right']: '50%' }}
                />
              </div>
              <span className={`text-right font-bold [font-variant-numeric:tabular-nums] text-[13px] ${member.netCents > 0 ? 'text-[var(--pos)]' : member.netCents < 0 ? 'text-[var(--neg)]' : 'text-[var(--muted)]'}`}>
                {member.netCents > 0 ? '+' : ''}{euros(member.netCents)}
              </span>
            </div>
          ))}
        </div>
        {balances.data.suggestedNextPayerId && (
          <div className="mt-[14px] p-[11px_14px] bg-[var(--accent-soft)] border border-[rgba(251,191,36,0.15)] rounded-[var(--radius-sm)] text-[13px] flex items-center gap-2">
            <TrendingUp size={ICON_SIZE} className="text-[var(--accent)] shrink-0" />
            <span>
              {t('balance.nextPayer')} <strong>{memberName(group, balances.data.suggestedNextPayerId)}</strong>
              {' — '}{balances.data.suggestedNextPayerReason}
            </span>
          </div>
        )}
      </section>

      <section className={cls.cardPad}>
        <h3>{t('balance.toBalance')}</h3>
        {balances.data.transfers.length === 0 ? (
          <div className="flex items-center gap-2.5 py-2.5 text-[var(--pos)]">
            <CheckCircle size={18} />
            <span className="text-[13px]">{t('balance.allSettled')}</span>
          </div>
        ) : (
          <>
            <p className="text-[var(--muted)] text-[13px] mb-3">
              {t('balance.transfersNeeded', { count: balances.data.transfers.length })}
            </p>
            <ul className="list-none m-0 p-0">
              {balances.data.transfers.map((tr) => (
                <li key={`${tr.fromMemberId}-${tr.toMemberId}`} className="flex flex-wrap items-center gap-[8px_10px] py-3 border-b border-[var(--border)] last:border-b-0">
                  <div className="flex items-center gap-2 flex-wrap text-[14px] grow min-w-0">
                    <strong>{memberName(group, tr.fromMemberId)}</strong>
                    <ArrowRight size={ICON_SIZE} className="text-[var(--muted)] shrink-0" />
                    <strong>{memberName(group, tr.toMemberId)}</strong>
                  </div>
                  <strong className="[font-variant-numeric:tabular-nums]">{euros(tr.amountCents)}</strong>
                  <div className="flex items-center gap-[6px]">
                    <Button variant="ghost" size="sm" onClick={() => setReminding(tr)}>
                      <Share2 size={ICON_SIZE} />
                      {t('balance.remind')}
                    </Button>
                    <Button size="sm" disabled={settle.isPending} onClick={() => settle.mutate(tr)}>
                      <CheckCircle size={ICON_SIZE} />
                      {t('balance.settled')}
                    </Button>
                  </div>
                </li>
              ))}
            </ul>
            {settle.error && <p className="text-[var(--neg)] text-[13px]">{settle.error.message}</p>}
          </>
        )}
      </section>

      {settlements.data && settlements.data.length > 0 && (
        <section className={cls.cardPad}>
          <h3>{t('settlement.title')}</h3>
          <ul className="list-none m-0 p-0">
            {settlements.data.map((s) => (
              <li key={s.id} className="flex items-center gap-[10px] py-[9px] border-b border-[var(--border)] last:border-b-0">
                <span className="grow min-w-0 text-[var(--muted)] text-[13px]">
                  {longDate(s.settledOn)} · {t('settlement.repaid', { from: memberName(group, s.fromMemberId), to: memberName(group, s.toMemberId) })}
                </span>
                <strong className="[font-variant-numeric:tabular-nums]">{euros(s.amountCents)}</strong>
                <Button
                  variant="icon"
                  title={t('common.cancel')}
                  onClick={() => confirm(t('settlement.cancelConfirm')) && undo.mutate(s.id)}
                >
                  <RotateCcw size={ICON_SIZE} />
                </Button>
              </li>
            ))}
          </ul>
        </section>
      )}

      {reminding && (
        <ReminderDialog
          group={group}
          transfer={reminding}
          onClose={() => setReminding(null)}
        />
      )}
    </div>
  );
}