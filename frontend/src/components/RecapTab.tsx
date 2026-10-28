import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { ChevronLeft, ChevronRight, Sparkles, Lightbulb, BarChart3 } from 'lucide-react';
import { api, type GroupDetail } from '@/api';
import { euros } from '@/utils/money';
import { cls } from '@/lib/cls';
import { Button } from '@/components/ui/Button';
import { EmptyState } from '@/components/ui/EmptyState';
import { MemberStatsTable } from '@/components/ui/MemberStatsTable';
import { currentMonth, shiftMonth } from '@/utils/date';
import { ICON_SIZE } from '@/constants/components';


interface RecapTabProps {
  group: GroupDetail;
}


export function RecapTab({ group }: RecapTabProps) {
  const { t, i18n } = useTranslation();
  const [month, setMonth] = useState(currentMonth());
  const recap = useQuery({ queryKey: ['recap', group.id, month], queryFn: () => api.recap(group.id, month), staleTime: 60_000 });
  const label = new Date(month + '-01T12:00:00').toLocaleDateString(i18n.language, { month: 'long', year: 'numeric' });

  const summary = recap.data?.stats.current;
  const maxCat = Math.max(1, ...(summary?.byCategory.map((category) => category.cents) ?? [1]));

  return (
    <div className="flex flex-col gap-4">
      <div className="flex justify-center items-center gap-4 bg-[var(--surface)] border border-[var(--border)] rounded-[var(--radius)] p-[10px_16px]">
        <Button variant="icon" onClick={() => setMonth(shiftMonth(month, -1))} aria-label={t('recap.prevMonth')}>
          <ChevronLeft size={20} />
        </Button>
        <strong className="capitalize text-base min-w-[160px] text-center">{label}</strong>
        <Button variant="icon" disabled={month >= currentMonth()} onClick={() => setMonth(shiftMonth(month, 1))} aria-label={t('recap.nextMonth')}>
          <ChevronRight size={20} />
        </Button>
      </div>

      {recap.isLoading && <div className={`${cls.skeleton} h-[120px] rounded-[var(--radius)]`} />}

      {recap.data?.recap && (
        <section className="p-5 bg-[linear-gradient(145deg,rgba(58,45,11,0.7),rgba(29,22,5,0.5))] border border-[rgba(251,191,36,0.2)] rounded-[var(--radius)]">
          <div className="flex items-center gap-2 mb-2.5">
            <Sparkles size={ICON_SIZE} className="text-[var(--accent)]" />
            <span className="inline-flex items-center gap-[3px] text-[11px] py-[2px] px-[7px] rounded-full bg-[var(--accent-soft)] text-[var(--accent)] font-semibold border border-[rgba(251,191,36,0.15)]">
              {t('recap.aiGenerated')}
            </span>
          </div>
          <h2 className="mb-2">{recap.data.recap.title}</h2>
          <p className="my-2 text-[14px] text-[var(--text-2)]">{recap.data.recap.summary}</p>
          <ul className="pl-[18px] my-2">
            {recap.data.recap.insights.map((i) => (
              <li key={i} className="text-[14px] text-[var(--text-2)] my-1">{i}</li>
            ))}
          </ul>
          {recap.data.recap.tip && (
            <p className="font-semibold text-[14px] text-[var(--text)] flex items-center gap-1.5 mt-2">
              <Lightbulb size={ICON_SIZE} className="text-[var(--accent)] shrink-0" />
              {recap.data.recap.tip}
            </p>
          )}
        </section>
      )}
      {recap.data?.aiError && <p className="text-[var(--muted)] text-[13px]">{t('recap.aiError', { error: recap.data.aiError })}</p>}

      {summary && (
        <>
          <div className="grid grid-cols-3 gap-2">
            <div className={`${cls.card} p-[14px_14px_12px] flex flex-col gap-1`}>
              <span className="text-[var(--muted)] text-[11px] font-semibold tracking-[0.06em] uppercase">{t('common.total')}</span>
              <strong className="text-[22px] font-extrabold tracking-[-0.03em] [font-variant-numeric:tabular-nums]">{euros(summary.totalCents)}</strong>
            </div>
            <div className={`${cls.card} p-[14px_14px_12px] flex flex-col gap-1`}>
              <span className="text-[var(--muted)] text-[11px] font-semibold tracking-[0.06em] uppercase">{t('common.expenses')}</span>
              <strong className="text-[22px] font-extrabold tracking-[-0.03em] [font-variant-numeric:tabular-nums]">{summary.expenseCount}</strong>
            </div>
            <div className={`${cls.card} p-[14px_14px_12px] flex flex-col gap-1`}>
              <span className="text-[var(--muted)] text-[11px] font-semibold tracking-[0.06em] uppercase">{t('recap.vsPrevMonth')}</span>
              <strong className={`text-[22px] font-extrabold tracking-[-0.03em] [font-variant-numeric:tabular-nums] ${recap.data!.stats.deltaPercent! > 0 ? 'text-[var(--neg)]' : 'text-[var(--pos)]'}`}>
                {recap.data!.stats.deltaPercent == null ? '—' : `${recap.data!.stats.deltaPercent > 0 ? '+' : ''}${recap.data!.stats.deltaPercent} %`}
              </strong>
            </div>
          </div>

          {summary.expenseCount === 0 ? (
            <EmptyState icon={BarChart3} title={t('recap.empty')} />
          ) : (
            <>
              <section className={cls.cardPad}>
                <h3>{t('recap.byCategory')}</h3>
                {summary.byCategory.map((category) => (
                  <div key={category.category} className="grid grid-cols-[1fr_140px_72px] gap-[10px] items-center py-1.5 text-[14px]">
                    <span>{category.label}</span>
                    <div className="h-1.5 bg-[var(--surface)] rounded-[3px] overflow-hidden">
                      <div className="h-full bg-[linear-gradient(90deg,rgba(249,115,22,0.6),var(--primary))] rounded-[3px]" style={{ width: `${(category.cents / maxCat) * 100}%` }} />
                    </div>
                    <strong className="text-right [font-variant-numeric:tabular-nums]">{euros(category.cents)}</strong>
                  </div>
                ))}
              </section>
              <section className={cls.cardPad}>
                <h3>{t('recap.byMember')}</h3>
                <MemberStatsTable rows={summary.byMember} />
                {summary.biggestExpense && (
                  <p className="text-[var(--muted)] text-[13px] mt-2.5">
                    {t('recap.biggestExpense', { name: summary.biggestExpense, amount: euros(summary.biggestExpenseCents) })}
                  </p>
                )}
              </section>
            </>
          )}
        </>
      )}
    </div>
  );
}