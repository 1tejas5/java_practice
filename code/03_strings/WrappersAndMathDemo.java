import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/*
 * TOPIC: Wrapper classes (autoboxing, Integer cache, NPE traps), Math, BigDecimal, BigInteger
 * RUN:   java code/03_strings/WrappersAndMathDemo.java
 */
public class WrappersAndMathDemo {

    public static void main(String[] args) {
        section("1. Autoboxing and unboxing");
        Integer boxed = 10;                    // Integer.valueOf(10)
        int raw = boxed;                       // boxed.intValue()
        List<Integer> list = new ArrayList<>();
        list.add(raw);                         // autoboxing into a collection
        System.out.println("boxed=" + boxed + ", raw=" + raw + ", list=" + list);

        section("2. The Integer cache (-128..127)");
        Integer a = 127, b = 127, c = 128, d = 128;
        System.out.println("127 == 127 : " + (a == b) + "   (same cached object)");
        System.out.println("128 == 128 : " + (c == d) + "  (two objects!)");
        System.out.println("128 equals : " + c.equals(d) + "   <- always use equals");
        Long big = 10L;
        System.out.println("Long(10).equals(10)  : " + big.equals(10) + "  (10 boxes to Integer)");
        System.out.println("Long(10).equals(10L) : " + big.equals(10L));

        section("3. Unboxing null -> NullPointerException");
        Integer maybe = null;
        try {
            int boom = maybe;
        } catch (NullPointerException e) {
            System.out.println("NPE: " + e.getMessage());
        }

        section("4. remove(int index) vs remove(Object value)");
        List<Integer> nums = new ArrayList<>(List.of(10, 20, 30));
        nums.remove(1);
        System.out.println("remove(1)                 -> " + nums + "  (index!)");
        nums.remove(Integer.valueOf(10));
        System.out.println("remove(Integer.valueOf(10)) -> " + nums + "  (value)");

        section("5. Parsing and conversions");
        System.out.println("parseInt(\"42\")+1   = " + (Integer.parseInt("42") + 1));
        System.out.println("valueOf(\"42\")      = " + Integer.valueOf("42") + " (an Integer)");
        System.out.println("parseInt(\"ff\",16)  = " + Integer.parseInt("ff", 16));
        System.out.println("toBinaryString(10) = " + Integer.toBinaryString(10));
        System.out.println("toHexString(255)   = " + Integer.toHexString(255));
        System.out.println("Double.parseDouble = " + Double.parseDouble("3.5"));
        System.out.println("Boolean.parseBoolean(\"TRUE\") = " + Boolean.parseBoolean("TRUE"));
        try {
            Integer.parseInt("12a");
        } catch (NumberFormatException e) {
            System.out.println("NumberFormatException: " + e.getMessage());
        }

        section("6. Math");
        System.out.println("Math.round(2.5)=" + Math.round(2.5) + ", Math.round(-2.5)=" + Math.round(-2.5)
                + ", Math.round(-2.6)=" + Math.round(-2.6));
        System.out.println("floor(-1.5)=" + Math.floor(-1.5) + ", ceil(-1.5)=" + Math.ceil(-1.5));
        System.out.println("pow(2,10)=" + Math.pow(2, 10) + " (a double), sqrt(16)=" + Math.sqrt(16));
        System.out.println("-7 % 3=" + (-7 % 3) + ", floorMod(-7,3)=" + Math.floorMod(-7, 3));
        int dice = (int) (Math.random() * 6) + 1;
        System.out.println("random dice roll 1..6 = " + dice);

        section("7. BigDecimal for money");
        System.out.println("double:     0.1 + 0.2 = " + (0.1 + 0.2));
        System.out.println("BigDecimal: 0.1 + 0.2 = " + new BigDecimal("0.1").add(new BigDecimal("0.2")));
        System.out.println("new BigDecimal(0.1)   = " + new BigDecimal(0.1) + "  <- never use the double constructor");
        System.out.println("BigDecimal.valueOf(0.1)= " + BigDecimal.valueOf(0.1));
        BigDecimal ten = new BigDecimal("10");
        System.out.println("10 / 3 (2 dp, HALF_UP) = " + ten.divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP));
        try {
            ten.divide(new BigDecimal("3"));
        } catch (ArithmeticException e) {
            System.out.println("10 / 3 with no scale  -> ArithmeticException: " + e.getMessage());
        }
        BigDecimal two0 = new BigDecimal("2.0"), two00 = new BigDecimal("2.00");
        System.out.println("2.0 equals 2.00 ? " + two0.equals(two00) + "  (scale differs)");
        System.out.println("2.0 compareTo 2.00 == 0 ? " + (two0.compareTo(two00) == 0) + "  <- compare values like this");
        BigDecimal price = new BigDecimal("199.99");
        BigDecimal withTax = price.multiply(new BigDecimal("1.18")).setScale(2, RoundingMode.HALF_UP);
        System.out.println("199.99 + 18% GST = " + withTax);

        section("8. BigInteger for huge numbers");
        BigInteger fact = BigInteger.ONE;
        for (int i = 2; i <= 30; i++) fact = fact.multiply(BigInteger.valueOf(i));
        System.out.println("30! = " + fact);
        System.out.println("long would overflow: max long = " + Long.MAX_VALUE);
    }

    static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}
