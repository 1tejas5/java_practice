import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/*
 * PRACTICE TEMPLATE - copy this file, rename the class (and the file), and write your solutions.
 * RUN: java exercises/practice/Practice.java
 *
 * The check(...) helper prints PASS/FAIL so you get instant feedback, like a tiny unit test.
 */
public class Practice {

    public static void main(String[] args) {
        // Example: exercise 1.2 - reverse an integer
        check("1.2 reverse 1234", 4321, reverse(1234));
        check("1.2 reverse -560", -65, reverse(-560));

        // TODO: add your own checks here
        // check("1.3 isPrime(29)", true, isPrime(29));

        summary();
    }

    // ===================== YOUR SOLUTIONS =====================

    static int reverse(int n) {
        int sign = n < 0 ? -1 : 1;
        n = Math.abs(n);
        int rev = 0;
        while (n > 0) {
            rev = rev * 10 + n % 10;
            n /= 10;
        }
        return sign * rev;
    }

    // ===================== TEST HELPER (don't change) =====================
    private static int passed, failed;

    static void check(String name, Object expected, Object actual) {
        boolean ok = Objects.deepEquals(expected, actual);
        if (ok) passed++;
        else failed++;
        System.out.printf("%s %-35s expected=%s actual=%s%n", ok ? "PASS" : "FAIL", name,
                str(expected), str(actual));
    }

    static void summary() {
        System.out.printf("%n%d passed, %d failed%n", passed, failed);
    }

    private static String str(Object o) {
        if (o instanceof int[] a) return Arrays.toString(a);
        if (o instanceof Object[] a) return Arrays.deepToString(a);
        return String.valueOf(o);
    }
}
