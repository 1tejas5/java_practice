package com.minibank;

import com.minibank.cli.Command;
import com.minibank.cli.CommandParser;
import com.minibank.model.AccountType;
import com.minibank.model.Customer;
import com.minibank.model.SavingsAccount;
import com.minibank.model.Transaction;
import com.minibank.model.TransactionType;
import com.minibank.util.IdGenerator;
import com.minibank.util.Money;
import com.minibank.testing.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.minibank.testing.Assert.assertEquals;
import static com.minibank.testing.Assert.assertThrows;
import static com.minibank.testing.Assert.assertTrue;

/** Tests for the model, utilities and the command parser (records, sealed types, patterns). */
public class ModelAndParserTest {

    @Test("Customer builder validates its input")
    void customerBuilder() {
        Customer c = Customer.builder("C-1", "Asha").email("asha@mail.com").build();
        assertEquals("Asha (C-1)", c.toString());
        assertThrows(IllegalStateException.class, () -> Customer.builder("C-2", "Bad").email("no-at-sign").build());
        assertThrows(IllegalStateException.class, () -> Customer.builder("C-3", " ").build());
    }

    @Test("customers are equal by id")
    void customerEquality() {
        Customer a = Customer.builder("C-9", "Asha").build();
        Customer b = Customer.builder("C-9", "Asha K").build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test("Money parses with scale 2 and formats nicely")
    void money() {
        assertEquals(new BigDecimal("10.50"), Money.of("10.5"));
        assertEquals(new BigDecimal("1000.00"), Money.of(1000));
        assertTrue(Money.format(Money.of("1234.5")).startsWith("Rs. "), "currency prefix");
    }

    @Test("IdGenerator produces unique, prefixed ids")
    void ids() {
        String a = IdGenerator.INSTANCE.next("T"), b = IdGenerator.INSTANCE.next("T");
        assertTrue(a.startsWith("T-") && !a.equals(b), a + " vs " + b);
    }

    @Test("Transaction record validates and normalizes")
    void transactionRecord() {
        Transaction t = new Transaction("TX-1", "ACC-1", TransactionType.DEPOSIT, Money.of(5), Money.of(5), LocalDateTime.now(), null);
        assertEquals("", t.note());
        assertThrows(NullPointerException.class, () -> new Transaction(null, "A", TransactionType.DEPOSIT, Money.ZERO, Money.ZERO, LocalDateTime.now(), ""));
        assertTrue(t.toStatementLine().contains("DEPOSIT"), "statement line has the type");
    }

    @Test("savings interest is 4% a year, paid monthly")
    void savingsInterest() {
        SavingsAccount acc = new SavingsAccount("S-1", Customer.builder("C-1", "Asha").build());
        acc.deposit(Money.of(12_000), TransactionType.DEPOSIT, "");
        assertEquals(Money.of("40"), acc.monthlyInterest());
    }

    @Test("parser turns text into sealed Command records")
    void parser() {
        assertEquals(new Command.Transfer("ACC-1", "ACC-2", Money.of(750)), CommandParser.parse("transfer acc-1 ACC-2 750"));
        assertEquals(new Command.Open("CUST-1", AccountType.SAVINGS, Money.of(5000)), CommandParser.parse("open CUST-1 savings 5000"));
        assertEquals(new Command.Simulate(10_000), CommandParser.parse("simulate"));
        assertTrue(CommandParser.parse("deposit ACC-1") instanceof Command.Invalid, "missing amount -> Invalid");
        assertTrue(CommandParser.parse("deposit ACC-1 abc") instanceof Command.Invalid, "bad amount -> Invalid");
        assertTrue(CommandParser.parse("open CUST-1 gold 10") instanceof Command.Invalid, "bad type -> Invalid");
        assertTrue(CommandParser.parse("dance") instanceof Command.Invalid, "unknown verb -> Invalid");
    }

    @Test("pattern matching over the sealed Command type")
    void patternMatching() {
        Command cmd = CommandParser.parse("withdraw ACC-7 300");
        String described = switch (cmd) {
            case Command.Withdraw(String acc, BigDecimal amt) when amt.compareTo(Money.of(1000)) < 0 -> "small withdrawal from " + acc;
            case Command.Withdraw w -> "large withdrawal";
            default -> "something else";
        };
        assertEquals("small withdrawal from ACC-7", described);
    }
}
