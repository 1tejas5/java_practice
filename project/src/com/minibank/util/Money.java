package com.minibank.util;

import com.minibank.exception.InvalidAmountException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * CONCEPTS: utility class (final + private constructor + static methods), BigDecimal done right
 * (String constructor, fixed scale, compareTo for comparisons), static factory methods.
 */
public final class Money {

    public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY);

    private Money() { }                              // no instances

    /** Always build money from a String: new BigDecimal(0.1) would be imprecise. */
    public static BigDecimal of(String amount) {
        try {
            return new BigDecimal(amount.strip()).setScale(2, RoundingMode.HALF_EVEN);
        } catch (NumberFormatException e) {
            throw new InvalidAmountException("Not a valid amount: '" + amount + "'");
        }
    }

    public static BigDecimal of(long rupees) {
        return BigDecimal.valueOf(rupees).setScale(2, RoundingMode.UNNECESSARY);
    }

    public static void requirePositive(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Amount must be positive, got " + amount);
        }
    }

    /** "Rs. 12,345.60" (works for negative balances too). */
    public static String format(BigDecimal amount) {
        return String.format("Rs. %,.2f", amount);
    }
}
