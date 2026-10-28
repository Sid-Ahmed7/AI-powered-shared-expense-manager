import { ICON_SIZE } from '@/constants/components';
import { Flame } from 'lucide-react';

interface SidebarProps {
  subheader?: React.ReactNode;
  nav: React.ReactNode;
  footer: React.ReactNode;
}

export function Sidebar(props: SidebarProps) {
  return (
    <aside className="w-[252px] shrink-0 h-screen bg-[var(--surface)] border-r border-[var(--border)] flex flex-col sticky top-0 overflow-y-auto">
      <div className="px-4 py-[18px] border-b border-[var(--border)] shrink-0">
        <div className="flex items-center gap-[9px] font-extrabold text-[19px] tracking-[-0.04em] text-[var(--text)] no-underline">
          <span className="inline-grid place-items-center w-8 h-8 rounded-[10px] bg-[linear-gradient(145deg,#fb923c,#ea580c)] text-white shrink-0 shadow-[var(--glow)] transition-shadow">
            <Flame size={ICON_SIZE} />
          </span>
          Fendly
        </div>
      </div>
      {props.subheader && (
        <div className="px-4 py-3 pb-3.5 border-b border-[var(--border)] shrink-0">
          {props.subheader}
        </div>
      )}
      <nav className="flex-1 p-2 flex flex-col gap-0.5 overflow-y-auto min-h-0">
        {props.nav}
      </nav>
      <div className="p-2 pb-3.5 border-t border-[var(--border)] flex flex-col gap-0.5 shrink-0">
        {props.footer}
      </div>
    </aside>
  );
}