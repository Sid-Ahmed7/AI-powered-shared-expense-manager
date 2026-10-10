package com.pactole.services.account;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.pactole.common.ClientInfo;
import com.pactole.entities.Account;
import com.pactole.enums.RefreshReason;
import com.pactole.services.auth.AuthService;
import com.pactole.services.password.PasswordService;
import com.pactole.services.refreshToken.SessionService;

import jakarta.transaction.Transactional;

@Service 
public class AccountService implements IAccountService {
    private final AuthService authService;
    private final SessionService sessionService;
    private final PasswordService passwordService;

    public AccountService(AuthService authService, SessionService sessionService, PasswordService passwordService) {
        this.authService = authService;
        this.sessionService = sessionService;
        this.passwordService = passwordService;
    }

    @Transactional
    public int changePassword(UUID accountId, UUID currentSession, String currentPassword, String newPassword, ClientInfo client) {
        Account account = authService.activeAccount(accountId);
        passwordService.requirePassword(account, currentPassword);
        passwordService.validateNewPassword(newPassword, account);
        account.changePassword(passwordService.encodePassword(newPassword));
        int revoked = sessionService.revokeAll(accountId, RefreshReason.PASSWORD_CHANGED, currentSession);
        return revoked;
    }    

    @Transactional 
    public int revokeOtherSession(UUID accountId, UUID currentSession, ClientInfo client) {
        int revoked = sessionService.revokeAll(accountId, RefreshReason.LOGOUT_ALL, currentSession);
        return revoked;
    }

    @Transactional
    public void revokeSession(UUID userId, UUID sessionId, ClientInfo client) {
        sessionService.revokeOwnSession(userId, sessionId);
    }

    // @Transactional 
    // public Map<String, Object> exportData(UUID accountId, ClientInfo client) {
        
    // }

    @Transactional
    public void deleteAccount(UUID accountId, String password, ClientInfo client) {
        Account account = authService.activeAccount(accountId);
        passwordService.requirePassword(account, password);
        sessionService.revokeAll(accountId, RefreshReason.ACCOUNT_DELETED, null);
        account.anonymizeAccount(passwordService.encodePassword(UUID.randomUUID().toString()));
    }





}
