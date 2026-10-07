package com.pactole.exceptions;

public final class Errors {

    private Errors() {
    }

    public static class Validation extends RuntimeException {
        private final String messageKey;
        private final Object[] args;

        public Validation(String messageKey, Object... args) {
            super(messageKey);
            this.messageKey = messageKey;
            this.args = args != null ? args : new Object[0];
        }

        public String getMessageKey() { return messageKey; }
        public Object[] getArgs() { return args; }
    }

    public static class NotFound extends RuntimeException {
        private final String messageKey;
        private final Object[] args;

        public NotFound(String messageKey, Object... args) {
            super(messageKey);
            this.messageKey = messageKey;
            this.args = args != null ? args : new Object[0];
        }

        public String getMessageKey() { return messageKey; }
        public Object[] getArgs() { return args; }
    }

    public static class Unauthorized extends RuntimeException {
        private final String messageKey;
        private final Object[] args;

        public Unauthorized(String messageKey, Object... args) {
            super(messageKey);
            this.messageKey = messageKey;
            this.args = args != null ? args : new Object[0];
        }

        public String getMessageKey() { return messageKey; }
        public Object[] getArgs() { return args; }
    }

    public static class Forbidden extends RuntimeException {
        private final String messageKey;
        private final Object[] args;

        public Forbidden(String messageKey, Object... args) {
            super(messageKey);
            this.messageKey = messageKey;
            this.args = args != null ? args : new Object[0];
        }

        public String getMessageKey() { return messageKey; }
        public Object[] getArgs() { return args; }
    }

    public static class Conflict extends RuntimeException {
        private final String messageKey;
        private final Object[] args;
        private final String code;

        public Conflict(String messageKey) {
            this(messageKey, null);
        }

        public Conflict(String messageKey, String code, Object... args) {
            super(messageKey);
            this.messageKey = messageKey;
            this.code = code;
            this.args = args != null ? args : new Object[0];
        }

        public String getMessageKey() { return messageKey; }
        public Object[] getArgs() { return args; }
        public String getCode() { return code; }
    }

    public static class AiUnavailable extends RuntimeException {
        public AiUnavailable(String message) {
            super(message);
        }
    }

    public static class TooManyRequests extends RuntimeException {
        private final long retryAfterSeconds;

        public TooManyRequests(String message, long retryAfterSeconds) {
            super(message);
            this.retryAfterSeconds = retryAfterSeconds;
        }

        public long retryAfterSeconds() {
            return retryAfterSeconds;
        }
    }
}
