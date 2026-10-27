package com.minibank.model;

import com.minibank.util.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * CONCEPTS: INHERITANCE (IS-A Account), method OVERRIDING, constants, BigDecimal maths.
 * Rule: must always keep a minimum balance; earns interest monthly.
 */
public class SavingsAccount extends Account {

    public static final BigDecimal MINIMUM_BALANCE = Money.of("1000");
    private static final BigDecimal TWELVE = BigDecimal.valueOf(12);

    public SavingsAccount(String id, Customer owner) {
        super(id, owner, AccountType.SAVINGS);
    }

    @Override
    protected boolean canWithdraw(BigDecimal amount) {
        return balance().subtract(amount).compareTo(MINIMUM_BALANCE) >= 0;   // compareTo, never equals, for BigDecimal
    }

    @Override
    public BigDecimal availableToWithdraw() {
        return balance().subtract(MINIMUM_BALANCE).max(Money.ZERO);
    }

    @Override
    public BigDecimal monthlyInterest() {
        return balance().multiply(type().annualInterestRate())
                .divide(TWELVE, 2, RoundingMode.HALF_EVEN);                // banker's rounding
    }
}
