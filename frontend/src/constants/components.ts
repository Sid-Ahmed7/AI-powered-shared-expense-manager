import { cls } from "@/lib/cls";
import type { Tone } from "@/types";
import type { ButtonVariant, MemberAvatarSizes, SizeComponents } from "@/types/components";

export const ICON_SIZE = 15;
export const ICON_CHECK_CIRCLE_SIZE = 48;
export const INPUT_LENGTH = 128;
export const KEY = 'pactole-theme';

export const VARIANT_CLASS: Record<ButtonVariant, string> = {
  primary: cls.btnPrimary,
  ghost: cls.btnGhost,
  danger: cls.btnDanger,
  icon: cls.iconBtn,
  seg: cls.segBtn,
};
export const SIZES_AVATAR: Record<SizeComponents, MemberAvatarSizes> = {
  sm: { outer: 'w-[26px] h-[26px]', text: 'text-[11px]', radius: 'rounded-[7px]'},
  md: { outer: 'w-[44px] h-[44px]', text: 'text-[18px]', radius: 'rounded-[13px]'},
  lg: { outer: 'w-16 h-16', text: 'text-[26px]', radius: 'rounded-full'},
};

export const DEVICE = /iPhone|iPad|Android/;
export const BASE = import.meta.env.VITE_API_URL ?? '';
export const CLIENT_HEADER = { 'X-Pactole-Client': 'web' };
export const BUBBLE_BASE = 'max-w-[82%] p-[10px_14px] rounded-2xl text-[14px] leading-[1.55] whitespace-pre-wrap';
export const BUBBLE_USER = `${BUBBLE_BASE} self-end bg-[linear-gradient(145deg,var(--primary),#ea580c)] text-white rounded-br-[4px] shadow-[0_2px_12px_rgba(249,115,22,0.25)]`;
export const BUBBLE_ASSISTANT = `${BUBBLE_BASE} self-start bg-[var(--surface-2)] border border-[var(--border)] rounded-bl-[4px] text-[var(--text-2)]`;
export const ACTION_CARD = '!flex-col !py-[18px] !px-3 !bg-[var(--surface)] !text-[13px] !text-[var(--muted)] !rounded-[var(--radius)] [&:hover:not(:disabled)]:!bg-[var(--primary-dim)] [&:hover:not(:disabled)]:!border-[rgba(249,115,22,0.4)] [&:hover:not(:disabled)]:!text-[var(--text)]';
export const BTN = '!min-w-[30px] !h-[30px] !px-1.5 !py-0 !rounded-lg !text-[13px] !font-medium';
export const ACTIVE = '!bg-[var(--primary-dim)] !border-[rgba(249,115,22,0.3)] !text-[var(--primary)] !font-bold';
export const UPLOAD_AREA = '!grid !place-items-center !min-h-[180px] !border-2 !border-dashed !rounded-[var(--radius)] !bg-[var(--surface)] !text-[var(--muted)] [&:hover:not(:disabled)]:!border-[var(--primary)] [&:hover:not(:disabled)]:!bg-[var(--primary-dim)] overflow-hidden';
export const TONES: Tone[] = ['SYMPA', 'DROLE', 'FORMEL'];
export const CHIP_BASE = 'border font-[inherit] text-[13px] font-medium cursor-pointer transition-all rounded-full py-[5px] px-3';
export const OFF = `${CHIP_BASE} border-[var(--border)] bg-transparent text-[var(--muted)] hover:border-[rgba(249,115,22,0.4)] hover:text-[var(--text)]`;
export const ON = `${CHIP_BASE} bg-[var(--primary)] border-[var(--primary)] text-white`;
export const TH = 'text-left p-[9px_6px] border-b border-[var(--border)] text-[var(--muted)] font-bold text-[11px] uppercase tracking-[0.06em]';
export const TD = 'p-[9px_6px] border-b border-[var(--border)] last:border-b-0';
export const BTN_CLS = 'appearance-none border border-transparent rounded-[10px] py-[11px] px-[18px] font-[inherit] text-[14px] font-semibold cursor-pointer text-center no-underline inline-flex items-center justify-center gap-[7px] tracking-[-0.01em] whitespace-nowrap transition-[background,border-color,box-shadow,transform,opacity] [&:active:not(:disabled)]:scale-[0.96] disabled:opacity-[0.38] disabled:cursor-not-allowed focus-visible:outline-2 focus-visible:outline-[var(--primary)] focus-visible:outline-offset-2';
export const LANGUAGES: string[] = ['fr', 'en']; 
export const BACK_LINK = 'inline-flex items-center gap-1 text-[14px] font-semibold text-[var(--muted)] no-underline py-1 pl-1 pr-2 rounded-lg transition-all hover:text-[var(--text)] hover:bg-[var(--surface-2)] hover:opacity-100';
export const SIDEBAR_LINK ='flex items-center gap-2.5 px-2.5 py-2 rounded-[9px] text-sm font-medium text-[var(--text-2)] no-underline transition-all cursor-pointer bg-transparent border-0 font-[inherit] w-full text-left hover:bg-[var(--surface-2)] hover:text-[var(--text)] hover:opacity-100';
export const SIDEBAR_LINK_ACTIVE = 'bg-[var(--primary-dim)] text-[var(--primary)] font-semibold';
