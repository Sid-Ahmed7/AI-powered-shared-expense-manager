package com.pactole.controller;

import com.pactole.config.PactoleProperties;
import com.pactole.config.SecretsValidator;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.List;

@TestConfiguration
public class ControllerTestConfig {

    @Bean
    @Primary
    public SecretsValidator testSecretsValidator() {
        SecretsValidator sv = Mockito.mock(SecretsValidator.class);
        Mockito.when(sv.jwtKey()).thenReturn(new byte[32]);
        return sv;
    }

    @Bean
    @Primary
    public PactoleProperties testPactoleProperties() {
        return new PactoleProperties(
            List.of("http://localhost"),
            new PactoleProperties.Security(
                new PactoleProperties.Refresh("rt", false, 30, 90, 5),
                new PactoleProperties.Lockout(5, 15),
                new PactoleProperties.RateLimits(120, 20, 10, 10),
                new PactoleProperties.Jwt("test-secret", "pactole", "pactole-app", 10, "sid")
            ),
            new PactoleProperties.Ai(null, "http://localhost", "/test", "test", "test", 5, 100, 500)
        );
    }
}
