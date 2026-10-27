import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

/*
 * TOPIC: Comparable (natural order, inside the class) vs Comparator (custom orders, outside)
 * RUN:   java code/06_collections/ComparableComparatorDemo.java
 */
public class ComparableComparatorDemo {

    public static void main(String[] args) {
        List<Employee> staff = new ArrayList<>(List.of(
                new Employee(3, "Ravi", "IT", 70_000),
                new Employee(1, "Asha", "HR", 55_000),
                new Employee(4, "Meera", "IT", 90_000),
                new Employee(2, "John", "HR", 55_000),
                new Employee(5, "Zara", "Sales", 62_000)));

        section("1. Comparable: natural order (by id) via Collections.sort");
        Collections.sort(staff);
        staff.forEach(e -> System.out.println("  " + e));

        section("2. Comparator: by salary descending");
        staff.sort(Comparator.comparingDouble(Employee::salary).reversed());
        staff.forEach(e -> System.out.println("  " + e));

        section("3. Comparator chain: dept asc, then salary desc, then name asc");
        staff.sort(Comparator.comparing(Employee::dept)
                .thenComparing(Employee::salary, Comparator.reverseOrder())
                .thenComparing(Employee::name));
        staff.forEach(e -> System.out.println("  " + e));

        section("4. Old style: anonymous Comparator class (what the lambda replaces)");
        staff.sort(new Comparator<Employee>() {
            @Override
            public int compare(Employee a, Employee b) {
                return a.name().compareTo(b.name());
            }
        });
        System.out.println("  by name: " + staff.stream().map(Employee::name).toList());

        section("5. Strings: natural, reverse, case-insensitive, by length");
        List<String> words = new ArrayList<>(List.of("banana", "Apple", "cherry", "fig", "date"));
        Collections.sort(words);
        System.out.println("  natural (uppercase first!): " + words);
        words.sort(String.CASE_INSENSITIVE_ORDER);
        System.out.println("  case-insensitive          : " + words);
        words.sort(Comparator.reverseOrder());
        System.out.println("  reverse                   : " + words);
        words.sort(Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder()));
        System.out.println("  by length, then alpha     : " + words);

        section("6. nulls in sorting");
        List<String> withNull = new ArrayList<>(Arrays.asList("b", null, "a"));
        withNull.sort(Comparator.nullsFirst(Comparator.naturalOrder()));
        System.out.println("  nullsFirst: " + withNull);

        section("7. TreeSet with a Comparator");
        TreeSet<Employee> bySalary = new TreeSet<>(Comparator.comparingDouble(Employee::salary)
                .thenComparingInt(Employee::id));   // tie-breaker so equal salaries are not 'duplicates'
        bySalary.addAll(staff);
        System.out.println("  lowest paid : " + bySalary.first());
        System.out.println("  highest paid: " + bySalary.last());

        section("8. TRAP: subtraction in compare() overflows");
        Comparator<Integer> broken = (a, b) -> a - b;
        Comparator<Integer> safe = Integer::compare;
        int big = Integer.MAX_VALUE, negative = -10;
        System.out.println("  broken compare(MAX, -10) = " + broken.compare(big, negative) + " (negative -> WRONG order)");
        System.out.println("  safe   compare(MAX, -10) = " + safe.compare(big, negative));
    }

    static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}

record Employee(int id, String name, String dept, double salary) implements Comparable<Employee> {
    @Override
    public int compareTo(Employee other) {
        return Integer.compare(this.id, other.id);   // natural order = by id
    }

    @Override
    public String toString() {
        return String.format("#%d %-6s %-6s %,.0f", id, name, dept, salary);
    }
}
