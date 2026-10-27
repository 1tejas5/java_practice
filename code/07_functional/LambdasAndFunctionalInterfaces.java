import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/*
 * TOPIC: Lambdas, the big-4 functional interfaces, composition, method references,
 *        effectively final capture, lambda vs anonymous class, custom functional interface
 * RUN:   java code/07_functional/LambdasAndFunctionalInterfaces.java
 */
public class LambdasAndFunctionalInterfaces {

    private final String name = "OuterObject";

    public static void main(String[] args) {
        section("1. Evolution: anonymous class -> lambda -> method reference");
        List<String> words = new ArrayList<>(List.of("banana", "fig", "apple"));
        words.sort(new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                return Integer.compare(a.length(), b.length());
            }
        });
        System.out.println("anonymous class : " + words);
        words.sort((a, b) -> b.length() - a.length());
        System.out.println("lambda (desc)   : " + words);
        words.sort(Comparator.comparingInt(String::length));
        System.out.println("method reference: " + words);

        section("2. The big 4: Predicate tests, Function transforms, Consumer eats, Supplier gives");
        Predicate<String> isLong = s -> s.length() > 4;
        Function<String, Integer> length = String::length;
        Consumer<String> shout = s -> System.out.println("  consumed: " + s.toUpperCase() + "!");
        Supplier<List<String>> listMaker = ArrayList::new;
        System.out.println("Predicate isLong.test(\"banana\") = " + isLong.test("banana"));
        System.out.println("Function length.apply(\"banana\") = " + length.apply("banana"));
        shout.accept("banana");
        List<String> fresh = listMaker.get();
        System.out.println("Supplier gave a new " + fresh.getClass().getSimpleName() + " " + fresh);

        section("3. Operators and Bi-functions");
        UnaryOperator<String> trimUpper = s -> s.strip().toUpperCase();
        BinaryOperator<Integer> add = Integer::sum;
        BiFunction<String, Integer, String> repeat = String::repeat;
        System.out.println("UnaryOperator  -> " + trimUpper.apply("  hi  "));
        System.out.println("BinaryOperator -> " + add.apply(2, 3));
        System.out.println("BiFunction     -> " + repeat.apply("ab", 3));
        IntPredicate even = n -> n % 2 == 0;            // primitive version, no boxing
        System.out.println("IntPredicate even.test(4) -> " + even.test(4));

        section("4. Composition");
        Predicate<String> startsWithA = s -> s.startsWith("a");
        Predicate<String> combo = isLong.and(startsWithA);
        System.out.println("isLong AND startsWithA on 'apple'  = " + combo.test("apple"));
        System.out.println("isLong OR  startsWithA on 'ant'    = " + isLong.or(startsWithA).test("ant"));
        System.out.println("NOT isLong on 'fig'                = " + isLong.negate().test("fig"));
        System.out.println("Predicate.not in a filter          = "
                + List.of("", "a", "", "b").stream().filter(Predicate.not(String::isEmpty)).toList());
        Function<Integer, Integer> doubleIt = x -> x * 2;
        Function<Integer, Integer> plus3 = x -> x + 3;
        System.out.println("doubleIt.andThen(plus3).apply(5)   = " + doubleIt.andThen(plus3).apply(5) + "  (5*2)+3");
        System.out.println("doubleIt.compose(plus3).apply(5)   = " + doubleIt.compose(plus3).apply(5) + "  (5+3)*2");

        section("5. The 4 kinds of method references");
        Function<String, Integer> staticRef = Integer::parseInt;          // static
        Consumer<String> boundRef = System.out::println;                  // instance method of a particular object
        Function<String, String> unboundRef = String::toUpperCase;        // instance method of an arbitrary object
        Supplier<StringBuilder> ctorRef = StringBuilder::new;             // constructor
        Function<Integer, int[]> arrayCtor = int[]::new;
        System.out.println("Integer::parseInt(\"42\")+1 = " + (staticRef.apply("42") + 1));
        System.out.print("System.out::println -> ");
        boundRef.accept("printed via bound reference");
        System.out.println("String::toUpperCase     = " + unboundRef.apply("java"));
        System.out.println("StringBuilder::new      = '" + ctorRef.get().append("built") + "'");
        System.out.println("int[]::new (5).length   = " + arrayCtor.apply(5).length);

        section("6. Effectively final capture");
        int base = 10;
        Function<Integer, Integer> addBase = x -> x + base;
        System.out.println("addBase(5) = " + addBase.apply(5));
        // base = 20;    // uncommenting makes the lambda above a compile error
        int[] counter = {0};                                      // workaround (array element is mutable) - use sparingly
        List.of("a", "b", "c").forEach(s -> counter[0]++);
        System.out.println("counted via array trick = " + counter[0]);

        section("7. 'this' in a lambda vs an anonymous class");
        new LambdasAndFunctionalInterfaces().showThis();

        section("8. Custom functional interface + higher-order function");
        Validator<String> notBlank = s -> !s.isBlank();
        Validator<String> emailLike = s -> s.contains("@");
        Validator<String> both = notBlank.and(emailLike);
        for (String input : Arrays.asList("asha@mail.com", "   ", "no-at-sign")) {
            System.out.println("'" + input + "' valid? " + both.validate(input));
        }
        System.out.println("applyTwice(x*3, 2) = " + applyTwice(x -> x * 3, 2));
        Function<Integer, Function<Integer, Integer>> adder = a -> b -> a + b;   // currying
        System.out.println("curried adder(4)(5) = " + adder.apply(4).apply(5));
    }

    void showThis() {
        Runnable lambda = () -> System.out.println("lambda    this = " + this.name + " (the enclosing object)");
        Runnable anon = new Runnable() {
            final String name = "AnonymousObject";

            @Override
            public void run() {
                System.out.println("anonymous this = " + this.name + " (the anonymous class itself)");
            }
        };
        lambda.run();
        anon.run();
    }

    static <T> T applyTwice(UnaryOperator<T> f, T value) {     // takes a function as an argument
        return f.apply(f.apply(value));
    }

    static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}

@FunctionalInterface
interface Validator<T> {
    boolean validate(T t);

    default Validator<T> and(Validator<T> other) {
        return t -> validate(t) && other.validate(t);
    }
}
