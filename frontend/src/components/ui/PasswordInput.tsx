import { useState } from "react";
import { Eye, EyeClosed, Lock } from "lucide-react";
import { cls } from "@/lib/cls";
import { Button } from "@/components/ui/Button";
import { ICON_SIZE, INPUT_LENGTH } from "@/constants/components";

interface PasswordInputProps {
    value: string;
    onChange: (value: string) => void;
    autoComplete?: string;
    placeholder?: string;
    hasError?: boolean;
}

export function PasswordInput(props : PasswordInputProps) {
    const [showPassword, setShowPassword] = useState(false);
    return (
    <div className="relative">
      <span className="absolute left-[13px] top-1/2 -translate-y-1/2 text-[var(--muted)] pointer-events-none flex">
        <Lock size={ICON_SIZE} />
      </span>
      <input
        className={`${cls.input} pl-[38px] pr-[44px] ${props.hasError ? 'border-[var(--neg)] focus:border-[var(--neg)]' : ''}`}
        type={showPassword ? 'text' : 'password'}
        value={props.value}
        onChange={(e) => props.onChange(e.target.value)}
        autoComplete={props.autoComplete}
        placeholder={props.placeholder ?? '**********'}
        maxLength={INPUT_LENGTH}
      />
      <Button
        type="button"
        variant="icon"
        size="xs"
        onClick={() => setShowPassword((show) => !show)}
        className="absolute right-3 top-1/2 -translate-y-1/2"
      >
        {showPassword ? <EyeClosed size={ICON_SIZE} /> : <Eye size={ICON_SIZE} />}
      </Button>
    </div>
  );
}