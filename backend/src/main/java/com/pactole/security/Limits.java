package com.pactole.security;

import com.pactole.config.PactoleProperties;
import com.pactole.config.PactoleProperties.Ai;
import com.pactole.config.PactoleProperties.RateLimits;

import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class Limits {

    public final RateLimiter.Limit globalPerIp;
    public final RateLimiter.Limit authPerIp;
    public final RateLimiter.Limit loginPerEmail;
    public final RateLimiter.Limit joinPerIp;
    public final RateLimiter.Limit aiPerUserHour;
    public final RateLimiter.Limit aiPerUserDay;

    public Limits(PactoleProperties properties) {
        RateLimits rates = properties.security().rateLimits();
        Ai ai = properties.ai();
        this.globalPerIp = new RateLimiter.Limit("global-ip", rates.globalPerIpPerMinute(), Duration.ofMinutes(1));
        this.authPerIp = new RateLimiter.Limit("auth-ip", rates.authPerIpPerMinute(), Duration.ofMinutes(1));
        this.loginPerEmail = new RateLimiter.Limit("login-email", rates.loginPerEmailPer15minutes(), Duration.ofMinutes(15));
        this.joinPerIp = new RateLimiter.Limit("join-ip", rates.joinPerIpPerMinute(), Duration.ofMinutes(1));
        this.aiPerUserHour = new RateLimiter.Limit("ai-user-hour", Math.max(1, ai.callsPerHour()), Duration.ofHours(1));
        this.aiPerUserDay = new RateLimiter.Limit("ai-user-day", Math.max(1, ai.callsPerDay()), Duration.ofDays(1));
    }
}
