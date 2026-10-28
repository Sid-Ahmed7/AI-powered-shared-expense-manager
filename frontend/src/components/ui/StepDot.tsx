interface StepDotProps {
  nb: number;
  active: boolean;
}

export function StepDot(props: StepDotProps) {
  return (
    <div className={`w-7 h-7 rounded-full grid place-items-center text-xs font-extrabold shrink-0 mt-0.5 transition-all ${props.active ? 'bg-[var(--primary)] border border-[var(--primary)] text-white shadow-[0_0_12px_rgba(249,115,22,0.3)]' : 'bg-[var(--surface)] border border-[var(--border)] text-[var(--muted)]'}`}>
      {props.nb}
    </div>
  );
}