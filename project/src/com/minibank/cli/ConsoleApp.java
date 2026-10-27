package com.minibank.cli;

import com.minibank.exception.BankException;
import com.minibank.model.Account;
import com.minibank.model.Customer;
import com.minibank.model.Transaction;
import com.minibank.service.BankService;
import com.minibank.service.ReportService;
import com.minibank.sim.ConcurrencySimulation;
import com.minibank.util.CsvExporter;
import com.minibank.util.Money;

import java.nio.file.Path;
import java.util.Scanner;

/**
 * CONCEPTS: Scanner input loop, PATTERN MATCHING switch over a sealed type with RECORD PATTERNS,
 * one catch block for the whole checked-exception hierarchy (BankException).
 */
public class ConsoleApp {

    private final BankService bank;
    private final ReportService reports;
    private final Path exportDir;

    public ConsoleApp(BankService bank, ReportService reports, Path exportDir) {
        this.bank = bank;
        this.reports = reports;
        this.exportDir = exportDir;
    }

    public void run() {
        System.out.println("\nWelcome to MiniBank interactive mode.\n" + CommandParser.help());
        System.out.println("\nExisting accounts:");
        bank.allAccounts().forEach(a -> System.out.println("  " + a.describe()));
        try (Scanner in = new Scanner(System.in)) {
            while (true) {
                System.out.print("\nminibank> ");
                if (!in.hasNextLine()) return;
                String line = in.nextLine();
                if (line.isBlank()) continue;
                Command cmd = CommandParser.parse(line);
                if (cmd instanceof Command.Exit) {
                    System.out.println("Bye!");
                    return;
                }
                execute(cmd);
            }
        }
    }

    /** Public so it can be unit-tested or scripted without a console. */
    public void execute(Command cmd) {
        try {
            switch (cmd) {
                case Command.Register(String name, String email) -> {
                    Customer c = bank.registerCustomer(name, email);
                    System.out.println("Registered " + c);
                }
                case Command.Open(String customerId, var type, var amount) -> {
                    Account a = bank.openAccount(customerId, type, amount);
                    System.out.println("Opened " + a.describe());
                }
                case Command.Deposit(String acc, var amount) -> print(bank.deposit(acc, amount));
                case Command.Withdraw(String acc, var amount) -> print(bank.withdraw(acc, amount));
                case Command.Transfer(String from, String to, var amount) -> {
                    bank.transfer(from, to, amount);
                    System.out.println("Transferred " + Money.format(amount) + " from " + from + " to " + to);
                }
                case Command.Balance(String acc) -> System.out.println(acc + " balance: " + Money.format(bank.balance(acc)));
                case Command.Statement(String acc) -> bank.statement(acc).forEach(t -> System.out.println("  " + t.toStatementLine()));
                case Command.ListAccounts() -> bank.allAccounts().forEach(a -> System.out.println("  " + a.describe()));
                case Command.Report() -> System.out.println(reports.summary());
                case Command.Interest() -> System.out.println("Interest credited to " + bank.applyMonthlyInterest() + " account(s)");
                case Command.Export(String acc) -> System.out.println("Exported to " + CsvExporter.exportStatement(bank.account(acc), exportDir).toAbsolutePath());
                case Command.Simulate(int n) -> System.out.println(ConcurrencySimulation.run(bank, n, true));
                case Command.Help() -> System.out.println(CommandParser.help());
                case Command.Exit() -> System.out.println("(exit is handled by the input loop)");
                case Command.Invalid(String input, String reason) -> System.out.println("Invalid: " + reason);
            }
        } catch (BankException e) {                          // InsufficientFunds, AccountNotFound, CustomerNotFound...
            System.out.println("Rejected: " + e.getMessage());
        } catch (RuntimeException e) {                       // e.g. InvalidAmountException
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void print(Transaction t) {
        System.out.println("OK " + t.toStatementLine());
    }
}
