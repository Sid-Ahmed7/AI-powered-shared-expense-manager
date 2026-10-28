export const SPECIAL_CHARS = '!@#$%^&*()_+\\-=\\[\\]{}|;\':",.<>?/`~';

export const PASSWORD_PATTERNS = [
  /[a-z]/,
  /[A-Z]/,
  /\d/,
  /[!@#$%^&*()_+\-=\[\]{}|;':",.<>?/`~]/,
] as const;

export const PASSWORD_MIN_LENGTH = 12;
export const PASSWORD_STRONG_LENGTH = 16;
export const PASSWORD_EXCELLENT_LENGTH = 20;
export const PASSWORD_MIN_VARIETY = 3;

export const PASSWORD_SCORE_CONFIGS = [
  { color: 'var(--neg)',    bg: 'rgba(248,113,113,0.15)', width: '28%'  },
  { color: 'var(--accent)', bg: 'rgba(251,191,36,0.12)',  width: '55%'  },
  { color: 'var(--pos)',    bg: 'rgba(52,211,153,0.12)',  width: '80%'  },
  { color: 'var(--pos)',    bg: 'rgba(52,211,153,0.15)',  width: '100%' },
] as const;
