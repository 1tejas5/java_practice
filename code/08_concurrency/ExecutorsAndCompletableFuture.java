import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/*
 * TOPIC: ExecutorService, Future, invokeAll, scheduling, ThreadPoolExecutor + rejection,
 *        submit vs execute, CompletableFuture pipelines (thenApply/Compose/Combine/allOf/exceptionally)
 * RUN:   java code/08_concurrency/ExecutorsAndCompletableFuture.java
 */
public class ExecutorsAndCompletableFuture {

    public static void main(String[] args) throws Exception {
        section("1. Fixed thread pool: 6 tasks, 3 threads (threads are REUSED)");
        ExecutorService pool = Executors.newFixedThreadPool(3);
        for (int i = 1; i <= 6; i++) {
            int id = i;
            pool.execute(() -> log("task " + id));
        }
        pool.shutdown();                                   // no new tasks, finish queued ones
        pool.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("isShutdown=" + pool.isShutdown() + ", isTerminated=" + pool.isTerminated());

        section("2. Future: submit a Callable, get() blocks, get(timeout) can time out");
        try (ExecutorService ex = Executors.newFixedThreadPool(2)) {
            Future<Integer> quick = ex.submit(() -> slowSquare(7, 100));
            Future<Integer> slow = ex.submit(() -> slowSquare(9, 2000));
            System.out.println("quick.get() = " + quick.get());
            try {
                slow.get(200, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                System.out.println("slow.get(200ms) -> TimeoutException; cancelling: " + slow.cancel(true));
            }
            System.out.println("slow.isCancelled() = " + slow.isCancelled());

            section("3. invokeAll / invokeAny");
            List<Callable<String>> jobs = List.of(
                    () -> { Thread.sleep(300); return "A"; },
                    () -> { Thread.sleep(100); return "B"; },
                    () -> { Thread.sleep(200); return "C"; });
            for (Future<String> f : ex.invokeAll(jobs)) System.out.print(f.get() + " ");
            System.out.println("<- invokeAll keeps submission order");
            System.out.println("invokeAny -> " + ex.invokeAny(jobs) + " (the fastest)");

            section("4. submit() captures exceptions, execute() does not");
            Future<?> failing = ex.submit(() -> { throw new IllegalStateException("boom inside task"); });
            try {
                failing.get();
            } catch (ExecutionException e) {
                System.out.println("submit -> ExecutionException, cause: " + e.getCause().getMessage());
            }
        }

        section("5. ScheduledExecutorService");
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        long start = System.currentTimeMillis();
        var heartbeat = scheduler.scheduleAtFixedRate(
                () -> log("heartbeat at +" + (System.currentTimeMillis() - start) + " ms"), 0, 100, TimeUnit.MILLISECONDS);
        scheduler.schedule(() -> log("one-shot after 250 ms"), 250, TimeUnit.MILLISECONDS);
        Thread.sleep(380);
        heartbeat.cancel(false);
        scheduler.shutdown();
        scheduler.awaitTermination(1, TimeUnit.SECONDS);

        section("6. ThreadPoolExecutor: core=1, max=2, queue=1 -> the 4th task is REJECTED");
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 2, 30, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1), new ThreadPoolExecutor.AbortPolicy());
        for (int i = 1; i <= 4; i++) {
            int id = i;
            try {
                tpe.execute(() -> { sleep(200); log("bounded task " + id); });
                System.out.println("submitted " + id + " -> poolSize=" + tpe.getPoolSize() + ", queued=" + tpe.getQueue().size());
            } catch (RejectedExecutionException e) {
                System.out.println("submitted " + id + " -> REJECTED (core busy, queue full, max reached)");
            }
        }
        tpe.shutdown();
        tpe.awaitTermination(2, TimeUnit.SECONDS);

        section("7. CompletableFuture pipeline");
        long t0 = System.currentTimeMillis();
        CompletableFuture<String> userF = CompletableFuture.supplyAsync(() -> fetch("user:asha", 300));
        CompletableFuture<Double> priceF = CompletableFuture.supplyAsync(() -> { sleep(300); return 999.0; });
        String summary = userF
                .thenApply(String::toUpperCase)                                          // map
                .thenCompose(u -> CompletableFuture.supplyAsync(() -> fetch(u + "/orders", 200)))   // flatMap (dependent)
                .thenCombine(priceF, (orders, price) -> orders + " total=" + price)       // combine independent
                .join();
        System.out.println("result: " + summary + "  in ~" + (System.currentTimeMillis() - t0) + " ms (user & price ran in parallel)");

        section("8. allOf / anyOf");
        List<CompletableFuture<String>> calls = List.of(
                CompletableFuture.supplyAsync(() -> fetch("service-A", 150)),
                CompletableFuture.supplyAsync(() -> fetch("service-B", 50)),
                CompletableFuture.supplyAsync(() -> fetch("service-C", 100)));
        System.out.println("anyOf -> " + CompletableFuture.anyOf(calls.toArray(new CompletableFuture[0])).join());
        CompletableFuture.allOf(calls.toArray(new CompletableFuture[0])).join();
        System.out.println("allOf -> " + calls.stream().map(CompletableFuture::join).toList());

        section("9. Error handling: exceptionally / handle");
        String recovered = CompletableFuture.supplyAsync(ExecutorsAndCompletableFuture::callPaymentService)
                .exceptionally(ex -> "fallback (" + ex.getCause().getMessage() + ")")
                .join();
        System.out.println("exceptionally -> " + recovered);
        String handled = CompletableFuture.supplyAsync(() -> 10 / 0)
                .handle((value, ex) -> ex == null ? "value " + value : "handled " + ex.getCause().getClass().getSimpleName())
                .join();
        System.out.println("handle        -> " + handled);
        CompletableFuture.supplyAsync(() -> "async data")
                .thenAccept(d -> log("thenAccept consumed: " + d))
                .thenRun(() -> log("thenRun: all done"))
                .join();
    }

    static int slowSquare(int n, long ms) throws InterruptedException {
        Thread.sleep(ms);
        return n * n;
    }

    static String callPaymentService() {
        throw new IllegalStateException("payment service down");
    }

    static String fetch(String what, long ms) {
        sleep(ms);
        return what;
    }

    static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    static void log(String msg) {
        System.out.println("  [" + Thread.currentThread().getName() + "] " + msg);
    }

    static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}
