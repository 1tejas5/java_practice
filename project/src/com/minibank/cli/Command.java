package com.minibank.cli;

import com.minibank.model.AccountType;

import java.math.BigDecimal;

/**
 * CONCEPTS: SEALED interface + RECORDS = an algebraic data type.
 * The compiler knows every possible command, so the switch in ConsoleApp needs no default
 * and fails to compile if a new command is added but not handled.
 */
public sealed interface Command {

    record Register(String name, String email) implements Command { }

    record Open(String customerId, AccountType type, BigDecimal amount) implements Command { }

    record Deposit(String accountId, BigDecimal amount) implements Command { }

    record Withdraw(String accountId, BigDecimal amount) implements Command { }

    record Transfer(String from, String to, BigDecimal amount) implements Command { }

    record Balance(String accountId) implements Command { }

    record Statement(String accountId) implements Command { }

    record ListAccounts() implements Command { }

    record Report() implements Command { }

    record Interest() implements Command { }

    record Export(String accountId) implements Command { }

    record Simulate(int transfers) implements Command { }

    record Help() implements Command { }

    record Exit() implements Command { }

    record Invalid(String input, String reason) implements Command { }
}
