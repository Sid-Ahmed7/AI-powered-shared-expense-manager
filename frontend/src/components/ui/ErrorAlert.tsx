interface ErrorAlertProps {
    message: string | null;
}

export function ErrorAlert(props: ErrorAlertProps) {
  if (!props.message) return null;
  return (
    <div className="px-[14px] py-[10px] bg-[var(--neg-soft)] border border-[rgba(248,113,113,0.25)] rounded-[var(--radius-sm)] text-sm text-[var(--neg)]">
      {props.message}
    </div>
  );
}