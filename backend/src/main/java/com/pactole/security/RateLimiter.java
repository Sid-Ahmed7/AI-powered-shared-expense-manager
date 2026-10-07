package com.pactole.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.pactole.constants.CommonConstants;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;


@Component
public class RateLimiter {

    private static final Logger log = LoggerFactory.getLogger(RateLimiter.class);

    public record Limit(String name, int capacity, Duration period) {
        public Limit {
            if (capacity <= 0 || period.isZero() || period.isNegative()) {
                throw new IllegalArgumentException("Limite invalide : " + name);
            }
        }
    }

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final LongSupplier nanoClock;

    public RateLimiter() {
        this(System::nanoTime);
    }

    RateLimiter(LongSupplier nanoClock) {
        this.nanoClock = nanoClock;
    }

    public long tryAcquire(Limit limit, String key) {
        if (key == null || key.isBlank()) key = "unknown";
        String fullKey = limit.name() + ':' + key;
        Bucket bucket = buckets.get(fullKey);
        if (bucket == null) {
            if (buckets.size() >= CommonConstants.MAX_KEYS) {
                evictIdle();
                if (buckets.size() >= CommonConstants.MAX_KEYS) {
                    log.warn("Limiteur saturé ({} clés) : requête non comptée", buckets.size());
                    return 0;
                }
            }
            bucket = buckets.computeIfAbsent(fullKey, k -> new Bucket(limit, nanoClock.getAsLong()));
        }
        return bucket.tryConsume(nanoClock.getAsLong());
    }

    /** Vide le seau d'une clé (ex. après une connexion réussie). */
    public void reset(Limit limit, String key) {
        buckets.remove(limit.name() + ':' + key);
    }

    @Scheduled(fixedRate = 60_000)
    public void evictIdle() {
        long now = nanoClock.getAsLong();
        buckets.entrySet().removeIf(e -> e.getValue().isIdleAndFull(now));
    }

    int size() {
        return buckets.size();
    }

    static final class Bucket {
        private final double capacity;
        private final double refillPerNano;
        private double tokens;
        private long last;

        Bucket(Limit limit, long now) {
            this.capacity = limit.capacity();
            this.refillPerNano = limit.capacity() / (double) limit.period().toNanos();
            this.tokens = capacity;
            this.last = now;
        }

        synchronized long tryConsume(long now) {
            refill(now);
            if (tokens >= 1) {
                tokens -= 1;
                return 0;
            }
            double missingNanos = (1 - tokens) / refillPerNano;
            return Math.max(1, (long) Math.ceil(missingNanos / 1_000_000_000d));
        }

        synchronized boolean isIdleAndFull(long now) {
            refill(now);
            return tokens >= capacity;
        }

        private void refill(long now) {
            long elapsed = now - last;
            if (elapsed > 0) {
                tokens = Math.min(capacity, tokens + elapsed * refillPerNano);
                last = now;
            }
        }
    }
}
