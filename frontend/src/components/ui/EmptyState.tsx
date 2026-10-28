import type { ElementType } from "react";

interface EmptyStateProps {
  icon: ElementType;
  title: string;
  description?: string;
  action?: React.ReactNode;
}

export function EmptyState({ icon: Icon, title, description, action }: EmptyStateProps) {
  return (
    <div className="bg-[var(--card)] border border-[var(--border)] rounded-[var(--radius)] py-10 px-5 text-center max-w-sm">
      <Icon size={32} className="text-[var(--primary)] mb-2.5" />
      <p className="font-semibold mb-1">{title}</p>
      {description && <p className="text-[var(--muted)] text-[13px] m-0">{description}</p>}
      {action && <div className="mt-4">{action}</div>}
    </div>
  );
}