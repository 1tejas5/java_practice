import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/*
 * TOPIC: Map basics, iteration, Java 8 map methods (merge, computeIfAbsent...),
 *        HashMap vs LinkedHashMap vs TreeMap, TreeMap navigation, LRU cache.
 * RUN:   java code/06_collections/MapsDemo.java
 */
public class MapsDemo {

    public static void main(String[] args) {
        section("1. Basics");
        Map<String, Integer> stock = new HashMap<>();
        stock.put("apple", 10);
        Integer old = stock.put("apple", 15);         // same key -> replace, returns the old value
        stock.put("banana", 7);
        stock.put(null, 0);                           // HashMap allows ONE null key
        System.out.println("stock = " + stock + ", old value returned by put = " + old);
        System.out.println("get(kiwi) = " + stock.get("kiwi") + ", getOrDefault(kiwi, 0) = " + stock.getOrDefault("kiwi", 0));
        System.out.println("containsKey(apple) = " + stock.containsKey("apple") + ", containsValue(7) = " + stock.containsValue(7));
        stock.putIfAbsent("banana", 999);             // ignored, banana is present
        stock.remove(null);
        System.out.println("after putIfAbsent/remove = " + stock);

        section("2. Iterating a map");
        for (Map.Entry<String, Integer> e : stock.entrySet()) {
            System.out.println("  entrySet: " + e.getKey() + " -> " + e.getValue());
        }
        stock.forEach((k, v) -> System.out.println("  forEach : " + k + " -> " + v));
        System.out.println("  keySet=" + stock.keySet() + " values=" + stock.values());

        section("3. Java 8 power methods");
        String text = "the cat and the dog and the bird";
        Map<String, Integer> freq = new TreeMap<>();
        for (String w : text.split(" ")) freq.merge(w, 1, Integer::sum);
        System.out.println("word count with merge       = " + freq);

        Map<Integer, List<String>> byLength = new TreeMap<>();
        for (String w : List.of("hi", "cat", "dog", "to", "horse")) {
            byLength.computeIfAbsent(w.length(), k -> new ArrayList<>()).add(w);
        }
        System.out.println("group by length (multimap)  = " + byLength);

        freq.computeIfPresent("cat", (k, v) -> v + 100);
        freq.compute("zebra", (k, v) -> v == null ? 1 : v + 1);
        System.out.println("computeIfPresent/compute    = cat=" + freq.get("cat") + ", zebra=" + freq.get("zebra"));
        freq.replaceAll((k, v) -> v * 10);
        System.out.println("replaceAll x10              = " + freq);

        section("4. Ordering: HashMap vs LinkedHashMap vs TreeMap");
        String[] keys = {"mango", "apple", "kiwi", "banana"};
        Map<String, Integer> hm = new HashMap<>(), lhm = new LinkedHashMap<>(), tm = new TreeMap<>();
        for (String k : keys) {
            hm.put(k, k.length());
            lhm.put(k, k.length());
            tm.put(k, k.length());
        }
        System.out.println("inserted     : mango, apple, kiwi, banana");
        System.out.println("HashMap      : " + hm + "  (no order guarantee)");
        System.out.println("LinkedHashMap: " + lhm + "  (insertion order)");
        System.out.println("TreeMap      : " + tm + "  (sorted keys)");

        section("5. TreeMap navigation: grade slabs");
        TreeMap<Integer, String> grades = new TreeMap<>(Map.of(90, "A", 75, "B", 60, "C", 40, "D", 0, "F"));
        for (int marks : new int[]{95, 82, 61, 45, 12}) {
            System.out.println("  marks " + marks + " -> grade " + grades.floorEntry(marks).getValue());
        }
        System.out.println("firstKey=" + grades.firstKey() + " lastKey=" + grades.lastKey()
                + " headMap(60)=" + grades.headMap(60) + " descending=" + grades.descendingMap());

        section("6. LRU cache with LinkedHashMap(accessOrder = true)");
        LRUCache<String, String> cache = new LRUCache<>(3);
        cache.put("a", "A");
        cache.put("b", "B");
        cache.put("c", "C");
        cache.get("a");                             // a becomes most recently used
        cache.put("d", "D");                        // evicts b (least recently used)
        System.out.println("cache = " + cache + "  (b was evicted)");

        section("7. Sort a map by VALUE (classic interview question)");
        Map<String, Integer> scores = Map.of("Asha", 82, "Ravi", 95, "Meera", 78, "John", 88);
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(scores.entrySet());
        entries.sort(Map.Entry.<String, Integer>comparingByValue().reversed());
        Map<String, Integer> sorted = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : entries) sorted.put(e.getKey(), e.getValue());
        System.out.println("by value desc = " + sorted);

        section("8. Immutable maps");
        Map<String, Integer> fixed = Map.of("x", 1, "y", 2);
        try {
            fixed.put("z", 3);
        } catch (UnsupportedOperationException e) {
            System.out.println("Map.of(...).put -> UnsupportedOperationException");
        }
        try {
            Map.of("k", null);
        } catch (NullPointerException e) {
            System.out.println("Map.of with a null value -> NullPointerException");
        }
    }

    static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}

class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;

    LRUCache(int capacity) {
        super(capacity, 0.75f, true);                // true = access order
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }
}
