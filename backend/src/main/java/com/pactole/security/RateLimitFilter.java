package com.pactole.security;

import com.pactole.common.ClientInfo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiter limiter;
    private final Limits limits;
    private final ProblemResponses problemResponses;
    private final MessageSource messages;

    public RateLimitFilter(RateLimiter limiter, Limits limits, ProblemResponses problemResponses, MessageSource messages) {
        this.limiter = limiter;
        this.limits = limits;
        this.problemResponses = problemResponses;
        this.messages = messages;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/") || "OPTIONS".equals(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String ip = ClientInfo.of(request).ip();
        String path = request.getRequestURI();

        long wait = limiter.tryAcquire(limits.globalPerIp, ip);
        if (wait == 0 && path.startsWith("/api/auth/")) {
            wait = limiter.tryAcquire(limits.authPerIp, ip);
        }
        if (wait == 0 && path.startsWith("/api/join/")) {
            wait = limiter.tryAcquire(limits.joinPerIp, ip);
        }
        if (wait > 0) {
            String detail = messages.getMessage("rate.limit.exceeded", new Object[]{wait},
                    "Too many requests", LocaleContextHolder.getLocale());
            problemResponses.write(response, 429, detail, HttpHeaders.RETRY_AFTER, String.valueOf(wait));
            return;
        }
        chain.doFilter(request, response);
    }
}
