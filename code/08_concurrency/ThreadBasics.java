import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/*
 * TOPIC: Creating threads (5 ways), start() vs run(), join, sleep, thread states, daemon, interrupt
 * RUN:   java code/08_concurrency/ThreadBasics.java
 */
public class ThreadBasics {

    public static void main(String[] args) throws Exception {
        section("1. Five ways to create a thread");
        Thread t1 = new MyThread();                                     // 1. extend Thread
        Thread t2 = new Thread(new MyTask(), "runnable-thread");        // 2. implement Runnable
        Thread t3 = new Thread(() -> say("lambda Runnable"), "lambda-thread");   // 3. lambda
        t1.start();
        t2.start();
        t3.start();
        t1.join();
        t2.join();
        t3.join();
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {  // 4. executor + Callable
            Callable<Integer> answer = () -> {
                say("Callable computing");
                return 6 * 7;
            };
            Future<Integer> f = pool.submit(answer);
            System.out.println("Callable returned " + f.get());
        }
        Thread v = Thread.ofVirtual().name("virtual-1").start(() -> say("virtual thread (Java 21), isVirtual=" + Thread.currentThread().isVirtual()));
        v.join();                                                       // 5. virtual thread

        section("2. start() vs run()");
        Thread runner = new Thread(() -> say("inside task"), "NEW-THREAD");
        runner.run();      // runs on main, like a normal method call
        runner.start();    // runs on NEW-THREAD
        runner.join();
        try {
            runner.start();
        } catch (IllegalThreadStateException e) {
            System.out.println("start() twice -> IllegalThreadStateException");
        }

        section("3. Thread states");
        Object lock = new Object();
        Thread sleeper = new Thread(() -> sleep(300), "sleeper");
        System.out.println("before start      : " + sleeper.getState());
        sleeper.start();
        Thread.sleep(50);
        System.out.println("while sleeping    : " + sleeper.getState());
        sleeper.join();
        System.out.println("after finishing   : " + sleeper.getState());

        Thread waiter = new Thread(() -> {
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException ignored) {
                }
            }
        }, "waiter");
        waiter.start();
        Thread.sleep(50);
        System.out.println("inside wait()     : " + waiter.getState());

        Thread blocked;
        synchronized (lock) {                     // main holds the lock...
            blocked = new Thread(() -> {
                synchronized (lock) { }           // ...so this thread can't enter
            }, "blocked");
            blocked.start();
            Thread.sleep(50);
            System.out.println("waiting for lock  : " + blocked.getState());
            lock.notifyAll();                     // release the waiter too
        }
        waiter.join();
        blocked.join();

        section("4. join(): wait for workers to finish");
        Thread[] workers = new Thread[3];
        for (int i = 0; i < workers.length; i++) {
            int id = i;
            workers[i] = new Thread(() -> {
                sleep(100 * (id + 1));
                say("worker " + id + " done");
            });
            workers[i].start();
        }
        for (Thread w : workers) w.join();
        System.out.println("all workers joined -> main continues");

        section("5. interrupt(): cooperative cancellation");
        Thread longTask = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Thread.sleep(100);            // throws InterruptedException when interrupted
                }
            } catch (InterruptedException e) {
                say("interrupted while sleeping -> cleaning up and exiting");
                Thread.currentThread().interrupt();   // restore the flag (best practice)
            }
        }, "long-task");
        longTask.start();
        Thread.sleep(250);
        longTask.interrupt();
        longTask.join();

        section("6. Daemon threads");
        Thread daemon = new Thread(() -> {
            while (true) sleep(1000);             // never ends...
        }, "daemon-heartbeat");
        daemon.setDaemon(true);                   // ...but the JVM will not wait for it
        daemon.start();
        System.out.println("daemon started; isDaemon=" + daemon.isDaemon() + ". main ends -> JVM exits anyway");
        System.out.println("main thread name=" + Thread.currentThread().getName()
                + ", priority=" + Thread.currentThread().getPriority());
    }

    static void say(String msg) {
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

class MyThread extends Thread {
    MyThread() {
        super("extends-thread");
    }

    @Override
    public void run() {
        ThreadBasics.say("extends Thread");
    }
}

class MyTask implements Runnable {
    @Override
    public void run() {
        ThreadBasics.say("implements Runnable");
    }
}
