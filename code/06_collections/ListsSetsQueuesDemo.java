import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

/*
 * TOPIC: List, Set, Queue/Deque, PriorityQueue - behaviour, ordering, and when to use which.
 * RUN:   java code/06_collections/ListsSetsQueuesDemo.java
 */
public class ListsSetsQueuesDemo {

    public static void main(String[] args) {
        section("1. List: ordered, indexed, duplicates allowed");
        List<String> list = new ArrayList<>();
        list.add("B");
        list.add("A");
        list.add("B");                       // duplicate OK
        list.add(0, "Z");                    // insert at index
        System.out.println("list            = " + list);
        System.out.println("get(1)          = " + list.get(1));
        System.out.println("indexOf(B)      = " + list.indexOf("B") + ", lastIndexOf(B) = " + list.lastIndexOf("B"));
        list.set(1, "X");
        list.remove("B");                    // removes the FIRST "B"
        System.out.println("after set/remove= " + list);
        System.out.println("subList(0,2)    = " + list.subList(0, 2) + " (a view)");
        System.out.println("Java 21: getFirst=" + list.getFirst() + ", getLast=" + list.getLast() + ", reversed=" + list.reversed());

        section("2. ArrayList vs LinkedList: get(i) cost");
        List<Integer> arrayList = new ArrayList<>();
        List<Integer> linkedList = new LinkedList<>();
        for (int i = 0; i < 50_000; i++) {
            arrayList.add(i);
            linkedList.add(i);
        }
        System.out.println("ArrayList  random get x20000: " + timeGets(arrayList) + " ms");
        System.out.println("LinkedList random get x20000: " + timeGets(linkedList) + " ms  <- O(n) per get");

        section("3. Set: unique elements - Hash vs Linked vs Tree");
        List<String> fruits = List.of("banana", "apple", "cherry", "apple", "date");
        Set<String> hash = new HashSet<>(fruits);
        Set<String> linked = new LinkedHashSet<>(fruits);
        Set<String> tree = new TreeSet<>(fruits);
        System.out.println("source        = " + fruits);
        System.out.println("HashSet       = " + hash + "    (Hurry: no order guarantee)");
        System.out.println("LinkedHashSet = " + linked + "    (Line: insertion order)");
        System.out.println("TreeSet       = " + tree + "    (Tidy: sorted)");
        System.out.println("add duplicate returns " + hash.add("apple"));

        section("4. TreeSet navigation");
        TreeSet<Integer> ts = new TreeSet<>(List.of(10, 20, 30, 40));
        System.out.println("set=" + ts + " first=" + ts.first() + " last=" + ts.last());
        System.out.println("floor(25)=" + ts.floor(25) + " ceiling(25)=" + ts.ceiling(25)
                + " lower(20)=" + ts.lower(20) + " higher(20)=" + ts.higher(20));
        System.out.println("headSet(30)=" + ts.headSet(30) + " tailSet(30)=" + ts.tailSet(30)
                + " subSet(15,35)=" + ts.subSet(15, 35) + " descending=" + ts.descendingSet());

        section("5. TRAP: TreeSet uses the comparator for uniqueness, not equals()");
        Set<String> byLength = new TreeSet<>(Comparator.comparingInt(String::length));
        byLength.addAll(List.of("cat", "dog", "horse", "ox"));
        System.out.println("TreeSet by length of [cat, dog, horse, ox] = " + byLength + "  <- 'dog' vanished!");

        section("6. Queue (FIFO) and Stack (LIFO) with ArrayDeque");
        Queue<String> queue = new ArrayDeque<>();
        queue.offer("first");
        queue.offer("second");
        queue.offer("third");
        System.out.println("queue peek=" + queue.peek() + ", poll=" + queue.poll() + ", now=" + queue);
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(1);
        stack.push(2);
        stack.push(3);
        System.out.println("stack=" + stack + " pop=" + stack.pop() + " peek=" + stack.peek());
        Queue<String> empty = new ArrayDeque<>();
        System.out.println("poll() on empty = " + empty.poll() + " (polite)");
        try {
            empty.remove();
        } catch (java.util.NoSuchElementException e) {
            System.out.println("remove() on empty -> NoSuchElementException (loud)");
        }
        System.out.println("balanced \"{[()]}\" ? " + isBalanced("{[()]}") + ", \"{[(])}\" ? " + isBalanced("{[(])}"));

        section("7. PriorityQueue: min-heap by default");
        PriorityQueue<Integer> minHeap = new PriorityQueue<>(List.of(5, 1, 8, 3, 9, 2));
        System.out.println("printed (heap layout, NOT sorted): " + minHeap);
        StringBuilder order = new StringBuilder();
        while (!minHeap.isEmpty()) order.append(minHeap.poll()).append(' ');
        System.out.println("poll order (sorted):              " + order);
        PriorityQueue<Task> tasks = new PriorityQueue<>(Comparator.comparingInt(Task::priority));
        tasks.add(new Task("deploy", 2));
        tasks.add(new Task("fix prod bug", 1));
        tasks.add(new Task("write docs", 5));
        while (!tasks.isEmpty()) System.out.println("  doing " + tasks.poll());
        System.out.println("top 3 largest of [4,9,1,7,3,8] = " + topK(List.of(4, 9, 1, 7, 3, 8), 3));
    }

    static long timeGets(List<Integer> list) {
        long start = System.nanoTime();
        long sum = 0;
        for (int i = 0; i < 20_000; i++) sum += list.get((i * 7919) % list.size());
        return (System.nanoTime() - start) / 1_000_000;
    }

    static boolean isBalanced(String s) {
        Deque<Character> st = new ArrayDeque<>();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '(', '[', '{' -> st.push(c);
                case ')' -> { if (st.isEmpty() || st.pop() != '(') return false; }
                case ']' -> { if (st.isEmpty() || st.pop() != '[') return false; }
                case '}' -> { if (st.isEmpty() || st.pop() != '{') return false; }
                default -> { }
            }
        }
        return st.isEmpty();
    }

    static List<Integer> topK(List<Integer> nums, int k) {
        PriorityQueue<Integer> heap = new PriorityQueue<>();      // min-heap of size k
        for (int n : nums) {
            heap.offer(n);
            if (heap.size() > k) heap.poll();                      // drop the smallest
        }
        List<Integer> result = new ArrayList<>(heap);
        result.sort(Comparator.reverseOrder());
        return result;
    }

    static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}

record Task(String name, int priority) { }
