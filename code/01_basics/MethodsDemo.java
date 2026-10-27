/*
 * TOPIC: Methods - pass-by-value, overloading resolution, varargs, recursion
 * RUN:   java code/01_basics/MethodsDemo.java
 */
public class MethodsDemo {

    public static void main(String[] args) {
        section("1. Pass-by-value (primitives)");
        int n = 1;
        tryToChange(n);
        System.out.println("n after tryToChange: " + n + "  (a copy was changed)");

        section("2. Pass-by-value (objects: the REFERENCE is copied)");
        StringBuilder a = new StringBuilder("Hello");
        StringBuilder b = new StringBuilder("Hi");
        mutateAndReassign(a, b);
        System.out.println("a = " + a + "   <- object was MUTATED through the copied reference");
        System.out.println("b = " + b + "            <- reassigning the parameter did NOT affect the caller");

        int[] arr = {1, 2};
        Integer p = 10, q = 20;
        swap(p, q);
        System.out.println("swap(Integer) has no effect: p=" + p + ", q=" + q);
        swapInArray(arr);
        System.out.println("swapping INSIDE an array works: " + arr[0] + "," + arr[1]);

        section("3. Overloading - Exact > Widening > Boxing > Varargs");
        byte small = 5;
        show(small);             // no show(byte): widening to int wins over boxing
        show(5);                 // exact match int
        show(5L);                // exact long
        show(Integer.valueOf(5)); // exact Integer
        show('c');               // char widens to int
        show("text");            // String
        show((Object) "text");   // forced to Object
        print(null);             // most SPECIFIC type wins: String over Object

        section("4. Varargs");
        System.out.println("sum()        = " + sum());
        System.out.println("sum(1)       = " + sum(1));
        System.out.println("sum(1,2,3)   = " + sum(1, 2, 3));
        System.out.println("sum(array)   = " + sum(new int[]{4, 5}));
        System.out.println(String.format("%s scored %d", "Ann", 91));   // printf/format use varargs

        section("5. Recursion");
        System.out.println("factorial(10) = " + factorial(10));
        System.out.println("fib(10)       = " + fib(10));
        System.out.println("power(2,10)   = " + power(2, 10));
        try {
            infinite(1);
        } catch (StackOverflowError e) {
            System.out.println("Recursion without a base case -> StackOverflowError");
        }
    }

    static void tryToChange(int x) {
        x = 99;
    }

    static void mutateAndReassign(StringBuilder sb, StringBuilder sb2) {
        sb.append(" World");
        sb2 = new StringBuilder("New");
    }

    static void swap(Integer x, Integer y) {
        Integer t = x;
        x = y;
        y = t;
    }

    static void swapInArray(int[] a) {
        int t = a[0];
        a[0] = a[1];
        a[1] = t;
    }

    static void show(int x)     { System.out.println("show(int)     <- " + x); }
    static void show(long x)    { System.out.println("show(long)    <- " + x); }
    static void show(Integer x) { System.out.println("show(Integer) <- " + x); }
    static void show(String x)  { System.out.println("show(String)  <- " + x); }
    static void show(Object x)  { System.out.println("show(Object)  <- " + x); }

    static void print(Object o) { System.out.println("print(Object)"); }
    static void print(String s) { System.out.println("print(String) <- null picks the most specific type"); }

    static int sum(int... nums) {
        int total = 0;
        for (int v : nums) total += v;
        return total;
    }

    static long factorial(int n) {
        return n <= 1 ? 1 : n * factorial(n - 1);
    }

    static int fib(int n) {
        return n < 2 ? n : fib(n - 1) + fib(n - 2);
    }

    static long power(long base, int exp) {      // fast exponentiation: O(log n)
        if (exp == 0) return 1;
        long half = power(base, exp / 2);
        return exp % 2 == 0 ? half * half : half * half * base;
    }

    static int infinite(int n) {
        return infinite(n + 1);
    }

    static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}
