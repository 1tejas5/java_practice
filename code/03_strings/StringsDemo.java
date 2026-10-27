import java.util.Arrays;

/*
 * TOPIC: String pool, ==, equals, intern, immutability, common methods, StringBuilder, performance
 * RUN:   java code/03_strings/StringsDemo.java
 */
public class StringsDemo {

    public static void main(String[] args) {
        section("1. String pool and ==");
        String s1 = "Java";
        String s2 = "Java";
        String s3 = new String("Java");
        String s4 = s3.intern();
        System.out.println("s1 == s2      : " + (s1 == s2) + "   (same pooled literal)");
        System.out.println("s1 == s3      : " + (s1 == s3) + "  (new forces a heap object)");
        System.out.println("s1.equals(s3) : " + s1.equals(s3) + "   (same characters)");
        System.out.println("s1 == s4      : " + (s1 == s4) + "   (intern returns the pooled copy)");

        section("2. Compile-time vs runtime concatenation");
        String part = "Ja";
        final String finalPart = "Ja";
        System.out.println("\"Ja\" + \"va\" == \"Java\"     : " + (("Ja" + "va") == "Java"));
        System.out.println("part + \"va\" == \"Java\"     : " + ((part + "va") == "Java"));
        System.out.println("finalPart + \"va\" == \"Java\": " + ((finalPart + "va") == "Java"));

        section("3. Immutability");
        String s = "hello";
        s.toUpperCase();                         // result thrown away
        System.out.println("after s.toUpperCase(): " + s);
        s = s.toUpperCase();
        System.out.println("after s = s.toUpperCase(): " + s);
        String concat = s.concat(" WORLD");
        System.out.println("concat returns a new String: " + concat + ", s still " + s);

        section("4. Everyday methods");
        String text = "  Hello, Java World  ";
        String t = text.strip();
        System.out.println("strip()         -> '" + t + "'");
        System.out.println("length()        -> " + t.length());
        System.out.println("charAt(4)       -> " + t.charAt(4));
        System.out.println("substring(7,11) -> " + t.substring(7, 11));
        System.out.println("indexOf(\"o\")    -> " + t.indexOf("o") + ", lastIndexOf -> " + t.lastIndexOf("o"));
        System.out.println("contains(Java)  -> " + t.contains("Java"));
        System.out.println("replace(o,0)    -> " + t.replace('o', '0'));
        System.out.println("replaceAll      -> " + "a1b22c333".replaceAll("\\d+", "#"));
        System.out.println("split(\", \")     -> " + Arrays.toString(t.split(", ")));
        System.out.println("join            -> " + String.join(" | ", "a", "b", "c"));
        System.out.println("repeat          -> " + "ab".repeat(3));
        System.out.println("isEmpty/isBlank -> " + "   ".isEmpty() + " / " + "   ".isBlank());
        System.out.println("compareTo       -> " + "apple".compareTo("banana") + " (negative = comes first)");
        System.out.println("equalsIgnoreCase-> " + "JAVA".equalsIgnoreCase("java"));
        System.out.println("formatted       -> " + "%s is %d years old".formatted("Asha", 25));
        System.out.println("lines().count() -> " + "a\nb\nc".lines().count());

        section("5. Regex traps in split");
        System.out.println("\"a.b.c\".split(\".\")   -> " + Arrays.toString("a.b.c".split(".")) + "  <- '.' means ANY char!");
        System.out.println("\"a.b.c\".split(\"\\\\.\") -> " + Arrays.toString("a.b.c".split("\\.")));
        System.out.println("\"a,b,,\".split(\",\")   -> " + Arrays.toString("a,b,,".split(",")) + "  <- trailing empties dropped");
        System.out.println("null + \"a\"            -> " + (null + "a"));

        section("6. Characters");
        String mixed = "Ab3 z9!";
        int letters = 0, digits = 0, others = 0;
        for (char c : mixed.toCharArray()) {
            if (Character.isLetter(c)) letters++;
            else if (Character.isDigit(c)) digits++;
            else others++;
        }
        System.out.println(mixed + " -> letters=" + letters + ", digits=" + digits + ", others=" + others);
        System.out.println("'7' - '0' = " + ('7' - '0') + "  (char digit to int)");

        section("7. StringBuilder");
        StringBuilder sb = new StringBuilder("Java");
        sb.append(" rocks").insert(0, ">> ").replace(3, 7, "JAVA").reverse();
        System.out.println("chained ops + reverse: " + sb);
        sb.reverse().setCharAt(0, '#');
        System.out.println("setCharAt            : " + sb);
        System.out.println("palindrome 'madam'?  : " + isPalindrome("madam"));
        StringBuilder x = new StringBuilder("same"), y = new StringBuilder("same");
        System.out.println("sb.equals(sameText)  : " + x.equals(y) + "  <- StringBuilder does NOT override equals");
        System.out.println("compareTo == 0       : " + (x.compareTo(y) == 0));

        section("8. Performance: + in a loop vs StringBuilder");
        int n = 20_000;
        long start = System.nanoTime();
        String slow = "";
        for (int i = 0; i < n; i++) slow += "x";
        long slowMs = (System.nanoTime() - start) / 1_000_000;
        start = System.nanoTime();
        StringBuilder fast = new StringBuilder();
        for (int i = 0; i < n; i++) fast.append("x");
        long fastMs = (System.nanoTime() - start) / 1_000_000;
        System.out.println("String +=       : " + slowMs + " ms");
        System.out.println("StringBuilder   : " + fastMs + " ms  (same length: " + (slow.length() == fast.length()) + ")");
    }

    static boolean isPalindrome(String s) {
        return new StringBuilder(s).reverse().toString().equals(s);
    }

    static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}
