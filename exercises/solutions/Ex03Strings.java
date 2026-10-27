import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/*
 * SOLUTIONS - Exercises section 3: Strings
 * RUN: java exercises/solutions/Ex03Strings.java
 */
public class Ex03Strings {

    public static void main(String[] args) {
        check("3.1 reverseWords", "Java love I", reverseWords("  I   love Java "));
        check("3.2 palindrome sentence", true, isPalindrome("A man, a plan, a canal: Panama"));
        check("3.2 not palindrome", false, isPalindrome("race a car"));
        check("3.3 anagram", true, isAnagram("Listen", "Silent"));
        check("3.3 not anagram", false, isAnagram("abc", "abd"));
        check("3.4 frequency", Map.of('b', 1, 'a', 3, 'n', 2), charFrequency("banana"));
        check("3.4 keeps order", List.of('b', 'a', 'n'), List.copyOf(charFrequency("banana").keySet()));
        check("3.5 firstUnique(swiss)", 'w', firstNonRepeating("swiss"));
        check("3.5 firstUnique(aabb)", null, firstNonRepeating("aabb"));
        check("3.6 compress", "a3b2c1", compress("aaabbc"));
        check("3.6 compress single", "x1", compress("x"));
        check("3.7 vowels/consonants", new int[]{3, 7}, vowelsAndConsonants("Hello World"));
        check("3.8 rotation", true, isRotation("waterbottle", "erbottlewat"));
        check("3.8 not rotation", false, isRotation("abcd", "acbd"));
        check("3.9 capitalize", "Hello Java World", capitalize("hello java world"));
        check("3.10 longest unique (abcabcbb)", 3, longestUniqueSubstring("abcabcbb"));
        check("3.10 longest unique (pwwkew)", 3, longestUniqueSubstring("pwwkew"));
        check("3.10 longest unique (bbbb)", 1, longestUniqueSubstring("bbbb"));
        summary();
    }

    // 3.1 - strip + split on runs of whitespace
    static String reverseWords(String s) {
        String[] words = s.strip().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = words.length - 1; i >= 0; i--) {
            sb.append(words[i]);
            if (i > 0) sb.append(' ');
        }
        return sb.toString();
    }

    // 3.2 - two pointers, skip non-alphanumerics
    static boolean isPalindrome(String s) {
        int l = 0, r = s.length() - 1;
        while (l < r) {
            char a = s.charAt(l), b = s.charAt(r);
            if (!Character.isLetterOrDigit(a)) l++;
            else if (!Character.isLetterOrDigit(b)) r--;
            else if (Character.toLowerCase(a) != Character.toLowerCase(b)) return false;
            else {
                l++;
                r--;
            }
        }
        return true;
    }

    // 3.3 - frequency array is O(n); sorting would be O(n log n)
    static boolean isAnagram(String a, String b) {
        a = a.toLowerCase();
        b = b.toLowerCase();
        if (a.length() != b.length()) return false;
        int[] count = new int[26];
        for (int i = 0; i < a.length(); i++) {
            count[a.charAt(i) - 'a']++;
            count[b.charAt(i) - 'a']--;
        }
        for (int c : count) if (c != 0) return false;
        return true;
    }

    // 3.4
    static Map<Character, Integer> charFrequency(String s) {
        Map<Character, Integer> freq = new LinkedHashMap<>();
        for (char c : s.toCharArray()) freq.merge(c, 1, Integer::sum);
        return freq;
    }

    // 3.5
    static Character firstNonRepeating(String s) {
        Map<Character, Integer> freq = charFrequency(s);
        for (Map.Entry<Character, Integer> e : freq.entrySet()) if (e.getValue() == 1) return e.getKey();
        return null;
    }

    // 3.6
    static String compress(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); ) {
            char c = s.charAt(i);
            int j = i;
            while (j < s.length() && s.charAt(j) == c) j++;
            sb.append(c).append(j - i);
            i = j;
        }
        return sb.toString();
    }

    // 3.7
    static int[] vowelsAndConsonants(String s) {
        int v = 0, c = 0;
        for (char ch : s.toLowerCase().toCharArray()) {
            if (!Character.isLetter(ch)) continue;
            if ("aeiou".indexOf(ch) >= 0) v++;
            else c++;
        }
        return new int[]{v, c};
    }

    // 3.8
    static boolean isRotation(String a, String b) {
        return a.length() == b.length() && (a + a).contains(b);
    }

    // 3.9
    static String capitalize(String s) {
        StringBuilder sb = new StringBuilder();
        for (String w : s.split(" ")) {
            if (!sb.isEmpty()) sb.append(' ');
            if (!w.isEmpty()) sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1).toLowerCase());
        }
        return sb.toString();
    }

    // 3.10 - sliding window, O(n)
    static int longestUniqueSubstring(String s) {
        Map<Character, Integer> lastSeen = new HashMap<>();
        int best = 0, start = 0;
        for (int end = 0; end < s.length(); end++) {
            char c = s.charAt(end);
            Integer prev = lastSeen.get(c);
            if (prev != null && prev >= start) start = prev + 1;   // jump past the repeat
            lastSeen.put(c, end);
            best = Math.max(best, end - start + 1);
        }
        return best;
    }

    // ---------------- test helper ----------------
    private static int passed, failed;

    static void check(String name, Object expected, Object actual) {
        boolean ok = Objects.deepEquals(expected, actual);
        if (ok) passed++;
        else failed++;
        System.out.printf("%s %-28s %s%n", ok ? "PASS" : "FAIL", name, ok ? "" : "expected=" + str(expected) + " actual=" + str(actual));
    }

    static void summary() {
        System.out.printf("%n%d passed, %d failed%n", passed, failed);
    }

    private static String str(Object o) { return o instanceof int[] a ? Arrays.toString(a) : String.valueOf(o); }
}
