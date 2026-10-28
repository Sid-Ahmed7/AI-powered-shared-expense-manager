import { api } from "@/api";
import { createGroupSchema, type CreateGroupInput } from "@/schemas";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { StepDot } from "@/components/ui/StepDot";
import { Flame, Type, UserPlus, Users } from "lucide-react";
import { ICON_SIZE } from "@/constants/components";
import { cls } from "@/lib/cls";
import { ErrorAlert } from "@/components/ui/ErrorAlert";
import { Button } from "@/components/ui/Button";

export function CreateGroupForm() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [members, setMembers] = useState<string[]>(['']);
  const [serverError, setServerError] = useState<string | null>(null);

  const { register, handleSubmit, watch, formState: { isSubmitting } } = useForm<CreateGroupInput>({ resolver: zodResolver(createGroupSchema) });

  const name = watch('name', '');
  const filledMembers = members.filter(Boolean).length;

  const create = useMutation({
    mutationFn: (data: CreateGroupInput) => api.createGroup({ name: data.name, members: members.map((m) => m.trim()).filter(Boolean) }),
    onSuccess: (group) => {
      void queryClient.invalidateQueries({ queryKey: ['groups'] });
      navigate(`/groups/${group.id}`);
    },
    onError: (err) => setServerError((err as Error).message),
  });

  const submit = handleSubmit((data) => {
    setServerError(null);
    create.mutate(data);
  });

  return (
    <form className="flex flex-col gap-4" onSubmit={submit}>
      <div className="flex gap-[14px] items-start">
        <StepDot nb={1} active />
        <div className="flex-1 min-w-0">
          <div className="bg-[var(--card)] border border-[var(--border)] rounded-[var(--radius)] p-[18px]">
            <div className="flex items-center gap-2 mb-[14px]">
              <Type size={ICON_SIZE} className="text-[var(--muted)] shrink-0" />
              <span className="text-xs font-bold text-[var(--muted)] uppercase tracking-[0.08em]">
                {t('groups.nameStep')}
              </span>
            </div>
            <input
              className={`${cls.input} [font-size:16px] font-semibold`}
              placeholder={t('groups.namePlaceholder')}
              autoFocus
              {...register('name')}
            />
          </div>
        </div>
      </div>

      <div className="ml-[13px] w-0.5 h-3 bg-[var(--border)] rounded-sm self-start" />

      <div className="flex gap-[14px] items-start">
        <StepDot nb={2} active={!!name.trim()} />
        <div className="flex-1 min-w-0">
          <div className={`bg-[var(--card)] border border-[var(--border)] rounded-[var(--radius)] p-[18px] transition-opacity duration-200 ${name.trim() ? 'opacity-100' : 'opacity-55'}`}>
            <div className="flex items-center gap-2 mb-1.5">
              <Users size={ICON_SIZE} className="text-[var(--muted)] shrink-0" />
              <span className="text-xs font-bold text-[var(--muted)] uppercase tracking-[0.08em]">
                {t('groups.participantsLabel')}
                {filledMembers > 0 && <span className="ml-2 text-[var(--primary)] font-bold">{filledMembers}</span>}
              </span>
            </div>
            <p className="text-[var(--muted)] text-[13px] mb-[14px]">{t('groups.participantsHint')}</p>
            <div className="flex flex-col gap-2">
              {members.map((m, i) => (
                <div key={i} className="flex items-center gap-2.5">
                  <div className={`w-7 h-7 rounded-full grid place-items-center shrink-0 transition-all ${m ? 'bg-[var(--primary-soft)] border border-[rgba(249,115,22,0.2)]' : 'bg-[var(--surface)] border border-[var(--border)]'}`}>
                    {m ? (
                      <span className="text-[11px] font-extrabold text-[var(--primary)]">{m.charAt(0).toUpperCase()}</span>
                    ) : (
                      <UserPlus size={ICON_SIZE} className="text-[var(--muted)]" />
                    )}
                  </div>
                  <input
                    className={`${cls.input} flex-1`}
                    placeholder={t('groups.memberPlaceholder', { num: i + 1 })}
                    value={m}
                    maxLength={40}
                    disabled={!name.trim()}
                    onChange={(e) => {
                      const next = [...members];
                      next[i] = e.target.value;
                      if (i === members.length - 1 && e.target.value) next.push('');
                      setMembers(next);
                    }}
                  />
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      <ErrorAlert message={serverError} />

      <Button fullWidth loading={isSubmitting} disabled={!name.trim()} className="!py-[13px]">
        <Flame size={ICON_SIZE} />
        {t('groups.createBtn')}
      </Button>
    </form>
  );
}