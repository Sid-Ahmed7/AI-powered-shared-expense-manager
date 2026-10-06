package com.pactole.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Base64;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SecretsValidator {

    private final byte[] jwtKey;

    public SecretsValidator(
            @Value("${pactole.jwt.secret:}") String secret,
            @Value("${pactole.security.known-public-secrets:}") String knownPublicSecretsRaw,
            Environment environment) {

        if (secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET manquant (générez-le avec : openssl rand -base64 32)");
        }
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(secret.strip());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("JWT_SECRET doit être encodé en base64 (openssl rand -base64 32)", e);
        }
        if (decoded.length < 32) {
            throw new IllegalStateException("JWT_SECRET trop court : 256 bits (32 octets) minimum pour HS256");
        }
        boolean devOrTest = environment.acceptsProfiles(Profiles.of("dev", "test"));
        if (!devOrTest) {
            Set<String> knownPublicSecrets = Arrays.stream(knownPublicSecretsRaw.split(","))
                    .map(String::strip)
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toSet());
            if (knownPublicSecrets.contains(secret.strip())) {
                throw new IllegalStateException("JWT_SECRET de développement détecté hors profil dev : refus de démarrer");
            }
        }
        this.jwtKey = decoded;
    }

    public byte[] jwtKey() {
        return jwtKey.clone();
    }
}
