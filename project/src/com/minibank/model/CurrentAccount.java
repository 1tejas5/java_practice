package com.minibank.model;

import com.minibank.util.Money;

import java.math.BigDecimal;

/**
 * CONCEPTS: a second subclass with DIFFERENT behaviour for the same methods (runtime polymorphism).
 * Rule: may go negative down to an overdraft limit; earns no interest.
 */
public class CurrentAccount extends Account {

    public static final BigDecimal DEFAULT_OVERDRAFT = Money.of("10000");
    private final BigDecimal overdraftLimit;

    public CurrentAccount(String id, Customer owner) {
        this(id, owner, DEFAULT_OVERDRAFT);              // constructor chaining with this(...)
    }

    public CurrentAccount(String id, Customer owner, BigDecimal overdraftLimit) {
        super(id, owner, AccountType.CURRENT);
        this.overdraftLimit = overdraftLimit;
    }

    @Override
    protected boolean canWithdraw(BigDecimal amount) {
        return balance().subtract(amount).compareTo(overdraftLimit.negate()) >= 0;
    }

    @Override
    public BigDecimal availableToWithdraw() {
        return balance().add(overdraftLimit);
    }

    @Override
    public BigDecimal monthlyInterest() {
        return Money.ZERO;
    }

    public BigDecimal overdraftLimit() {
        return overdraftLimit;
    }
}
