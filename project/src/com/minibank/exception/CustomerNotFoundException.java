package com.minibank.exception;

/** CONCEPTS: specific checked exception in a hierarchy. */
public class CustomerNotFoundException extends BankException {

    public CustomerNotFoundException(String customerId) {
        super("No customer with id " + customerId);
    }
}
