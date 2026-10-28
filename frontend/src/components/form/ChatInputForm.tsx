import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useTranslation } from 'react-i18next';
import { Send } from 'lucide-react';
import { cls } from '@/lib/cls';
import { Button } from '@/components/ui/Button';
import { chatInputSchema, type ChatInputData } from '@/schemas';
import { ICON_SIZE } from '@/constants/components';

interface ChatInputFormProps {
  onSubmit: (question: string) => void;
  disabled?: boolean;
  placeholder?: string;
}

export function ChatInputForm({ onSubmit, disabled = false, placeholder }: ChatInputFormProps) {
  const { t } = useTranslation();
  const { register, handleSubmit, reset, watch } = useForm<ChatInputData>({resolver: zodResolver(chatInputSchema),defaultValues: { question: '' }});

  const question = watch('question');

  const submit = handleSubmit((data) => {
    onSubmit(data.question.trim());
    reset();
  });

  return (
    <form className="flex gap-2 pt-3 border-t border-[var(--border)] items-center" onSubmit={submit}>
      <input
        {...register('question')}
        className={`${cls.input} grow`}
        placeholder={placeholder ?? t('ai.questionPlaceholder')}
        maxLength={500}
        disabled={disabled}
      />
      <Button type="submit" className="shrink-0" disabled={!question?.trim() || disabled} aria-label={t('common.send')}>
        <Send size={ICON_SIZE} />
      </Button>
    </form>
  );
}