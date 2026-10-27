import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Predicate;

/*
 * SOLUTIONS - Exercises section 5: Generics
 * RUN: java exercises/solutions/Ex05Generics.java
 */
public class Ex05Generics {

    public static void main(String[] args) {
        // 5.1
        Pair<String, Integer> p = new Pair<>("age", 30);
        Pair<Integer, String> swapped = p.swap();
        check("5.1 swap", "Pair[first=30, second=age]", swapped.toString());

        // 5.2
        check("5.2 max ints", 9, max(List.of(3, 9, 2)));
        check("5.2 max strings", "pear", max(List.of("apple", "pear", "fig")));

        // 5.3
        ArrayStack<String> stack = new ArrayStack<>();
        for (int i = 1; i <= 20; i++) stack.push("item" + i);       // forces several resizes
        check("5.3 size", 20, stack.size());
        check("5.3 peek", "item20", stack.peek());
        check("5.3 pop", "item20", stack.pop());
        check("5.3 size after pop", 19, stack.size());
        ArrayStack<Integer> empty = new ArrayStack<>();
        check("5.3 pop empty", "NoSuchElementException", attempt(empty::pop));

        // 5.4
        check("5.4 sum ints", 6.0, sum(List.of(1, 2, 3)));
        check("5.4 sum doubles", 4.0, sum(List.of(1.5, 2.5)));
        check("5.4 sum longs", 30.0, sum(List.of(10L, 20L)));

        // 5.5
        Predicate<Object> notNull = Objects::nonNull;              // a Predicate<Object> works for any T (? super T)
        check("5.5 countIf even", 2, countIf(List.of(1, 2, 3, 4), n -> n % 2 == 0));
        check("5.5 countIf super", 2, countIf(Arrays.asList("a", null, "b"), notNull));

        // 5.6
        List<Number> dest = new ArrayList<>();
        copyInto(dest, List.of(1, 2));
        copyInto(dest, List.of(3.5));
        check("5.6 copyInto", List.of(1, 2, 3.5), dest);
        summary();
    }

    // 5.1
    record Pair<A, B>(A first, B second) {
        Pair<B, A> swap() {
            return new Pair<>(second, first);
        }
    }

    // 5.2
    static <T extends Comparable<T>> T max(List<T> list) {
        if (list.isEmpty()) throw new NoSuchElementException("empty list");
        T best = list.get(0);
        for (T t : list) if (t.compareTo(best) > 0) best = t;
        return best;
    }

    // 5.3
    static class ArrayStack<T> {
        private Object[] data = new Object[4];
        private int size;

        void push(T item) {
            if (size == data.length) data = Arrays.copyOf(data, data.length * 2);   // grow x2
            data[size++] = item;
        }

        @SuppressWarnings("unchecked")
        T pop() {
            if (size == 0) throw new NoSuchElementException("stack is empty");
            T item = (T) data[--size];
            data[size] = null;                                     // avoid loitering (memory leak)
            return item;
        }

        @SuppressWarnings("unchecked")
        T peek() {
            if (size == 0) throw new NoSuchElementException("stack is empty");
            return (T) data[size - 1];
        }

        boolean isEmpty() { return size == 0; }

        int size() { return size; }
    }

    // 5.4 - producer: extends
    static double sum(List<? extends Number> nums) {
        double total = 0;
        for (Number n : nums) total += n.doubleValue();
        return total;
    }

    // 5.5 - the predicate CONSUMES T values: super
    static <T> int countIf(Collection<T> items, Predicate<? super T> condition) {
        int count = 0;
        for (T item : items) if (condition.test(item)) count++;
        return count;
    }

    // 5.6 - PECS in one signature
    static <T> void copyInto(List<? super T> dest, List<? extends T> src) {
        for (T t : src) dest.add(t);
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
