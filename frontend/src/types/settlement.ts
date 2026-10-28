import type { Transfer } from './group';

export interface Settlement {
  id: string;
  fromMemberId: string;
  toMemberId: string;
  amountCents: number;
  settledOn: string;
  note?: string;
  createdAt: string;
}

export interface CreateFromSuggestion {
  groupId: string;
  transfer: Transfer;
}

export interface DeleteSettlement {
  groupId: string;
  settlementId: string;
}
