import { FormLabel } from "@/components/ui/FormLabel";

interface FieldGroupProps {
  children: React.ReactNode;
  right?: React.ReactNode;
  label: string;
  className?: string;
}

export function FieldGroup({ label, right, className, children }: FieldGroupProps) {
  return (
    <label className={['flex flex-col gap-[7px]', className].filter(Boolean).join(' ')}>
      <FormLabel right={right}>{label}</FormLabel>
      {children}
    </label>
  );
}