package com.minibank.testing;

import java.math.BigDecimal;
import java.util.Objects;

/** CONCEPTS: static utility methods, generics with bounded wildcards, a functional interface. */
public final class Assert {

    private Assert() { }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    public static void assertEquals(Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError("expected <" + expected + "> but was <" + actual + ">");
        }
    }

    /** BigDecimal-aware: 10.0 and 10.00 are equal here (compareTo, not equals). */
    public static void assertMoney(String expected, BigDecimal actual) {
        if (new BigDecimal(expected).compareTo(actual) != 0) {
            throw new AssertionError("expected amount <" + expected + "> but was <" + actual + ">");
        }
    }

    public static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static <T extends Throwable> T assertThrows(Class<T> expectedType, ThrowingRunnable code) {
        try {
            code.run();
        } catch (Throwable t) {
            if (expectedType.isInstance(t)) return expectedType.cast(t);
            throw new AssertionError("expected " + expectedType.getSimpleName() + " but got " + t.getClass().getSimpleName() + ": " + t.getMessage());
        }
        throw new AssertionError("expected " + expectedType.getSimpleName() + " but nothing was thrown");
    }
}
