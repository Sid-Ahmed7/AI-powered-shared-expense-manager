package com.pactole.common;

import com.pactole.constants.CommonConstants;

public final class LogSanitizer {
    
    private LogSanitizer() {}

    public static String clean(String value) {
        if (value == null) return null;
        String cleaned = value.replaceAll(CommonConstants.LOG_CLEAN, "_");
        return cleaned.length() > 200 ? cleaned.substring(0, 200) + "…" : cleaned;
    }
}
