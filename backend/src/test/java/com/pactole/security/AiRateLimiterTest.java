package com.pactole.security;

import com.pactole.config.PactoleProperties;
import com.pactole.exceptions.Errors;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class AiRateLimiterTest {

    private AiRateLimiter buildLimiter(int callsPerHour, int callsPerDay) {
        PactoleProperties props = new PactoleProperties(
            List.of("http://localhost"),
            new PactoleProperties.Security(
                new PactoleProperties.Refresh("rt", false, 30, 90, 5),
                new PactoleProperties.Lockout(5, 15),
                new PactoleProperties.RateLimits(120, 20, 10, 10),
                new PactoleProperties.Jwt("test", "pactole", "pactole-app", 10, "sid")
            ),
            new PactoleProperties.Ai(null, "http://localhost", "/test", "test", "test", 5, callsPerHour, callsPerDay)
        );
        return new AiRateLimiter(new RateLimiter(), new Limits(props));
    }

    @Test
    void acquire_withinAllLimits_doesNotThrow() {
        AiRateLimiter ai = buildLimiter(5, 100);
        UUID id = UUID.randomUUID();
        assertThatNoException().isThrownBy(() -> {
            ai.acquire(id);
            ai.acquire(id);
        });
    }

    @Test
    void acquire_exceedsHourlyLimit_throwsTooManyRequests() {
        AiRateLimiter ai = buildLimiter(2, 100);
        UUID id = UUID.randomUUID();
        ai.acquire(id);
        ai.acquire(id);
        assertThatThrownBy(() -> ai.acquire(id))
            .isInstanceOf(Errors.TooManyRequests.class)
            .extracting(e -> ((Errors.TooManyRequests) e).getMessageKey())
            .isEqualTo("ai.rate.limit.wait");
    }

    @Test
    void acquire_exceedsDailyLimit_throwsTooManyRequests() {
        AiRateLimiter ai = buildLimiter(100, 2);
        UUID id = UUID.randomUUID();
        ai.acquire(id);
        ai.acquire(id);
        assertThatThrownBy(() -> ai.acquire(id))
            .isInstanceOf(Errors.TooManyRequests.class)
            .extracting(e -> ((Errors.TooManyRequests) e).getMessageKey())
            .isEqualTo("ai.rate.limit.wait");
    }

    @Test
    void acquire_differentAccounts_independentBuckets() {
        AiRateLimiter ai = buildLimiter(1, 1);
        UUID id1 = UUID.randomUUID(), id2 = UUID.randomUUID();
        ai.acquire(id1); 
        assertThatNoException().isThrownBy(() -> ai.acquire(id2));
    }
}
