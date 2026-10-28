export interface User {
  id: string;
  email: string;
  displayName: string;
}

export interface AuthPayload {
  accessToken: string;
  expiresIn: number;
  user: User;
}

export interface SessionInfo {
  sessionId: string;
  startedAt: string;
  lastUsedAt: string;
  userAgent?: string;
  ipMasked?: string;
  current: boolean;
}

export interface SecurityEventInfo {
  type: string;
  date: string;
  ip?: string;
  userAgent?: string;
}

export interface AuthState {
  user: User | null;
  loading: boolean;
  signIn: (payload: AuthPayload) => void;
  signOut: () => Promise<void>;
  endSession: () => void;
}

export interface ChangePassword {
  currentPassword: string;
  newPassword: string;
}

export interface Login {
  email: string;
  password: string;
}

export interface Register {
  email: string;
  password: string;
  fullName: string;
}

export interface DeleteAccount {
  password: string;
}