import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useTranslation } from 'react-i18next';
import { UserPlus } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { addMemberSchema, type AddMemberInput } from '@/schemas';
import { ICON_SIZE } from '@/constants/components';

interface AddMemberFormProps {
  onSubmit: (name: string) => void;
  disabled?: boolean;
}

export function AddMemberForm({ onSubmit, disabled = false }: AddMemberFormProps) {
  const { t } = useTranslation();
  const { register, handleSubmit, watch, reset } = useForm<AddMemberInput>({resolver: zodResolver(addMemberSchema),defaultValues: { name: '' }});

  const name = watch('name');

  const submit = handleSubmit((data) => {
    onSubmit(data.name.trim());
    reset();
  });

  return (
    <form onSubmit={submit}>
      <div className="flex gap-2 bg-[var(--surface)] border border-[var(--border)] rounded-[var(--radius)] p-[6px_6px_6px_14px] items-center transition-all focus-within:border-[var(--border-focus)] focus-within:shadow-[0_0_0_3px_var(--primary-dim)]">
        <UserPlus size={ICON_SIZE} className="text-[var(--muted)] shrink-0" />
        <input
          {...register('name')}
          className="flex-1 border-0 bg-transparent font-[inherit] text-[var(--text)] py-2 min-w-0 text-[14px] tracking-[0.06em] placeholder:text-[var(--muted)] focus:outline-none"
          placeholder={t('members.addPlaceholder')}
          maxLength={40}
          disabled={disabled}
        />
        <Button type="submit" variant="ghost" size="sm" disabled={!name?.trim() || disabled} loading={disabled}>
          {t('common.add')}
        </Button>
      </div>
    </form>
  );
}