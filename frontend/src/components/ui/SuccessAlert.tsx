import { ICON_CHECK_CIRCLE_SIZE } from "@/constants/components";
import { CheckCircle } from "lucide-react";

interface SuccessAlertProps {
  title: string;
  body: string;
  action?: React.ReactNode;
}

export function SuccessAlert(props: SuccessAlertProps) {
  return (
    <div className="flex flex-col items-center gap-4 py-4 text-center">
      <CheckCircle size={ICON_CHECK_CIRCLE_SIZE} className="text-[var(--pos)]" strokeWidth={1.5} />
      <div>
        <h2 className="mb-2">{props.title}</h2>
        <p className="text-[14px] text-[var(--muted)] leading-relaxed">{props.body}</p>
      </div>
      {props.action}
    </div>
  );
}