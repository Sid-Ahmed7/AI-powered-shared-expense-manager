import { Flame } from 'lucide-react';

export function BrandedHeader() {
  return (
    <div className="max-w-[360px] text-center flex flex-col items-center gap-3">
      <div className="inline-grid place-items-center w-16 h-16 rounded-[20px] bg-[linear-gradient(145deg,#fb923c,#ea580c)] text-white shadow-[var(--glow),0_12px_40px_rgba(249,115,22,0.25)] shrink-0">
        <Flame size={30} strokeWidth={2.2} />
      </div>
      <h1 className="text-[32px] bg-[linear-gradient(135deg,var(--text)_30%,var(--primary)_100%)] [-webkit-background-clip:text] [-webkit-text-fill-color:transparent] [background-clip:text]">
        Fendly
      </h1>
    </div>
  );
}