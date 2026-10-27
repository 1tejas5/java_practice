package com.minibank.model;

import java.math.BigDecimal;

/**
 * CONCEPTS: enum with fields, a constructor and methods.
 * Each constant is a singleton object carrying its own data.
 */
public enum AccountType {
    SAVINGS("Savings", new BigDecimal("0.04")),   // 4% per year
    CURRENT("Current", BigDecimal.ZERO);          // no interest

    private final String label;
    private final BigDecimal annualInterestRate;

    AccountType(String label, BigDecimal annualInterestRate) {   // enum constructors are implicitly private
        this.label = label;
        this.annualInterestRate = annualInterestRate;
    }

    public String label() {
        return label;
    }

    public BigDecimal annualInterestRate() {
        return annualInterestRate;
    }

    /** Lenient parsing for user input such as "savings" or "CURRENT". */
    public static AccountType parse(String text) {
        return valueOf(text.strip().toUpperCase());
    }
}
