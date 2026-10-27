package com.minibank.model;

/**
 * CONCEPTS: enum with a boolean field so behaviour lives WITH the data
 * (no if/else chains elsewhere asking "is this a credit?").
 */
public enum TransactionType {
    DEPOSIT(true),
    WITHDRAWAL(false),
    TRANSFER_IN(true),
    TRANSFER_OUT(false),
    INTEREST(true);

    private final boolean credit;

    TransactionType(boolean credit) {
        this.credit = credit;
    }

    public boolean isCredit() {
        return credit;
    }

    /** "+" for money coming in, "-" for money going out. */
    public String sign() {
        return credit ? "+" : "-";
    }
}
