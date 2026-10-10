package com.pactole.services.account;

import java.util.UUID;

import com.pactole.common.ClientInfo;

public interface IAccountService {
    
    int changePassword(UUID accountId, UUID currentSession, String currentPassword, String newPassword, ClientInfo client);
    int revokeOtherSession(UUID accountId, UUID currentSession, ClientInfo client);
    void revokeSession(UUID accountId, UUID sessionId, ClientInfo client);
    // Map<String, Object> exportData(UUID accountId, ClientInfo client);
    void deleteAccount(UUID accountId, String password, ClientInfo client);
}
