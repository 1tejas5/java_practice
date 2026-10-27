package com.minibank.service;

import com.minibank.event.TransactionListener;
import com.minibank.exception.AccountNotFoundException;
import com.minibank.exception.BankException;
import com.minibank.exception.CustomerNotFoundException;
import com.minibank.exception.InsufficientFundsException;
import com.minibank.model.Account;
import com.minibank.model.AccountType;
import com.minibank.model.Customer;
import com.minibank.model.Transaction;
import com.minibank.model.TransactionType;
import com.minibank.repository.Repository;
import com.minibank.util.IdGenerator;
import com.minibank.util.Money;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The heart of the application: business rules.
 *
 * CONCEPTS:
 *  - DEPENDENCY INVERSION / constructor injection: depends on Repository interfaces, not on a map or a DB
 *  - checked exceptions in signatures (throws BankException ...)
 *  - OBSERVER: listeners notified after every transaction
 *  - DEADLOCK-FREE transfers: lock two accounts in a GLOBAL ORDER (by id) - breaks "circular wait"
 *  - Optional.orElseThrow with an exception supplier
 */
public class BankService {

    private final Repository<Customer, String> customers;
    private final Repository<Account, String> accounts;
    private final List<TransactionListener> listeners = new CopyOnWriteArrayList<>();

    public BankService(Repository<Customer, String> customers, Repository<Account, String> accounts) {
        this.customers = customers;
        this.accounts = accounts;
    }

    public void addListener(TransactionListener listener) {
        listeners.add(listener);
    }

    public void removeListener(TransactionListener listener) {   // "unsubscribe"
        listeners.remove(listener);
    }

    // ------------------------------------------------------------------ customers & accounts
    public Customer registerCustomer(String name, String email) {
        Customer c = Customer.builder(IdGenerator.INSTANCE.next("CUST"), name).email(email).build();
        return customers.save(c);
    }

    public Account openAccount(String customerId, AccountType type, BigDecimal openingDeposit) throws CustomerNotFoundException {
        Customer owner = customers.findById(customerId).orElseThrow(() -> new CustomerNotFoundException(customerId));
        Account account = AccountFactory.create(type, IdGenerator.INSTANCE.next("ACC"), owner);
        accounts.save(account);
        if (openingDeposit.compareTo(BigDecimal.ZERO) > 0) {
            publish(account, account.deposit(openingDeposit, TransactionType.DEPOSIT, "opening deposit"));
        }
        return account;
    }

    public Account account(String accountId) throws AccountNotFoundException {
        return accounts.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    public List<Account> allAccounts() {
        return accounts.findAll().stream().sorted((a, b) -> a.id().compareTo(b.id())).toList();
    }

    public List<Customer> allCustomers() {
        return customers.findAll();
    }

    public List<Account> accountsOf(String customerId) {
        return accounts.findWhere(a -> a.owner().id().equals(customerId));
    }

    // ------------------------------------------------------------------ money movement
    public Transaction deposit(String accountId, BigDecimal amount) throws AccountNotFoundException {
        Account acc = account(accountId);
        Transaction tx = acc.deposit(amount, TransactionType.DEPOSIT, "cash deposit");
        publish(acc, tx);
        return tx;
    }

    public Transaction withdraw(String accountId, BigDecimal amount) throws BankException {
        Account acc = account(accountId);
        Transaction tx = acc.withdraw(amount, TransactionType.WITHDRAWAL, "cash withdrawal");
        publish(acc, tx);
        return tx;
    }

    /**
     * Atomic, thread-safe, deadlock-free transfer.
     * Both monitors are held while the money moves, so no other thread ever sees "money in flight".
     */
    public void transfer(String fromId, String toId, BigDecimal amount) throws BankException {
        if (fromId.equals(toId)) throw new BankException("Cannot transfer to the same account");
        Money.requirePositive(amount);
        Account from = account(fromId);
        Account to = account(toId);

        Account first = from.id().compareTo(to.id()) < 0 ? from : to;    // global lock order
        Account second = first == from ? to : from;

        Transaction out, in;
        synchronized (first) {
            synchronized (second) {
                out = from.withdraw(amount, TransactionType.TRANSFER_OUT, "to " + to.id());   // may throw -> nothing changed
                in = to.deposit(amount, TransactionType.TRANSFER_IN, "from " + from.id());
            }
        }
        publish(from, out);                                               // notify OUTSIDE the locks
        publish(to, in);
    }

    /** Credits each account's monthly interest (polymorphic: savings > 0, current = 0). */
    public int applyMonthlyInterest() {
        int credited = 0;
        for (Account acc : accounts.findAll()) {
            BigDecimal interest = acc.monthlyInterest();
            if (interest.compareTo(BigDecimal.ZERO) > 0) {
                publish(acc, acc.deposit(interest, TransactionType.INTEREST, "monthly interest"));
                credited++;
            }
        }
        return credited;
    }

    // ------------------------------------------------------------------ queries
    public BigDecimal balance(String accountId) throws AccountNotFoundException {
        return account(accountId).balance();
    }

    public List<Transaction> statement(String accountId) throws AccountNotFoundException {
        return account(accountId).history();
    }

    /** Sum of all balances - used to prove transfers never create or destroy money. */
    public BigDecimal totalMoney() {
        return accounts.findAll().stream().map(Account::balance).reduce(Money.ZERO, BigDecimal::add);
    }

    /** Convenience for callers that want a boolean instead of an exception. */
    public boolean tryTransfer(String fromId, String toId, BigDecimal amount) {
        try {
            transfer(fromId, toId, amount);
            return true;
        } catch (InsufficientFundsException e) {
            return false;
        } catch (BankException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private void publish(Account account, Transaction tx) {
        for (TransactionListener l : listeners) l.onTransaction(account, tx);
    }
}
