package com.pactole.security;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.pactole.exceptions.Errors;

@Component 
public class AiRateLimiter {
    
    private final RateLimiter rateLimiter;
    private final Limits limits;

    public AiRateLimiter(RateLimiter rateLimiter, Limits limits) {
        this.rateLimiter = rateLimiter;
        this.limits = limits;
    }

    public void acquire(UUID accountId) {
        String key = String.valueOf(accountId);
        long wait = rateLimiter.tryAcquire(limits.aiPerUserHour, key);
        if(wait == 0) {
            wait = rateLimiter.tryAcquire(limits.aiPerUserDay, key);
        }
        if (wait > 0) {
            throw new Errors.TooManyRequests("ai.rate.limit.wait", wait);
        }
    }
}
