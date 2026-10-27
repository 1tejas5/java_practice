import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
 * TOPIC: Modern Java (10 -> 21): var, text blocks, switch expressions, records, sealed types,
 *        pattern matching (instanceof, switch, guards, null, record patterns), sequenced collections,
 *        newer String/Stream/Math APIs.
 * RUN:   java code/11_modern/ModernJavaDemo.java      (needs Java 21+)
 */
public class ModernJavaDemo {

    public static void main(String[] args) {
        section("1. var (Java 10)");
        var names = new ArrayList<String>();
        names.add("Asha");
        var scores = new HashMap<String, List<Integer>>();
        scores.put("Asha", List.of(90, 85));
        for (var e : scores.entrySet()) System.out.println(e.getKey() + " -> " + e.getValue());
        var inferred = new ArrayList<>();          // careful: ArrayList<Object>
        inferred.add(1);
        inferred.add("mixed");
        System.out.println("var with <> infers Object: " + inferred);

        section("2. Text blocks (Java 15)");
        String json = """
                {
                  "name": "Asha",
                  "skills": ["Java", "SQL"]
                }
                """;
        System.out.print(json);
        String sql = """
                SELECT id, name FROM employees \
                WHERE dept = '%s'""".formatted("IT");
        System.out.println(sql + "   <- '\\' joined the lines");

        section("3. Switch expressions (Java 14)");
        for (String day : List.of("SAT", "MON", "WED")) {
            int hours = switch (day) {
                case "SAT", "SUN" -> 0;
                case "MON" -> {
                    System.out.print("(Monday!) ");
                    yield 10;
                }
                default -> 8;
            };
            System.out.println(day + " -> work hours " + hours);
        }

        section("4. Records (Java 16)");
        Point p = new Point(3, 4);
        System.out.println(p + " x()=" + p.x() + " distance=" + p.distanceFromOrigin());
        System.out.println("equals by value: " + p.equals(new Point(3, 4)) + ", hashCode equal: "
                + (p.hashCode() == new Point(3, 4).hashCode()));
        System.out.println("Range.of(9, 2) = " + Range.of(9, 2) + ", length " + Range.of(9, 2).length());
        try {
            new Range(5, 1);
        } catch (IllegalArgumentException e) {
            System.out.println("compact constructor rejected: " + e.getMessage());
        }
        List<String> items = new ArrayList<>(List.of("pen"));
        Order order = new Order("O-1", items);
        items.add("hacked");
        System.out.println(order + "  <- defensive copy keeps it immutable");
        record MinMax(int min, int max) { }                // LOCAL record: a named multi-value return
        int[] data = {7, 2, 9, 4};
        MinMax mm = new MinMax(Arrays.stream(data).min().orElseThrow(), Arrays.stream(data).max().orElseThrow());
        System.out.println("local record " + mm);
        Map<Point, String> labels = Map.of(new Point(0, 0), "origin");
        System.out.println("records as map keys: " + labels.get(new Point(0, 0)));

        section("5. Sealed interface + records + EXHAUSTIVE switch (no default)");
        List<Payment> payments = List.of(new Card("4111-xxxx", 2000), new Upi("asha@okbank", 500), new Cash(15_000));
        for (Payment pay : payments) {
            System.out.printf("%-38s fee = %.2f%n", pay, fee(pay));
        }

        section("6. Pattern matching for instanceof (Java 16)");
        Object[] things = {"hello", 42, 3.5, List.of(1, 2), null};
        for (Object o : things) {
            if (o instanceof String s && s.length() > 3) {
                System.out.println("long string: " + s.toUpperCase());
            } else if (o instanceof Integer n) {
                System.out.println("integer + 1 = " + (n + 1));
            } else {
                System.out.println("other: " + o);
            }
        }

        section("7. Switch patterns with guards and null (Java 21)");
        Object[] inputs = {null, 7, 250, "Java", new int[]{1, 2, 3}, List.of("a"), 4.5};
        for (Object o : inputs) System.out.println(describe(o));

        section("8. Record patterns - deconstruction, nested (Java 21)");
        Object[] shapes = {new Point(0, 0), new Point(2, 5), new Line(new Point(0, 0), new Point(3, 4)), "nope"};
        for (Object o : shapes) System.out.println(where(o));
        Object maybePoint = new Point(6, 8);
        if (maybePoint instanceof Point(int x, int y) && x < y) {
            System.out.println("instanceof with a record pattern: x=" + x + ", y=" + y);
        }

        section("9. Sealed expression tree evaluated by recursion + patterns");
        Expr expr = new Add(new Num(2), new Mul(new Num(3), new Num(4)));   // 2 + 3 * 4
        System.out.println(show(expr) + " = " + eval(expr));

        section("10. Sequenced collections (Java 21)");
        List<Integer> list = new ArrayList<>(List.of(2, 3));
        list.addFirst(1);
        list.addLast(4);
        System.out.println(list + " first=" + list.getFirst() + " last=" + list.getLast() + " reversed=" + list.reversed());
        var set = new LinkedHashSet<>(List.of("x", "y", "z"));
        System.out.println("LinkedHashSet reversed view: " + set.reversed());

        section("11. Newer APIs");
        System.out.println("\"  \".isBlank()          = " + "  ".isBlank());
        System.out.println("\" hi \".strip()          = '" + " hi ".strip() + "'");
        System.out.println("\"ab\".repeat(3)          = " + "ab".repeat(3));
        System.out.println("transform               = " + "java".transform(s -> s + " rocks"));
        System.out.println("Math.clamp(150, 0, 100) = " + Math.clamp(150, 0, 100));
        System.out.println("StringBuilder.repeat    = " + new StringBuilder().repeat("-=", 5));
        System.out.println("splitWithDelimiters     = " + Arrays.toString("a,b;c".splitWithDelimiters("[,;]", 0)));
        System.out.println("Stream.toList()         = " + java.util.stream.Stream.of(3, 1, 2).sorted().toList());
        var stats = java.util.stream.Stream.of(5, 1, 9).collect(Collectors.teeing(
                Collectors.minBy(Integer::compare), Collectors.maxBy(Integer::compare),
                (min, max) -> "min=" + min.orElseThrow() + ", max=" + max.orElseThrow()));
        System.out.println("Collectors.teeing       = " + stats);
    }

    static double fee(Payment p) {
        return switch (p) {                        // exhaustive over the sealed type
            case Card c -> c.amount() * 0.02;
            case Upi u -> 0;
            case Cash c -> c.amount() > 10_000 ? 50 : 0;
        };
    }

    static String describe(Object o) {
        return switch (o) {
            case null -> "null handled explicitly";
            case Integer i when i > 100 -> "big integer " + i;
            case Integer i -> "small integer " + i;
            case String s -> "string of length " + s.length();
            case int[] arr -> "int[] with sum " + Arrays.stream(arr).sum();
            case List<?> l -> "list of size " + l.size();
            default -> "something else: " + o.getClass().getSimpleName();
        };
    }

    static String where(Object o) {
        return switch (o) {
            case Point(var x, var y) when x == 0 && y == 0 -> "origin";
            case Point(var x, var y) -> "point at (" + x + ", " + y + ")";
            case Line(Point(var x1, var y1), Point(var x2, var y2)) ->
                    "line of length " + Math.hypot(x2 - x1, y2 - y1);
            default -> "not a shape: " + o;
        };
    }

    static int eval(Expr e) {
        return switch (e) {
            case Num n -> n.value();
            case Add(Expr l, Expr r) -> eval(l) + eval(r);
            case Mul(Expr l, Expr r) -> eval(l) * eval(r);
        };
    }

    static String show(Expr e) {
        return switch (e) {
            case Num n -> String.valueOf(n.value());
            case Add a -> "(" + show(a.left()) + " + " + show(a.right()) + ")";
            case Mul m -> show(m.left()) + " * " + show(m.right());
        };
    }

    static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}

record Point(int x, int y) {
    double distanceFromOrigin() {
        return Math.hypot(x, y);
    }
}

record Line(Point start, Point end) { }

record Range(int low, int high) {
    Range {                                        // compact canonical constructor
        if (low > high) throw new IllegalArgumentException("low " + low + " > high " + high);
    }

    static Range of(int a, int b) {
        return new Range(Math.min(a, b), Math.max(a, b));
    }

    int length() {
        return high - low;
    }
}

record Order(String id, List<String> items) {
    Order {
        items = List.copyOf(items);                // defensive copy
    }
}

sealed interface Payment permits Card, Upi, Cash { }

record Card(String number, double amount) implements Payment { }

record Upi(String vpa, double amount) implements Payment { }

record Cash(double amount) implements Payment { }

sealed interface Expr permits Num, Add, Mul { }

record Num(int value) implements Expr { }

record Add(Expr left, Expr right) implements Expr { }

record Mul(Expr left, Expr right) implements Expr { }
