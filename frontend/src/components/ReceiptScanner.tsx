import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { Capacitor } from '@capacitor/core';
import { Camera as CameraIcon, Image as GalleryIcon, Sparkles, X, Plus } from 'lucide-react';
import { api, type GroupDetail, type ReceiptDraft } from '@/api';
import { euros, parseAmount } from '@/utils/money';
import { Modal } from '@/components/Modal';
import { cls } from '@/lib/cls';
import { Button } from '@/components/ui/Button';
import { Chip } from '@/components/ui/Chip';
import { AINotice } from '@/components/ui/AINotice';
import { FieldGroup } from '@/components/ui/FieldGroup';
import type { EditableItem } from '@/types/components';
import { UPLOAD_AREA } from '@/constants/components';
import { takePhotoNative, pickFromGalleryNative } from '@/utils/scanner';

  
interface ReceiptScannerProps {
  group: GroupDetail;
  onClose: () => void;
  aiEnabled: boolean;
}

export function ReceiptScanner({ group, onClose }: ReceiptScannerProps) {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const allIds = group.members.map((member) => member.id);
  const [draft, setDraft] = useState<ReceiptDraft | null>(null);
  const [items, setItems] = useState<EditableItem[]>([]);
  const [total, setTotal] = useState('');
  const [description, setDescription] = useState('');
  const [payer, setPayer] = useState(group.myMemberId);
  const [preview, setPreview] = useState<string | null>(null);

  const scan = useMutation({
    mutationFn: (file: File) => api.scanReceipt(group.id, file),
    onSuccess: (d) => {
      setDraft(d);
      setItems(d.items.map((i) => ({ label: i.label, amount: i.amount.toFixed(2).replace('.', ','), memberIds: allIds })));
      setTotal(d.total.toFixed(2).replace('.', ','));
      setDescription(d.merchant ?? t('receipt.defaultMerchant'));
    },
  });

  const save = useMutation({
    mutationFn: () =>
      api.createExpense(group.id, {
        description,
        amount: parseAmount(total)!,
        payerMemberId: payer,
        category: draft?.category,
        date: draft?.date,
        splitMode: 'ITEMIZED',
        source: 'AI_RECEIPT',
        force: true,
        items: items
          .filter((i) => parseAmount(i.amount))
          .map((i) => ({ label: i.label, amount: parseAmount(i.amount)!, memberIds: i.memberIds })),
      }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['expenses', group.id] });
      void queryClient.invalidateQueries({ queryKey: ['balances', group.id] });
      onClose();
    },
  });

  const toggle = (index: number, memberId: string) => {
    setItems(
      items.map((item, i) => {
        if (i !== index) return item;
        const has = item.memberIds.includes(memberId);
        return { ...item, memberIds: has ? item.memberIds.filter((id) => id !== memberId) : [...item.memberIds, memberId] };
      }),
    );
  };

  const perMember = new Map<string, number>();
  let itemsTotal = 0;
  for (const item of items) {
    const cents = Math.round(Number(parseAmount(item.amount) ?? 0) * 100);
    itemsTotal += cents;
    if (item.memberIds.length === 0) continue;
    for (const id of item.memberIds) perMember.set(id, (perMember.get(id) ?? 0) + cents / item.memberIds.length);
  }
  const totalCents = Math.round(Number(parseAmount(total) ?? 0) * 100);
  const ratio = itemsTotal > 0 ? totalCents / itemsTotal : 0;
  const unassigned = items.some((i) => i.memberIds.length === 0);

  const uploadContent = preview
    ? <img src={preview} alt={t('receipt.previewAlt')} className="max-w-full max-h-[300px]" />
    : (
      <div className="flex flex-col items-center gap-2.5">
        <CameraIcon size={32} className="text-[var(--primary)]" />
        <span>{t('receipt.uploadHint')}</span>
      </div>
    );

  return (
    <Modal title={t('receipt.title')} onClose={onClose}>
      {!draft && (
        <div className="flex flex-col gap-4">
          {Capacitor.isNativePlatform() ? (
            preview ? (
              <img src={preview} alt={t('receipt.previewAlt')} className="max-w-full max-h-[300px] mx-auto" />
            ) : (
              <div className="flex flex-col gap-2">
                <Button
                  type="button"
                  variant="ghost"
                  className={`${UPLOAD_AREA} ${scan.isPending ? 'animate-pulse' : ''}`}
                  onClick={async () => {
                    const file = await takePhotoNative();
                    if (!file) return;
                    setPreview(URL.createObjectURL(file));
                    scan.mutate(file);
                  }}
                >
                  <div className="flex flex-col items-center gap-2.5">
                    <CameraIcon size={32} className="text-[var(--primary)]" />
                    <span>{t('receipt.takePhoto')}</span>
                  </div>
                </Button>
                <Button
                  type="button"
                  variant="ghost"
                  className={`${UPLOAD_AREA} ${scan.isPending ? 'animate-pulse' : ''}`}
                  onClick={async () => {
                    const file = await pickFromGalleryNative();
                    if (!file) return;
                    setPreview(URL.createObjectURL(file));
                    scan.mutate(file);
                  }}
                >
                  <div className="flex flex-col items-center gap-2.5">
                    <GalleryIcon size={32} className="text-[var(--primary)]" />
                    <span>{t('receipt.chooseFromGallery')}</span>
                  </div>
                </Button>
              </div>
            )
          ) : (
            <label className={`grid place-items-center min-h-[180px] border-2 border-dashed border-[var(--border)] rounded-[var(--radius)] bg-[var(--surface)] cursor-pointer overflow-hidden text-[var(--muted)] transition-all hover:border-[var(--primary)] hover:bg-[var(--primary-dim)] ${scan.isPending ? 'animate-pulse' : ''}`}>
              {uploadContent}
              <input
                type="file"
                accept="image/jpeg,image/png,image/webp"
                capture="environment"
                hidden
                onChange={(e) => {
                  const file = e.target.files?.[0];
                  if (!file) return;
                  setPreview(URL.createObjectURL(file));
                  scan.mutate(file);
                }}
              />
            </label>
          )}
          {scan.isPending && (
            <p className="text-center text-[var(--muted)] flex items-center justify-center gap-1.5">
              <Sparkles size={14} className="text-[var(--accent)]" />
              {t('receipt.scanning')}
            </p>
          )}
          {scan.error && <p className="text-[var(--neg)]">{scan.error.message}</p>}
        </div>
      )}

      {draft && (
        <div className="flex flex-col gap-4">
          <AINotice items={draft.warnings} />

          <div className="flex items-center gap-[10px] flex-wrap">
            <FieldGroup label={t('receipt.description')} className="grow min-w-0">
              <input className={cls.input} value={description} onChange={(e) => setDescription(e.target.value)} />
            </FieldGroup>
            <FieldGroup label={t('receipt.totalPaid')} className="w-[120px]">
              <input className={cls.input} inputMode="decimal" value={total} onChange={(e) => setTotal(e.target.value)} />
            </FieldGroup>
            <FieldGroup label={t('receipt.paidBy')} className="grow min-w-0">
              <select className={`${cls.input} ${cls.selectArrow}`} value={payer} onChange={(e) => setPayer(e.target.value)}>
                {group.members.map((member) => (
                  <option key={member.id} value={member.id}>{member.name}</option>
                ))}
              </select>
            </FieldGroup>
          </div>

          <p className="text-[var(--muted)] text-[13px]">{t('receipt.membersHint')}</p>

          <ul className="list-none m-0 p-0 flex flex-col gap-2">
            {items.map((item, index) => (
              <li key={index} className={`bg-[var(--surface)] border rounded-[var(--radius-sm)] p-[10px_12px] flex flex-col gap-2 ${item.memberIds.length === 0 ? 'border-[var(--neg)]' : 'border-[var(--border)]'}`}>
                <div className="flex items-center gap-[10px]">
                  <input
                    className={`${cls.input} ${cls.inputSm} grow min-w-0`}
                    value={item.label}
                    onChange={(e) => setItems(items.map((it, i) => (i === index ? { ...it, label: e.target.value } : it)))}
                  />
                  <input
                    className={`${cls.input} ${cls.inputSm} !w-[120px]`}
                    inputMode="decimal"
                    value={item.amount}
                    onChange={(e) => setItems(items.map((it, i) => (i === index ? { ...it, amount: e.target.value } : it)))}
                  />
                  <Button
                    variant="icon"
                    onClick={() => setItems(items.filter((_, i) => i !== index))}
                    aria-label="Retirer"
                  >
                    <X size={14} />
                  </Button>
                </div>
                <div className="flex flex-wrap gap-1.5">
                  {group.members.map((member) => (
                    <Chip
                      key={member.id}
                      active={item.memberIds.includes(member.id)}
                      onClick={() => toggle(index, member.id)}
                    >
                      {member.name}
                    </Chip>
                  ))}
                </div>
              </li>
            ))}
          </ul>

          <Button
            variant="ghost"
            size="sm"
            onClick={() => setItems([...items, { label: t('receipt.newItemLabel'), amount: '0,00', memberIds: allIds }])}
          >
            <Plus size={14} />
            {t('receipt.addLine')}
          </Button>

          <div className={`${cls.card} p-[14px_16px] flex flex-col gap-[6px]`}>
            <strong className="text-[13px]">{t('receipt.eachPays')}</strong>
            {group.members.filter((member) => perMember.has(member.id)).map((member) => (
                <div key={member.id} className="flex items-center justify-between mt-1.5">
                  <span className="text-[13px]">{member.name}</span>
                  <strong className="[font-variant-numeric:tabular-nums]">
                    {euros(Math.round((perMember.get(member.id) ?? 0) * ratio))}
                  </strong>
                </div>
              ))}
            {itemsTotal !== totalCents && itemsTotal > 0 && (
              <p className="text-[var(--muted)] text-[13px] mt-2 mb-0">
                {t('receipt.gap', { amount: euros(Math.abs(totalCents - itemsTotal)) })}
              </p>
            )}
          </div>

          {unassigned && <p className="text-[var(--neg)] text-[13px]">{t('receipt.unassigned')}</p>}
          {save.error && <p className="text-[var(--neg)]">{save.error.message}</p>}
          <Button
            fullWidth
            disabled={unassigned || !parseAmount(total) || items.length === 0}
            loading={save.isPending}
            onClick={() => save.mutate()}
          >
            <Sparkles size={15} />
            {t('receipt.saveExpense')}
          </Button>
        </div>
      )}
    </Modal>
  );
}