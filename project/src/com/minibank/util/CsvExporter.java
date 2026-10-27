package com.minibank.util;

import com.minibank.model.Account;
import com.minibank.model.Transaction;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * CONCEPTS: NIO.2 file I/O (Path, Files.createDirectories, Files.write, Files.readAllLines),
 * exception translation (checked IOException -> UncheckedIOException with the cause kept).
 */
public final class CsvExporter {

    private static final String HEADER = "id,timestamp,type,amount,balanceAfter,note";

    private CsvExporter() { }

    /** Writes the account's statement to dir/<accountId>.csv and returns the file path. */
    public static Path exportStatement(Account account, Path dir) {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (Transaction t : account.history()) {
            lines.add(String.join(",", t.id(), t.timestamp().toString(), t.type().name(),
                    t.amount().toPlainString(), t.balanceAfter().toPlainString(), escape(t.note())));
        }
        try {
            Files.createDirectories(dir);
            Path file = dir.resolve(account.id() + ".csv");
            Files.write(file, lines);                                  // UTF-8, creates or overwrites
            return file;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not export statement of " + account.id(), e);
        }
    }

    public static List<String> read(Path file) {
        try {
            return Files.readAllLines(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String escape(String s) {
        return s.contains(",") || s.contains("\"") ? "\"" + s.replace("\"", "\"\"") + "\"" : s;
    }
}
