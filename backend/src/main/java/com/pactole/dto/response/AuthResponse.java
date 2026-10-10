package com.pactole.dto.response;

import com.pactole.entities.Account;
import com.pactole.services.refreshToken.SessionService;

public record AuthResponse(Account account, SessionService.IssuedSession session) {}
