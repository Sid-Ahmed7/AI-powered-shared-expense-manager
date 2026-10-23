package com.pactole.services.refreshToken;

import java.time.Instant;
import java.util.UUID;

import com.pactole.dto.request.session.SessionRequest;

public interface ISessionService {
    public record IssuedSession(SessionRequest sessionRequest) {}

    public record SessionView(UUID sessionId, Instant startedAt, Instant lastUsedAt, String userAgent, String ipMasked, boolean current) {}

    public record SecurityEvent(String type, Instant date, String userAgent) {}
}
