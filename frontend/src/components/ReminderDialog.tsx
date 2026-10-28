import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { Share2 } from 'lucide-react';
import { api, type Tone, type Transfer } from '@/api';
import type { GroupDetail } from '@/api';
import { Modal } from '@/components/Modal';
import { memberName } from '@/utils/members';
import { cls } from '@/lib/cls';
import { Button } from '@/components/ui/Button';
import { ICON_SIZE, TONES } from '@/constants/components';

interface ReminderDialogProps {
  group: GroupDetail;
  transfer: Transfer;
  onClose: () => void;
}

export function ReminderDialog({ group, transfer, onClose }: ReminderDialogProps) {
  const { t } = useTranslation();
  const [activeTone, setActiveTone] = useState<Tone>('SYMPA');
  const [copied, setCopied] = useState(false);

  const reminder = useMutation({
    mutationFn: (selected: Tone) => api.reminder(group.id, { ...transfer, tone: selected }),
  });

  const generate = (selected: Tone) => {
    setActiveTone(selected);
    setCopied(false);
    reminder.mutate(selected);
  };

  const share = async () => {
    const text = reminder.data?.message ?? '';
    if (navigator.share) {
      await navigator.share({ text }).catch(() => {});
    } else {
      await navigator.clipboard.writeText(text);
      setCopied(true);
    }
  };

  return (
    <Modal title={t('balance.remindTitle', { name: memberName(group, transfer.fromMemberId) })} onClose={onClose}>
      <div className="flex flex-col gap-4">
        <p className="text-[var(--muted)] text-[13px]">{t('balance.remindHint')}</p>
        <div className={cls.segmented}>
          
          {(TONES).map((option) => (
            <Button
              key={option}
              variant="seg"
              data-active={(option === activeTone && reminder.data) || undefined}
              className={option === activeTone && reminder.data ? cls.segBtnActive : ''}
              onClick={() => generate(option)}
            >
              {t(`tone.${option}`)}
            </Button>
          ))}
        </div>
        {reminder.isPending && <p className="text-[var(--muted)] text-center">{t('ai.generating')}</p>}
        {reminder.data && (
          <blockquote className="m-0 p-[14px_16px] bg-[var(--surface)] border border-[var(--border)] [border-left:3px_solid_var(--primary)] rounded-[0_var(--radius-sm)_var(--radius-sm)_0] text-[14px] not-italic leading-[1.6] text-[var(--text-2)]">
            {reminder.data.message}
          </blockquote>
        )}
        {reminder.error && <p className="text-[var(--neg)]">{reminder.error.message}</p>}
        {reminder.data && (
          <Button onClick={share}>
            <Share2 size={ICON_SIZE} />
            {copied ? t('common.copied') : t('common.share')}
          </Button>
        )}
      </div>
    </Modal>
  );
}