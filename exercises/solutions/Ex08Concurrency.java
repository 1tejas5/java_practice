import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

/*
 * SOLUTIONS - Exercises section 8: Concurrency
 * RUN: java exercises/solutions/Ex08Concurrency.java
 */
public class Ex08Concurrency {

    public static void main(String[] args) throws Exception {
        // 8.1
        check("8.1 synchronized", 40_000, hammer(new SyncCounter()));
        check("8.1 AtomicInteger", 40_000, hammer(new AtomicCounter()));
        check("8.1 ReentrantLock", 40_000, hammer(new LockCounter()));

        // 8.2
        int[] data = IntStream.rangeClosed(1, 1_000_000).toArray();
        check("8.2 parallel sum", 500_000_500_000L, parallelSum(data, 4));

        // 8.3
        check("8.3 three threads in turn", "T1:1 T2:2 T3:3 T1:4 T2:5 T3:6 T1:7 T2:8 T3:9", roundRobin(3, 9));

        // 8.4
        check("8.4 combine", "Rs.999, 7 left", productInfo(false));
        check("8.4 fallback", "unavailable", productInfo(true));

        // 8.5
        check("8.5 producer-consumer sum", 5050, producerConsumerSum());
        summary();
    }

    // ============ 8.1 ============
    interface Counter {
        void increment();

        int get();
    }

    static class SyncCounter implements Counter {
        private int n;

        public synchronized void increment() { n++; }

        public synchronized int get() { return n; }
    }

    static class AtomicCounter implements Counter {
        private final AtomicInteger n = new AtomicInteger();

        public void increment() { n.incrementAndGet(); }

        public int get() { return n.get(); }
    }

    static class LockCounter implements Counter {
        private final ReentrantLock lock = new ReentrantLock();
        private int n;

        public void increment() {
            lock.lock();
            try {
                n++;
            } finally {
                lock.unlock();
            }
        }

        public int get() {
            lock.lock();
            try {
                return n;
            } finally {
                lock.unlock();
            }
        }
    }

    static int hammer(Counter c) throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        for (int t = 0; t < 4; t++) {
            Thread th = new Thread(() -> {
                for (int i = 0; i < 10_000; i++) c.increment();
            });
            threads.add(th);
            th.start();
        }
        for (Thread th : threads) th.join();
        return c.get();
    }

    // ============ 8.2 ============
    static long parallelSum(int[] data, int chunks) throws Exception {
        try (ExecutorService pool = Executors.newFixedThreadPool(chunks)) {
            List<Future<Long>> parts = new ArrayList<>();
            int size = (data.length + chunks - 1) / chunks;
            for (int c = 0; c < chunks; c++) {
                int from = c * size, to = Math.min(data.length, from + size);
                Callable<Long> task = () -> {
                    long s = 0;
                    for (int i = from; i < to; i++) s += data[i];
                    return s;
                };
                parts.add(pool.submit(task));
            }
            long total = 0;
            for (Future<Long> f : parts) total += f.get();          // combine partial results
            return total;
        }
    }

    // ============ 8.3 ============
    static String roundRobin(int threads, int max) throws InterruptedException {
        Object lock = new Object();
        int[] next = {1};
        StringBuffer out = new StringBuffer();                   // thread-safe buffer
        List<Thread> workers = new ArrayList<>();
        for (int t = 0; t < threads; t++) {
            int id = t;
            Thread th = new Thread(() -> {
                synchronized (lock) {
                    while (true) {
                        while (next[0] <= max && (next[0] - 1) % threads != id) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return;
                            }
                        }
                        if (next[0] > max) {
                            lock.notifyAll();
                            return;
                        }
                        if (!out.isEmpty()) out.append(' ');
                        out.append("T").append(id + 1).append(':').append(next[0]++);
                        lock.notifyAll();
                    }
                }
            });
            workers.add(th);
            th.start();
        }
        for (Thread th : workers) th.join();
        return out.toString();
    }

    // ============ 8.4 ============
    static String productInfo(boolean stockServiceDown) {
        CompletableFuture<Double> price = CompletableFuture.supplyAsync(() -> {
            sleep(200);
            return 999.0;
        });
        CompletableFuture<Integer> stock = CompletableFuture.supplyAsync(() -> {
            sleep(150);
            if (stockServiceDown) throw new IllegalStateException("stock service down");
            return 7;
        });
        return price.thenCombine(stock, (p, s) -> "Rs." + p.intValue() + ", " + s + " left")
                .exceptionally(ex -> "unavailable")
                .join();
    }

    // ============ 8.5 ============
    static int producerConsumerSum() throws InterruptedException {
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(10);
        int poison = -1;
        int[] sum = {0};
        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 100; i++) queue.put(i);
                queue.put(poison);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        Thread consumer = new Thread(() -> {
            try {
                for (int v = queue.take(); v != poison; v = queue.take()) sum[0] += v;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        producer.start();
        consumer.start();
        producer.join();
        consumer.join();                                          // join() makes sum[0] visible to main
        return sum[0];
    }

    static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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
