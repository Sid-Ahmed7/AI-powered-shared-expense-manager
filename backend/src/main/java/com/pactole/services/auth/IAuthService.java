package com.pactole.services.auth;

import java.util.UUID;

import com.pactole.common.ClientInfo;
import com.pactole.dto.response.auth.AuthResponse;
import com.pactole.entities.Account;

public interface IAuthService {
    AuthResponse register(String email, String password, String fullName, ClientInfo client);
    AuthResponse login(String email, String password, ClientInfo client);
    Account activeAccount(UUID account);
}
