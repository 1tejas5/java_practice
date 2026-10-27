import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/*
 * TOPIC: The 20 most-asked Java 8 stream interview problems (see notes/07 section 8).
 * RUN:   java code/07_functional/StreamInterviewProblems.java
 * PRACTICE: cover the code, read the problem title, write your own solution, then compare.
 */
public class StreamInterviewProblems {

    record Employee(String name, String dept, double salary) { }

    public static void main(String[] args) {
        List<Integer> nums = List.of(5, 3, 8, 3, 1, 9, 8);
        List<String> words = List.of("apple", "banana", "kiwi", "cherry", "fig");
        String s = "programming";
        List<Employee> emps = List.of(
                new Employee("Asha", "IT", 85_000), new Employee("Ravi", "IT", 65_000),
                new Employee("Meera", "HR", 50_000), new Employee("John", "Sales", 55_000),
                new Employee("Zara", "IT", 95_000), new Employee("Karan", "HR", 48_000));

        p(1, "even numbers squared", nums.stream().filter(n -> n % 2 == 0).map(n -> n * n).toList());

        p(2, "sum / max / average",
                nums.stream().mapToInt(Integer::intValue).sum() + " / "
                        + nums.stream().max(Integer::compare).get() + " / "
                        + String.format("%.4f", nums.stream().mapToInt(Integer::intValue).average().orElse(0)));

        p(3, "remove duplicates (keep order)", nums.stream().distinct().toList());

        Set<Integer> seen = new HashSet<>();
        p(4, "find duplicates", nums.stream().filter(n -> !seen.add(n)).collect(Collectors.toSet()));

        p(5, "second highest", nums.stream().distinct().sorted(Comparator.reverseOrder()).skip(1).findFirst());

        Map<Character, Long> freq = s.chars().mapToObj(c -> (char) c)
                .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()));
        p(6, "char frequency of '" + s + "'", freq);

        p(7, "first NON-repeated char", freq.entrySet().stream()
                .filter(e -> e.getValue() == 1).map(Map.Entry::getKey).findFirst());

        Set<Character> seenChars = new HashSet<>();
        p(8, "first REPEATED char", s.chars().mapToObj(c -> (char) c).filter(c -> !seenChars.add(c)).findFirst());

        p(9, "longest word", words.stream().max(Comparator.comparingInt(String::length)).get());

        p(10, "sort by length then alpha",
                words.stream().sorted(Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder())).toList());

        p(11, "join uppercase", words.stream().map(String::toUpperCase).collect(Collectors.joining(", ")));

        p(12, "word -> length map", words.stream()
                .collect(Collectors.toMap(Function.identity(), String::length, (a, b) -> a, LinkedHashMap::new)));

        p(13, "group words by length", words.stream().collect(Collectors.groupingBy(String::length)));

        p(14, "partition even / odd", nums.stream().collect(Collectors.partitioningBy(n -> n % 2 == 0)));

        p(15, "flatten list of lists", List.of(List.of(1, 2), List.of(3), List.of(4, 5)).stream()
                .flatMap(List::stream).toList());

        p(16, "reverse each word", Arrays.stream("I love Java".split(" "))
                .map(w -> new StringBuilder(w).reverse().toString()).collect(Collectors.joining(" ")));

        String pal = "racecar";
        p(17, "is '" + pal + "' a palindrome", IntStream.range(0, pal.length() / 2)
                .allMatch(i -> pal.charAt(i) == pal.charAt(pal.length() - 1 - i)));

        Map<String, String> topPerDept = emps.stream().collect(Collectors.groupingBy(Employee::dept,
                java.util.TreeMap::new,
                Collectors.collectingAndThen(Collectors.maxBy(Comparator.comparingDouble(Employee::salary)),
                        opt -> opt.map(Employee::name).orElse("-"))));
        p(18, "highest paid per dept", topPerDept);

        p(19, "top 3 earners", emps.stream().sorted(Comparator.comparingDouble(Employee::salary).reversed())
                .limit(3).map(Employee::name).toList());

        String text = "the quick fox and the lazy dog and the cat";
        p(20, "word frequency sorted desc", Arrays.stream(text.split("\\s+"))
                .collect(Collectors.groupingBy(String::toLowerCase, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .limit(3).toList());

        System.out.println("\n--- BONUS ---");
        p(21, "anagrams? listen/silent", isAnagram("listen", "silent"));
        p(22, "sum of digits of 98765", String.valueOf(98765).chars().map(c -> c - '0').sum());
        p(23, "count vowels in 'education'", "education".chars().filter(c -> "aeiou".indexOf(c) >= 0).count());
        p(24, "average salary in IT", emps.stream().filter(e -> e.dept().equals("IT"))
                .mapToDouble(Employee::salary).average().orElse(0));
        p(25, "nth fibonacci list (10)", java.util.stream.Stream.iterate(new long[]{0, 1}, f -> new long[]{f[1], f[0] + f[1]})
                .limit(10).map(f -> f[0]).toList());
        Optional<String> none = words.stream().filter(w -> w.length() > 20).findAny();
        p(26, "findAny on no match", none.isPresent() ? none.get() : "Optional.empty");
    }

    static boolean isAnagram(String a, String b) {
        return Arrays.equals(a.chars().sorted().toArray(), b.chars().sorted().toArray());
    }

    static void p(int n, String title, Object result) {
        System.out.printf("%2d. %-34s -> %s%n", n, title, result);
    }
}
