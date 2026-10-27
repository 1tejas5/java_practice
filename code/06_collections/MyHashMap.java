import java.util.Objects;

/*
 * TOPIC: HashMap INTERNALS - build a simplified HashMap and WATCH it work:
 *        hashing, bucket index, collisions (chaining), equals() lookup, update, resize.
 * RUN:   java code/06_collections/MyHashMap.java
 *
 * The real java.util.HashMap works the same way, plus: tree bins (Red-Black tree when a bucket
 * reaches 8 nodes and the table has >= 64 buckets).
 */
public class MyHashMap<K, V> {

    static final int DEFAULT_CAPACITY = 16;
    static final float LOAD_FACTOR = 0.75f;

    static class Node<K, V> {                     // one entry in a bucket's linked list
        final int hash;
        final K key;
        V value;
        Node<K, V> next;

        Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    private Node<K, V>[] table;
    private int size;
    private boolean verbose = true;

    @SuppressWarnings("unchecked")
    public MyHashMap(int capacity) {
        table = (Node<K, V>[]) new Node[capacity];
    }

    // Same spreading function as java.util.HashMap: mixes high bits into low bits
    static int hash(Object key) {
        if (key == null) return 0;                // null key -> bucket 0
        int h = key.hashCode();
        return h ^ (h >>> 16);
    }

    private int indexFor(int hash, int length) {
        return (length - 1) & hash;               // fast modulo because length is a power of 2
    }

    public V put(K key, V value) {
        int hash = hash(key);
        int index = indexFor(hash, table.length);
        log("put(" + key + ", " + value + ") hash=" + hash + " -> bucket " + index);

        for (Node<K, V> n = table[index]; n != null; n = n.next) {
            if (n.hash == hash && Objects.equals(n.key, key)) {   // hashCode found the bucket, equals finds the key
                V old = n.value;
                n.value = value;
                log("   key exists -> value REPLACED (" + old + " -> " + value + ")");
                return old;
            }
        }
        if (table[index] != null) log("   COLLISION: bucket " + index + " already has entries, chaining");
        Node<K, V> newNode = new Node<>(hash, key, value, null);
        if (table[index] == null) {
            table[index] = newNode;
        } else {                                  // append at the tail (Java 8 style)
            Node<K, V> tail = table[index];
            while (tail.next != null) tail = tail.next;
            tail.next = newNode;
        }
        if (++size > table.length * LOAD_FACTOR) resize();
        return null;
    }

    public V get(K key) {
        int hash = hash(key);
        for (Node<K, V> n = table[indexFor(hash, table.length)]; n != null; n = n.next) {
            if (n.hash == hash && Objects.equals(n.key, key)) return n.value;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        Node<K, V>[] old = table;
        table = (Node<K, V>[]) new Node[old.length * 2];
        log("   RESIZE: size " + size + " > " + (int) (old.length * LOAD_FACTOR)
                + " (capacity " + old.length + " x 0.75) -> new capacity " + table.length);
        boolean wasVerbose = verbose;
        verbose = false;
        size = 0;
        for (Node<K, V> head : old) {
            for (Node<K, V> n = head; n != null; n = n.next) put(n.key, n.value);   // rehash everything
        }
        verbose = wasVerbose;
    }

    public void printBuckets() {
        System.out.println("   table (capacity " + table.length + ", size " + size + "):");
        for (int i = 0; i < table.length; i++) {
            if (table[i] == null) continue;
            StringBuilder sb = new StringBuilder("   [" + i + "] ");
            for (Node<K, V> n = table[i]; n != null; n = n.next) {
                sb.append(n.key).append('=').append(n.value);
                if (n.next != null) sb.append(" -> ");
            }
            System.out.println(sb);
        }
    }

    private void log(String msg) {
        if (verbose) System.out.println(msg);
    }

    // ---------------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("=== 1. Normal puts, an update and a null key ===");
        MyHashMap<String, Integer> map = new MyHashMap<>(DEFAULT_CAPACITY);
        map.put("Asha", 90);
        map.put("Ravi", 70);
        map.put("Asha", 95);          // update
        map.put(null, 0);             // null key -> bucket 0
        map.printBuckets();
        System.out.println("get(Asha)=" + map.get("Asha") + ", get(null)=" + map.get(null) + ", get(Nobody)=" + map.get("Nobody"));

        System.out.println("\n=== 2. Forced collisions: a key whose hashCode() is ALWAYS 42 ===");
        MyHashMap<BadKey, String> bad = new MyHashMap<>(DEFAULT_CAPACITY);
        bad.put(new BadKey("x"), "X");
        bad.put(new BadKey("y"), "Y");
        bad.put(new BadKey("z"), "Z");
        bad.printBuckets();
        System.out.println("get(y) still works (equals decides): " + bad.get(new BadKey("y")));

        System.out.println("\n=== 3. Resize: capacity 4, threshold 4 x 0.75 = 3 -> resizes when size exceeds 3 ===");
        MyHashMap<Integer, String> small = new MyHashMap<>(4);
        for (int i = 1; i <= 7; i++) small.put(i * 10, "v" + i);
        small.printBuckets();
    }
}

class BadKey {
    private final String id;

    BadKey(String id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return 42;                                // terrible but LEGAL - every key collides
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof BadKey b && b.id.equals(id);
    }

    @Override
    public String toString() {
        return "BadKey(" + id + ")";
    }
}
