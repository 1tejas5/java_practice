package com.minibank;

import com.minibank.event.FraudDetector;
import com.minibank.exception.AccountNotFoundException;
import com.minibank.exception.BankException;
import com.minibank.exception.CustomerNotFoundException;
import com.minibank.exception.InsufficientFundsException;
import com.minibank.exception.InvalidAmountException;
import com.minibank.model.Account;
import com.minibank.model.AccountType;
import com.minibank.model.Customer;
import com.minibank.model.Transaction;
import com.minibank.model.TransactionType;
import com.minibank.repository.InMemoryRepository;
import com.minibank.service.BankService;
import com.minibank.service.ReportService;
import com.minibank.sim.ConcurrencySimulation;
import com.minibank.util.CsvExporter;
import com.minibank.util.Money;
import com.minibank.testing.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.minibank.testing.Assert.assertEquals;
import static com.minibank.testing.Assert.assertMoney;
import static com.minibank.testing.Assert.assertThrows;
import static com.minibank.testing.Assert.assertTrue;

/** Business-rule tests. Each test gets a NEW instance, so a fresh bank (see TestRunner). */
public class BankServiceTest {

    private final BankService bank = new BankService(new InMemoryRepository<>(Customer::id), new InMemoryRepository<>(Account::id));
    private final Customer asha = bank.registerCustomer("Asha", "asha@mail.com");

    private Account open(AccountType type, long amount) throws BankException {
        return bank.openAccount(asha.id(), type, Money.of(amount));
    }

    @Test("deposit increases the balance and records a transaction")
    void deposit() throws Exception {
        Account acc = open(AccountType.SAVINGS, 5_000);
        Transaction tx = bank.deposit(acc.id(), Money.of(1_500));
        assertMoney("6500", acc.balance());
        assertEquals(TransactionType.DEPOSIT, tx.type());
        assertEquals(2, acc.history().size());                     // opening deposit + this one
    }

    @Test("savings account must keep the minimum balance")
    void savingsMinimumBalance() throws Exception {
        Account acc = open(AccountType.SAVINGS, 5_000);
        InsufficientFundsException e = assertThrows(InsufficientFundsException.class,
                () -> bank.withdraw(acc.id(), Money.of(4_500)));
        assertMoney("4000", e.available());
        assertMoney("5000", acc.balance());                        // unchanged after the failure
        bank.withdraw(acc.id(), Money.of(4_000));
        assertMoney("1000", acc.balance());
    }

    @Test("current account can go into overdraft up to its limit")
    void currentOverdraft() throws Exception {
        Account acc = open(AccountType.CURRENT, 1_000);
        bank.withdraw(acc.id(), Money.of(10_000));
        assertMoney("-9000", acc.balance());
        assertThrows(InsufficientFundsException.class, () -> bank.withdraw(acc.id(), Money.of(1_001)));
    }

    @Test("transfer moves money between accounts")
    void transfer() throws Exception {
        Account a = open(AccountType.SAVINGS, 10_000);
        Account b = open(AccountType.CURRENT, 0);
        bank.transfer(a.id(), b.id(), Money.of(2_500));
        assertMoney("7500", a.balance());
        assertMoney("2500", b.balance());
        assertEquals(TransactionType.TRANSFER_OUT, a.history().getLast().type());
        assertEquals(TransactionType.TRANSFER_IN, b.history().getLast().type());
    }

    @Test("a failed transfer changes nothing (atomicity)")
    void failedTransferIsAtomic() throws Exception {
        Account a = open(AccountType.SAVINGS, 2_000);
        Account b = open(AccountType.SAVINGS, 2_000);
        assertThrows(InsufficientFundsException.class, () -> bank.transfer(a.id(), b.id(), Money.of(1_500)));
        assertMoney("2000", a.balance());
        assertMoney("2000", b.balance());
        assertEquals(1, b.history().size());
    }

    @Test("unknown ids raise specific checked exceptions")
    void notFound() {
        assertThrows(AccountNotFoundException.class, () -> bank.deposit("ACC-0", Money.of(10)));
        assertThrows(CustomerNotFoundException.class, () -> bank.openAccount("CUST-0", AccountType.SAVINGS, Money.of(10)));
        assertThrows(BankException.class, () -> bank.balance("nope"));          // via the base class
    }

    @Test("non-positive amounts are rejected with an unchecked exception")
    void invalidAmounts() throws Exception {
        Account acc = open(AccountType.SAVINGS, 5_000);
        assertThrows(InvalidAmountException.class, () -> bank.deposit(acc.id(), Money.of("-1")));
        assertThrows(InvalidAmountException.class, () -> bank.withdraw(acc.id(), Money.ZERO));
        assertThrows(InvalidAmountException.class, () -> Money.of("12abc"));
    }

    @Test("interest is polymorphic: savings earn it, current accounts do not")
    void interest() throws Exception {
        Account savings = open(AccountType.SAVINGS, 12_000);       // 4% p.a. -> 40.00 per month
        Account current = open(AccountType.CURRENT, 12_000);
        assertEquals(1, bank.applyMonthlyInterest());
        assertMoney("12040", savings.balance());
        assertMoney("12000", current.balance());
    }

    @Test("listeners are notified (observer) and the fraud detector flags big amounts")
    void listeners() throws Exception {
        List<String> seen = new ArrayList<>();
        FraudDetector fraud = new FraudDetector(Money.of(50_000), false);
        bank.addListener((acc, tx) -> seen.add(tx.type().name()));
        bank.addListener(fraud);
        Account a = open(AccountType.SAVINGS, 60_000);
        Account b = open(AccountType.SAVINGS, 1_000);
        bank.transfer(a.id(), b.id(), Money.of(100));
        assertEquals(List.of("DEPOSIT", "DEPOSIT", "TRANSFER_OUT", "TRANSFER_IN"), seen);
        assertEquals(1, fraud.alerts().size());                    // only the 60,000 opening deposit
    }

    @Test("20,000 concurrent transfers never create or destroy money")
    void concurrencyConservesMoney() throws Exception {
        for (int i = 0; i < 6; i++) open(i % 2 == 0 ? AccountType.SAVINGS : AccountType.CURRENT, 10_000);
        ConcurrencySimulation.Result r = ConcurrencySimulation.run(bank, 20_000, true);
        assertTrue(r.moneyConserved(), "money changed: " + r.totalBefore() + " -> " + r.totalAfter());
        assertEquals(20_000, r.succeeded() + r.rejected());
    }

    @Test("reports aggregate correctly with streams")
    void reports() throws Exception {
        Customer ravi = bank.registerCustomer("Ravi", "ravi@mail.com");
        open(AccountType.SAVINGS, 10_000);
        open(AccountType.CURRENT, 5_000);
        bank.openAccount(ravi.id(), AccountType.SAVINGS, Money.of(20_000));
        ReportService reports = new ReportService(bank);
        assertMoney("30000", reports.totalBalanceByType().get(AccountType.SAVINGS));
        assertEquals("Ravi", reports.topCustomers(1).getFirst().customer().name());
        assertMoney("20000", reports.largestTransaction().orElseThrow().amount());
        assertTrue(reports.summary().contains("BANK REPORT"), "summary should have a title");
    }

    @Test("CSV export writes a header plus one line per transaction")
    void csvExport() throws Exception {
        Account acc = open(AccountType.SAVINGS, 5_000);
        bank.deposit(acc.id(), Money.of(250));
        Path dir = Files.createTempDirectory("minibank-test");
        Path file = CsvExporter.exportStatement(acc, dir);
        List<String> lines = CsvExporter.read(file);
        assertEquals(3, lines.size());
        assertTrue(lines.get(0).startsWith("id,timestamp"), "header expected");
        Files.delete(file);
        Files.delete(dir);
    }

    @Test("accounts are listed in id order")
    void listing() throws Exception {
        open(AccountType.SAVINGS, 1_000);
        open(AccountType.CURRENT, 1_000);
        List<Account> all = bank.allAccounts();
        assertEquals(all.stream().sorted(Comparator.comparing(Account::id)).toList(), all);
    }
}
