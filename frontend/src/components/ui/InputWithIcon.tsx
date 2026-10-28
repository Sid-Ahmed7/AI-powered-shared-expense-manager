import { ICON_SIZE } from "@/constants/components";
import { cls } from "@/lib/cls";
import type { ElementType } from "react";
import type React from "react";


interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
    icon: ElementType
}

export function InputWithIcon({ icon: Icon, className, ...props }: InputProps) {
    return (
        <div className="relative">
        <span className="absolute left-[13px] top-1/2 -translate-y-1/2 text-[var(--muted)] pointer-events-none flex">
            <Icon size={ICON_SIZE} />
        </span>
        <input {...props} className={`${cls.input} pl-[38px] ${className ?? ''}`} />
        </div>
    );
}

