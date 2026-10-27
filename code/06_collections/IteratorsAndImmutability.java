import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/*
 * TOPIC: Iterator & ListIterator, fail-fast vs fail-safe, safe removal,
 *        unmodifiable view vs immutable copy, Collections utilities, Java 21 sequenced collections
 * RUN:   java code/06_collections/IteratorsAndImmutability.java
 */
public class IteratorsAndImmutability {

    public static void main(String[] args) {
        section("1. Fail-fast: modifying during for-each");
        List<Integer> nums = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        try {
            for (Integer n : nums) if (n % 2 == 0) nums.remove(n);
        } catch (ConcurrentModificationException e) {
            System.out.println("ArrayList for-each + remove -> ConcurrentModificationException");
        }
        Map<String, Integer> map = new HashMap<>(Map.of("a", 1, "b", 2));
        try {
            for (String k : map.keySet()) map.put(k + "!", 0);
        } catch (ConcurrentModificationException e) {
            System.out.println("HashMap keySet loop + put   -> ConcurrentModificationException");
        }

        section("2. Three safe ways to remove while iterating");
        List<Integer> a = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        Iterator<Integer> it = a.iterator();
        while (it.hasNext()) if (it.next() % 2 == 0) it.remove();
        System.out.println("Iterator.remove() -> " + a);
        List<Integer> b = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        b.removeIf(n -> n % 2 == 0);
        System.out.println("removeIf()        -> " + b);
        List<Integer> c = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        for (int i = c.size() - 1; i >= 0; i--) if (c.get(i) % 2 == 0) c.remove(i);
        System.out.println("backward index loop -> " + c);

        section("3. Fail-safe / weakly consistent iterators");
        List<String> cow = new CopyOnWriteArrayList<>(List.of("x", "y"));
        for (String s : cow) cow.add(s + "2");                  // iterates a SNAPSHOT
        System.out.println("CopyOnWriteArrayList after adding during iteration: " + cow);
        Map<String, Integer> chm = new ConcurrentHashMap<>(Map.of("k1", 1, "k2", 2));
        for (String k : chm.keySet()) {
            chm.put(k, chm.get(k) * 10);                        // update while iterating
            chm.putIfAbsent("added", 99);                       // structural change while iterating
        }
        System.out.println("ConcurrentHashMap modified during iteration -> " + new java.util.TreeMap<>(chm) + " (no CME)");

        section("4. ListIterator: both directions + set/add");
        List<String> letters = new ArrayList<>(List.of("a", "b", "c"));
        ListIterator<String> li = letters.listIterator();
        while (li.hasNext()) {
            String s = li.next();
            li.set(s.toUpperCase());
            if (s.equals("b")) li.add("b+");
        }
        System.out.print("forward with set/add -> " + letters + " | backward: ");
        while (li.hasPrevious()) System.out.print(li.previous() + " ");
        System.out.println();

        section("5. Unmodifiable VIEW vs immutable COPY");
        List<String> source = new ArrayList<>(List.of("one", "two"));
        List<String> view = Collections.unmodifiableList(source);
        List<String> copy = List.copyOf(source);
        source.add("three");
        System.out.println("source=" + source);
        System.out.println("unmodifiable view sees the change: " + view);
        System.out.println("List.copyOf snapshot does not:     " + copy);
        try {
            view.add("x");
        } catch (UnsupportedOperationException e) {
            System.out.println("view.add -> UnsupportedOperationException");
        }
        List<Integer> fixedSize = Arrays.asList(1, 2, 3);
        fixedSize.set(0, 100);
        System.out.println("Arrays.asList set OK -> " + fixedSize + " (but add/remove throw)");
        try {
            List.of(1, null);
        } catch (NullPointerException e) {
            System.out.println("List.of(1, null) -> NullPointerException");
        }
        try {
            java.util.Set.of("a", "a");
        } catch (IllegalArgumentException e) {
            System.out.println("Set.of(\"a\",\"a\") -> IllegalArgumentException: " + e.getMessage());
        }

        section("6. Collections utility methods");
        List<Integer> data = new ArrayList<>(List.of(5, 3, 8, 3, 1));
        System.out.println("max=" + Collections.max(data) + " min=" + Collections.min(data)
                + " frequency(3)=" + Collections.frequency(data, 3));
        Collections.sort(data);
        System.out.println("sorted=" + data + " binarySearch(5)=" + Collections.binarySearch(data, 5));
        Collections.reverse(data);
        System.out.println("reversed=" + data);
        Collections.swap(data, 0, data.size() - 1);
        System.out.println("swap first/last=" + data);
        System.out.println("nCopies(3, \"ab\")=" + Collections.nCopies(3, "ab"));
        System.out.println("disjoint([1,2],[3,4])=" + Collections.disjoint(List.of(1, 2), List.of(3, 4)));

        section("7. Java 21 Sequenced collections");
        List<String> seq = new ArrayList<>(List.of("b", "c"));
        seq.addFirst("a");
        seq.addLast("d");
        System.out.println("list=" + seq + " first=" + seq.getFirst() + " last=" + seq.getLast() + " reversed=" + seq.reversed());
        LinkedHashMap<String, Integer> lhm = new LinkedHashMap<>();
        lhm.put("one", 1);
        lhm.put("two", 2);
        lhm.putFirst("zero", 0);
        System.out.println("LinkedHashMap=" + lhm + " firstEntry=" + lhm.firstEntry() + " lastEntry=" + lhm.lastEntry());
    }

    static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}
