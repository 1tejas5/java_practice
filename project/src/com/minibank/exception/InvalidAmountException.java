package com.minibank.exception;

/**
 * CONCEPTS: UNCHECKED exception (extends RuntimeException).
 * A negative or zero amount is a caller bug / bad input - fail fast, no forced try/catch.
 */
public class InvalidAmountException extends RuntimeException {

    public InvalidAmountException(String message) {
        super(message);
    }
}
