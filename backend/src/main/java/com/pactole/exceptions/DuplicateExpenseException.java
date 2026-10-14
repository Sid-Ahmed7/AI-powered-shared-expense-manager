package com.pactole.exceptions;

public class DuplicateExpenseException extends RuntimeException {

    private final java.util.UUID duplicateOf;

    public DuplicateExpenseException(String message, java.util.UUID duplicateOf) {
        super(message);
        this.duplicateOf = duplicateOf;
    }

    public java.util.UUID duplicate() {
        return duplicateOf;
    }
    
}
