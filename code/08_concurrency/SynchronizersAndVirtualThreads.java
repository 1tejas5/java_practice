import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/*
 * TOPIC: CountDownLatch, CyclicBarrier, Semaphore, ConcurrentHashMap atomic updates, ThreadLocal,
 *        and Java 21 VIRTUAL THREADS vs platform threads
 * RUN:   java code/08_concurrency/SynchronizersAndVirtualThreads.java
 */
public class SynchronizersAndVirtualThreads {

    public static void main(String[] args) throws Exception {
        section("1. CountDownLatch: main waits for 3 services to start (wait for EVENTS)");
        CountDownLatch ready = new CountDownLatch(3);
        for (String svc : List.of("database", "cache", "messaging")) {
            new Thread(() -> {
                sleep((long) (Math.random() * 200));
                log(svc + " is up");
                ready.countDown();
            }).start();
        }
        ready.await();                                   // blocks until the count reaches 0
        System.out.println("all services up -> application starts");

        section("2. CyclicBarrier: 3 runners wait for EACH OTHER at every checkpoint (reusable)");
        CyclicBarrier barrier = new CyclicBarrier(3, () -> System.out.println("  --- everyone reached the checkpoint ---"));
        Thread[] runners = new Thread[3];
        for (int i = 0; i < 3; i++) {
            int id = i;
            runners[i] = new Thread(() -> {
                try {
                    for (int lap = 1; lap <= 2; lap++) {
                        sleep((long) (Math.random() * 150));
                        log("runner " + id + " finished lap " + lap);
                        barrier.await();
                    }
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
            });
            runners[i].start();
        }
        for (Thread r : runners) r.join();

        section("3. Semaphore: only 2 of 5 users can use the printer at once");
        Semaphore printers = new Semaphore(2);
        AtomicInteger inUse = new AtomicInteger();
        try (ExecutorService pool = Executors.newFixedThreadPool(5)) {
            for (int i = 1; i <= 5; i++) {
                int user = i;
                pool.submit(() -> {
                    try {
                        printers.acquire();
                        try {
                            log("user " + user + " printing (in use: " + inUse.incrementAndGet() + ")");
                            sleep(150);
                            inUse.decrementAndGet();
                        } finally {
                            printers.release();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
        }

        section("4. ConcurrentHashMap: atomic merge from many threads");
        Map<String, Integer> hits = new ConcurrentHashMap<>();
        try (ExecutorService pool = Executors.newFixedThreadPool(8)) {
            for (int i = 0; i < 10_000; i++) {
                String page = (i % 3 == 0) ? "/home" : (i % 3 == 1) ? "/cart" : "/pay";
                pool.submit(() -> hits.merge(page, 1, Integer::sum));   // atomic per key
            }
        }
        System.out.println("page hits = " + new java.util.TreeMap<>(hits) + " (sum = "
                + hits.values().stream().mapToInt(Integer::intValue).sum() + ")");

        section("5. ThreadLocal: each thread has its own copy");
        ThreadLocal<StringBuilder> perThread = ThreadLocal.withInitial(StringBuilder::new);
        Thread a = new Thread(() -> {
            perThread.get().append("A-data");
            log("sees '" + perThread.get() + "'");
            perThread.remove();                          // always clean up (important in pools)
        }, "thread-A");
        Thread b = new Thread(() -> {
            perThread.get().append("B-data");
            log("sees '" + perThread.get() + "'");
            perThread.remove();
        }, "thread-B");
        a.start();
        b.start();
        a.join();
        b.join();

        section("6. Virtual threads (Java 21): 2,000 blocking tasks of 100 ms each");
        int tasks = 2_000;
        long start = System.currentTimeMillis();
        try (ExecutorService platform = Executors.newFixedThreadPool(100)) {
            IntStream.range(0, tasks).forEach(i -> platform.submit(() -> sleep(100)));
        }
        long platformMs = System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        try (ExecutorService virtual = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, tasks).forEach(i -> virtual.submit(() -> sleep(100)));
        }
        long virtualMs = System.currentTimeMillis() - start;
        System.out.println("100 platform threads : " + platformMs + " ms  (2000 tasks / 100 threads = 20 rounds x 100 ms)");
        System.out.println("virtual threads      : " + virtualMs + " ms  (all 2000 sleep at the same time)");

        Thread vt = Thread.ofVirtual().name("my-virtual").unstarted(   // virtual threads are unnamed by default
                () -> log("isVirtual=" + Thread.currentThread().isVirtual()));
        vt.start();
        vt.join();
        System.out.println("Remember: virtual threads help BLOCKING (I/O) work, not CPU-bound work. Don't pool them.");
    }

    static void log(String msg) {
        System.out.println("  [" + Thread.currentThread().getName() + "] " + msg);
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
