package com.pactole.dto.request.refresh;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class RefreshTokenRequest {
     private UUID userId;
     private UUID familyId;
     private String tokenHash;
     private Instant familyStartedAt;
     private Instant expiresAt;
}
