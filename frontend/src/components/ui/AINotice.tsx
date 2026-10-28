import { ICON_SIZE } from "@/constants/components";
import { Sparkles } from "lucide-react";

interface AINoticeProps {
  title?: string;
  items: string[];
}

export function AINotice(props: AINoticeProps) {
  if (props.items.length === 0) return null;
  return (
    <div className="bg-[linear-gradient(135deg,var(--accent-soft),rgba(58,45,11,0.5))] border border-[rgba(251,191,36,0.2)] rounded-[var(--radius-sm)] p-[12px_14px] text-[13px]">
      {props.title && (
        <div className="flex items-center gap-1.5 mb-1.5">
          <Sparkles size={ICON_SIZE} className="text-[var(--accent)]" />
          <strong className="text-[13px]">{props.title}</strong>
        </div>
      )}
      <ul className="m-0 pl-4">
        {props.items.map((item) => (
          <li key={item}>{item}</li>
        ))}
      </ul>
    </div>
  );
}