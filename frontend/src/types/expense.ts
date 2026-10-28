export type SplitMode = 'EQUAL' | 'EXACT' | 'PERCENT' | 'SHARES' | 'ITEMIZED';
export type Source = 'MANUAL' | 'AI_TEXT' | 'AI_RECEIPT';

export interface Category {
  value: string;
  label: string;
}

export interface SplitValue {
  memberId: string;
  value?: number | string | null;
}

export interface Item {
  label: string;
  amountCents: number;
  memberIds: string[];
}

export interface Expense {
  id: string;
  description: string;
  amountCents: number;
  payerMemberId: string;
  category: string;
  categoryLabel: string;
  splitMode: SplitMode;
  date: string;
  shares: { memberId: string; amountCents: number; weight?: number }[];
  items?: Item[];
  source: Source;
  createdAt: string;
  version: number;
}

export interface ExpenseRequest {
  description: string;
  amount: string;
  payerMemberId: string;
  category?: string;
  splitMode: SplitMode;
  date?: string;
  splits?: SplitValue[];
  items?: { label: string; amount: string; memberIds: string[] }[];
  source?: Source;
  version?: number;
  force?: boolean;
}

export interface Page<T> {
  items: T[];
  total: number;
  page: number;
  size: number;
}

export interface ExpenseListParams {
  q?: string;
  category?: string;
  memberId?: string;
  from?: string;
  to?: string;
  page?: number;
  size?: number;
}

export interface CreateExpense {
  groupId: string;
  data: ExpenseRequest;
}

export interface UpdateExpense {
  groupId: string;
  expenseId: string;
  data: ExpenseRequest;
}

export interface DeleteExpense {
  groupId: string;
  expenseId: string;
}