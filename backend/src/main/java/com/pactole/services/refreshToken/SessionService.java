package com.pactole.services.refreshToken;

import com.pactole.builder.RefreshTokenBuilder;
import com.pactole.common.ClientInfo;
import com.pactole.common.HashToken;
import com.pactole.config.PactoleProperties;
import com.pactole.dto.request.session.SessionRequest;
import com.pactole.dto.request.session.SessionViewRequest;
import com.pactole.entities.RefreshToken;
import com.pactole.enums.RefreshReason;
import com.pactole.exceptions.Errors;
import com.pactole.repository.RefreshTokenRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class SessionService implements ISessionService {


    private static final SecureRandom RANDOM = new SecureRandom();

    private final RefreshTokenRepository tokens;
    private final PactoleProperties.Refresh config;
    private final RefreshTokenBuilder refreshTokenBuilder;

    public SessionService(RefreshTokenRepository tokens, PactoleProperties properties, RefreshTokenBuilder refreshTokenBuilder) {
        this.tokens = tokens;
        this.config = properties.security().refresh();
        this.refreshTokenBuilder = refreshTokenBuilder;
    }

    @Transactional
    public IssuedSession start(UUID userId, ClientInfo client) {
        Instant now = Instant.now();
        return issue(userId, UUID.randomUUID(), now, now, client);
    }

    @Transactional(noRollbackFor = {Errors.Unauthorized.class, Errors.Conflict.class})
    public IssuedSession rotate(String rawToken, ClientInfo client) {
        if (rawToken == null || rawToken.isBlank() || rawToken.length() > 100) {
            throw new Errors.Unauthorized("auth.session.expired");
        }
        Instant now = Instant.now();
        RefreshToken token = tokens.findByHashForUpdate(HashToken.hash(rawToken))
                .orElseThrow(() -> new Errors.Unauthorized("auth.session.expired"));

        if (token.getRevokedAt() != null) {
            boolean rotated = RefreshReason.ROTATED.name().equals(token.getRevokeReason());
            if (rotated && token.getReplacedAt() != null && token.getReplacedAt().isAfter(now.minusSeconds(config.reuseGraceSeconds()))) {
                throw new Errors.Conflict("auth.session.concurrent", "REFRESH_RACE");
            }
            if (rotated) {
                revokeFamily(token.getFamilyId(), RefreshReason.REUSE_DETECTED);
            }
            throw new Errors.Unauthorized("auth.session.expired");
        }

        Instant absoluteEnd = token.getFamilyStartedAt().plus(Duration.ofDays(config.absoluteDays()));
        if (!token.getExpiresAt().isAfter(now) || !absoluteEnd.isAfter(now)) {
            token.revoke(RefreshReason.SESSION_REVOKED);
            throw new Errors.Unauthorized("auth.session.expired");
        }

        token.revoke(RefreshReason.ROTATED);
        return issue(token.getUserId(), token.getFamilyId(), token.getFamilyStartedAt(), now, client);
    }

    @Transactional
    public UUID logout(String rawToken) {
        if (rawToken == null || rawToken.isBlank() || rawToken.length() > 100) return null;
        return tokens.findByHashForUpdate(HashToken.hash(rawToken)).map(token -> {
            revokeFamily(token.getFamilyId(), RefreshReason.LOGOUT);
            return token.getUserId();
        }).orElse(null);
    }

    @Transactional
    public void revokeFamily(UUID familyId, RefreshReason reason) {
        tokens.findByFamilyId(familyId).forEach(token -> token.revoke(reason));
    }

    @Transactional
    public int revokeAll(UUID userId, RefreshReason reason, UUID keepSessionId) {
        List<RefreshToken> active = tokens.findActiveByUser(userId, Instant.now());
        int count = 0;
        for (RefreshToken token : active) {
            if (!Objects.equals(token.getFamilyId(), keepSessionId)) {
                revokeFamily(token.getFamilyId(), reason);
                count++;
            }
        }
        return count;
    }

    @Transactional
    public void revokeOwnSession(UUID userId, UUID sessionId) {
        List<RefreshToken> family = tokens.findByFamilyId(sessionId);
        if (family.isEmpty() || family.stream().anyMatch(token -> !userId.equals(token.getUserId()))) {
            throw new Errors.NotFound("auth.session.not.found");
        }
        revokeFamily(sessionId, RefreshReason.SESSION_REVOKED);
    }

    @Transactional(readOnly = true)
    public List<SessionView> list(UUID userId, UUID currentSessionId) {
        return tokens.findActiveByUser(userId, Instant.now()).stream()
                .map(token -> new SessionView(new SessionViewRequest(
                        token.getFamilyId(), token.getFamilyStartedAt(), token.getCreatedAt(),
                        token.getUserAgent(), maskIp(token.getIp()), token.getFamilyId().equals(currentSessionId))))
                .toList();
    }

    private IssuedSession issue(UUID userId, UUID familyId, Instant familyStart, Instant now, ClientInfo client) {
        byte[] bytes = new byte[32]; 
        RANDOM.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant sliding = now.plus(Duration.ofDays(config.slidingDays()));
        Instant absolute = familyStart.plus(Duration.ofDays(config.absoluteDays()));
        Instant expiresAt = sliding.isBefore(absolute) ? sliding : absolute;
        tokens.save(refreshTokenBuilder.buildRefreshToken(userId, familyId, raw, familyStart, expiresAt, client));
        return new IssuedSession(new SessionRequest(userId, familyId, raw, expiresAt));
    }
    
    static String maskIp(String ip) {
        if (ip == null) return null;
        if (ip.contains(".")) {
            int last = ip.lastIndexOf('.');
            return ip.substring(0, last) + ".x";
        }
        String[] parts = ip.split(":");
        return parts.length >= 3 ? parts[0] + ":" + parts[1] + ":" + parts[2] + ":…" : "…";
    }
}
