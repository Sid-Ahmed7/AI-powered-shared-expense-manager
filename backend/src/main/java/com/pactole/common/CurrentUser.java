package com.pactole.common;

import com.pactole.config.PactoleProperties;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentUser {

    private final String sessionClaim;

    public CurrentUser(PactoleProperties properties) {
        this.sessionClaim = properties.security().jwt().sessionClaim();
    }

    public static UUID id(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    public UUID sessionId(Jwt jwt) {
        return UUID.fromString(jwt.getClaimAsString(sessionClaim));
    }
}
