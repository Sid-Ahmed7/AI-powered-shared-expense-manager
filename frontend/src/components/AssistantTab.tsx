import { useEffect, useRef, useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { Sparkles, Bot } from 'lucide-react';
import { api, type Category, type ChatTurn, type GroupDetail } from '@/api';
import { EmptyState } from '@/components/ui/EmptyState';
import { Chip } from '@/components/ui/Chip';
import { ChatInputForm } from '@/components/form/ChatInputForm';
import { BUBBLE_ASSISTANT, BUBBLE_USER, ICON_SIZE } from '@/constants/components';


interface AssistantTabProps {
  group: GroupDetail;
  categories: Category[];
  aiEnabled: boolean;
}

export function AssistantTab({ group, aiEnabled }: AssistantTabProps) {
  const { t } = useTranslation();
  const [messages, setMessages] = useState<Array<ChatTurn & { tools?: string[] }>>([]);
  const endRef = useRef<HTMLDivElement>(null);

  const ask = useMutation({
    mutationFn: (question: string) =>
      api.ask(group.id, question, messages.map(({ role, content }) => ({ role, content }))),
    onSuccess: (res) => setMessages((message) => [...message, { role: 'assistant', content: res.answer, tools: res.toolsUsed }]),
    onError: (e) => setMessages((message) => [...message, { role: 'assistant', content: '⚠️ ' + e.message }]),
  });

  useEffect(() => { endRef.current?.scrollIntoView({ behavior: 'smooth' }); }, [messages, ask.isPending]);

  const send = (question: string) => {
    if (!question.trim() || ask.isPending) return;
    setMessages((m) => [...m, { role: 'user', content: question.trim() }]);
    ask.mutate(question.trim());
  };

  if (!aiEnabled) {
    return <EmptyState icon={Bot} title={t('ai.requiresKey')} />;
  }

  const suggestions = t('ai.suggestions', { returnObjects: true }) as string[];
  const toolLabels = t('ai.tools', { returnObjects: true }) as Record<string, string>;

  return (
    <div className="flex flex-col h-[calc(100vh-180px)] min-h-[420px]">
      <div className="flex-1 overflow-y-auto flex flex-col gap-[10px] py-1 pb-3 scroll-smooth">
        {messages.length === 0 && (
          <div className="text-[var(--muted)] text-[14px]">
            <div className="flex items-center gap-2 mb-2.5">
              <div className="w-8 h-8 rounded-[10px] bg-[var(--accent-soft)] border border-[rgba(251,191,36,0.2)] grid place-items-center">
                <Sparkles size={ICON_SIZE} className="text-[var(--accent)]" />
              </div>
              <span className="font-semibold text-sm">Assistant Pactole</span>
            </div>
            <p>{t('ai.intro', { group: group.name })}</p>
            <div className="flex flex-wrap gap-1.5 mt-2">
              {suggestions.map((s) => (
                <Chip key={s} onClick={() => send(s)}>{s}</Chip>
              ))}
            </div>
          </div>
        )}
        {messages.map((message, i) => (
          <div key={i} className={message.role === 'user' ? BUBBLE_USER : BUBBLE_ASSISTANT}>
            <div>{message.content}</div>
            {message.tools && message.tools.length > 0 && (
              <div className="mt-1.5 text-[11px] text-[var(--muted)] opacity-80">
                <Sparkles size={10} className="inline mr-1" />
                {t('ai.consulted')} {[...new Set(message.tools)].map((tool) => toolLabels[tool] ?? tool).join(', ')}
              </div>
            )}
          </div>
        ))}
        {ask.isPending && (
          <div className={`${BUBBLE_ASSISTANT} animate-pulse flex items-center gap-1.5`}>
            <Sparkles size={12} className="text-[var(--accent)]" />
            <span>{t('ai.generating', '…')}</span>
          </div>
        )}
        <div ref={endRef} />
      </div>

      <ChatInputForm
        onSubmit={send}
        disabled={ask.isPending}
      />
    </div>
  );
}