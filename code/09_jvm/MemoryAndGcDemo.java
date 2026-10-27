import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/*
 * TOPIC: JVM memory in action - heap stats, stack depth & StackOverflowError, class loaders,
 *        GC eligibility, weak/soft references, WeakHashMap, memory pools & collectors, OOM
 * RUN:   java code/09_jvm/MemoryAndGcDemo.java
 * TRY:   java -Xss256k code/09_jvm/MemoryAndGcDemo.java   (smaller stack -> lower recursion depth)
 *        java -Xmx64m -XX:+UseSerialGC code/09_jvm/MemoryAndGcDemo.java
 */
public class MemoryAndGcDemo {

    static int depth = 0;

    public static void main(String[] args) {
        section("1. Heap numbers (Runtime)");
        printHeap("at start");
        List<int[]> garbage = new ArrayList<>();
        for (int i = 0; i < 200; i++) garbage.add(new int[50_000]);   // ~40 MB
        printHeap("after allocating ~40 MB");
        garbage = null;                                               // now unreachable -> eligible for GC
        System.gc();                                                  // only a REQUEST
        printHeap("after garbage = null + System.gc()");

        section("2. The stack: each call adds a frame -> StackOverflowError");
        try {
            recurse();
        } catch (StackOverflowError e) {
            System.out.println("StackOverflowError at depth ~" + depth + " (change with -Xss)");
        }

        section("3. Class loaders (parent delegation)");
        System.out.println("String.class loader             : " + String.class.getClassLoader() + "  <- null = Bootstrap");
        System.out.println("java.sql.Connection loader      : " + java.sql.Connection.class.getClassLoader().getName());
        ClassLoader mine = MemoryAndGcDemo.class.getClassLoader();
        System.out.println("this class loader               : " + mine.getClass().getSimpleName()
                + " (source-file mode uses an in-memory loader)");
        ClassLoader cl = mine;
        StringBuilder chain = new StringBuilder();
        while (cl != null) {
            chain.append(cl.getName() == null ? cl.getClass().getSimpleName() : cl.getName()).append(" -> ");
            cl = cl.getParent();
        }
        System.out.println("parent chain                    : " + chain + "Bootstrap");

        section("4. Weak vs Soft vs Strong references");
        Object strong = new Object();
        WeakReference<Object> weak = new WeakReference<>(new Object());       // only weakly reachable
        SoftReference<byte[]> soft = new SoftReference<>(new byte[1_000_000]); // kept unless memory is tight
        WeakReference<Object> weakButStrong = new WeakReference<>(strong);     // also strongly reachable
        System.gc();
        sleep(100);
        System.out.println("weak ref cleared after GC?                 " + (weak.get() == null) + "  (usually true)");
        System.out.println("weak ref to a STRONGLY reachable object?   cleared=" + (weakButStrong.get() == null));
        System.out.println("soft ref cleared (plenty of memory)?       " + (soft.get() == null));

        section("5. WeakHashMap: entries vanish when keys are no longer referenced");
        Map<Object, String> cache = new WeakHashMap<>();
        Object key1 = new Object();
        Object key2 = new Object();
        cache.put(key1, "session-1");
        cache.put(key2, "session-2");
        key2 = null;                                   // drop the only strong ref to key2
        System.gc();
        sleep(100);
        System.out.println("WeakHashMap size after GC = " + cache.size() + "  (key2's entry usually removed)");

        section("6. Island of isolation (cycle) is still collected");
        Node a = new Node();
        Node b = new Node();
        a.other = b;
        b.other = a;                                   // a <-> b cycle
        WeakReference<Node> watch = new WeakReference<>(a);
        a = null;
        b = null;                                      // no GC root reaches the cycle anymore
        System.gc();
        sleep(100);
        System.out.println("cycle collected? " + (watch.get() == null) + "  (tracing GC, not reference counting)");

        section("7. Memory pools and collectors in this JVM");
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            System.out.printf("  pool %-32s type=%s%n", pool.getName(), pool.getType());
        }
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            System.out.printf("  GC   %-32s collections=%d time=%d ms%n", gc.getName(), gc.getCollectionCount(), gc.getCollectionTime());
        }

        section("8. OutOfMemoryError (requesting more than the max heap)");
        long max = Runtime.getRuntime().maxMemory();
        if (max < 15L * 1024 * 1024 * 1024) {
            try {
                long[] huge = new long[(int) (max / 8 + 1_000)];    // bigger than the whole heap
                System.out.println("allocated?! " + huge.length);
            } catch (OutOfMemoryError e) {
                System.out.println("OutOfMemoryError: " + e.getMessage() + "  (max heap " + max / (1024 * 1024) + " MB)");
            }
        } else {
            System.out.println("skipped: max heap is very large on this machine");
        }
        System.out.println("JVM still alive after catching the OOM: the failed allocation never happened.");
    }

    static void recurse() {
        depth++;
        recurse();
    }

    static void printHeap(String label) {
        Runtime rt = Runtime.getRuntime();
        long mb = 1024 * 1024;
        long used = (rt.totalMemory() - rt.freeMemory()) / mb;
        System.out.printf("  %-36s used=%4d MB  committed=%4d MB  max=%5d MB%n", label, used, rt.totalMemory() / mb, rt.maxMemory() / mb);
    }

    static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}

class Node {
    Node other;
    byte[] payload = new byte[1024];
}
