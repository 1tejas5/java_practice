package com.minibank.exception;

/** CONCEPTS: specific checked exception in a hierarchy. */
public class AccountNotFoundException extends BankException {

    public AccountNotFoundException(String accountId) {
        super("No account with id " + accountId);
    }
}
