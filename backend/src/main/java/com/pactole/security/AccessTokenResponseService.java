package com.pactole.security;

import com.pactole.config.PactoleProperties;
import com.pactole.dto.response.AccessTokenResponse;
import com.pactole.entities.Account;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Service
public class AccessTokenResponseService {

    private final JwtEncoder encoder;
    private final PactoleProperties.Jwt config;

    public AccessTokenResponseService(JwtEncoder encoder, PactoleProperties properties) {
        this.encoder = encoder;
        this.config = properties.security().jwt();
    }

    public AccessTokenResponse issue(Account user, UUID sessionId) {
        Instant now = Instant.now();
        Duration ttl = Duration.ofMinutes(config.accessTokenMinutes());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(config.issuer())
                .audience(List.of(config.audience()))
                .subject(String.valueOf(user.getId()))
                .issuedAt(now)
                .notBefore(now)
                .expiresAt(now.plus(ttl))
                .id(UUID.randomUUID().toString())
                .claim(config.sessionClaim(), sessionId.toString())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        return new AccessTokenResponse(encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue(), ttl.toSeconds());
    }
}
