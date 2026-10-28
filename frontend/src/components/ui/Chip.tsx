import { OFF, ON } from "@/constants/components";

interface ChipProps {
  active?: boolean;
  onClick?: () => void;
  children: React.ReactNode;
}



export function Chip({ active = false, onClick, children }: ChipProps) {
  return (
    <button type="button" className={active ? ON : OFF} onClick={onClick}>
      {children}
    </button>
  );
}