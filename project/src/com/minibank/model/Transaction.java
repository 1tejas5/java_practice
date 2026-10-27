package com.minibank.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * CONCEPTS: record (immutable data carrier), compact constructor validation,
 * java.time, instance methods on a record.
 */
public record Transaction(String id,
                          String accountId,
                          TransactionType type,
                          BigDecimal amount,
                          BigDecimal balanceAfter,
                          LocalDateTime timestamp,
                          String note) {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");

    public Transaction {                                   // compact constructor: validate, fields assigned after
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(accountId, "accountId");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(balanceAfter, "balanceAfter");
        Objects.requireNonNull(timestamp, "timestamp");
        note = note == null ? "" : note;                   // normalize instead of storing null
    }

    public String formattedTime() {
        return timestamp.format(FORMAT);
    }

    /** One line for statements: "TX-1 | 27-Oct-2026 10:15:00 | DEPOSIT | +5,000.00 | 15,000.00 | salary". */
    public String toStatementLine() {
        return String.format("%-8s | %s | %-12s | %s%,12.2f | %,12.2f | %s",
                id, formattedTime(), type, type.sign(), amount, balanceAfter, note);
    }
}
