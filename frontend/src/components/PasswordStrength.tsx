import { ShieldCheck, ShieldAlert, Shield } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import {PASSWORD_PATTERNS, PASSWORD_SCORE_CONFIGS, PASSWORD_MIN_LENGTH, PASSWORD_STRONG_LENGTH, PASSWORD_EXCELLENT_LENGTH, PASSWORD_MIN_VARIETY} from '@/constants/password';

const ICONS = [ShieldAlert, Shield, ShieldCheck, ShieldCheck] as const;

export function PasswordStrength({ password, touched }: { password: string; touched?: boolean }) {
  const { t } = useTranslation();
  if (!password) return null;

  const length = [...password].length;
  const variety = PASSWORD_PATTERNS.filter((r) => r.test(password)).length;
  const score = length < PASSWORD_MIN_LENGTH ? 0 : length >= PASSWORD_EXCELLENT_LENGTH ? 3 : variety >= PASSWORD_MIN_VARIETY || length >= PASSWORD_STRONG_LENGTH ? 2 : 1;

  const labels = [
    t('auth.passwordStrength.tooShort', { length, min: PASSWORD_MIN_LENGTH }),
    t('auth.passwordStrength.fair'),
    t('auth.passwordStrength.strong'),
    t('auth.passwordStrength.excellent'),
  ];

  const cfg = PASSWORD_SCORE_CONFIGS[score];
  const Icon = ICONS[score];
  const label = labels[score];
  const showError = score === 0 && touched;

  return (
    <div className="flex flex-col gap-1.5">
      <div className="h-1 bg-[var(--border)] rounded overflow-hidden">
        <div className="h-full rounded transition-[width,background] duration-300 ease-in-out" style={{width: cfg.width, background: cfg.color, boxShadow: score >= 2 ? `0 0 8px ${cfg.color}40` : undefined}}/>
      </div>
      <div className="flex items-center gap-[5px] text-xs font-semibold transition-colors duration-300" style={{ color: showError ? 'var(--neg)' : score === 0 ? 'var(--muted)' : cfg.color }}>
        <Icon size={12} />
        {score === 0 ? label : (
            <span style={{background: cfg.bg, padding: '2px 8px', borderRadius: 10, border: `1px solid ${cfg.color}30`}}>
              {label}
            </span>
          )
        }
      </div>
    </div>
  );
}
