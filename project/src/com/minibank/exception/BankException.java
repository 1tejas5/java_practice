package com.minibank.exception;

/**
 * CONCEPTS: CHECKED exception hierarchy. The base class lets callers catch "any banking problem"
 * with one catch block, while subclasses carry specific details.
 * Checked = recoverable business conditions the caller must consciously handle.
 */
public class BankException extends Exception {

    public BankException(String message) {
        super(message);
    }

    public BankException(String message, Throwable cause) {
        super(message, cause);
    }
}
