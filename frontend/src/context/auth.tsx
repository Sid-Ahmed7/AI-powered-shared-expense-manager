import { createContext, useCallback, useContext, useEffect, useRef, useState, type ReactNode } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useQueryClient } from '@tanstack/react-query';
import { logout as apiLogout, refreshSession, setAccessToken, setSessionEndedHandler, type AuthPayload, type User } from '@/api';
import type { AuthState } from '@/types';


const AuthContext = createContext<AuthState | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const queryClient = useQueryClient();
  const started = useRef(false);

  const endSession = useCallback(() => {
    setAccessToken(null);
    setUser(null);
    queryClient.clear(); 
  }, [queryClient]);

  const signOut = useCallback(async () => {
    await apiLogout();
    endSession();
  }, [endSession]);

  useEffect(() => {
    setSessionEndedHandler(endSession);
    if (started.current) return; 
    started.current = true;
    refreshSession().then((payload) => setUser(payload?.user ?? null)).finally(() => setLoading(false));
  }, [endSession]);

  useEffect(() => {
    if (typeof BroadcastChannel === 'undefined') return;
    const channel = new BroadcastChannel('pactole-auth');
    channel.onmessage = (e) => e.data === 'logout' && endSession();
    return () => channel.close();
  }, [endSession]);

  const signIn = (payload: AuthPayload) => {
    setAccessToken(payload.accessToken);
    setUser(payload.user);
  };

  const signOutEverywhere = async () => {
    await signOut();
    if (typeof BroadcastChannel !== 'undefined') {
      const channel = new BroadcastChannel('pactole-auth');
      channel.postMessage('logout');
      channel.close();
    }
  };

  return (
    <AuthContext.Provider value={{ user, loading, signIn, signOut: signOutEverywhere, endSession }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth hors de AuthProvider');
  return ctx;
}

export function RequireAuth({ children }: { children: ReactNode }) {
  const { user, loading } = useAuth();
  const location = useLocation();
  if (loading) return <div className="text-center text-[var(--muted)] max-w-xl mx-auto px-4 pb-[72px]">Chargement…</div>;
  if (!user) return <Navigate to="/login" state={{ from: location.pathname }} replace />;
  return <>{children}</>;
}

/** Évite les redirections ouvertes : on ne renvoie que vers un chemin interne. */
export function safeRedirect(path: unknown): string {
  return typeof path === 'string' && path.startsWith('/') && !path.startsWith('//') ? path : '/';
}
