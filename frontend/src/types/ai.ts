import type { SplitMode, SplitValue } from './expense';

export type Tone = 'SYMPA' | 'DROLE' | 'FORMEL';

export interface AiStatus {
  enabled: boolean;
  model: string;
}

export interface ExpenseDraft {
  description: string;
  amount?: number;
  payerMemberId: string;
  category: string;
  date: string;
  splitMode: SplitMode;
  splits: SplitValue[];
  warnings: string[];
  questions: string[];
}

export interface ReceiptDraft {
  merchant?: string;
  date: string;
  total: number;
  category: string;
  items: { label: string; amount: number }[];
  warnings: string[];
}

export interface ChatTurn {
  role: 'user' | 'assistant';
  content: string;
}

export interface CategoryTotal {
  category: string;
  label: string;
  cents: number;
  count: number;
}

export interface Summary {
  totalCents: number;
  expenseCount: number;
  byCategory: CategoryTotal[];
  byMember: { memberId: string; name: string; paidCents: number; consumedCents: number }[];
  biggestExpense?: string;
  biggestExpenseCents: number;
}

export interface RecapResponse {
  stats: { month: string; current: Summary; previous: Summary; deltaPercent?: number };
  recap?: { title?: string; summary?: string; insights: string[]; tip?: string };
  aiError?: string;
}

export interface ParseExpense {
  groupId: string;
  text: string
}

export interface ScanReceipt {
  groupId: string;
  file: File
}

export interface Ask {
  groupId: string;
  question: string;
  history?: ChatTurn[] 
}

export interface Reminder {
  groupId: string;
  fromMemberId: string;
  toMemberId: string;
  amountCents: number;
  tone?: Tone;
}