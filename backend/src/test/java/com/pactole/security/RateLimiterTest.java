package com.pactole.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.*;

class RateLimiterTest {

    private AtomicLong clock;
    private RateLimiter limiter;

    @BeforeEach
    void setUp() {
        clock = new AtomicLong(0);
        limiter = new RateLimiter(clock::get);
    }

    @Test
    void tryAcquire_firstRequest_isAllowed() {
        RateLimiter.Limit limit = new RateLimiter.Limit("test", 5, Duration.ofSeconds(10));
        assertThat(limiter.tryAcquire(limit, "key")).isEqualTo(0L);
    }

    @Test
    void tryAcquire_withinCapacity_allAllowed() {
        RateLimiter.Limit limit = new RateLimiter.Limit("test", 3, Duration.ofSeconds(60));
        assertThat(limiter.tryAcquire(limit, "key")).isEqualTo(0L);
        assertThat(limiter.tryAcquire(limit, "key")).isEqualTo(0L);
        assertThat(limiter.tryAcquire(limit, "key")).isEqualTo(0L);
    }

    @Test
    void tryAcquire_afterCapacityExhausted_returnsPositiveWait() {
        RateLimiter.Limit limit = new RateLimiter.Limit("test", 2, Duration.ofSeconds(60));
        limiter.tryAcquire(limit, "key");
        limiter.tryAcquire(limit, "key");
        long wait = limiter.tryAcquire(limit, "key");
        assertThat(wait).isGreaterThan(0L);
    }

    @Test
    void tryAcquire_afterRefill_isAllowedAgain() {
        RateLimiter.Limit limit = new RateLimiter.Limit("test", 1, Duration.ofSeconds(10));
        limiter.tryAcquire(limit, "key"); // exhaust
        // advance clock by 10 seconds (enough to refill 1 token)
        clock.set(10_000_000_000L);
        long wait = limiter.tryAcquire(limit, "key");
        assertThat(wait).isEqualTo(0L);
    }

    @Test
    void reset_afterExhaustion_allowsNextRequest() {
        RateLimiter.Limit limit = new RateLimiter.Limit("test", 1, Duration.ofSeconds(60));
        limiter.tryAcquire(limit, "key"); // exhaust
        assertThat(limiter.tryAcquire(limit, "key")).isGreaterThan(0L);
        limiter.reset(limit, "key");
        assertThat(limiter.tryAcquire(limit, "key")).isEqualTo(0L);
    }

    @Test
    void limit_zeroCapacity_throwsIllegalArgument() {
        assertThatThrownBy(() -> new RateLimiter.Limit("bad", 0, Duration.ofSeconds(1)))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void limit_negativeCapacity_throwsIllegalArgument() {
        assertThatThrownBy(() -> new RateLimiter.Limit("bad", -1, Duration.ofSeconds(1)))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void limit_zeroPeriod_throwsIllegalArgument() {
        assertThatThrownBy(() -> new RateLimiter.Limit("bad", 5, Duration.ZERO))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void tryAcquire_nullKey_usesUnknownFallback() {
        RateLimiter.Limit limit = new RateLimiter.Limit("test", 5, Duration.ofSeconds(60));
        assertThat(limiter.tryAcquire(limit, null)).isEqualTo(0L);
    }
}
