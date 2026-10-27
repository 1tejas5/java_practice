import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/*
 * SOLUTIONS - Exercises section 1: Basics
 * RUN: java exercises/solutions/Ex01Basics.java
 */
public class Ex01Basics {

    public static void main(String[] args) {
        check("1.1 fizzBuzz(15)", List.of("1", "2", "Fizz", "4", "Buzz", "Fizz", "7", "8", "Fizz", "Buzz", "11", "Fizz", "13", "14", "FizzBuzz"), fizzBuzz(15));
        check("1.2 reverse(-1234)", -4321, reverse(-1234));
        check("1.2 reverse(1200)", 21, reverse(1200));
        check("1.3 isPrime(29)", true, isPrime(29));
        check("1.3 isPrime(1)", false, isPrime(1));
        check("1.3 isPrime(91)", false, isPrime(91));
        check("1.4 fibonacci(7)", List.of(0L, 1L, 1L, 2L, 3L, 5L, 8L), fibonacci(7));
        check("1.5 digitSum(9875)", 29, digitSum(9875));
        check("1.6 isArmstrong(153)", true, isArmstrong(153));
        check("1.6 isArmstrong(9474)", true, isArmstrong(9474));
        check("1.6 isArmstrong(123)", false, isArmstrong(123));
        check("1.7 secondLargest", 6, secondLargest(new int[]{4, 8, 1, 8, 6}));
        int[] arr = {1, 2, 3, 4, 5};
        rotateRight(arr, 2);
        check("1.8 rotateRight k=2", new int[]{4, 5, 1, 2, 3}, arr);
        check("1.9 binarySearch(7)", 3, binarySearch(new int[]{1, 3, 5, 7, 9}, 7));
        check("1.9 binarySearch(4)", -1, binarySearch(new int[]{1, 3, 5, 7, 9}, 4));
        int[] z = {0, 1, 0, 3, 12};
        moveZeros(z);
        check("1.10 moveZeros", new int[]{1, 3, 12, 0, 0}, z);
        check("1.11 multiply", new int[][]{{19, 22}, {43, 50}}, multiply(new int[][]{{1, 2}, {3, 4}}, new int[][]{{5, 6}, {7, 8}}));
        check("1.12 pyramid(3)", "  1\n 1 2\n1 2 3", pyramid(3));
        System.out.println("\n" + pyramid(5));
        summary();
    }

    // 1.1
    static List<String> fizzBuzz(int n) {
        List<String> out = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            if (i % 15 == 0) out.add("FizzBuzz");
            else if (i % 3 == 0) out.add("Fizz");
            else if (i % 5 == 0) out.add("Buzz");
            else out.add(String.valueOf(i));
        }
        return out;
    }

    // 1.2
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

    // 1.3 - only test up to sqrt(n), and skip even numbers
    static boolean isPrime(int n) {
        if (n < 2) return false;
        if (n % 2 == 0) return n == 2;
        for (int i = 3; (long) i * i <= n; i += 2) {
            if (n % i == 0) return false;
        }
        return true;
    }

    // 1.4
    static List<Long> fibonacci(int count) {
        List<Long> out = new ArrayList<>();
        long a = 0, b = 1;
        for (int i = 0; i < count; i++) {
            out.add(a);
            long next = a + b;
            a = b;
            b = next;
        }
        return out;
    }

    // 1.5
    static int digitSum(int n) {
        int sum = 0;
        for (n = Math.abs(n); n > 0; n /= 10) sum += n % 10;
        return sum;
    }

    // 1.6
    static boolean isArmstrong(int n) {
        int digits = String.valueOf(n).length();
        int sum = 0;
        for (int t = n; t > 0; t /= 10) sum += (int) Math.pow(t % 10, digits);
        return sum == n;
    }

    // 1.7 - single pass, O(n)
    static int secondLargest(int[] a) {
        int first = Integer.MIN_VALUE, second = Integer.MIN_VALUE;
        for (int v : a) {
            if (v > first) {
                second = first;
                first = v;
            } else if (v > second && v != first) {
                second = v;
            }
        }
        return second;
    }

    // 1.8 - reversal algorithm: reverse all, reverse first k, reverse the rest
    static void rotateRight(int[] a, int k) {
        k %= a.length;
        reverse(a, 0, a.length - 1);
        reverse(a, 0, k - 1);
        reverse(a, k, a.length - 1);
    }

    private static void reverse(int[] a, int l, int r) {
        while (l < r) {
            int t = a[l];
            a[l++] = a[r];
            a[r--] = t;
        }
    }

    // 1.9
    static int binarySearch(int[] sorted, int key) {
        int lo = 0, hi = sorted.length - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;          // avoids (lo + hi) overflow
            if (sorted[mid] == key) return mid;
            if (sorted[mid] < key) lo = mid + 1;
            else hi = mid - 1;
        }
        return -1;
    }

    // 1.10
    static void moveZeros(int[] a) {
        int insert = 0;
        for (int v : a) if (v != 0) a[insert++] = v;
        while (insert < a.length) a[insert++] = 0;
    }

    // 1.11
    static int[][] multiply(int[][] a, int[][] b) {
        int n = a.length, m = b[0].length, common = b.length;
        int[][] c = new int[n][m];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++)
                for (int k = 0; k < common; k++)
                    c[i][j] += a[i][k] * b[k][j];
        return c;
    }

    // 1.12
    static String pyramid(int rows) {
        StringBuilder sb = new StringBuilder();
        for (int r = 1; r <= rows; r++) {
            sb.append(" ".repeat(rows - r));
            for (int c = 1; c <= r; c++) {
                sb.append(c);
                if (c < r) sb.append(' ');
            }
            if (r < rows) sb.append('\n');
        }
        return sb.toString();
    }

    // ---------------- test helper ----------------
    private static int passed, failed;

    static void check(String name, Object expected, Object actual) {
        boolean ok = Objects.deepEquals(expected, actual);
        if (ok) passed++;
        else failed++;
        System.out.printf("%s %-28s %s%n", ok ? "PASS" : "FAIL", name, ok ? "" : "expected=" + str(expected) + " actual=" + str(actual));
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
