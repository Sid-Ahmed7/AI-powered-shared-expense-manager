import { useTranslation } from 'react-i18next';
import { euros } from '@/utils/money';

interface BalanceBadgeProps { 
    cents: number
}

export function BalanceBadge(props: BalanceBadgeProps) {
  const { t } = useTranslation();
  if (props.cents === 0)
    return (
      <span className="flex flex-col items-end font-bold [font-variant-numeric:tabular-nums] text-[var(--muted)] text-[13px]">
        {t('balance.zero')}
      </span>
    );
  return (
    <span className={`flex flex-col items-end font-bold [font-variant-numeric:tabular-nums] ${props.cents > 0 ? 'text-[var(--pos)]' : 'text-[var(--neg)]'}`}>
      <small className="font-medium text-[10px] tracking-[0.02em]">
        {props.cents > 0 ? t('balance.owedToYou') : t('balance.youOwe')}
      </small>
      {euros(Math.abs(props.cents))}
    </span>
  );
}