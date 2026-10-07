package com.pactole.security;

import com.pactole.common.TextMatching;
import com.pactole.constants.CommonConstants;
import com.pactole.exceptions.Errors;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;


@Component
public class PasswordPolicy {

    private final Set<String> common;

    public PasswordPolicy() {
        this.common = loadCommonPasswords();
    }

    public void validate(String password, String email, String displayName) {
        if (password == null || password.codePointCount(0, password.length()) < CommonConstants.MIN_LENGTH) {
            throw new Errors.Validation("validation.password.too.short", CommonConstants.MIN_LENGTH);
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > CommonConstants.MAX_BYTES) {
            throw new Errors.Validation("validation.password.too.long", CommonConstants.MAX_BYTES);
        }
        if (password.isBlank() || password.chars().distinct().count() < 4) {
            throw new Errors.Validation("validation.password.too.repetitive");
        }
        String normalized = TextMatching.normalize(password).replace(" ", "");
        if (common.contains(password.toLowerCase()) || common.contains(normalized)) {
            throw new Errors.Validation("validation.password.common");
        }
        String localPart = email == null ? "" : TextMatching.normalize(email.split("@")[0]).replace(" ", "");
        if (localPart.length() >= 4 && normalized.contains(localPart)) {
            throw new Errors.Validation("validation.password.contains.email");
        }
        String name = displayName == null ? "" : TextMatching.normalize(displayName).replace(" ", "");
        if (name.length() >= 4 && normalized.contains(name)) {
            throw new Errors.Validation("validation.password.contains.name");
        }
    }

    private static Set<String> loadCommonPasswords() {
        Set<String> set = new HashSet<>();
        try (InputStream in = PasswordPolicy.class.getResourceAsStream(CommonConstants.PASSWORD_TXT)) {
            if (in == null) {
                throw new IllegalStateException("Liste des mots de passe courants introuvable : /security/common-passwords.txt");
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                reader.lines()
                        .map(String::strip)
                        .filter(l -> !l.isEmpty() && !l.startsWith("#"))
                        .map(String::toLowerCase)
                        .forEach(set::add);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return Set.copyOf(set);
    }
}
