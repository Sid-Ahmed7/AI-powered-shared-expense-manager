export type ButtonVariant = 'primary' | 'ghost' | 'danger' | 'icon' | 'seg';
export type ButtonSize = 'default' | 'sm' | 'xs';
export type SizeComponents =  'sm' | 'md' | 'lg';
export type Theme = 'dark' | 'light';
export type Section = 'profile' | 'security' | 'activity' | 'data' | 'preferences';
export type AuthType = 'login' | 'register';

export interface MemberAvatarSizes {
    outer: string;
    text: string;
    radius: string;
}
export interface EditableItem {
  label: string;
  amount: string;
  memberIds: string[];
}

export interface MemberRow {
  memberId: string;
  name: string;
  paidCents: number;
  consumedCents: number;
}

export interface AccountNavItem {
  key: Section;
  label: string;
  icon: React.ReactNode;
}

export interface AuthMode {
  mode: AuthType;
}