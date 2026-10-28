import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter, HashRouter, Navigate, Route, Routes } from 'react-router-dom';
import { Capacitor } from '@capacitor/core';
import { AuthProvider, RequireAuth } from '@/context/auth';
import { AuthPage } from '@/pages/AuthPage';
import { ForgotPasswordPage } from '@/pages/ForgotPasswordPage';
import { ResetPasswordPage } from '@/pages/ResetPasswordPage';
import { GroupsPage } from '@/pages/GroupsPage';
import { NewGroupPage } from '@/pages/NewGroupPage';
import { GroupPage } from '@/pages/GroupPage';
import { JoinPage } from '@/pages/JoinPage';
import { AccountPage } from '@/pages/AccountPage';
import '@/i18n';
import '@/styles.css';

const queryClient = new QueryClient({
  defaultOptions: { queries: { staleTime: 15_000, retry: 1, refetchOnWindowFocus: false } },
});

const Router = Capacitor.isNativePlatform() ? HashRouter : BrowserRouter;

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <Router>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<AuthPage mode="login" />} />
            <Route path="/register" element={<AuthPage mode="register" />} />
            <Route path="/forgot-password" element={<ForgotPasswordPage />} />
            <Route path="/reset-password" element={<ResetPasswordPage />} />
            <Route path="/" element={<RequireAuth><GroupsPage /></RequireAuth>} />
            <Route path="/groups/new" element={<RequireAuth><NewGroupPage /></RequireAuth>} />
            <Route path="/groups/:groupId/*" element={<RequireAuth><GroupPage /></RequireAuth>} />
            <Route path="/join/:code" element={<RequireAuth><JoinPage /></RequireAuth>} />
            <Route path="/account" element={<RequireAuth><AccountPage /></RequireAuth>} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </AuthProvider>
      </Router>
    </QueryClientProvider>
  </StrictMode>,
);
