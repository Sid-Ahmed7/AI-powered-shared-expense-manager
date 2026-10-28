import { useMemo, useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { Trash2, Check } from 'lucide-react';
import { api, ApiError, type Category, type Expense, type ExpenseDraft, type ExpenseRequest, type GroupDetail, type SplitMode } from '@/api';
import { euros, parseAmount } from '@/utils/money';
import { Modal } from '@/components/Modal';
import { cls } from '@/lib/cls';
import { FieldGroup } from '@/components/ui/FieldGroup';
import { FormLabel } from '@/components/ui/FormLabel';
import { AINotice } from '@/components/ui/AINotice';
import { Button } from '@/components/ui/Button';
import { expenseFormSchema, type ExpenseFormInput } from '@/schemas';
import { ICON_SIZE } from '@/constants/components';
import { today } from '@/utils/date';

interface ExpenseFormProps  {
  expense?: Expense;
  draft?: ExpenseDraft;
  onClose: () => void;
  group: GroupDetail;
  categories: Category[];
}

export function ExpenseForm({ group, categories, expense, draft, onClose }: ExpenseFormProps) {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const initialMode: SplitMode = expense?.splitMode ?? draft?.splitMode ?? 'EQUAL';
  const initialMembers = expense ? expense.shares.map((share) => share.memberId) : draft ? draft.splits.map((split) => split.memberId) : group.members.map((member) => member.id);

  const { register, handleSubmit, watch, getValues, formState: { isValid } } = useForm<ExpenseFormInput>({
    resolver: zodResolver(expenseFormSchema),
    mode: 'onChange',
    defaultValues: {
      description: expense?.description ?? draft?.description ?? '',
      amount: expense ? (expense.amountCents / 100).toFixed(2).replace('.', ',') : draft?.amount != null ? String(draft.amount).replace('.', ',') : '',
      payerMemberId: expense?.payerMemberId ?? draft?.payerMemberId ?? group.myMemberId,
      category: expense?.category ?? draft?.category ?? 'AUTRE',
      date: expense?.date ?? draft?.date ?? today(),
    },
  });

  const [mode, setMode] = useState<SplitMode>(initialMode);
  const [selected, setSelected] = useState<Set<string>>(new Set(initialMembers));
  const [values, setValues] = useState<Record<string, string>>(() => {
    const init: Record<string, string> = {};
    expense?.shares.forEach((share) => share.weight != null && (init[share.memberId] = String(share.weight).replace('.', ',')));
    draft?.splits.forEach((split) => split.value != null && (init[split.memberId] = String(split.value).replace('.', ',')));
    return init;
  });
  const [duplicate, setDuplicate] = useState<string | null>(null);

  const watchedAmount = watch('amount');
  const parsedAmount = parseAmount(watchedAmount);
  const sum = useMemo(() => [...selected].reduce((acc, id) => acc + (Number((values[id] ?? '').replace(',', '.')) || 0), 0),[selected, values]);

  const modeHint = (splitMode: SplitMode): string => {
    if (splitMode === 'EXACT') return '€';
    if (splitMode === 'PERCENT') return '%';
    if (splitMode === 'SHARES') return t('expenses.sharesHint');
    return '';
  };

  const buildBody = (force: boolean): ExpenseRequest => {
    const { description, amount, payerMemberId, category, date } = getValues();
    const body: ExpenseRequest = {
      description: description.trim(),
      amount: parseAmount(amount)!,
      payerMemberId,
      category,
      date,
      splitMode: mode,
      source: expense?.source ?? (draft ? 'AI_TEXT' : 'MANUAL'),
      version: expense?.version,
      force,
    };
    if (mode === 'ITEMIZED') {
      body.items = expense?.items?.map((i) => ({ label: i.label, amount: (i.amountCents / 100).toFixed(2), memberIds: i.memberIds }));
    } else {
      body.splits = [...selected].map((id) => ({
        memberId: id,
        value: mode === 'EQUAL' ? null : (values[id] ?? '0').replace(',', '.'),
      }));
    }
    return body;
  };

  const save = useMutation({
    mutationFn: (force: boolean) => {
      const body = buildBody(force);
      return expense ? api.updateExpense(group.id, expense.id, body) : api.createExpense(group.id, body);
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['expenses', group.id] });
      void queryClient.invalidateQueries({ queryKey: ['balances', group.id] });
      void queryClient.invalidateQueries({ queryKey: ['recap', group.id] });
      void queryClient.invalidateQueries({ queryKey: ['groups'] });
      onClose();
    },
    onError: (e) => {
      if (e instanceof ApiError && e.body?.code === 'DUPLICATE_SUSPECTED') setDuplicate(e.message);
    },
  });

  const remove = useMutation({
    mutationFn: () => api.deleteExpense(group.id, expense!.id),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['expenses', group.id] });
      void queryClient.invalidateQueries({ queryKey: ['balances', group.id] });
      void queryClient.invalidateQueries({ queryKey: ['recap', group.id] });
      void queryClient.invalidateQueries({ queryKey: ['groups'] });
      onClose();
    },
  });

  const toggle = (id: string) => {
    const next = new Set(selected);
    if (next.has(id)) next.delete(id);
    else next.add(id);
    setSelected(next);
  };

  const target = mode === 'PERCENT' ? 100 : mode === 'EXACT' && parsedAmount ? Number(parsedAmount) : null;
  const sumOk = target === null || Math.abs(sum - target) < 0.005;
  const valid = isValid && !!parsedAmount && (mode === 'ITEMIZED' || selected.size > 0) && sumOk;

  return (
    <Modal title={expense ? t('expenses.edit') : t('expenses.new')} onClose={onClose}>
      <form className="flex flex-col gap-4" onSubmit={handleSubmit(() => { setDuplicate(null); save.mutate(false); })}>
        {draft && (
          <AINotice
            title={t('expenses.aiPrefilled')}
            items={[...draft.questions, ...draft.warnings]}
          />
        )}

        <div className="flex flex-col gap-[14px] p-4 bg-[var(--surface)] border border-[var(--border)] rounded-[var(--radius-sm)]">
          <FieldGroup label={t('expenses.description')}>
            <input {...register('description')} className={cls.input} maxLength={200} autoFocus={!draft} placeholder="Ex : Pizza entre amis" />
          </FieldGroup>
          <div className="flex items-center gap-[10px] flex-wrap">
            <FieldGroup label={t('expenses.amount')} className="grow min-w-0">
              <div className="relative">
                <span className="absolute left-3 top-1/2 -translate-y-1/2 text-[var(--muted)] text-[14px] font-semibold pointer-events-none">€</span>
                <input {...register('amount')} className={`${cls.input} pl-7`} inputMode="decimal" placeholder="0,00" />
              </div>
            </FieldGroup>
            <FieldGroup label={t('expenses.date')}>
              <input {...register('date')} className={cls.input} type="date" />
            </FieldGroup>
          </div>
        </div>

        <div className="flex flex-col gap-[14px] p-4 bg-[var(--surface)] border border-[var(--border)] rounded-[var(--radius-sm)]">
          <div className="flex items-center gap-[10px] flex-wrap">
            <FieldGroup label={t('expenses.paidBy')} className="grow min-w-0">
              <select {...register('payerMemberId')} className={`${cls.input} ${cls.selectArrow}`}>
                {group.members.map((member) => (
                  <option key={member.id} value={member.id}>
                    {member.name}{member.me ? ` (${t('common.me')})` : ''}
                  </option>
                ))}
              </select>
            </FieldGroup>
            <FieldGroup label={t('expenses.category')} className="grow min-w-0">
              <select {...register('category')} className={`${cls.input} ${cls.selectArrow}`}>
                {categories.map((category) => (
                  <option key={category.value} value={category.value}>{category.label}</option>
                ))}
              </select>
            </FieldGroup>
          </div>
        </div>

        {mode === 'ITEMIZED' && expense?.items ? (
          <div className="flex flex-col gap-[7px]">
            <FormLabel>{t('expenses.itemized')}</FormLabel>
            <ul className="list-none m-0 p-0">
              {expense.items.map((i, idx) => (
                <li key={idx} className="grid grid-cols-[1fr_auto] gap-[2px_12px] py-[7px] border-b border-[var(--border)] text-[14px] last:border-b-0">
                  <span>{i.label}</span>
                  <span className="text-[var(--muted)] text-[13px]">{i.memberIds.map((id) => group.members.find((m) => m.id === id)?.name).join(', ')}</span>
                  <strong>{euros(i.amountCents)}</strong>
                </li>
              ))}
            </ul>
            <Button type="button" variant="ghost" size="sm" onClick={() => setMode('EQUAL')}>
              {t('expenses.switchToEqual')}
            </Button>
          </div>
        ) : (
          <div className="flex flex-col gap-[7px]">
            <FormLabel>{t('expenses.splitMode')}</FormLabel>
            <div className={cls.segmented}>
              {(['EQUAL', 'EXACT', 'PERCENT', 'SHARES'] as Exclude<SplitMode, 'ITEMIZED'>[]).map((m) => (
                <button type="button" key={m} className={`${cls.segBtn} ${mode === m ? cls.segBtnActive : ''}`} data-active={mode === m || undefined} onClick={() => setMode(m)}>
                  {t(`expenses.modes.${m}`)}
                </button>
              ))}
            </div>
            <div className="bg-[var(--surface)] border border-[var(--border)] rounded-[var(--radius-sm)] overflow-hidden">
              {group.members.map((member) => {
                const on = selected.has(member.id);
                const hint = modeHint(mode);
                return (
                  <div key={member.id} className={`flex justify-between items-center p-[10px_14px] border-b border-[var(--border)] min-h-[48px] transition-colors hover:bg-[rgba(255,255,255,0.02)] last:border-b-0 ${on ? '' : 'opacity-40'}`}>
                    <label className="flex items-center gap-[10px] font-medium cursor-pointer text-[14px]">
                      <input type="checkbox" checked={on} onChange={() => toggle(member.id)} className="w-[17px] h-[17px] accent-[var(--primary)] cursor-pointer" />
                      {member.name}
                    </label>
                    {mode === 'EQUAL' ? (
                      <span className="text-[var(--muted)] text-[13px]">
                        {on && parsedAmount ? '≈ ' + euros(Math.round((Number(parsedAmount) * 100) / selected.size)) : ''}
                      </span>
                    ) : (
                      on && (
                        <span className="flex items-center gap-[6px]">
                          <input
                            className={`${cls.input} ${cls.inputSm} !w-auto`}
                            inputMode="decimal"
                            value={values[member.id] ?? ''}
                            onChange={(e) => setValues({ ...values, [member.id]: e.target.value })}
                          />
                          <span className="text-[var(--muted)] text-[13px]">{hint}</span>
                        </span>
                      )
                    )}
                  </div>
                );
              })}
            </div>
            {target !== null && (
              <p className={`text-[13px] ${sumOk ? 'text-[var(--muted)]' : 'text-[var(--neg)]'}`}>
                {t('expenses.totalEntered')} {sum.toLocaleString()} / {target.toLocaleString()} {mode === 'PERCENT' ? '%' : '€'}
              </p>
            )}
          </div>
        )}

        {duplicate && (
          <div className="bg-[var(--neg-soft)] border border-[rgba(248,113,113,0.2)] rounded-[var(--radius-sm)] p-[12px_14px]">
            <p>{duplicate}</p>
            <Button type="button" variant="ghost" size="sm" onClick={() => save.mutate(true)}>
              {t('expenses.saveAnyway')}
            </Button>
          </div>
        )}
        {save.error && !duplicate && <p className="text-[var(--neg)]">{save.error.message}</p>}

        <div className="flex items-center gap-[10px]">
          {expense && (
            <Button
              type="button"
              variant="danger"
              onClick={() => confirm(t('expenses.deleteConfirm')) && remove.mutate()}
            >
              <Trash2 size={ICON_SIZE} />
              {t('common.delete')}
            </Button>
          )}
          <span className="grow min-w-0" />
          <Button type="submit" disabled={!valid} loading={save.isPending}>
            <Check size={ICON_SIZE} />
            {t('common.save')}
          </Button>
        </div>
      </form>
    </Modal>
  );
}
