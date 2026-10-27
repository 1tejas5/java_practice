import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

/*
 * TOPIC: "Predict the output" puzzles - the answers in notes/15-tricky-output-questions.md
 *        were produced by THIS program.
 * RUN:   java code/15_tricky/TrickyOutputs.java
 * HOW TO PRACTISE: open the notes, cover the answers, predict each one, then run this file.
 * NOTE: your IDE will show many warnings here ("dead code", "unlikely argument type"...).
 *       That is the point - every warning IS one of the traps.
 */
public class TrickyOutputs {

    public static void main(String[] args) {
        // ---------------- operators & types ----------------
        q(1, 1 + 2 + "3" + 4 + 5);
        q(2, "" + 'a' + 'b');
        q(3, 'a' + 'b');
        q(4, 'a' + 'b' + "c");
        char c = 'A';
        c += 1;
        q(5, c);
        int i = 0;
        i = i++ + ++i;
        q(6, i);
        int x = 5;
        x = x++ * 2 + x;
        q(7, x);
        int y = 10;
        y = y++;
        q(8, y);
        q(9, 010);
        q(10, '7' + 1);
        q(11, (byte) 200);
        q(12, Integer.MAX_VALUE + 1);
        q(13, Math.abs(Integer.MIN_VALUE));
        q(14, 0.1 + 0.2);
        q(15, 0.1 + 0.2 == 0.3);
        q(16, 0.1f + 0.2f == 0.3f);
        q(17, 10.0 / 0);
        q(18, 0.0 / 0 == 0.0 / 0);
        q(19, Math.round(2.5) + " " + Math.round(-2.5) + " " + Math.round(-2.51));
        q(20, Math.min(Double.MIN_VALUE, 0.0));
        Object o = true ? 1 : 2.0;
        q(21, o);
        int k = 0;
        q(22, true ? 'a' : 0);
        q(23, true ? 'a' : k);
        boolean flag = false;
        String r24 = "no";
        if (flag = true) r24 = "yes";
        q(24, r24);
        q(25, -7 % 3 + " " + 7 % -3);

        // ---------------- strings ----------------
        q(26, ("Ja" + "va") == "Java");
        String part = "Ja";
        q(27, (part + "va") == "Java");
        q(28, new String("Java") == "Java");
        q(29, new String("Java").intern() == "Java");
        String s = "a";
        s.concat("b");
        q(30, s);
        String nothing = null;
        q(31, nothing + "x");
        q(32, "abc".substring(3).isEmpty() + " " + "abc".substring(1, 1).isEmpty());
        q(33, "a,b,,c,,".split(",").length);
        q(34, "a.b.c".split(".").length);
        q(35, new StringBuilder("a").equals(new StringBuilder("a")));
        q(36, "" + 1 + 2 + " vs " + (1 + 2) + "");

        // ---------------- wrappers ----------------
        Integer a1 = 127, a2 = 127, b1 = 128, b2 = 128;
        q(37, (a1 == a2) + " " + (b1 == b2));
        q(38, Long.valueOf(1).equals(1));
        q(39, Integer.valueOf(1).equals(Long.valueOf(1)));
        q(40, new BigDecimal("2.0").equals(new BigDecimal("2.00")));
        q(41, Character.getNumericValue('a'));

        // ---------------- collections ----------------
        List<Integer> list = new ArrayList<>(List.of(1, 2, 3));
        list.remove(1);
        q(42, list);
        int[] prim = {1, 2, 3};
        q(43, Arrays.asList(prim).size() + " vs " + Arrays.asList(1, 2, 3).size());
        q(44, attempt(() -> Arrays.asList(1, 2, 3).add(4)));
        q(45, attempt(() -> List.of(1, 2, 3).contains(null)));
        Set<Short> shorts = new HashSet<>();
        for (short sh = 0; sh < 100; sh++) {
            shorts.add(sh);
            shorts.remove(sh - 1);          // sh - 1 is an int -> boxes to Integer, never matches a Short
        }
        q(46, shorts.size());
        Map<String, Integer> hm = new HashMap<>();
        hm.put(null, 1);
        q(47, hm.get(null));
        q(48, attempt(() -> new TreeMap<String, Integer>().put(null, 1)));
        q(49, attempt(() -> {
            List<String> l = new ArrayList<>(List.of("a"));
            for (String e : l) l.add("b");
            return l;
        }));
        q(50, attempt(() -> {
            Object[] arr = new Integer[1];
            arr[0] = "s";
            return arr;
        }));

        // ---------------- OOP ----------------
        P p = new C();
        q(51, p.name + " " + p.getName() + " " + p.who());
        q(52, new Derived().describe);
        q(53, call(null));
        q(54, pick(5));

        // ---------------- exceptions ----------------
        q(55, finallyReturn());
        q(56, finallyAfterCatch());
        q(57, finallyModify());

        // ---------------- streams & misc ----------------
        StringBuilder trace = new StringBuilder();
        Stream.of(1, 2, 3).peek(trace::append);      // no terminal operation!
        q(58, "'" + trace + "'");
        StringBuilder order = new StringBuilder();
        Optional<Integer> first = Stream.of(1, 2, 3, 4)
                .peek(n -> order.append("p").append(n).append(' '))
                .map(n -> n * 2)
                .filter(n -> n > 2)
                .findFirst();
        q(59, first + " after " + order.toString().trim());
        StringBuilder loop = new StringBuilder();
        for (int n = 0; n < 3; n++) ;
        {
            loop.append("x");
        }
        q(60, loop);
    }

    // ---------------- helpers for the OOP puzzles ----------------
    static class P {
        String name = "P";

        String getName() { return name; }

        static String who() { return "P.who"; }
    }

    static class C extends P {
        String name = "C";

        @Override
        String getName() { return name; }

        static String who() { return "C.who"; }
    }

    static class Base {
        String describe;

        Base() { describe = hook(); }

        String hook() { return "base"; }
    }

    static class Derived extends Base {
        private String tag = "derived";              // normal field: assigned AFTER Base() finishes
        private final String constTag = "derived";   // final + constant: inlined by the compiler!

        @Override
        String hook() { return "tag=" + tag + ", constTag=" + constTag; }
    }

    static String call(Object o) { return "Object"; }

    static String call(String s) { return "String"; }

    static String pick(long v) { return "long"; }

    static String pick(Integer v) { return "Integer"; }

    static String pick(int... v) { return "varargs"; }

    @SuppressWarnings("finally")
    static int finallyReturn() {
        try {
            return 1;
        } finally {
            return 2;
        }
    }

    static String finallyAfterCatch() {
        StringBuilder sb = new StringBuilder();
        try {
            throw new RuntimeException();
        } catch (RuntimeException e) {
            sb.append("catch ");
            return sb.append("return").toString();
        } finally {
            sb.append(" + finally ran");
        }
    }

    static int finallyModify() {
        int v = 10;
        try {
            return v;
        } finally {
            v = 20;
        }
    }

    interface Risky {
        Object run() throws Exception;
    }

    static String attempt(Risky r) {
        try {
            return String.valueOf(r.run());
        } catch (Exception e) {
            return e.getClass().getSimpleName();
        }
    }

    static void q(int n, Object answer) {
        System.out.printf("Q%-3d %s%n", n, answer);
    }
}
