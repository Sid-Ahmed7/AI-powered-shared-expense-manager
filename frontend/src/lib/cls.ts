import { BTN_CLS } from "@/constants/components";

export const cls = {
  btnPrimary: `${BTN_CLS} bg-[var(--primary)] text-white border-[var(--primary)] shadow-[0_1px_8px_rgba(249,115,22,0.30)] [&:hover:not(:disabled)]:bg-[#fb923c] [&:hover:not(:disabled)]:shadow-[0_2px_18px_rgba(249,115,22,0.45)]`,
  btnGhost: `${BTN_CLS} border-[var(--border)] bg-transparent text-[var(--text-2)] [&:hover:not(:disabled)]:bg-[var(--surface-2)] [&:hover:not(:disabled)]:border-[rgba(255,255,255,0.12)] [&:hover:not(:disabled)]:text-[var(--text)]`,
  btnDanger: `${BTN_CLS} text-[var(--neg)] border-[rgba(248,113,113,0.35)] bg-transparent [&:hover:not(:disabled)]:bg-[var(--neg-soft)] [&:hover:not(:disabled)]:border-[var(--neg)]`,
  btnSm: 'py-[6px] px-[13px] text-[13px] rounded-[8px] gap-[5px]',
  iconBtn: 'border-0 bg-transparent text-[var(--muted)] cursor-pointer py-[6px] px-[8px] rounded-[8px] inline-flex items-center justify-center transition-[background,color] hover:bg-[var(--surface-2)] hover:text-[var(--text)]',
  card: 'bg-[var(--card)] border border-[var(--border)] rounded-[var(--radius)] transition-[border-color]',
  cardPad: 'bg-[var(--card)] border border-[var(--border)] rounded-[var(--radius)] p-[18px_20px] transition-[border-color]',
  skeleton: '[background:linear-gradient(90deg,var(--surface)_0%,var(--border)_50%,var(--surface)_100%)] [background-size:200%_100%] animate-[shimmer_1.5s_infinite]',
  input: 'w-full py-[11px] px-[14px] border border-[var(--border)] rounded-[var(--radius-sm)] bg-[var(--surface)] text-[var(--text)] font-[inherit] text-[14px] leading-[1.4] transition-[border-color,box-shadow,background] placeholder:text-[var(--muted)] placeholder:opacity-70 focus:outline-none focus:border-[var(--border-focus)] focus:bg-[var(--surface-2)] focus:shadow-[0_0_0_3px_var(--primary-dim)] [&:hover:not(:focus)]:border-[rgba(255,255,255,0.1)]',
  inputSm: '!py-[6px] !px-[10px] text-[13px]',
  segmented: 'flex gap-[3px] bg-[var(--surface)] p-[4px] rounded-[11px] border border-[var(--border)]',
  segBtn: 'flex-1 border-0 bg-transparent py-[7px] px-[10px] rounded-[8px] font-[inherit] text-[13px] font-semibold text-[var(--muted)] cursor-pointer whitespace-nowrap transition-[background,color] [&:hover:not([data-active])]:text-[var(--text-2)]',
  segBtnActive: 'bg-[var(--surface-2)] text-[var(--text)] shadow-[0_1px_4px_rgba(0,0,0,0.3)]',
  modalBackdrop: 'fixed inset-0 bg-[rgba(0,0,0,0.65)] backdrop-blur-[6px] flex items-end justify-center z-50 min-[700px]:items-center',
  modal: 'bg-[var(--surface-2)] border border-[var(--border)] [border-top:1px_solid_rgba(255,255,255,0.06)] w-full max-w-[600px] max-h-[92vh] rounded-[20px_20px_0_0] flex flex-col animate-[slide-up_0.22s_cubic-bezier(0.34,1.2,0.64,1)] shadow-[0_-8px_48px_rgba(0,0,0,0.5)] min-[700px]:rounded-[20px] min-[700px]:shadow-[var(--shadow)]',
  modalHead: 'flex justify-between items-center p-[18px_20px_14px] border-b border-[var(--border)] shrink-0',
  modalBody: 'p-[18px_20px_24px] overflow-y-auto',
  selectArrow: 'appearance-none bg-[var(--select-arrow)] bg-no-repeat [background-position:right_12px_center] [background-size:16px] cursor-pointer focus:bg-[var(--select-arrow-focus)] !pr-[38px] [&_option]:bg-[var(--card)] [&_option]:text-[var(--text)]',
};
