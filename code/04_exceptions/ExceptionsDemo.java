import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/*
 * TOPIC: try/catch/finally, multi-catch, finally traps, try-with-resources (order + suppressed),
 *        throw vs throws, propagation, custom checked/unchecked exceptions, chaining
 * RUN:   java code/04_exceptions/ExceptionsDemo.java
 */
public class ExceptionsDemo {

    public static void main(String[] args) {
        section("1. try / catch / finally - specific catch first");
        try {
            int[] a = new int[2];
            a[5] = 10;
            System.out.println("never printed");
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("caught specific: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("caught general");
        } finally {
            System.out.println("finally always runs");
        }

        section("2. Multi-catch");
        for (String input : new String[]{"10", "abc", "0"}) {
            try {
                System.out.println("100 / " + input + " = " + (100 / Integer.parseInt(input)));
            } catch (NumberFormatException | ArithmeticException e) {
                System.out.println("bad input '" + input + "' -> " + e.getClass().getSimpleName());
            }
        }

        section("3. finally traps");
        System.out.println("returnInFinally()       = " + returnInFinally() + "  (finally's return wins)");
        System.out.println("modifyInFinally()       = " + modifyInFinally() + "  (return value already saved)");
        System.out.println("finallySwallowsException= " + finallySwallowsException() + "  (exception vanished!)");

        section("4. try-with-resources: reverse close order + suppressed exceptions");
        try (Resource first = new Resource("first", false);
             Resource second = new Resource("second", false)) {
            System.out.println("  using both resources");
        }
        try (Resource r = new Resource("faulty", true)) {
            throw new IllegalStateException("failure in try body");
        } catch (IllegalStateException e) {
            System.out.println("  main exception : " + e.getMessage());
            for (Throwable s : e.getSuppressed()) {
                System.out.println("  suppressed     : " + s.getMessage());
            }
        }

        section("5. Checked exception from real I/O");
        try {
            String content = Files.readString(Path.of("does-not-exist.txt"));
            System.out.println(content);
        } catch (IOException e) {                      // compiler FORCES us to handle this
            System.out.println("IOException (" + e.getClass().getSimpleName() + "): " + e.getMessage());
        }

        section("6. Propagation up the call stack");
        try {
            level1();
        } catch (IllegalArgumentException e) {
            System.out.println("caught in main: " + e.getMessage());
            StackTraceElement[] trace = e.getStackTrace();
            for (int i = 0; i < 3; i++) System.out.println("   at " + trace[i].getMethodName());
        }

        section("7. Custom checked + unchecked exceptions");
        Wallet wallet = new Wallet(500);
        try {
            wallet.pay(200);
            wallet.pay(400);
        } catch (InsufficientFundsException e) {
            System.out.println("business rule: " + e.getMessage() + " (short by " + e.getShortBy() + ")");
        }
        try {
            wallet.pay(-5);
        } catch (InvalidAmountException e) {
            System.out.println("validation: " + e.getMessage());
        } catch (InsufficientFundsException e) {
            System.out.println("not reached");
        }

        section("8. Chaining: wrap a low-level exception, keep the cause");
        try {
            new UserRepository().find(42);
        } catch (DataAccessException e) {
            System.out.println("high-level: " + e.getMessage());
            System.out.println("root cause: " + e.getCause().getClass().getSimpleName() + " - " + e.getCause().getMessage());
        }

        section("9. Helpful NullPointerException messages (Java 14+)");
        try {
            User u = new User(null);
            System.out.println(u.name.length());
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }

        section("10. ConcurrentModificationException and UnsupportedOperationException");
        List<Integer> nums = new ArrayList<>(List.of(1, 2, 3, 4));
        try {
            for (Integer n : nums) if (n % 2 == 0) nums.remove(n);
        } catch (java.util.ConcurrentModificationException e) {
            System.out.println("CME: modified the list while iterating");
        }
        nums.removeIf(n -> n % 2 == 0);
        System.out.println("removeIf is safe -> " + nums);
        try {
            List.of(1, 2).add(3);
        } catch (UnsupportedOperationException e) {
            System.out.println("List.of(...).add -> UnsupportedOperationException");
        }
    }

    @SuppressWarnings("finally")
    static int returnInFinally() {
        try {
            return 1;
        } finally {
            return 2;
        }
    }

    static int modifyInFinally() {
        int x = 1;
        try {
            return x;
        } finally {
            x = 99;
        }
    }

    @SuppressWarnings("finally")
    static String finallySwallowsException() {
        try {
            throw new RuntimeException("important error");
        } finally {
            return "normal value";
        }
    }

    static void level1() { level2(); }

    static void level2() { level3(); }

    static void level3() { throw new IllegalArgumentException("thrown deep inside level3"); }

    static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}

class Resource implements AutoCloseable {
    private final String name;
    private final boolean failOnClose;

    Resource(String name, boolean failOnClose) {
        this.name = name;
        this.failOnClose = failOnClose;
        System.out.println("  open  " + name);
    }

    @Override
    public void close() {
        System.out.println("  close " + name);
        if (failOnClose) throw new IllegalStateException("failure in close() of " + name);
    }
}

class InsufficientFundsException extends Exception {          // CHECKED
    private final double shortBy;

    InsufficientFundsException(String message, double shortBy) {
        super(message);
        this.shortBy = shortBy;
    }

    double getShortBy() {
        return shortBy;
    }
}

class InvalidAmountException extends RuntimeException {      // UNCHECKED
    InvalidAmountException(String message) {
        super(message);
    }
}

class Wallet {
    private double balance;

    Wallet(double balance) {
        this.balance = balance;
    }

    void pay(double amount) throws InsufficientFundsException {
        if (amount <= 0) throw new InvalidAmountException("amount must be positive, got " + amount);
        if (amount > balance) {
            throw new InsufficientFundsException("cannot pay " + amount + ", balance " + balance, amount - balance);
        }
        balance -= amount;
        System.out.println("paid " + amount + ", left " + balance);
    }
}

class DataAccessException extends RuntimeException {
    DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}

class UserRepository {
    void find(int id) {
        try {
            throw new java.sql.SQLException("connection refused: db-host:5432");   // simulated driver error
        } catch (java.sql.SQLException e) {
            throw new DataAccessException("could not load user " + id, e);
        }
    }
}

class User {
    String name;

    User(String name) {
        this.name = name;
    }
}
