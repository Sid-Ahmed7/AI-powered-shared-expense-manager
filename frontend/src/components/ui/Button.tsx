import type React from 'react';
import { cls } from '@/lib/cls';
import type { ButtonSize, ButtonVariant } from '@/types/components';
import { VARIANT_CLASS } from '@/constants/components';

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  size?: ButtonSize;
  loading?: boolean;
  fullWidth?: boolean;
}


export function Button({variant = 'primary', size = 'default', loading = false, fullWidth = false, disabled, className, children, ...props}: ButtonProps) {
  return (
    <button
      {...props}
      disabled={disabled ?? loading}
      className={[
        VARIANT_CLASS[variant],
        size === 'sm' ? cls.btnSm : '',
        size === 'xs' ? 'p-1' : '',
        fullWidth ? 'w-full' : '',
        className ?? '',
      ].filter(Boolean).join(' ')}
    >
      {loading ? '…' : children}
    </button>
  );
}