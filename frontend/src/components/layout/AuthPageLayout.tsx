interface AuthPageLayoutProps {
  children: React.ReactNode;
}

export function AuthPageLayout(props: AuthPageLayoutProps) {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center px-4 py-8 gap-7 bg-[radial-gradient(ellipse_60%_40%_at_50%_0%,rgba(249,115,22,0.08)_0%,transparent_65%),radial-gradient(ellipse_40%_30%_at_80%_80%,rgba(251,191,36,0.04)_0%,transparent_60%),var(--bg)]">
      {props.children}
    </div>
  );
}