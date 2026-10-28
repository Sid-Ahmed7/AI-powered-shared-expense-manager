import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useTranslation } from 'react-i18next';
import { Sparkles } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { Chip } from '@/components/ui/Chip';
import { parseSchema, type ParseInput } from '@/schemas';
import { ICON_SIZE } from '@/constants/components';

interface AiTextFormProps {
  onSubmit: (text: string) => void;
  disabled?: boolean;
  examples?: string[];
}

export function AiTextForm({ onSubmit, disabled = false, examples }: AiTextFormProps) {
  const { t } = useTranslation();
  const { register, handleSubmit, watch, reset, setValue } = useForm<ParseInput>({resolver: zodResolver(parseSchema),defaultValues: { text: '' }});

  const text = watch('text');

  const submit = handleSubmit((data) => {
    onSubmit(data.text.trim());
    reset();
  });

  return (
    <div className="flex flex-col gap-2">
      <form
        className="flex items-center gap-[10px] p-[8px_8px_8px_14px] bg-[linear-gradient(135deg,var(--surface),var(--surface-2))] border-[1.5px] border-[rgba(251,191,36,0.25)] rounded-[var(--radius)] shadow-[0_0_20px_rgba(251,191,36,0.06)] transition-all focus-within:border-[rgba(251,191,36,0.5)] focus-within:shadow-[0_0_28px_rgba(251,191,36,0.12)]"
        onSubmit={submit}
      >
        <Sparkles size={ICON_SIZE} className="text-[var(--accent)] shrink-0" />
        <input
          {...register('text')}
          className="flex-1 border-0 bg-transparent font-[inherit] text-[14px] text-[var(--text)] py-2 min-w-0 placeholder:text-[var(--muted)] focus:outline-none"
          placeholder={t('expenses.aiPlaceholder')}
          maxLength={500}
          disabled={disabled}
        />
        <Button size="sm" disabled={!text?.trim() || disabled} loading={disabled}>
          {t('common.add')}
        </Button>
      </form>
      {examples && examples.length > 0 && !text && (
        <div className="flex flex-wrap gap-1.5">
          {examples.map((ex) => (
            <Chip key={ex} onClick={() => setValue('text', ex)}>{ex}</Chip>
          ))}
        </div>
      )}
    </div>
  );
}