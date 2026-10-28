interface FormLabelProps {
  children: React.ReactNode;
  right?: React.ReactNode;
}

export function FormLabel(props: FormLabelProps) {
  return (
    <span className="text-[12px] font-bold tracking-[0.04em] uppercase text-[var(--muted)] flex items-center justify-between">
      <span>{props.children}</span>
      {props.right}
    </span>
  );
}