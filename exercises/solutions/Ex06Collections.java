import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;

/*
 * SOLUTIONS - Exercises section 6: Collections
 * RUN: java exercises/solutions/Ex06Collections.java
 */
public class Ex06Collections {

    public static void main(String[] args) {
        check("6.1 dedupe keep order", List.of(3, 1, 2), dedupe(List.of(3, 1, 3, 2, 1)));
        check("6.2 top 2 words", List.of("the", "and"), topK("the cat and the dog and the bird".split(" "), 2));
        check("6.3 group anagrams", List.of(List.of("eat", "tea", "ate"), List.of("tan", "nat"), List.of("bat")),
                groupAnagrams(List.of("eat", "tea", "tan", "ate", "nat", "bat")));
        check("6.4 two sum", new int[]{0, 1}, twoSum(new int[]{2, 7, 11, 15}, 9));
        check("6.4 two sum later", new int[]{1, 2}, twoSum(new int[]{3, 2, 4}, 6));
        check("6.5 valid brackets", true, isValid("{[()]}()"));
        check("6.5 invalid brackets", false, isValid("{[(])}"));
        check("6.5 unclosed", false, isValid("(("));
        check("6.6 merge sorted", List.of(1, 2, 3, 4, 7, 9), mergeSorted(List.of(1, 4, 7), List.of(2, 3, 9)));

        List<Employee> staff = new ArrayList<>(List.of(new Employee("Ravi", "IT", 65000), new Employee("Meera", "HR", 50000),
                new Employee("Zara", "IT", 95000), new Employee("Karan", "HR", 48000)));
        staff.sort(Comparator.comparing(Employee::dept).thenComparing(Employee::salary, Comparator.reverseOrder()));
        check("6.7 sort dept, salary desc", List.of("Meera", "Karan", "Zara", "Ravi"), staff.stream().map(Employee::name).toList());

        LruCache<String, Integer> cache = new LruCache<>(2);
        cache.put("a", 1);
        cache.put("b", 2);
        cache.get("a");                                   // a is now most recently used
        cache.put("c", 3);                                // evicts b
        check("6.8 LRU keys", List.of("a", "c"), List.copyOf(cache.keySet()));

        check("6.9 kth largest", 5, kthLargest(new int[]{3, 2, 1, 5, 6, 4}, 2));
        check("6.10 intersection", List.of(2, 2), intersect(List.of(1, 2, 2, 1), List.of(2, 2)));
        check("6.10 intersection 2", List.of(9, 4), intersect(List.of(4, 9, 5), List.of(9, 4, 9, 8, 4)));   // order of 2nd list
        summary();
    }

    // 6.1
    static <T> List<T> dedupe(List<T> items) {
        return new ArrayList<>(new LinkedHashSet<>(items));
    }

    // 6.2 - count with a map, then sort entries by count desc, word asc
    static List<String> topK(String[] words, int k) {
        Map<String, Integer> freq = new HashMap<>();
        for (String w : words) freq.merge(w, 1, Integer::sum);
        return freq.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .limit(k).map(Map.Entry::getKey).toList();
    }

    // 6.3 - key = sorted characters
    static List<List<String>> groupAnagrams(List<String> words) {
        Map<String, List<String>> groups = new LinkedHashMap<>();
        for (String w : words) {
            char[] chars = w.toCharArray();
            Arrays.sort(chars);
            groups.computeIfAbsent(new String(chars), k -> new ArrayList<>()).add(w);
        }
        return new ArrayList<>(groups.values());
    }

    // 6.4 - remember value -> index; look up the complement
    static int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> seen = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            Integer j = seen.get(target - nums[i]);
            if (j != null) return new int[]{j, i};
            seen.put(nums[i], i);
        }
        return new int[0];
    }

    // 6.5
    static boolean isValid(String s) {
        Deque<Character> stack = new ArrayDeque<>();
        Map<Character, Character> pairs = Map.of(')', '(', ']', '[', '}', '{');
        for (char c : s.toCharArray()) {
            if (pairs.containsValue(c)) stack.push(c);
            else if (pairs.containsKey(c) && (stack.isEmpty() || stack.pop() != pairs.get(c))) return false;
        }
        return stack.isEmpty();
    }

    // 6.6 - two pointers, O(n + m)
    static List<Integer> mergeSorted(List<Integer> a, List<Integer> b) {
        List<Integer> out = new ArrayList<>(a.size() + b.size());
        int i = 0, j = 0;
        while (i < a.size() && j < b.size()) out.add(a.get(i) <= b.get(j) ? a.get(i++) : b.get(j++));
        while (i < a.size()) out.add(a.get(i++));
        while (j < b.size()) out.add(b.get(j++));
        return out;
    }

    // 6.7
    record Employee(String name, String dept, double salary) { }

    // 6.8
    static class LruCache<K, V> extends LinkedHashMap<K, V> {
        private final int capacity;

        LruCache(int capacity) {
            super(16, 0.75f, true);
            this.capacity = capacity;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > capacity;
        }
    }

    // 6.9 - keep a min-heap of the k largest seen so far: O(n log k)
    static int kthLargest(int[] nums, int k) {
        PriorityQueue<Integer> heap = new PriorityQueue<>();
        for (int n : nums) {
            heap.offer(n);
            if (heap.size() > k) heap.poll();
        }
        return heap.peek();
    }

    // 6.10 - count the first list, consume counts with the second
    static List<Integer> intersect(List<Integer> a, List<Integer> b) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int x : a) counts.merge(x, 1, Integer::sum);
        List<Integer> out = new ArrayList<>();
        for (int x : b) {
            if (counts.getOrDefault(x, 0) > 0) {
                out.add(x);
                counts.merge(x, -1, Integer::sum);
            }
        }
        return out;
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
