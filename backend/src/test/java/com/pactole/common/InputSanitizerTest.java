package com.pactole.common;

import com.pactole.exceptions.Errors;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class InputSanitizerTest {

    @Test
    void clean_validName_returnsNormalized() {
        assertThat(InputSanitizer.clean("Alice", "name")).isEqualTo("Alice");
    }

    @Test
    void clean_leadingTrailingSpaces_areStripped() {
        assertThat(InputSanitizer.clean("  Alice  ", "name")).isEqualTo("Alice");
    }

    @Test
    void clean_multipleInternalSpaces_collapsedToOne() {
        assertThat(InputSanitizer.clean("Alice  Bob", "name")).isEqualTo("Alice Bob");
    }

    @Test
    void clean_null_throwsValidation() {
        assertThatThrownBy(() -> InputSanitizer.clean(null, "name"))
            .isInstanceOf(Errors.Validation.class)
            .extracting(e -> ((Errors.Validation) e).getMessageKey())
            .isEqualTo("validation.required");
    }

    @Test
    void clean_forbiddenChars_throwsValidation() {
        assertThatThrownBy(() -> InputSanitizer.clean("<script>", "name"))
            .isInstanceOf(Errors.Validation.class)
            .extracting(e -> ((Errors.Validation) e).getMessageKey())
            .isEqualTo("validation.field.invalid");
    }

    @Test
    void text_validInput_returnsNormalized() {
        assertThat(InputSanitizer.text("Hello world", "note", 200)).isEqualTo("Hello world");
    }

    @Test
    void text_null_throwsValidation() {
        assertThatThrownBy(() -> InputSanitizer.text(null, "note", 200))
            .isInstanceOf(Errors.Validation.class)
            .extracting(e -> ((Errors.Validation) e).getMessageKey())
            .isEqualTo("validation.required");
    }

    @Test
    void text_exceedsMaxCodePoints_throwsValidation() {
        String tooLong = "a".repeat(201);
        assertThatThrownBy(() -> InputSanitizer.text(tooLong, "note", 200))
            .isInstanceOf(Errors.Validation.class)
            .extracting(e -> ((Errors.Validation) e).getMessageKey())
            .isEqualTo("validation.field.length");
    }

    @Test
    void text_invisibleChars_areRemovedOrReplaced() {
        String result = InputSanitizer.text("Hello​World", "note", 200);
        assertThat(result).doesNotContain("​");
    }
}