import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/*
 * SOLUTIONS - Exercises section 7: Streams
 * RUN: java exercises/solutions/Ex07Streams.java
 */
public class Ex07Streams {

    record Employee(String name, String dept, double salary, int age, String city) { }

    static final List<Employee> EMPS = List.of(
            new Employee("Asha", "IT", 85000, 29, "Pune"),
            new Employee("Ravi", "IT", 65000, 35, "Mumbai"),
            new Employee("Meera", "HR", 50000, 41, "Pune"),
            new Employee("John", "Sales", 55000, 31, "Delhi"),
            new Employee("Zara", "IT", 95000, 38, "Pune"),
            new Employee("Karan", "HR", 48000, 26, "Mumbai"),
            new Employee("Asha", "Sales", 60000, 33, "Delhi"));

    public static void main(String[] args) {
        // 7.1
        List<String> olderThan30 = EMPS.stream().filter(e -> e.age() > 30).map(Employee::name).sorted().toList();
        check("7.1 older than 30", List.of("Asha", "John", "Meera", "Ravi", "Zara"), olderThan30);

        // 7.2
        Map<String, Double> avgByDept = EMPS.stream()
                .collect(Collectors.groupingBy(Employee::dept, TreeMap::new, Collectors.averagingDouble(Employee::salary)));
        check("7.2 avg by dept", "{HR=49000.0, IT=81666.67, Sales=57500.0}", format(avgByDept));

        // 7.3
        Map<String, String> topByDept = EMPS.stream().collect(Collectors.groupingBy(Employee::dept, TreeMap::new,
                Collectors.collectingAndThen(Collectors.maxBy(Comparator.comparingDouble(Employee::salary)),
                        o -> o.map(Employee::name).orElse("-"))));
        check("7.3 top earner per dept", Map.of("HR", "Meera", "IT", "Zara", "Sales", "Asha"), topByDept);

        // 7.4
        List<String> cityCounts = EMPS.stream()
                .collect(Collectors.groupingBy(Employee::city, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .map(e -> e.getKey() + "=" + e.getValue())
                .toList();
        check("7.4 count per city", List.of("Pune=3", "Delhi=2", "Mumbai=2"), cityCounts);

        // 7.5
        double second = EMPS.stream().map(Employee::salary).distinct()
                .sorted(Comparator.reverseOrder()).skip(1).findFirst().orElseThrow();
        check("7.5 second highest", 85000.0, second);

        // 7.6
        Map<Boolean, List<String>> partition = EMPS.stream().collect(Collectors.partitioningBy(
                e -> e.salary() >= 60_000, Collectors.mapping(Employee::name, Collectors.toList())));
        check("7.6 >= 60k", List.of("Asha", "Ravi", "Zara", "Asha"), partition.get(true));
        check("7.6 < 60k", List.of("Meera", "John", "Karan"), partition.get(false));

        // 7.7
        String depts = EMPS.stream().map(Employee::dept).distinct().sorted().collect(Collectors.joining(", "));
        check("7.7 departments", "HR, IT, Sales", depts);

        // 7.8
        List<String> duplicates = EMPS.stream()
                .collect(Collectors.groupingBy(Employee::name, Collectors.counting()))
                .entrySet().stream().filter(e -> e.getValue() > 1).map(Map.Entry::getKey).sorted().toList();
        check("7.8 duplicate names", List.of("Asha"), duplicates);

        // 7.9
        String richestCity = EMPS.stream()
                .collect(Collectors.groupingBy(Employee::city, Collectors.summingDouble(Employee::salary)))
                .entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElseThrow();
        check("7.9 richest city", "Pune", richestCity);

        // 7.10 - sort first, then group: groupingBy keeps encounter order inside each list
        Map<String, List<String>> namesBySalary = EMPS.stream()
                .sorted(Comparator.comparingDouble(Employee::salary).reversed())
                .collect(Collectors.groupingBy(Employee::dept, TreeMap::new, Collectors.mapping(Employee::name, Collectors.toList())));
        check("7.10 dept -> names by salary", Map.of("HR", List.of("Meera", "Karan"), "IT", List.of("Zara", "Asha", "Ravi"),
                "Sales", List.of("Asha", "John")), namesBySalary);

        // bonus: index by name, keeping the higher salary for duplicate names
        Map<String, Double> bestSalaryByName = EMPS.stream()
                .collect(Collectors.toMap(Employee::name, Employee::salary, Math::max, TreeMap::new));
        check("bonus toMap merge", 85000.0, bestSalaryByName.get("Asha"));
        check("bonus identity", 7, EMPS.stream().collect(Collectors.toMap(Function.identity(), Employee::age)).size());
        summary();
    }

    static String format(Map<String, Double> m) {
        return m.entrySet().stream()
                .map(e -> e.getKey() + "=" + (Math.round(e.getValue() * 100) / 100.0))
                .collect(Collectors.joining(", ", "{", "}"));
    }

    // ---------------- test helper ----------------
    private static int passed, failed;

    static void check(String name, Object expected, Object actual) {
        boolean ok = Objects.deepEquals(expected, actual);
        if (ok) passed++;
        else failed++;
        System.out.printf("%s %-30s %s%n", ok ? "PASS" : "FAIL", name, ok ? "" : "expected=" + expected + " actual=" + actual);
    }

    static void summary() {
        System.out.printf("%n%d passed, %d failed%n", passed, failed);
    }
}
