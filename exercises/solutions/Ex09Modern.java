import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
 * SOLUTIONS - Exercises section 9: Modern Java (records, sealed, pattern matching)
 * RUN: java exercises/solutions/Ex09Modern.java
 */
public class Ex09Modern {

    public static void main(String[] args) {
        // 9.1
        Temperature boiling = new Temperature(100);
        check("9.1 fahrenheit", 212.0, boiling.fahrenheit());
        check("9.1 ofFahrenheit", 37.0, Math.round(Temperature.ofFahrenheit(98.6).celsius() * 10) / 10.0);
        check("9.1 below absolute zero", "IllegalArgumentException", attempt(() -> new Temperature(-300)));
        check("9.1 value equality", true, new Temperature(20).equals(new Temperature(20)));

        // 9.2
        check("9.2 circle", "3.14", String.format("%.2f", area(new Circle(1))));
        check("9.2 square", 4.0, area(new Square(2)));
        check("9.2 rect", 6.0, area(new Rect(2, 3)));

        // 9.3  -(2 + 3) * 4
        Expr e = new Mul(new Neg(new Add(new Num(2), new Num(3))), new Num(4));
        check("9.3 eval", -20, eval(e));
        check("9.3 show", "-(2 + 3) * 4", show(e));

        // 9.4
        check("9.4 null", "null", describe(null));
        check("9.4 negative", "negative int -5", describe(-5));
        check("9.4 positive", "positive int 7", describe(7));
        check("9.4 blank", "blank string", describe("   "));
        check("9.4 text", "text 'hi'", describe("hi"));
        check("9.4 list", "list of 2", describe(List.of(1, 2)));
        check("9.4 other", "other Double", describe(2.5));

        // 9.5
        List<String> script = List.of("deposit 500", "withdraw 200", "balance", "withdraw 1000", "fly away");
        check("9.5 command parser", List.of("deposited 500 -> 500", "withdrew 200 -> 300", "balance is 300",
                "insufficient funds (balance 300)", "unknown command: fly away"), runScript(script));
        summary();
    }

    // ============ 9.1 ============
    record Temperature(double celsius) {
        static final double ABSOLUTE_ZERO = -273.15;

        Temperature {
            if (celsius < ABSOLUTE_ZERO) throw new IllegalArgumentException(celsius + " is below absolute zero");
        }

        double fahrenheit() {
            return celsius * 9 / 5 + 32;
        }

        static Temperature ofFahrenheit(double f) {
            return new Temperature((f - 32) * 5 / 9);
        }
    }

    // ============ 9.2 ============
    sealed interface Shape permits Circle, Square, Rect { }

    record Circle(double r) implements Shape { }

    record Square(double side) implements Shape { }

    record Rect(double w, double h) implements Shape { }

    static double area(Shape s) {
        return switch (s) {                          // exhaustive: no default
            case Circle c -> Math.PI * c.r() * c.r();
            case Square sq -> sq.side() * sq.side();
            case Rect(double w, double h) -> w * h;  // record pattern
        };
    }

    // ============ 9.3 ============
    sealed interface Expr permits Num, Add, Mul, Neg { }

    record Num(int value) implements Expr { }

    record Add(Expr left, Expr right) implements Expr { }

    record Mul(Expr left, Expr right) implements Expr { }

    record Neg(Expr inner) implements Expr { }

    static int eval(Expr e) {
        return switch (e) {
            case Num(int v) -> v;
            case Add(Expr l, Expr r) -> eval(l) + eval(r);
            case Mul(Expr l, Expr r) -> eval(l) * eval(r);
            case Neg(Expr inner) -> -eval(inner);
        };
    }

    static String show(Expr e) {
        return switch (e) {
            case Num(int v) -> String.valueOf(v);
            case Add(Expr l, Expr r) -> show(l) + " + " + show(r);
            case Mul(Expr l, Expr r) -> wrap(l) + " * " + wrap(r);
            case Neg(Expr inner) -> "-" + wrap(inner);
        };
    }

    private static String wrap(Expr e) {             // parenthesize additions inside * and -
        return e instanceof Add ? "(" + show(e) + ")" : show(e);
    }

    // ============ 9.4 ============
    static String describe(Object o) {
        return switch (o) {
            case null -> "null";
            case Integer i when i < 0 -> "negative int " + i;
            case Integer i -> "positive int " + i;
            case String s when s.isBlank() -> "blank string";
            case String s -> "text '" + s + "'";
            case List<?> l -> "list of " + l.size();
            default -> "other " + o.getClass().getSimpleName();
        };
    }

    // ============ 9.5 ============
    sealed interface Command permits Deposit, Withdraw, Balance, Unknown { }

    record Deposit(long amount) implements Command { }

    record Withdraw(long amount) implements Command { }

    record Balance() implements Command { }

    record Unknown(String text) implements Command { }

    static Command parse(String line) {
        String[] p = line.strip().split("\\s+");
        try {
            return switch (p[0].toLowerCase()) {
                case "deposit" -> new Deposit(Long.parseLong(p[1]));
                case "withdraw" -> new Withdraw(Long.parseLong(p[1]));
                case "balance" -> new Balance();
                default -> new Unknown(line);
            };
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            return new Unknown(line);
        }
    }

    static List<String> runScript(List<String> lines) {
        long balance = 0;
        List<String> out = new ArrayList<>();
        for (String line : lines) {
            Command cmd = parse(line);
            final long current = balance;   // a `when` guard may only read (effectively) final locals,
                                            // and `balance` changes, so the guard uses this snapshot
            switch (cmd) {
                case Deposit(long amt) -> {
                    balance += amt;
                    out.add("deposited " + amt + " -> " + balance);
                }
                case Withdraw(long amt) when amt > current -> out.add("insufficient funds (balance " + current + ")");
                case Withdraw(long amt) -> {
                    balance -= amt;
                    out.add("withdrew " + amt + " -> " + balance);
                }
                case Balance b -> out.add("balance is " + balance);
                case Unknown(String text) -> out.add("unknown command: " + text);
            }
        }
        return out;
    }

    // ---------------- test helpers ----------------
    interface Risky { Object run() throws Exception; }

    static String attempt(Risky r) {
        try {
            return String.valueOf(r.run());
        } catch (Exception e) {
            return e.getClass().getSimpleName();
        }
    }

    private static int passed, failed;

    static void check(String name, Object expected, Object actual) {
        boolean ok = Objects.deepEquals(expected, actual);
        if (ok) passed++;
        else failed++;
        System.out.printf("%s %-28s %s%n", ok ? "PASS" : "FAIL", name, ok ? "" : "expected=" + expected + " actual=" + actual);
    }

    static void summary() {
        System.out.printf("%n%d passed, %d failed%n", passed, failed);
    }
}
