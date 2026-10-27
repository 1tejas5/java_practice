/*
 * TOPIC: Primitive data types, literals, casting, promotion, overflow
 * RUN:   java code/01_basics/DataTypesAndCasting.java
 *
 * TIP: Before running, read each println and PREDICT the output. Then run and compare.
 */
public class DataTypesAndCasting {

    static int instanceLikeField;   // fields get default values (0 here)

    public static void main(String[] args) {
        section("1. The 8 primitives: sizes and ranges");
        System.out.println("byte   : " + Byte.MIN_VALUE + " to " + Byte.MAX_VALUE + "  (" + Byte.BYTES + " byte)");
        System.out.println("short  : " + Short.MIN_VALUE + " to " + Short.MAX_VALUE + "  (" + Short.BYTES + " bytes)");
        System.out.println("int    : " + Integer.MIN_VALUE + " to " + Integer.MAX_VALUE + "  (" + Integer.BYTES + " bytes)");
        System.out.println("long   : " + Long.MIN_VALUE + " to " + Long.MAX_VALUE + "  (" + Long.BYTES + " bytes)");
        System.out.println("float  : " + Float.MIN_VALUE + " to " + Float.MAX_VALUE + "  (" + Float.BYTES + " bytes)");
        System.out.println("double : " + Double.MIN_VALUE + " to " + Double.MAX_VALUE + "  (" + Double.BYTES + " bytes)");
        System.out.println("char   : " + (int) Character.MIN_VALUE + " to " + (int) Character.MAX_VALUE + "  (" + Character.BYTES + " bytes)");
        System.out.println("boolean: true / false (size is JVM-dependent)");

        section("2. Literals");
        int dec = 26, hex = 0x1A, oct = 032, bin = 0b11010;
        System.out.println("26 written 4 ways: " + dec + " " + hex + " " + oct + " " + bin);
        long big = 10_000_000_000L;          // underscores for readability, L for long
        float f = 1.5f;                      // f is required, decimals default to double
        char c1 = 'A', c2 = 65, c3 = 'A';
        System.out.println("big=" + big + ", f=" + f + ", chars=" + c1 + c2 + c3);
        System.out.println("default value of an int field = " + instanceLikeField);

        section("3. Widening (automatic) and narrowing (explicit)");
        int i = 100;
        long l = i;            // int -> long   automatic
        double d = l;          // long -> double automatic
        System.out.println("widened: " + d);

        double pi = 3.99;
        System.out.println("(int) 3.99      = " + (int) pi + "   <- truncated, not rounded");
        System.out.println("Math.round(3.99)= " + Math.round(pi));
        System.out.println("(byte) 130      = " + (byte) 130 + " <- wraps around (130 - 256)");
        System.out.println("(byte) 200      = " + (byte) 200);
        System.out.println("(int) 'A'       = " + (int) 'A');
        System.out.println("(char) 97       = " + (char) 97);

        long precise = 123_456_789_123L;
        float lossy = precise;               // "widening", but float has ~7 digits only
        System.out.println("long -> float loses precision: " + precise + " -> " + (long) lossy);

        section("4. Promotion in expressions");
        byte a = 10, b = 20;
        // byte sum = a + b;                 // compile error: a + b is int
        byte sum = (byte) (a + b);
        a += 5;                              // compound assignment hides a cast
        System.out.println("sum=" + sum + ", a after += 5 -> " + a);
        char ch = 'A';
        System.out.println("'A' + 1         = " + (ch + 1) + "  (int arithmetic)");
        System.out.println("(char)('A' + 1) = " + (char) (ch + 1));
        System.out.println("'a' + 'b'       = " + ('a' + 'b') + " (97 + 98)");
        System.out.println("\"\" + 'a' + 'b'  = " + ("" + 'a' + 'b'));

        section("5. Overflow is SILENT for int and long");
        int max = Integer.MAX_VALUE;
        System.out.println("MAX_VALUE + 1 = " + (max + 1));
        System.out.println("Math.abs(Integer.MIN_VALUE) = " + Math.abs(Integer.MIN_VALUE) + "  <- still negative!");
        try {
            Math.addExact(max, 1);
        } catch (ArithmeticException e) {
            System.out.println("Math.addExact throws: " + e.getMessage());
        }

        section("6. Floating point surprises");
        System.out.println("0.1 + 0.2        = " + (0.1 + 0.2));
        System.out.println("0.1 + 0.2 == 0.3 ? " + (0.1 + 0.2 == 0.3));
        System.out.println("10.0 / 0         = " + (10.0 / 0));
        System.out.println("0.0 / 0          = " + (0.0 / 0));
        System.out.println("NaN == NaN       ? " + (Double.NaN == Double.NaN) + "  -> use Double.isNaN()");
        try {
            System.out.println(10 / 0);
        } catch (ArithmeticException e) {
            System.out.println("10 / 0 (int)     -> ArithmeticException: " + e.getMessage());
        }
        System.out.println("BigDecimal 0.1+0.2 = "
                + new java.math.BigDecimal("0.1").add(new java.math.BigDecimal("0.2")) + "  <- use for money");
    }

    static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}
