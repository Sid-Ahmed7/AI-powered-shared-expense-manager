package com.pactole.security;

import com.pactole.exceptions.Errors;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PasswordPolicyTest {

    private final PasswordPolicy passwordPolicy = new PasswordPolicy();

    @Test
    void validate_strongPassword_passes() {
        assertThatNoException().isThrownBy(() -> passwordPolicy.validate("Unique$Pass#2024Xyz", "user@gmail.com", "Jane Doe"));
    }

    @Test
    void validate_tooShort_throwsValidation() {
        assertThatThrownBy(() ->
            passwordPolicy.validate("Short1!", "user@gmail.com", "Test"))
            .isInstanceOf(Errors.Validation.class)
            .extracting(e -> ((Errors.Validation) e).getMessageKey())
            .isEqualTo("validation.password.too.short");
    }

    @Test
    void validate_tooManyBytes_throwsValidation() {
        String longPassword = "Aa1!".repeat(19); 
        assertThatThrownBy(() ->
            passwordPolicy.validate(longPassword, "user@gmail.com", "Test"))
            .isInstanceOf(Errors.Validation.class)
            .extracting(e -> ((Errors.Validation) e).getMessageKey())
            .isEqualTo("validation.password.too.long");
    }

    @Test
    void validate_tooRepetitive_throwsValidation() {
        assertThatThrownBy(() ->
            passwordPolicy.validate("aaaaaaaaaaaaa", "user@gmail.com", "Test"))
            .isInstanceOf(Errors.Validation.class)
            .extracting(e -> ((Errors.Validation) e).getMessageKey())
            .isEqualTo("validation.password.too.repetitive");
    }

    @Test
    void validate_containsEmailLocalPart_throwsValidation() {
        assertThatThrownBy(() ->
            passwordPolicy.validate("johndoe2024password!!", "johndoe2024@gmail.com", "Test"))
            .isInstanceOf(Errors.Validation.class)
            .extracting(e -> ((Errors.Validation) e).getMessageKey())
            .isEqualTo("validation.password.contains.email");
    }

    @Test
    void validate_containsDisplayName_throwsValidation() {
        assertThatThrownBy(() ->
            passwordPolicy.validate("alicesmithPassword123!", "user@gmail.com", "Alice Smith"))
            .isInstanceOf(Errors.Validation.class)
            .extracting(e -> ((Errors.Validation) e).getMessageKey())
            .isEqualTo("validation.password.contains.name");
    }

    @Test
    void validate_nullEmail_doesNotThrowForEmailCheck() {
        assertThatNoException().isThrownBy(() ->
            passwordPolicy.validate("Unique$Pass#2024Xyz", null, "Jane Doe"));
    }
}
