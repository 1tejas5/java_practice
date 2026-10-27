import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantLock;

/*
 * TOPIC: Race condition (count++), fixes: synchronized, AtomicInteger, LongAdder, ReentrantLock;
 *        volatile visibility; static vs instance locks
 * RUN:   java code/08_concurrency/RaceConditionAndSync.java
 */
public class RaceConditionAndSync {

    static final int THREADS = 4;
    static final int INCREMENTS = 100_000;
    static final int EXPECTED = THREADS * INCREMENTS;

    public static void main(String[] args) throws Exception {
        section("1. The race: count++ from " + THREADS + " threads x " + INCREMENTS);
        UnsafeCounter unsafe = new UnsafeCounter();
        run(unsafe::increment);
        System.out.println("unsafe count     = " + unsafe.count + " (expected " + EXPECTED + ")  <- lost updates!");

        section("2. Fix: synchronized");
        SyncCounter sync = new SyncCounter();
        long t = run(sync::increment);
        System.out.println("synchronized     = " + sync.get() + "  (" + t + " ms)");

        section("3. Fix: AtomicInteger (lock-free CAS)");
        AtomicInteger atomic = new AtomicInteger();
        t = run(atomic::incrementAndGet);
        System.out.println("AtomicInteger    = " + atomic.get() + "  (" + t + " ms)");
        System.out.println("compareAndSet(" + EXPECTED + ", 0) -> " + atomic.compareAndSet(EXPECTED, 0) + ", now " + atomic.get());

        section("4. Fix: LongAdder (best under heavy contention)");
        LongAdder adder = new LongAdder();
        t = run(adder::increment);
        System.out.println("LongAdder        = " + adder.sum() + "  (" + t + " ms)");

        section("5. Fix: ReentrantLock");
        LockCounter lockCounter = new LockCounter();
        t = run(lockCounter::increment);
        System.out.println("ReentrantLock    = " + lockCounter.get() + "  (" + t + " ms)");

        section("6. volatile: visibility of a stop flag");
        VolatileWorker worker = new VolatileWorker();
        Thread th = new Thread(worker, "volatile-worker");
        th.start();
        Thread.sleep(100);
        worker.stop();                      // without volatile the loop might never see this
        th.join(2000);
        System.out.println("worker stopped? " + !th.isAlive() + " after " + worker.iterations + " iterations");

        section("7. Reentrancy: a synchronized method calling another synchronized method");
        new Reentrant().outer();

        section("8. Instance lock vs class lock are DIFFERENT locks");
        Locks locks = new Locks();
        Thread a = new Thread(locks::instanceMethod);
        Thread b = new Thread(Locks::staticMethod);
        long start = System.currentTimeMillis();
        a.start();
        b.start();
        a.join();
        b.join();
        System.out.println("both took ~" + (System.currentTimeMillis() - start) + " ms total (ran in parallel, ~200 not ~400)");
    }

    static long run(Runnable incrementOnce) throws InterruptedException {
        Thread[] threads = new Thread[THREADS];
        long start = System.currentTimeMillis();
        for (int i = 0; i < THREADS; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < INCREMENTS; j++) incrementOnce.run();
            });
            threads[i].start();
        }
        for (Thread th : threads) th.join();
        return System.currentTimeMillis() - start;
    }

    static void sleepQuietly(long ms) {
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

class UnsafeCounter {
    int count;

    void increment() {
        count++;                            // read -> add -> write: NOT atomic
    }
}

class SyncCounter {
    private int count;

    synchronized void increment() {         // locks 'this'
        count++;
    }

    synchronized int get() {
        return count;
    }
}

class LockCounter {
    private final ReentrantLock lock = new ReentrantLock();
    private int count;

    void increment() {
        lock.lock();
        try {
            count++;
        } finally {
            lock.unlock();                  // ALWAYS in finally
        }
    }

    int get() {
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
    }
}

class VolatileWorker implements Runnable {
    private volatile boolean running = true;
    volatile long iterations;

    @Override
    public void run() {
        while (running) iterations++;
    }

    void stop() {
        running = false;
    }
}

class Reentrant {
    synchronized void outer() {
        System.out.println("in outer(), holding the lock");
        inner();                            // same thread re-acquires the same lock: allowed
    }

    synchronized void inner() {
        System.out.println("in inner(), re-entered the same lock without deadlocking");
    }
}

class Locks {
    synchronized void instanceMethod() {    // lock = this object
        RaceConditionAndSync.sleepQuietly(200);
    }

    static synchronized void staticMethod() {   // lock = Locks.class
        RaceConditionAndSync.sleepQuietly(200);
    }
}
