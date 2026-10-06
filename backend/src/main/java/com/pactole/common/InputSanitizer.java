package com.pactole.common;

import java.text.Normalizer;

import com.pactole.constants.CommonConstants;
import com.pactole.exceptions.Errors;

public final class InputSanitizer {

    private InputSanitizer() {
    }

    public static String clean(String value, String field) {
        if (value == null) {
            throw new Errors.Validation("validation.required", field);
        }
        String s = Normalizer.normalize(value, Normalizer.Form.NFC).strip().replaceAll("\\s+", " ");
        if (!CommonConstants.ALLOWED.matcher(s).matches()) {
            throw new Errors.Validation("validation.field.invalid", field);
        }
        return s;
    }

    public static String text(String value, String field, int max) {
        if (value == null) {
            throw new Errors.Validation("validation.required", field);
        }
        String s = Normalizer.normalize(value, Normalizer.Form.NFC)
                .replaceAll(CommonConstants.INVISIBLE_CHARS, " ")
                .strip().replaceAll("\\s+", " ");
        if (s.isEmpty() || s.codePointCount(0, s.length()) > max) {
            throw new Errors.Validation("validation.field.length", field, max);
        }
        return s;
    }

}
