package com.minibank.model;

import com.minibank.exception.InsufficientFundsException;
import com.minibank.util.IdGenerator;
import com.minibank.util.Money;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * CONCEPTS:
 *  - ABSTRACTION: abstract class with abstract "hook" methods (canWithdraw, monthlyInterest)
 *  - ENCAPSULATION: balance is private; it changes only through validated deposit/withdraw
 *  - TEMPLATE METHOD: withdraw() fixes the algorithm, subclasses decide the rule (canWithdraw)
 *  - THREAD-SAFETY: synchronized methods (this object's monitor) - BankService also locks
 *    accounts in a fixed order for transfers (reentrant locks make the nesting legal)
 *  - BigDecimal for money, defensive copies for the transaction history
 */
public abstract class Account {

    private final String id;
    private final Customer owner;
    private final AccountType type;
    private final LocalDateTime openedAt = LocalDateTime.now();
    private BigDecimal balance = Money.ZERO;
    private final List<Transaction> history = new ArrayList<>();

    protected Account(String id, Customer owner, AccountType type) {
        this.id = id;
        this.owner = owner;
        this.type = type;
    }

    // ------------------------------------------------------------------ the template methods
    public final synchronized Transaction deposit(BigDecimal amount, TransactionType type, String note) {
        Money.requirePositive(amount);
        balance = balance.add(amount);
        return record(type, amount, note);
    }

    public final synchronized Transaction withdraw(BigDecimal amount, TransactionType type, String note)
            throws InsufficientFundsException {
        Money.requirePositive(amount);
        if (!canWithdraw(amount)) {                                   // the subclass decides
            throw new InsufficientFundsException(id, amount, availableToWithdraw());
        }
        balance = balance.subtract(amount);
        return record(type, amount, note);
    }

    // ------------------------------------------------------------------ hooks for subclasses
    /** Rule that differs per account type (polymorphism). */
    protected abstract boolean canWithdraw(BigDecimal amount);

    /** How much could be withdrawn right now. */
    public abstract BigDecimal availableToWithdraw();

    /** Interest earned this month (may be zero). */
    public abstract BigDecimal monthlyInterest();

    // ------------------------------------------------------------------ queries
    public final synchronized BigDecimal balance() {
        return balance;
    }

    public final synchronized List<Transaction> history() {
        return List.copyOf(history);                                  // defensive, immutable copy
    }

    public String id() { return id; }

    public Customer owner() { return owner; }

    public AccountType type() { return type; }

    public LocalDateTime openedAt() { return openedAt; }

    public String describe() {
        return String.format("%-9s %-8s %-18s balance %s", id, type.label(), owner.name(), Money.format(balance()));
    }

    @Override
    public String toString() {
        return describe();
    }

    // ------------------------------------------------------------------ internals
    private Transaction record(TransactionType type, BigDecimal amount, String note) {
        Transaction tx = new Transaction(IdGenerator.INSTANCE.next("TX"), id, type, amount, balance, LocalDateTime.now(), note);
        history.add(tx);
        return tx;
    }
}
