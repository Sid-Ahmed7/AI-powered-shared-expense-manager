package com.pactole.exceptions;

import java.util.UUID;

public class DuplicateExpenseException extends RuntimeException {

    private final String messageKey;
    private final Object[] args;
    private final UUID duplicateOf;

    public DuplicateExpenseException(String messageKey, UUID duplicateOf, Object... args) {
        super(messageKey);
        this.messageKey = messageKey;
        this.duplicateOf = duplicateOf;
        this.args = args != null ? args : new Object[0];
    }

    public String getMessageKey() { return messageKey; }
    public Object[] getArgs() { return args; }
    public UUID duplicate() { return duplicateOf; }
}
