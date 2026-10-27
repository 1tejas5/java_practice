package com.minibank.event;

import com.minibank.model.Account;
import com.minibank.model.Transaction;

/**
 * CONCEPTS: OBSERVER pattern as a FUNCTIONAL INTERFACE - listeners can be classes or one-line lambdas:
 *   bank.addListener((acc, tx) -> System.out.println(tx));
 */
@FunctionalInterface
public interface TransactionListener {

    void onTransaction(Account account, Transaction transaction);
}
