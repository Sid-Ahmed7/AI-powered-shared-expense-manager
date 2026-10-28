import { useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { Sparkles, PenLine, Camera, Search } from 'lucide-react';
import { api, type Category, type Expense, type ExpenseDraft, type GroupDetail } from '@/api';
import { euros, longDate } from '@/utils/money';
import { ExpenseForm } from '@/components/form/ExpenseForm';
import { AiTextForm } from '@/components/form/AiTextForm';
import { ReceiptScanner } from '@/components/ReceiptScanner';
import { memberName } from '@/utils/members';
import { Button } from '@/components/ui/Button';
import { cls } from '@/lib/cls';
import { ACTION_CARD, ICON_SIZE } from '@/constants/components';

interface ExpensesTabProps {
  group: GroupDetail;
  categories: Category[];
  aiEnabled: boolean;
}

export function ExpensesTab(props: ExpensesTabProps) {
  const { t } = useTranslation();
  const { group, aiEnabled } = props;
  const [q, setQ] = useState('');
  const [editing, setEditing] = useState<Expense | null>(null);
  const [draft, setDraft] = useState<ExpenseDraft | null>(null);
  const [creating, setCreating] = useState(false);
  const [scanning, setScanning] = useState(false);

  const expenses = useQuery({
    queryKey: ['expenses', group.id, q],
    queryFn: () => api.expenses(group.id, { q, size: 100 }),
  });

  const parse = useMutation({
    mutationFn: (text: string) => api.parseExpense(group.id, text),
    onSuccess: (d) => setDraft(d),
  });

  const examples = t('expenses.examples', { returnObjects: true }) as string[];

  const byDay = new Map<string, Expense[]>();
  expenses.data?.items.forEach((e) => byDay.set(e.date, [...(byDay.get(e.date) ?? []), e]));


  return (
    <div className="flex flex-col gap-4">
      {aiEnabled ? (
        <AiTextForm
          onSubmit={(text) => parse.mutate(text)}
          disabled={parse.isPending}
          examples={examples}
        />
      ) : (
        <p className="text-[var(--muted)] text-[13px]">{t('expenses.aiDisabled')}</p>
      )}
      {parse.error && <p className="text-[var(--neg)] text-[13px]">{parse.error.message}</p>}

      <div className="grid grid-cols-2 gap-2">
        <Button variant="ghost" className={ACTION_CARD} onClick={() => setCreating(true)}>
          <PenLine size={22} className="text-[var(--primary)]" />
          <span>{t('expenses.manualEntry')}</span>
        </Button>
        <Button
          variant="ghost"
          className={ACTION_CARD}
          disabled={!aiEnabled}
          onClick={aiEnabled ? () => setScanning(true) : undefined}
        >
          <Camera size={22} className="text-[var(--primary)]" />
          <span>{t('expenses.scanReceipt')}</span>
        </Button>
      </div>

      <div className="relative">
        <span className="absolute left-3 top-1/2 -translate-y-1/2 text-[var(--muted)] pointer-events-none flex">
          <Search size={ICON_SIZE} />
        </span>
        <input className={`${cls.input} pl-[36px]`} type="search" placeholder={t('expenses.searchPlaceholder')} value={q} onChange={(e) => setQ(e.target.value)} />
      </div>

      {expenses.isLoading && <div className={`${cls.skeleton} h-[120px] rounded-[var(--radius)]`} />}
      {expenses.data?.items.length === 0 && (
        <div className={`${cls.card} py-12 px-5 text-center flex flex-col items-center gap-[10px] text-[var(--muted)]`}>
          <Search size={28} />
          <strong className="text-[15px] text-[var(--text-2)]">{q ? t('expenses.emptySearch') : t('expenses.empty')}</strong>
        </div>
      )}

      {[...byDay.entries()].map(([day, list]) => (
        <section key={day}>
          <h3 className="after:hidden mx-0.5 mt-[10px] mb-2 capitalize text-[12px] font-bold text-[var(--muted)] tracking-[0.04em]">{longDate(day)}</h3>
          <div className={`${cls.card} overflow-hidden`}>
            {list.map((e) => {
              const myShare = e.shares.find((share) => share.memberId === group.myMemberId)?.amountCents ?? 0;
              const iPaid = e.payerMemberId === group.myMemberId;
              return (
                <Button
                  key={e.id}
                  variant="ghost"
                  className="w-full !justify-start !gap-3 !p-[13px_16px] !border-x-0 !border-t-0 !border-b !border-b-[var(--border)] !rounded-none !bg-transparent !text-inherit !font-[inherit] !text-left [&:hover:not(:disabled)]:!bg-[var(--primary-dim)] [&:hover:not(:disabled)]:!border-b-[var(--border)] last:!border-b-0"
                  onClick={() => setEditing(e)}
                >
                  <span className="text-[11px] font-bold py-[3px] px-2 rounded-[6px] bg-[var(--surface)] border border-[var(--border)] text-[var(--muted)] whitespace-nowrap shrink-0 tracking-[0.02em] uppercase">
                    {e.categoryLabel}
                  </span>
                  <span className="grow min-w-0 flex flex-col gap-0.5">
                    <strong>{e.description}</strong>
                    <span className="text-[var(--muted)] text-[13px]">
                      {iPaid ? t('expenses.youPaid') : t('expenses.memberPaid', { name: memberName(group, e.payerMemberId) })}
                      {e.source !== 'MANUAL' && (
                        <span className="inline-flex items-center gap-[3px] ml-1.5 text-[11px] py-[2px] px-[7px] rounded-full bg-[var(--accent-soft)] text-[var(--accent)] font-semibold border border-[rgba(251,191,36,0.15)]">
                          {e.source === 'AI_RECEIPT' ? '📸' : <Sparkles size={10} />}
                        </span>
                      )}
                    </span>
                  </span>
                  <span className="flex flex-col items-end gap-0.5 [font-variant-numeric:tabular-nums]">
                    <strong>{euros(e.amountCents)}</strong>
                    <span className={`text-[13px] ${iPaid ? 'text-[var(--pos)]' : myShare ? 'text-[var(--neg)]' : 'text-[var(--muted)]'}`}>
                      {iPaid ? `+${euros(e.amountCents - myShare)}` : myShare ? `-${euros(myShare)}` : t('expenses.notConcerned')}
                    </span>
                  </span>
                </Button>
              );
            })}
          </div>
        </section>
      ))}

      {(creating || editing || draft) && (
        <ExpenseForm
          {...props}
          expense={editing ?? undefined}
          draft={draft ?? undefined}
          onClose={() => {
            setCreating(false);
            setEditing(null);
            setDraft(null);
          }}
        />
      )}
      {scanning && <ReceiptScanner {...props} onClose={() => setScanning(false)} />}
    </div>
  );
}