package com.pactole.constants;

import java.util.regex.Pattern;

public final class CommonConstants {
    
    private CommonConstants() {}

    public static final Integer MAX_TRUNCATE_REMOTE = 45;
    public static final Integer MAX_TRUNCATE_LOG = 200;
    public static final String LOG_CLEAN = "[\\r\\n\\t\\p{Cntrl}]";
    public static final Pattern ALLOWED = Pattern.compile("[\\p{L}\\p{M}0-9 .'’-]{1,40}");
    public static final String INVISIBLE_CHARS = "[\\p{Cc}\\p{Cf}&&[^\\u200D]]";
    public static final String HEADER = "X-Request-Id";
    public static final String MDC_KEY = "requestId";
    public static final Pattern SAFE = Pattern.compile("[A-Za-z0-9-]{8,64}");


}
