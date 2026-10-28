export { ApiError } from '@/lib/ApiError';
export { setAccessToken, setSessionEndedHandler, refreshSession, logout } from '@/lib/axios';

export type {
  User, AuthPayload, SessionInfo, SecurityEventInfo, Member, GroupSummary, GroupDetail, JoinPreview, Transfer, Balances, MemberBalance, SplitMode, Source, Category, SplitValue, Item, Expense, ExpenseRequest, Page, ExpenseListParams, Settlement, Tone, AiStatus, ExpenseDraft, ReceiptDraft, ChatTurn, Summary, RecapResponse, CategoryTotal,
} from '@/types';

import authService from '@/services/authService';
import accountService from '@/services/accountService';
import groupService from '@/services/groupService';
import metaService from '@/services/metaService';
import expenseService from '@/services/expenseService';
import settlementService from '@/services/settlementService';
import aiService from '@/services/aiService';

export const api = {
  ...authService,
  ...accountService,
  ...metaService,
  ...groupService,
  ...expenseService,
  ...settlementService,
  ...aiService,
};
