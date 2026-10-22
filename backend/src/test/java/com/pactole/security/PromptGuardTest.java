package com.pactole.security;

import com.pactole.constants.CommonConstants;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PromptGuardTest {

    @Test
    void clean_null_returnsEmptyString() {
        assertThat(PromptGuard.clean(null)).isEmpty();
    }

    @Test
    void clean_normalInput_returnsSameContent() {
        assertThat(PromptGuard.clean("Hello world")).isEqualTo("Hello world");
    }

    @Test
    void clean_leadingTrailingWhitespace_isStripped() {
        assertThat(PromptGuard.clean("  hello  ")).isEqualTo("hello");
    }

    @Test
    void clean_exceedsMaxLength_isTruncated() {
        String longInput = "a".repeat(CommonConstants.MAX_LENGTH_PROMPT + 1);
        assertThat(PromptGuard.clean(longInput)).hasSize(CommonConstants.MAX_LENGTH_PROMPT);
    }

    @Test
    void clean_exactlyMaxLength_isNotTruncated() {
        String input = "a".repeat(CommonConstants.MAX_LENGTH_PROMPT);
        assertThat(PromptGuard.clean(input)).hasSize(CommonConstants.MAX_LENGTH_PROMPT);
    }

    @Test
    void clean_formatControlChar_isRemoved() {
        String input = "Hello​World";
        assertThat(PromptGuard.clean(input)).isEqualTo("HelloWorld");
    }
}