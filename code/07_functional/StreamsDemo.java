import java.util.Arrays;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.averagingDouble;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;

/*
 * TOPIC: Stream pipeline, laziness, creation, intermediate & terminal ops, map vs flatMap,
 *        reduce, Collectors (groupingBy, partitioningBy, toMap, joining, statistics), parallel
 * RUN:   java code/07_functional/StreamsDemo.java
 */
public class StreamsDemo {

    record Employee(int id, String name, String dept, double salary, List<String> skills) { }

    static final List<Employee> EMPS = List.of(
            new Employee(1, "Asha", "IT", 85_000, List.of("Java", "Spring")),
            new Employee(2, "Ravi", "IT", 65_000, List.of("Java", "SQL")),
            new Employee(3, "Meera", "HR", 50_000, List.of("Recruiting")),
            new Employee(4, "John", "Sales", 55_000, List.of("Negotiation", "SQL")),
            new Employee(5, "Zara", "IT", 95_000, List.of("Kotlin", "Java")),
            new Employee(6, "Karan", "HR", 48_000, List.of("Payroll")));

    public static void main(String[] args) {
        section("1. A pipeline: names of IT employees earning > 60k, sorted");
        List<String> names = EMPS.stream()                       // source
                .filter(e -> e.dept().equals("IT"))              // intermediate
                .filter(e -> e.salary() > 60_000)
                .map(Employee::name)
                .sorted()
                .toList();                                       // terminal
        System.out.println(names);

        section("2. Laziness: nothing runs without a terminal op; elements flow ONE AT A TIME");
        Stream<String> pending = Stream.of("a", "b", "c").peek(s -> System.out.println("  peek " + s));
        System.out.println("stream built, nothing printed yet...");
        pending.map(String::toUpperCase).forEach(s -> System.out.println("  got " + s));
        System.out.println("short-circuit with findFirst:");
        Stream.of("x", "y", "z")
                .filter(s -> { System.out.println("  filter " + s); return true; })
                .map(s -> { System.out.println("  map " + s); return s.toUpperCase(); })
                .findFirst();

        section("3. Single use");
        Stream<Integer> once = Stream.of(1, 2, 3);
        once.count();
        try {
            once.count();
        } catch (IllegalStateException e) {
            System.out.println("reusing a stream -> " + e.getMessage());
        }

        section("4. Creating streams");
        System.out.println("IntStream.range(0,5)       = " + Arrays.toString(IntStream.range(0, 5).toArray()));
        System.out.println("IntStream.rangeClosed(1,5) = " + Arrays.toString(IntStream.rangeClosed(1, 5).toArray()));
        System.out.println("iterate x*2 limit 8        = " + Stream.iterate(1, x -> x * 2).limit(8).toList());
        System.out.println("iterate with condition     = " + Stream.iterate(1, x -> x <= 100, x -> x * 3).toList());
        System.out.println("generate limit 3           = " + Stream.generate(() -> "hi").limit(3).toList());
        System.out.println("\"hello\".chars() count     = " + "hello".chars().filter(c -> c == 'l').count() + " l's");

        section("5. Intermediate ops tour");
        List<Integer> nums = List.of(5, 3, 8, 3, 1, 9, 8, 2);
        System.out.println("source               " + nums);
        System.out.println("distinct             " + nums.stream().distinct().toList());
        System.out.println("sorted desc          " + nums.stream().sorted(Comparator.reverseOrder()).toList());
        System.out.println("skip(2).limit(3)     " + nums.stream().skip(2).limit(3).toList());
        List<Integer> sorted = nums.stream().sorted().toList();
        System.out.println("takeWhile(<5) sorted " + sorted.stream().takeWhile(n -> n < 5).toList());
        System.out.println("dropWhile(<5) sorted " + sorted.stream().dropWhile(n -> n < 5).toList());

        section("6. map vs flatMap");
        System.out.println("map(skills)          " + EMPS.stream().map(Employee::skills).toList());
        System.out.println("flatMap(skills)      " + EMPS.stream().flatMap(e -> e.skills().stream()).distinct().sorted().toList());
        List<String> sentences = List.of("hello world", "java streams rock");
        System.out.println("words of sentences   " + sentences.stream().flatMap(s -> Arrays.stream(s.split(" "))).toList());

        section("7. Terminal ops tour");
        System.out.println("count                " + nums.stream().count());
        System.out.println("sum (IntStream)      " + nums.stream().mapToInt(Integer::intValue).sum());
        System.out.println("average              " + nums.stream().mapToInt(Integer::intValue).average().orElse(0));
        System.out.println("max                  " + nums.stream().max(Integer::compare).orElseThrow());
        System.out.println("reduce sum           " + nums.stream().reduce(0, Integer::sum));
        System.out.println("reduce product       " + IntStream.rangeClosed(1, 5).reduce(1, (a, b) -> a * b) + "  (5!)");
        System.out.println("anyMatch(>8)         " + nums.stream().anyMatch(n -> n > 8));
        System.out.println("allMatch(>0)         " + nums.stream().allMatch(n -> n > 0));
        System.out.println("noneMatch(<0)        " + nums.stream().noneMatch(n -> n < 0));
        System.out.println("allMatch on EMPTY    " + Stream.<Integer>empty().allMatch(n -> n > 100) + "  (vacuous truth!)");
        System.out.println("findFirst            " + nums.stream().filter(n -> n > 5).findFirst());
        String[] arr = Stream.of("a", "b").toArray(String[]::new);
        System.out.println("toArray              " + Arrays.toString(arr));

        section("8. Collectors: grouping like SQL GROUP BY");
        Map<String, List<String>> namesByDept = EMPS.stream()
                .collect(groupingBy(Employee::dept, TreeMap::new, mapping(Employee::name, toList())));
        Map<String, Long> countByDept = EMPS.stream().collect(groupingBy(Employee::dept, TreeMap::new, counting()));
        Map<String, Double> avgByDept = EMPS.stream().collect(groupingBy(Employee::dept, TreeMap::new, averagingDouble(Employee::salary)));
        Map<String, Optional<Employee>> topByDept = EMPS.stream()
                .collect(groupingBy(Employee::dept, TreeMap::new, Collectors.maxBy(Comparator.comparingDouble(Employee::salary))));
        System.out.println("names by dept   " + namesByDept);
        System.out.println("count by dept   " + countByDept);
        System.out.println("avg salary      " + avgByDept);
        topByDept.forEach((d, e) -> System.out.println("top earner " + d + " -> " + e.map(Employee::name).orElse("-")));

        section("9. partitioningBy, toMap, joining, statistics");
        Map<Boolean, List<String>> richOrNot = EMPS.stream()
                .collect(partitioningBy(e -> e.salary() >= 60_000, mapping(Employee::name, toList())));
        System.out.println("salary >= 60k ? " + richOrNot);
        Map<Integer, String> byId = EMPS.stream().collect(Collectors.toMap(Employee::id, Employee::name));
        System.out.println("toMap id->name  " + byId);
        try {
            EMPS.stream().collect(Collectors.toMap(Employee::dept, Employee::name));
        } catch (IllegalStateException e) {
            System.out.println("toMap duplicate key -> " + e.getMessage());
        }
        Map<String, Double> maxSalaryByDept = EMPS.stream()
                .collect(Collectors.toMap(Employee::dept, Employee::salary, Math::max, TreeMap::new));
        System.out.println("toMap with merge fn (max) " + maxSalaryByDept);
        System.out.println("joining         " + EMPS.stream().map(Employee::name).collect(joining(", ", "[", "]")));
        DoubleSummaryStatistics stats = EMPS.stream().collect(Collectors.summarizingDouble(Employee::salary));
        System.out.printf("statistics      count=%d min=%.0f max=%.0f avg=%.2f sum=%.0f%n",
                stats.getCount(), stats.getMin(), stats.getMax(), stats.getAverage(), stats.getSum());
        var minMax = EMPS.stream().collect(Collectors.teeing(
                Collectors.minBy(Comparator.comparingDouble(Employee::salary)),
                Collectors.maxBy(Comparator.comparingDouble(Employee::salary)),
                (min, max) -> min.get().name() + " .. " + max.get().name()));
        System.out.println("teeing (min..max) " + minMax);

        section("10. Parallel streams");
        long primes = IntStream.rangeClosed(2, 200_000).parallel().filter(StreamsDemo::isPrime).count();
        System.out.println("primes below 200k (parallel) = " + primes);
        System.out.print("parallel forEach order is unpredictable: ");
        IntStream.range(0, 8).parallel().forEach(i -> System.out.print(i + " "));
        System.out.print("\nforEachOrdered keeps order:               ");
        IntStream.range(0, 8).parallel().forEachOrdered(i -> System.out.print(i + " "));
        System.out.println();
    }

    static boolean isPrime(int n) {
        if (n < 2) return false;
        for (int i = 2; (long) i * i <= n; i++) if (n % i == 0) return false;
        return true;
    }

    static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}
