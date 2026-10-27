package com.minibank.cli;

import com.minibank.exception.InvalidAmountException;
import com.minibank.model.AccountType;
import com.minibank.util.Money;

/**
 * CONCEPTS: String processing (strip, split with regex, toLowerCase), switch EXPRESSION on strings,
 * multi-catch, turning bad input into a value (Command.Invalid) instead of crashing.
 */
public final class CommandParser {

    private CommandParser() { }

    public static Command parse(String line) {
        String[] p = line.strip().split("\\s+");
        String verb = p[0].toLowerCase();
        try {
            return switch (verb) {
                case "register" -> new Command.Register(p[1], p.length > 2 ? p[2] : "");
                case "open" -> new Command.Open(p[1].toUpperCase(), AccountType.parse(p[2]), Money.of(p[3]));
                case "deposit" -> new Command.Deposit(p[1].toUpperCase(), Money.of(p[2]));
                case "withdraw" -> new Command.Withdraw(p[1].toUpperCase(), Money.of(p[2]));
                case "transfer" -> new Command.Transfer(p[1].toUpperCase(), p[2].toUpperCase(), Money.of(p[3]));
                case "balance" -> new Command.Balance(p[1].toUpperCase());
                case "statement" -> new Command.Statement(p[1].toUpperCase());
                case "accounts", "list" -> new Command.ListAccounts();
                case "report" -> new Command.Report();
                case "interest" -> new Command.Interest();
                case "export" -> new Command.Export(p[1].toUpperCase());
                case "simulate" -> new Command.Simulate(p.length > 1 ? Integer.parseInt(p[1]) : 10_000);
                case "help", "?" -> new Command.Help();
                case "exit", "quit" -> new Command.Exit();
                default -> new Command.Invalid(line, "unknown command '" + verb + "' (type help)");
            };
        } catch (ArrayIndexOutOfBoundsException e) {
            return new Command.Invalid(line, "missing arguments (type help)");
        } catch (InvalidAmountException | IllegalArgumentException e) {   // multi-catch
            return new Command.Invalid(line, e.getMessage());
        }
    }

    public static String help() {
        return """
                Commands:
                  register <name> <email>                  create a customer        e.g. register Asha asha@mail.com
                  open <customerId> <savings|current> <amt> open an account          e.g. open CUST-1001 savings 5000
                  deposit <acc> <amt>                       e.g. deposit ACC-1001 2500
                  withdraw <acc> <amt>                      e.g. withdraw ACC-1001 500
                  transfer <from> <to> <amt>                e.g. transfer ACC-1001 ACC-1002 750
                  balance <acc> | statement <acc> | export <acc>
                  accounts | report | interest | simulate [n] | help | exit""";
    }
}
