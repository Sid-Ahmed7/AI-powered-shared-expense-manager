import { ChevronLeft, ChevronRight } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { ACTIVE, BTN, ICON_SIZE } from '@/constants/components';

interface PaginationProps {
  page: number;
  totalPages: number;
  onPage: (p: number) => void;
  from?: number;
  to?: number;
  total?: number;
}

export function Pagination({ page, totalPages, onPage, from, to, total }: PaginationProps) {
  if (totalPages <= 1) return null;

  const pages: (number | '…')[] = [];
  if (totalPages <= 7) {
    for (let i = 0; i < totalPages; i++) pages.push(i);
  } else {
    pages.push(0);
    if (page > 2) pages.push('…');
    for (let i = Math.max(1, page - 1); i <= Math.min(totalPages - 2, page + 1); i++) pages.push(i);
    if (page < totalPages - 3) pages.push('…');
    pages.push(totalPages - 1);
  }



  return (
    <div className="flex items-center justify-between gap-3 pt-3.5 mt-1 border-t border-[var(--border)]">
      {from != null && to != null && total != null && (
        <span className="text-xs text-[var(--muted)] whitespace-nowrap">{from}–{to} / {total}</span>
      )}
      <div className="flex items-center gap-0.5 ml-auto">
        <Button variant="ghost" className={BTN} disabled={page === 0} onClick={() => onPage(page - 1)} aria-label="Page précédente">
          <ChevronLeft size={ICON_SIZE} />
        </Button>
        {pages.map((p, i) =>
          p === '…' ? (
            <span key={`e-${i}`} className="text-[13px] text-[var(--muted)] px-1 select-none">…</span>
          ) : (
            <Button key={p} variant="ghost" className={`${BTN}${p === page ? ` ${ACTIVE}` : ''}`} onClick={() => onPage(p)}>
              {p + 1}
            </Button>
          )
        )}
        <Button variant="ghost" className={BTN} disabled={page === totalPages - 1} onClick={() => onPage(page + 1)} aria-label="Page suivante">
          <ChevronRight size={ICON_SIZE} />
        </Button>
      </div>
    </div>
  );
}
