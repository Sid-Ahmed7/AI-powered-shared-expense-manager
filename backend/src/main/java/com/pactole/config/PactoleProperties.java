package com.pactole.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "pactole")
public record PactoleProperties(List<String> corsOrigins, Jwt jwt, Ai ai) {

    public record Security(Jwt jwt, Refresh refresh, Lockout lockout, RateLimits rateLimits,int maxJsonBodyBytes, int auditRetentionDays) {}

    public record Jwt(String secret, String issuer, String audience, int accessTokenMinutes) {}

    public record Refresh(String cookieName, boolean cookieSecure, int slidingDays, int absoluteDays, int reuseGraceSeconds) {}

    public record Lockout(int maxFailures, int durationMinutes) {}

    public record RateLimits(int globalPerIpPerMinute, int authPerIpPerMinute, int loginPerEmailPer15Minutes, int joinPerIpPerMinute) {}

    public record Ai(String apiKey, String baseUrl, String model, String visionModel, int timeoutSeconds, int callsPerHour, int callsPerDay) {
        public boolean enabled() {
            return apiKey != null && !apiKey.isBlank();
        }
    }
}
