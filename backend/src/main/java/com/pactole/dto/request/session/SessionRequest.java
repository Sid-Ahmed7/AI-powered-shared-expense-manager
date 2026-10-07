package com.pactole.dto.request.session;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class SessionRequest {

    private UUID userId;
    private UUID sessionId;
    private String rawRefreshToken;
    private Instant expiresAt;
}
