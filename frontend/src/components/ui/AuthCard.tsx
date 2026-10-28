
interface AuthCardProps {
  children: React.ReactNode;
}

export function AuthCard(props: AuthCardProps) {
  return (
    <div className="w-full max-w-[400px] p-[28px_28px_24px] bg-[var(--card)] border border-[var(--border)] rounded-[var(--radius-lg)] shadow-[var(--shadow)]">
      {props.children}
    </div>
  );
}