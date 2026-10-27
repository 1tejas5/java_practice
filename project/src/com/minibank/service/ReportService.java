package com.minibank.service;

import com.minibank.model.Account;
import com.minibank.model.AccountType;
import com.minibank.model.Customer;
import com.minibank.model.Transaction;
import com.minibank.model.TransactionType;
import com.minibank.util.Money;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.reducing;

/**
 * CONCEPTS: STREAMS everywhere - groupingBy with downstream collectors, reducing BigDecimals,
 * partitioningBy, flatMap, sorted with Comparators, Optional, EnumMap, records as results,
 * and a TEXT BLOCK for the report layout.
 */
public class ReportService {

    /** A small result record: perfect use of records as "named tuples". */
    public record CustomerWealth(Customer customer, BigDecimal total) { }

    private final BankService bank;

    public ReportService(BankService bank) {
        this.bank = bank;
    }

    public Map<AccountType, BigDecimal> totalBalanceByType() {
        return bank.allAccounts().stream().collect(groupingBy(Account::type, () -> new EnumMap<>(AccountType.class),
                reducing(Money.ZERO, Account::balance, BigDecimal::add)));
    }

    public List<CustomerWealth> topCustomers(int n) {
        return bank.allAccounts().stream()
                .collect(groupingBy(Account::owner, reducing(Money.ZERO, Account::balance, BigDecimal::add)))
                .entrySet().stream()
                .map(e -> new CustomerWealth(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(CustomerWealth::total).reversed()
                        .thenComparing(cw -> cw.customer().name()))
                .limit(n)
                .toList();
    }

    public List<Transaction> allTransactions() {
        return bank.allAccounts().stream()
                .flatMap(a -> a.history().stream())                       // List<List<Tx>> -> Stream<Tx>
                .sorted(Comparator.comparing(Transaction::timestamp).thenComparing(Transaction::id))
                .toList();
    }

    public Map<TransactionType, Long> transactionCountByType() {
        return allTransactions().stream().collect(groupingBy(Transaction::type, () -> new EnumMap<>(TransactionType.class), counting()));
    }

    public Optional<Transaction> largestTransaction() {
        return allTransactions().stream().max(Comparator.comparing(Transaction::amount));
    }

    public Map<Boolean, List<String>> overdrawnAccounts() {
        return bank.allAccounts().stream().collect(partitioningBy(a -> a.balance().signum() < 0,
                Collectors.mapping(Account::id, Collectors.toList())));
    }

    public String summary() {
        StringBuilder byType = new StringBuilder();
        totalBalanceByType().forEach((type, total) ->
                byType.append(String.format("   %-8s %s%n", type.label(), Money.format(total))));
        StringBuilder top = new StringBuilder();
        List<CustomerWealth> leaders = topCustomers(3);
        for (int i = 0; i < leaders.size(); i++) {
            top.append(String.format("   %d. %-18s %s%n", i + 1, leaders.get(i).customer().name(), Money.format(leaders.get(i).total())));
        }
        String largest = largestTransaction()
                .map(t -> t.type() + " " + Money.format(t.amount()) + " on " + t.accountId())
                .orElse("none");
        return """
                ================= BANK REPORT =================
                Customers: %d   Accounts: %d   Transactions: %d
                Total money held: %s
                Balance by account type:
                %s\
                Top customers:
                %s\
                Transactions by type: %s
                Largest transaction: %s
                Overdrawn accounts: %s
                ===============================================""".formatted(
                bank.allCustomers().size(), bank.allAccounts().size(), allTransactions().size(),
                Money.format(bank.totalMoney()), byType, top, transactionCountByType(), largest,
                overdrawnAccounts().get(true));
    }
}
