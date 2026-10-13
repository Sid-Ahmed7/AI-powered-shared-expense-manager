package com.pactole.dto.response.auth;

import com.pactole.entities.Account;
import com.pactole.services.refreshToken.SessionService;

public record AuthResponse(Account account, SessionService.IssuedSession session) {}
