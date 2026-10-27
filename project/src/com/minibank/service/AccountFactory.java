package com.minibank.service;

import com.minibank.model.Account;
import com.minibank.model.AccountType;
import com.minibank.model.CurrentAccount;
import com.minibank.model.Customer;
import com.minibank.model.SavingsAccount;

/**
 * CONCEPTS: FACTORY pattern - callers ask for an AccountType and never call `new SavingsAccount(...)`.
 * The exhaustive switch over the enum means adding a new AccountType produces a compile error here
 * until it is handled.
 */
public final class AccountFactory {

    private AccountFactory() { }

    public static Account create(AccountType type, String id, Customer owner) {
        return switch (type) {
            case SAVINGS -> new SavingsAccount(id, owner);
            case CURRENT -> new CurrentAccount(id, owner);
        };
    }
}
