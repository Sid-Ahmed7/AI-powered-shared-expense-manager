package com.pactole.constants;

import java.security.SecureRandom;
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
    public static final String NORMALIZE_FORM = "[\\p{InCombiningDiacriticalMarks}]";
    public static final Integer MIN_LENGTH = 12;
    public static final Integer MAX_BYTES = 72;
    public static final String PASSWORD_TXT = "/security/common-passwords.txt";
    public static final int MAX_KEYS = 200_000;
    public static final String PATH = "/api/auth";
    public static final String FIELD_CLEAN = "Nom Complet";
    public static final String CODE_ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    public static final Integer MAX_MEMBERS = 50;
    public static final Pattern INVITE_FORMAT = Pattern.compile("[A-HJ-NP-Z2-9]{8}");

}
