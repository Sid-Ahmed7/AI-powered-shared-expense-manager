package com.pactole.security;

import com.pactole.config.PactoleProperties;
import com.pactole.constants.CommonConstants;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;


@Component
public class RefreshCookies {


    private final PactoleProperties.Refresh config;

    public RefreshCookies(PactoleProperties properties) {
        this.config = properties.security().refresh();
    }

    public String name() {
        return config.cookieName();
    }

    public String create(String rawToken, Instant expiresAt) {
        long maxAge = Math.max(0, Duration.between(Instant.now(), expiresAt).toSeconds());
        return base(rawToken).maxAge(maxAge).build().toString();
    }

    public String clear() {
        return base("").maxAge(0).build().toString();
    }

    private ResponseCookie.ResponseCookieBuilder base(String value) {
        return ResponseCookie.from(config.cookieName(), value)
                .httpOnly(true)
                .secure(config.cookieSecure())
                .sameSite("Strict")
                .path(CommonConstants.PATH);
    }
}
