/*
 * TOPIC: Operators (++, short-circuit, shifts, ternary) and control flow (switch x3, loops, labels)
 * RUN:   java code/01_basics/OperatorsAndControlFlow.java
 */
public class OperatorsAndControlFlow {

    public static void main(String[] args) {
        section("1. Arithmetic");
        System.out.println("7 / 2   = " + (7 / 2) + "   (int division truncates)");
        System.out.println("-7 / 2  = " + (-7 / 2) + "  (truncates toward zero)");
        System.out.println("7.0 / 2 = " + (7.0 / 2));
        System.out.println("7 % 3   = " + (7 % 3));
        System.out.println("-7 % 3  = " + (-7 % 3) + "  (sign follows the dividend)");

        section("2. ++ and --");
        int x = 5;
        int y = x++ + ++x;          // 5 + 7
        System.out.println("x++ + ++x with x=5 -> y=" + y + ", x=" + x);
        int i = 0;
        i = i++;
        System.out.println("i = i++  -> i=" + i + "  (old value written back)");

        section("3. Short-circuit && vs non-short-circuit &");
        String s = null;
        if (s != null && s.length() > 0) {
            System.out.println("never printed");
        }
        System.out.println("&& skipped s.length() safely");
        try {
            if (s != null & s.length() > 0) {
                System.out.println("never printed");
            }
        } catch (NullPointerException e) {
            System.out.println("& evaluated both sides -> NullPointerException");
        }
        int calls = 0;
        boolean r = (calls++ > 10) && (calls++ > 10);
        System.out.println("after false && ..., calls=" + calls + " (right side skipped), result=" + r);

        section("4. Bitwise and shifts");
        System.out.println("5 & 3  = " + (5 & 3) + "   (0101 & 0011 = 0001)");
        System.out.println("5 | 3  = " + (5 | 3) + "   (0111)");
        System.out.println("5 ^ 3  = " + (5 ^ 3) + "   (0110) XOR");
        System.out.println("~5     = " + (~5) + "  (-(n+1))");
        System.out.println("1 << 4 = " + (1 << 4) + "  (multiply by 2^4)");
        System.out.println("-8 >> 1  = " + (-8 >> 1) + "  (sign kept)");
        System.out.println("-8 >>> 1 = " + (-8 >>> 1) + "  (zero filled)");
        System.out.println("is 10 even? " + ((10 & 1) == 0) + "  (bit trick)");

        section("5. Ternary");
        int marks = 82;
        String result = marks >= 40 ? "PASS" : "FAIL";
        System.out.println(marks + " -> " + result);
        Object o = true ? 1 : 2.0;   // numeric promotion: int 1 becomes double 1.0!
        System.out.println("true ? 1 : 2.0 -> " + o + "  (trap: both branches promoted to double)");

        section("6. Switch: classic, arrow, expression");
        for (int day = 1; day <= 3; day++) {
            classicSwitch(day);
        }
        classicFallThrough(2);
        for (int day : new int[]{1, 2, 4}) {
            System.out.println("arrow/expression day " + day + " -> " + dayType(day));
        }
        String cmd = "stop";
        switch (cmd) {                       // String switch (Java 7+)
            case "start" -> System.out.println("starting");
            case "stop" -> System.out.println("stopping");
            default -> System.out.println("unknown");
        }

        section("7. Loops");
        for (int k = 0; k < 3; k++) System.out.print("for" + k + " ");
        System.out.println();
        int w = 3;
        while (w > 0) System.out.print("while" + w-- + " ");
        System.out.println();
        int dw = 100;
        do {
            System.out.println("do-while runs once even though 100 > 5: dw=" + dw);
        } while (dw < 5);
        for (String name : new String[]{"Ann", "Bob"}) System.out.print("hi " + name + "  ");
        System.out.println();

        section("8. break, continue, labeled break");
        outer:
        for (int a = 0; a < 3; a++) {
            for (int b = 0; b < 3; b++) {
                if (b == 1) continue;        // skip b == 1
                if (a == 2) break outer;     // leave both loops
                System.out.println("a=" + a + ", b=" + b);
            }
        }
        System.out.println("left the outer loop");
    }

    static void classicSwitch(int day) {
        switch (day) {
            case 1:
                System.out.println("classic: Sunday");
                break;
            case 2:
                System.out.println("classic: Monday");
                break;
            default:
                System.out.println("classic: Other");
        }
    }

    static void classicFallThrough(int day) {
        System.out.print("fall-through from case 2: ");
        switch (day) {
            case 1:
                System.out.print("one ");
            case 2:
                System.out.print("two ");    // no break -> falls into case 3 and default
            case 3:
                System.out.print("three ");
            default:
                System.out.print("default ");
        }
        System.out.println();
    }

    static String dayType(int day) {
        return switch (day) {
            case 1, 7 -> "Weekend";
            case 2 -> {
                String mood = "ugh";
                yield "Monday (" + mood + ")";   // yield returns a value from a block
            }
            default -> "Weekday";
        };
    }

    static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}
