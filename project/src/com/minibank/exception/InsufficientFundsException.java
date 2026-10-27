package com.minibank.exception;

import com.minibank.util.Money;

import java.math.BigDecimal;

/** CONCEPTS: custom checked exception carrying context fields (not just a message). */
public class InsufficientFundsException extends BankException {

    private final String accountId;
    private final BigDecimal requested;
    private final BigDecimal available;

    public InsufficientFundsException(String accountId, BigDecimal requested, BigDecimal available) {
        super("Insufficient funds in " + accountId + ": requested " + Money.format(requested)
                + ", available " + Money.format(available));
        this.accountId = accountId;
        this.requested = requested;
        this.available = available;
    }

    public String accountId() { return accountId; }

    public BigDecimal requested() { return requested; }

    public BigDecimal available() { return available; }

    public BigDecimal shortfall() { return requested.subtract(available); }
}
