import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/*
 * TOPIC: Deadlock - create one (safely), DETECT it with ThreadMXBean, then FIX it two ways:
 *        1) consistent lock ordering   2) tryLock with timeout
 * RUN:   java code/08_concurrency/DeadlockDemo.java
 *
 * 4 conditions: "My House Never Collapses" - Mutual exclusion, Hold & wait, No preemption, Circular wait
 */
public class DeadlockDemo {

    public static void main(String[] args) throws Exception {
        section("1. Creating a deadlock (daemon threads, so the JVM can still exit)");
        Object lockA = new Object();
        Object lockB = new Object();
        Thread t1 = new Thread(() -> {
            synchronized (lockA) {
                log("holds A, wants B");
                sleep(100);
                synchronized (lockB) {
                    log("got both (never printed)");
                }
            }
        }, "Thread-1");
        Thread t2 = new Thread(() -> {
            synchronized (lockB) {
                log("holds B, wants A");
                sleep(100);
                synchronized (lockA) {
                    log("got both (never printed)");
                }
            }
        }, "Thread-2");
        t1.setDaemon(true);
        t2.setDaemon(true);
        t1.start();
        t2.start();
        Thread.sleep(500);

        section("2. Detecting it (what jstack does)");
        ThreadMXBean mx = ManagementFactory.getThreadMXBean();
        long[] ids = mx.findDeadlockedThreads();
        if (ids != null) {
            for (ThreadInfo info : mx.getThreadInfo(ids)) {
                System.out.println("DEADLOCKED: " + info.getThreadName() + " is " + info.getThreadState()
                        + " waiting for a lock held by " + info.getLockOwnerName());
            }
        }

        section("3. Fix #1: lock ordering (always lock the lower id first)");
        Account acc1 = new Account(1, 1000);
        Account acc2 = new Account(2, 1000);
        Thread x = new Thread(() -> { for (int i = 0; i < 1000; i++) Account.transferOrdered(acc1, acc2, 1); }, "x");
        Thread y = new Thread(() -> { for (int i = 0; i < 1000; i++) Account.transferOrdered(acc2, acc1, 1); }, "y");
        x.start();
        y.start();
        x.join();
        y.join();
        System.out.println("2000 opposite transfers done, no deadlock. Total money = " + (acc1.balance + acc2.balance));

        section("4. Fix #2: tryLock with timeout (back off and retry)");
        LockAccount la = new LockAccount("L1", 500);
        LockAccount lb = new LockAccount("L2", 500);
        Thread p = new Thread(() -> { for (int i = 0; i < 200; i++) LockAccount.transfer(la, lb, 1); }, "p");
        Thread q = new Thread(() -> { for (int i = 0; i < 200; i++) LockAccount.transfer(lb, la, 1); }, "q");
        p.start();
        q.start();
        p.join();
        q.join();
        System.out.println("400 transfers done with tryLock. Total = " + (la.balance + lb.balance)
                + ", retries needed = " + LockAccount.retries);
        System.out.println("\n(The deadlocked daemon threads are still stuck - the JVM exits because they are daemons.)");
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

class Account {
    final int id;
    int balance;

    Account(int id, int balance) {
        this.id = id;
        this.balance = balance;
    }

    static void transferOrdered(Account from, Account to, int amount) {
        Account first = from.id < to.id ? from : to;      // global order breaks the CIRCULAR WAIT
        Account second = from.id < to.id ? to : from;
        synchronized (first) {
            synchronized (second) {
                from.balance -= amount;
                to.balance += amount;
            }
        }
    }
}

class LockAccount {
    static volatile int retries;
    final String name;
    final ReentrantLock lock = new ReentrantLock();
    int balance;

    LockAccount(String name, int balance) {
        this.name = name;
        this.balance = balance;
    }

    static void transfer(LockAccount from, LockAccount to, int amount) {
        while (true) {
            try {
                if (from.lock.tryLock(10, TimeUnit.MILLISECONDS)) {
                    try {
                        if (to.lock.tryLock(10, TimeUnit.MILLISECONDS)) {
                            try {
                                from.balance -= amount;
                                to.balance += amount;
                                return;
                            } finally {
                                to.lock.unlock();
                            }
                        }
                    } finally {
                        from.lock.unlock();               // release what we hold -> breaks HOLD & WAIT
                    }
                }
                retries++;
                Thread.sleep((long) (Math.random() * 3)); // random back-off avoids livelock
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
