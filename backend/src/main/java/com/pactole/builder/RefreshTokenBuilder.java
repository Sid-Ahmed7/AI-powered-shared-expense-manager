package com.pactole.builder;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.pactole.common.ClientInfo;
import com.pactole.common.HashToken;
import com.pactole.entities.RefreshToken;

@Component
public class RefreshTokenBuilder {

    public RefreshToken buildRefreshToken(UUID userId, UUID familyId, String tokenHash, Instant familyStartedAt, Instant expiresAt, ClientInfo client) {
        return RefreshToken.builder()
                .userId(userId)
                .familyId(familyId)
                .tokenHash(HashToken.hash(tokenHash))
                .familyStartedAt(familyStartedAt)
                .expiresAt(expiresAt)
                .userAgent(client == null ? null : client.userAgent())
                .ip(client == null ? null : client.ip())
                .build();
    }
}
