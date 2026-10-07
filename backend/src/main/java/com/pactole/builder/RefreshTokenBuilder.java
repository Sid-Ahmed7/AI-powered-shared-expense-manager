package com.pactole.builder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.springframework.stereotype.Component;

import com.pactole.common.ClientInfo;
import com.pactole.common.HashToken;
import com.pactole.dto.request.refresh.RefreshTokenRequest;
import com.pactole.entities.RefreshToken;

@Component 
public class RefreshTokenBuilder {

    public RefreshToken buildRefreshToken(RefreshTokenRequest refreshTokenRequest, ClientInfo client) {
        return RefreshToken.builder()
                .userId(refreshTokenRequest.getUserId())
                .familyId(refreshTokenRequest.getFamilyId())
                .tokenHash(HashToken.hash(refreshTokenRequest.getTokenHash()))
                .familyStartedAt(refreshTokenRequest.getFamilyStartedAt())
                .expiresAt(refreshTokenRequest.getExpiresAt())
                .userAgent(client == null ? null : client.userAgent())
                .ip(client == null ? null : client.ip())
                .build();
    }

}
