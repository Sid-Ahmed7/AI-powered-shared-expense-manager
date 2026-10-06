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
        public NotFound(String message) {
            super(message);
        }
    }

    public static class Unauthorized extends RuntimeException {
        public Unauthorized(String message) {
            super(message);
        }
    }

    public static class Forbidden extends RuntimeException {
        public Forbidden(String message) {
            super(message);
        }
    }

    public static class Conflict extends RuntimeException {
        public Conflict(String message) {
            super(message);
        }
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
