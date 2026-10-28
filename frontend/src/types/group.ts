export interface Member {
  id: string;
  name: string;
  hasAccount: boolean;
  me: boolean;
}

export interface GroupSummary {
  id: string;
  name: string;
  currency: string;
  memberCount: number;
  expenseCount: number;
  myBalanceCents: number;
  totalSpentCents: number;
}

export interface GroupDetail {
  id: string;
  name: string;
  currency: string;
  inviteCode: string;
  myMemberId: string;
  owner: boolean;
  members: Member[];
}

export interface JoinPreview {
  groupName: string;
  unclaimedMembers: Member[];
}

export interface MemberBalance {
  memberId: string;
  name: string;
  paidCents: number;
  consumedCents: number;
  netCents: number;
}

export interface Transfer {
  fromMemberId: string;
  toMemberId: string;
  amountCents: number;
}

export interface Balances {
  members: MemberBalance[];
  transfers: Transfer[];
  suggestedNextPayerId?: string;
  suggestedNextPayerReason?: string;
}

export interface AddMember {
  groupId: string;
  name: string;
}

export interface JoinGroup {
  code: string;
  memberId?: string;
}
