package com.minibank;

import com.minibank.cli.ConsoleApp;
import com.minibank.event.FraudDetector;
import com.minibank.event.TransactionListener;
import com.minibank.exception.BankException;
import com.minibank.exception.InsufficientFundsException;
import com.minibank.exception.InvalidAmountException;
import com.minibank.model.Account;
import com.minibank.model.AccountType;
import com.minibank.model.Customer;
import com.minibank.model.Transaction;
import com.minibank.repository.InMemoryRepository;
import com.minibank.service.BankService;
import com.minibank.service.ReportService;
import com.minibank.sim.ConcurrencySimulation;
import com.minibank.util.CsvExporter;
import com.minibank.util.Money;

import java.nio.file.Path;
import java.util.List;

/**
 * Entry point.
 *   run.cmd          -> guided DEMO that walks through every concept
 *   run.cmd cli      -> interactive console banking
 *   run.cmd test     -> unit tests (see the test/ folder)
 *
 * CONCEPTS here: wiring objects together by hand (what Spring would do for us = "composition root"),
 * switch on a String, checked + unchecked exception handling.
 */
public class App {

    public static void main(String[] args) throws Exception {
        String mode = args.length > 0 ? args[0].toLowerCase() : "demo";

        // ---- composition root: create dependencies and inject them ----
        BankService bank = new BankService(new InMemoryRepository<>(Customer::id), new InMemoryRepository<>(Account::id));
        FraudDetector fraud = new FraudDetector(Money.of("50000"), true);
        bank.addListener(fraud);                                                 // Observer (class)
        ReportService reports = new ReportService(bank);
        Path exportDir = Path.of("out", "statements");

        switch (mode) {
            case "demo" -> runDemo(bank, reports, exportDir);
            case "cli" -> {
                seed(bank);
                new ConsoleApp(bank, reports, exportDir).run();
            }
            default -> System.out.println("Unknown mode '" + mode + "'. Use: demo | cli   (tests: run.cmd test)");
        }
    }

    // =====================================================================================
    static void runDemo(BankService bank, ReportService reports, Path exportDir) throws Exception {
        banner("1. OOP MODEL - Builder, Factory, inheritance & polymorphism");
        List<Account> accs = seed(bank);
        accs.forEach(a -> System.out.println("   " + a.describe() + "   [" + a.getClass().getSimpleName()
                + ", can withdraw " + Money.format(a.availableToWithdraw()) + "]"));

        Account asha = accs.get(0), ravi = accs.get(1), meera = accs.get(2);

        banner("2. ENCAPSULATION & EXCEPTIONS - rules live inside the objects");
        System.out.println("   deposit 2,500 -> " + bank.deposit(asha.id(), Money.of(2500)).toStatementLine());
        try {
            bank.withdraw(asha.id(), Money.of(1_00_000));                        // savings must keep Rs.1000
        } catch (InsufficientFundsException e) {                                  // CHECKED, specific
            System.out.println("   checked   -> " + e.getMessage() + " (short by " + Money.format(e.shortfall()) + ")");
        }
        try {
            bank.deposit(asha.id(), Money.of("-50"));
        } catch (InvalidAmountException e) {                                      // UNCHECKED
            System.out.println("   unchecked -> " + e.getMessage());
        }
        try {
            bank.balance("ACC-9999");
        } catch (BankException e) {                                               // catching via the BASE class
            System.out.println("   base type -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        System.out.println("   current account overdraft: withdraw 12,000 from " + ravi.id()
                + " (balance " + Money.format(ravi.balance()) + ")");
        bank.withdraw(ravi.id(), Money.of(12_000));
        System.out.println("   -> new balance " + Money.format(ravi.balance()) + "  (negative is allowed up to the limit)");

        banner("3. TRANSFERS + OBSERVER - listeners react to every transaction");
        TransactionListener notifier = (acc, tx) -> {                             // Observer (lambda)
            if (tx.type().name().startsWith("TRANSFER")) System.out.println("   [notify] " + acc.owner().name() + ": " + tx.type() + " " + Money.format(tx.amount()));
        };
        bank.addListener(notifier);
        bank.transfer(meera.id(), asha.id(), Money.of(5_000));
        bank.transfer(meera.id(), ravi.id(), Money.of(60_000));                   // big -> fraud detector fires
        bank.removeListener(notifier);                                            // unsubscribe (else 40,000 lines in step 7!)

        banner("4. INTEREST - same call, different behaviour per account type");
        accs.forEach(a -> System.out.println("   " + a.id() + " " + a.type().label() + " monthly interest = " + Money.format(a.monthlyInterest())));
        System.out.println("   credited to " + bank.applyMonthlyInterest() + " savings account(s)");

        banner("5. REPORTS - streams, groupingBy, flatMap, text blocks");
        System.out.println(reports.summary());

        banner("6. FILE I/O - export a statement as CSV with NIO.2");
        Path file = CsvExporter.exportStatement(asha, exportDir);
        System.out.println("   wrote " + file.toAbsolutePath());
        CsvExporter.read(file).stream().limit(4).forEach(l -> System.out.println("   | " + l));

        banner("7. CONCURRENCY - 20,000 random transfers in parallel, money must be conserved");
        System.out.println("   " + ConcurrencySimulation.run(bank, 20_000, false));
        System.out.println("   " + ConcurrencySimulation.run(bank, 20_000, true));
        System.out.println("   Note: virtual threads are not faster here - this work is CPU-bound and lock-heavy.\n"
                + "   They shine when tasks BLOCK (HTTP calls, DB queries): see code/08_concurrency/SynchronizersAndVirtualThreads.java");

        banner("8. STATEMENT of " + asha.id() + " (last 5) - records + java.time formatting");
        List<Transaction> history = bank.statement(asha.id());
        history.subList(Math.max(0, history.size() - 5), history.size())
                .forEach(t -> System.out.println("   " + t.toStatementLine()));

        banner("DONE");
        System.out.println("""
                   Next steps:
                     run.cmd cli    -> try it yourself (deposit, transfer, report, simulate ...)
                     run.cmd test   -> run the unit tests (custom @Test annotation + reflection runner)
                     Then build it yourself: follow the milestones in project/README.md""");
    }

    /** Creates 4 customers with 5 accounts. Returns the accounts in a fixed order. */
    static List<Account> seed(BankService bank) throws BankException {
        Customer asha = bank.registerCustomer("Asha Kulkarni", "asha@mail.com");
        Customer ravi = bank.registerCustomer("Ravi Sharma", "ravi@mail.com");
        Customer meera = bank.registerCustomer("Meera Iyer", "meera@mail.com");
        Customer john = bank.registerCustomer("John Dsouza", "john@mail.com");
        Account a1 = bank.openAccount(asha.id(), AccountType.SAVINGS, Money.of(25_000));
        Account a2 = bank.openAccount(ravi.id(), AccountType.CURRENT, Money.of(8_000));
        Account a3 = bank.openAccount(meera.id(), AccountType.SAVINGS, Money.of(1_50_000));
        Account a4 = bank.openAccount(john.id(), AccountType.SAVINGS, Money.of(40_000));
        Account a5 = bank.openAccount(asha.id(), AccountType.CURRENT, Money.of(12_000));
        return List.of(a1, a2, a3, a4, a5);
    }

    private static void banner(String title) {
        System.out.println("\n### " + title);
    }
}
