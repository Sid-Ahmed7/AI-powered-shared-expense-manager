package com.pactole.dto.request.session;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor 
public class SessionViewRequest {
    private UUID sessionId;
    private Instant startedAt;
    private Instant lastUsedAt;
    private String userAgent;
    private String ipMasked;
    private boolean current;
}
