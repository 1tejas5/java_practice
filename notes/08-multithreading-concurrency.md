# 08 · Multithreading & Concurrency

> ⏱️ **Revise in:** 90 min · 🎯 **Level:** Advanced · ▶️ **Code:** [code/08_concurrency](../code/08_concurrency) · 🏋️ **Practice:** [Exercises → Concurrency](../exercises/README.md#8-concurrency)

## 🧠 Mind Map

```
08 CONCURRENCY
├── Basics: process (own memory) vs thread (shares heap, own stack) · concurrency vs parallelism
├── Create: extends Thread · implements Runnable ✅ · Callable<V> (returns + throws)
│           · ExecutorService ✅✅ · Virtual threads (21) · start() ≠ run()
├── Lifecycle: NEW → RUNNABLE ⇄ BLOCKED | WAITING | TIMED_WAITING → TERMINATED
├── Thread methods: sleep · join · interrupt · yield · setDaemon · setPriority · currentThread
├── Problems
│   ├── Race condition (count++ isn't atomic) → synchronized / Atomic / Lock
│   ├── Visibility (stale CPU cache) → volatile / synchronized (happens-before)
│   ├── Deadlock ("My House Never Collapses" = 4 conditions) → lock ordering, tryLock
│   └── Livelock · Starvation
├── Tools
│   ├── synchronized (method / block / static) = intrinsic lock, reentrant
│   ├── volatile = visibility only, NOT atomicity
│   ├── Atomic* = lock-free CAS (AtomicInteger, LongAdder, AtomicReference)
│   ├── Lock: ReentrantLock (tryLock, fair, Condition) · ReadWriteLock · StampedLock
│   ├── wait/notify/notifyAll (inside synchronized, in a while loop)
│   └── ThreadLocal (per-thread copy)
├── Executor framework
│   ├── ExecutorService: submit · invokeAll · shutdown · awaitTermination
│   ├── Executors.newFixedThreadPool · Cached · Single · Scheduled · VirtualThreadPerTask
│   ├── ThreadPoolExecutor(core, max, keepAlive, queue, factory, rejectionPolicy)
│   ├── Future: get (blocks) · cancel · isDone
│   └── CompletableFuture: supplyAsync → thenApply → thenCompose / thenCombine → exceptionally → join
├── Concurrent collections: ConcurrentHashMap · CopyOnWriteArrayList · BlockingQueue (put/take)
├── Synchronizers: CountDownLatch (wait for N) · CyclicBarrier (N wait for each other) · Semaphore (N permits)
└── Virtual threads (Java 21): cheap, millions, great for blocking I/O · don't pool · beware pinning
```

## ⚡ 30-Second Summary

- **Threads share the heap**, so shared **mutable** data needs protection.
- `count++` = read + add + write → **race condition**. Fix it with `synchronized`, `AtomicInteger` or a `Lock`.
- `volatile` guarantees **visibility**, not **atomicity**.
- Prefer **ExecutorService** over raw threads, and **CompletableFuture** for async pipelines.
- Deadlock needs **all 4** Coffman conditions. Break one (usually with **consistent lock ordering**).
- Java 21 **virtual threads** make the blocking thread-per-task style scale to millions of tasks.

---

## 1. Process vs Thread · Concurrency vs Parallelism

| Process | Thread |
|---|---|
| independent program with its **own memory** | a lightweight unit **inside** a process |
| heavy to create, communicates via IPC | cheap to create, communicates via **shared heap** |
| a crash doesn't affect other processes | an uncaught exception kills only that thread, but shared state may be corrupted |

```
 JVM PROCESS
 ┌───────────────────────── HEAP (shared: objects, static data) ─────────────────────────┐
 │                                                                                         │
 └─────────────────────────────────────────────────────────────────────────────────────────┘
   Thread-1            Thread-2            Thread-3        ← each has its OWN
   [stack][PC]         [stack][PC]         [stack][PC]       stack (locals) + program counter
```

**Concurrency** = *dealing* with many things at once (interleaving, even on 1 core).
**Parallelism** = *doing* many things at once (simultaneously, on multiple cores).

---

## 2. Creating Threads

```java
// 1. Extend Thread (least flexible: uses up your single inheritance)
class MyThread extends Thread {
    @Override public void run() { System.out.println("Running in " + getName()); }
}
new MyThread().start();

// 2. Implement Runnable ✅ (separates the TASK from the THREAD)
Runnable task = () -> System.out.println("Running in " + Thread.currentThread().getName());
new Thread(task, "worker-1").start();

// 3. Callable: returns a value and can throw checked exceptions
Callable<Integer> calc = () -> 6 * 7;

// 4. ExecutorService ✅✅ (thread pool: reuse threads, manage the lifecycle)
try (ExecutorService pool = Executors.newFixedThreadPool(4)) {   // AutoCloseable since Java 19
    Future<Integer> f = pool.submit(calc);
    System.out.println(f.get());   // 42 (blocks until done)
}

// 5. Virtual thread (Java 21)
Thread.startVirtualThread(() -> System.out.println("I'm virtual!"));
Thread vt = Thread.ofVirtual().name("v-1").start(task);
```

| `Runnable` | `Callable<V>` |
|---|---|
| `void run()` | `V call() throws Exception` |
| no result | returns a result (via `Future`) |
| can't throw checked exceptions | can |
| `new Thread(r)` or an executor | executors only |

> ⚠️ **`start()` vs `run()`:** `start()` creates a **new thread**, which then calls `run()`. Calling `run()` directly
> just executes it **on the current thread**, like a normal method call. Calling `start()` twice →
> `IllegalThreadStateException`.

---

## 3. Thread Lifecycle (States)

```
                 start()
      NEW ─────────────────► RUNNABLE ◄──────────────────────────────┐
                              (ready ⇄ running: the scheduler decides) │
                 ┌──────────────┼──────────────────┐                 │
   waiting for a │              │ wait() / join()  │ sleep(ms) /     │ lock acquired /
   monitor lock  ▼              ▼ / park()         ▼ wait(ms)...     │ notify / time up /
              BLOCKED        WAITING          TIMED_WAITING ─────────┘ interrupt
                              │
                 run() ends   ▼
              ─────────────► TERMINATED
```

> 🧩 "**New Runners Block, Wait, Time-wait, Terminate.**"

| State | When |
|---|---|
| `NEW` | created, `start()` not yet called |
| `RUNNABLE` | running or ready to run |
| `BLOCKED` | waiting to **acquire a monitor lock** (synchronized) |
| `WAITING` | waiting indefinitely: `wait()`, `join()`, `LockSupport.park()` |
| `TIMED_WAITING` | `sleep(ms)`, `wait(ms)`, `join(ms)` |
| `TERMINATED` | `run()` finished (normally or with an exception) |

### Important thread methods

| Method | Does |
|---|---|
| `Thread.sleep(ms)` | pauses the current thread; **keeps its locks** |
| `t.join()` | the current thread waits until `t` finishes |
| `t.interrupt()` | sets the interrupt flag; a thread blocked in sleep/wait/join gets an `InterruptedException` |
| `Thread.interrupted()` vs `t.isInterrupted()` | static: checks **and clears** the flag · instance: just checks |
| `Thread.yield()` | a hint to let other threads run (rarely used) |
| `t.setDaemon(true)` | background thread; the JVM exits when only daemons remain (must be set before `start`) |
| `t.setPriority(1..10)` | a hint (default 5); platform-dependent |

### sleep() vs wait() ⭐

| | `sleep()` | `wait()` |
|---|---|---|
| Class | `Thread` (static) | `Object` |
| Releases the lock? | ❌ **no** | ✅ **yes** |
| Needs synchronized? | no | **yes** (else `IllegalMonitorStateException`) |
| Wakes up when | time elapses / interrupt | `notify()`/`notifyAll()`/timeout/interrupt |
| Purpose | pause execution | inter-thread communication |

---

## 4. Race Conditions & `synchronized`

```java
class Counter {
    private int count = 0;
    void increment() { count++; }      // ❌ NOT atomic: READ count → ADD 1 → WRITE count
}
// Two threads x 10,000 increments → often ends at less than 20,000 (lost updates)
```

```
 Thread A: read count (5)          add → 6         write 6
 Thread B:        read count (5)          add → 6         write 6     ← one update LOST
```

### Fix 1: `synchronized` (intrinsic lock / monitor)

```java
synchronized void increment() { count++; }          // locks `this`

void increment() {
    synchronized (this) { count++; }                // block: lock only the critical section
}

static synchronized void log() { }                  // locks Counter.class (the class object)

private final Object lock = new Object();           // ✅ private lock object: outsiders can't interfere
void safer() { synchronized (lock) { count++; } }
```

- Only **one thread at a time** can hold an object's monitor. Others go **BLOCKED**.
- **Reentrant:** a thread holding a lock can re-acquire it (a synchronized method can call another one).
- Gives **atomicity + visibility** (happens-before: an unlock happens-before the next lock on the same monitor).
- Instance-level and class-level locks are **different** locks.

### Fix 2: Atomic classes (lock-free, CAS)

```java
AtomicInteger count = new AtomicInteger();
count.incrementAndGet();             // ++count, atomically
count.getAndIncrement();             // count++
count.compareAndSet(5, 6);           // CAS: "if it's 5, set 6", in a single hardware instruction
count.updateAndGet(x -> x * 2);
LongAdder adder = new LongAdder();   // even faster under HIGH contention (striped cells)
```

> 🔑 **CAS (Compare-And-Swap):** read the value, compute the new one, then atomically "set it if it's still the old
> value", retrying otherwise. No locks, no blocking.

### `volatile`: visibility, not atomicity

```java
class Worker implements Runnable {
    private volatile boolean running = true;   // without volatile, the thread may never see the change
    public void run() { while (running) { /* work */ } }
    public void stop() { running = false; }
}
```

- Every read/write goes to **main memory** (not a stale CPU cache), and it prevents instruction reordering.
- ✅ Good for **flags** and **publishing** immutable objects (double-checked locking).
- ❌ **Not enough for `count++`** (still read-modify-write).

> 🧩 "**volatile = see · synchronized = see + lock**"

| | `volatile` | `synchronized` | `AtomicInteger` |
|---|---|---|---|
| Visibility | ✅ | ✅ | ✅ |
| Atomic compound ops (`++`) | ❌ | ✅ | ✅ |
| Blocking | no | yes | no (CAS spin) |
| Use for | flags, status | complex critical sections | counters, single-variable updates |

### Happens-before (Java Memory Model) in 5 rules
1. **Program order:** each action in a thread happens-before the thread's later actions.
2. **Monitor:** an unlock happens-before every later lock of the same monitor.
3. **Volatile:** a write happens-before every later read of the same variable.
4. **Thread start:** `t.start()` happens-before anything `t` does.
5. **Thread join:** everything `t` does happens-before `t.join()` returns.

---

## 5. wait / notify: Inter-thread Communication

```java
class Buffer {
    private final Queue<Integer> q = new LinkedList<>();
    private final int capacity = 5;

    public synchronized void produce(int item) throws InterruptedException {
        while (q.size() == capacity) wait();     // ✅ WHILE, not if (spurious wakeups / multiple consumers)
        q.add(item);
        notifyAll();                             // wake the waiting consumers
    }

    public synchronized int consume() throws InterruptedException {
        while (q.isEmpty()) wait();              // releases the lock while waiting
        int item = q.poll();
        notifyAll();                             // wake the waiting producers
        return item;
    }
}
```

**Rules:** call them **inside synchronized** on the **same object** · always wait in a **`while` loop** · prefer
`notifyAll()` (`notify()` wakes one arbitrary thread and can cause missed signals).

> 💡 Modern code uses **`BlockingQueue`** instead: `queue.put(x)` blocks when full, `queue.take()` blocks when empty.

---

## 6. Locks (`java.util.concurrent.locks`)

```java
private final ReentrantLock lock = new ReentrantLock();     // new ReentrantLock(true) = fair (FIFO)

void transfer() {
    lock.lock();
    try {
        // critical section
    } finally {
        lock.unlock();                                      // ALWAYS in finally!
    }
}

if (lock.tryLock(1, TimeUnit.SECONDS)) {                    // give up instead of waiting forever
    try { ... } finally { lock.unlock(); }
} else { /* fallback, avoids deadlock */ }

Condition notEmpty = lock.newCondition();                   // multiple wait-sets per lock
notEmpty.await(); notEmpty.signal();

ReadWriteLock rw = new ReentrantReadWriteLock();            // many readers OR one writer
rw.readLock().lock();  rw.writeLock().lock();
```

| | `synchronized` | `ReentrantLock` |
|---|---|---|
| Acquire/release | automatic (block scope) | manual `lock()`/`unlock()` in finally |
| Try without blocking | ❌ | ✅ `tryLock()`, with a timeout |
| Interruptible wait | ❌ | ✅ `lockInterruptibly()` |
| Fairness | ❌ | ✅ optional |
| Multiple conditions | ❌ (one wait-set) | ✅ `newCondition()` |
| Simplicity | ✅ simpler, can't forget unlock | more power, more responsibility |

---

## 7. Deadlock, Livelock, Starvation

```java
// Thread 1: lock A → (wants B)        Thread 2: lock B → (wants A)      → both wait FOREVER
```

```
   Thread-1 ──holds──► [Lock A] ◄──wants── Thread-2
       │                                      │
       └──wants──► [Lock B] ◄──holds──────────┘        (circular wait)
```

**4 necessary conditions (Coffman). Break any one to prevent deadlock:**

> 🧩 "**My House Never Collapses**": **M**utual exclusion · **H**old and wait · **N**o preemption · **C**ircular wait.

| Prevention technique | Breaks |
|---|---|
| **Lock ordering**: always acquire locks in the same global order (e.g. by account id) | Circular wait ✅ (most common) |
| `tryLock(timeout)` and back off | Hold and wait / no preemption |
| Acquire all locks at once / use a single lock | Hold and wait |
| Avoid nested locks, keep critical sections small | reduces risk |

**Detect:** `jstack <pid>` prints "Found one Java-level deadlock", or programmatically with
`ManagementFactory.getThreadMXBean().findDeadlockedThreads()`. See [DeadlockDemo.java](../code/08_concurrency/DeadlockDemo.java).

| Problem | What happens | Analogy |
|---|---|---|
| **Deadlock** | threads wait for each other forever (blocked) | two cars at a one-lane bridge |
| **Livelock** | threads keep reacting to each other, no progress (busy) | two people in a corridor both stepping aside, again and again |
| **Starvation** | a thread never gets the CPU/lock | a polite person never gets served at a crowded counter |

---

## 8. Executor Framework ⭐

```java
ExecutorService pool = Executors.newFixedThreadPool(4);
pool.execute(() -> System.out.println("fire and forget"));      // Runnable, no result
Future<String> f = pool.submit(() -> "result");                  // Callable → Future
List<Future<Integer>> all = pool.invokeAll(List.of(() -> 1, () -> 2));
String any = pool.invokeAny(List.of(() -> "fast", () -> "slow"));
pool.shutdown();                                                 // no new tasks; finish the queued ones
pool.awaitTermination(10, TimeUnit.SECONDS);
pool.shutdownNow();                                              // interrupt running tasks, return the queued ones
```

| Factory | Behaviour | Use for |
|---|---|---|
| `newFixedThreadPool(n)` | n threads, unbounded queue | steady CPU-bound workloads |
| `newCachedThreadPool()` | grows as needed, idle threads die after 60s | many short-lived tasks (risk: unbounded threads) |
| `newSingleThreadExecutor()` | 1 thread, tasks run sequentially | ordered background work |
| `newScheduledThreadPool(n)` | delayed/periodic tasks | cron-like jobs |
| `newWorkStealingPool()` | ForkJoinPool | divide-and-conquer |
| `newVirtualThreadPerTaskExecutor()` (21) | a new virtual thread per task | **I/O-bound** work, servers |

### ThreadPoolExecutor internals

```java
new ThreadPoolExecutor(
    2,                       // corePoolSize    : threads kept alive
    4,                       // maximumPoolSize : upper limit
    60, TimeUnit.SECONDS,    // keepAliveTime   : for the extra (non-core) threads
    new ArrayBlockingQueue<>(100),       // work queue
    Executors.defaultThreadFactory(),
    new ThreadPoolExecutor.CallerRunsPolicy());   // rejection policy
```

```
 new task → running < core?  ──yes──► start a new core thread
               │ no
               ▼
           queue not full? ──yes──► enqueue
               │ no
               ▼
           running < max?  ──yes──► start an extra thread
               │ no
               ▼
           REJECT → AbortPolicy (default, throws) · CallerRunsPolicy · DiscardPolicy · DiscardOldestPolicy
```

**Sizing rule of thumb:** CPU-bound → `#cores` (or +1) · I/O-bound → `#cores × (1 + waitTime/computeTime)`.

| `execute()` | `submit()` |
|---|---|
| `Executor` interface, takes a Runnable | `ExecutorService`, takes a Runnable or Callable |
| returns void | returns a `Future` |
| an exception goes to the thread's uncaught-exception handler | an exception is captured → `ExecutionException` on `future.get()` |

### Future

`get()` (blocks) · `get(timeout, unit)` · `cancel(mayInterrupt)` · `isDone()` · `isCancelled()`.
Its limitations (no chaining, no combining, blocking `get`) led to **CompletableFuture**.

---

## 9. CompletableFuture (Java 8): Async Pipelines

```java
CompletableFuture<String> userF  = CompletableFuture.supplyAsync(() -> fetchUser(42));     // runs on the common pool
CompletableFuture<Double> priceF = CompletableFuture.supplyAsync(() -> fetchPrice("SKU1"), myExecutor);

String result = userF
        .thenApply(String::toUpperCase)                         // map:     T → R
        .thenCompose(u -> CompletableFuture.supplyAsync(() -> fetchOrders(u)))  // flatMap: T → CF<R> (dependent)
        .thenCombine(priceF, (orders, price) -> orders + " @ " + price)         // combine 2 INDEPENDENT futures
        .exceptionally(ex -> "fallback: " + ex.getMessage())    // recover
        .join();                                                // wait (unchecked exception, unlike get())

CompletableFuture.allOf(f1, f2, f3).join();                     // wait for all
CompletableFuture.anyOf(f1, f2).join();                         // first one to finish
cf.thenAccept(System.out::println);                             // consume (no result)
cf.handle((value, ex) -> ex == null ? value : "default");       // success OR failure
```

| Method | Analogy | Input → Output |
|---|---|---|
| `thenApply` | `map` | T → R |
| `thenCompose` | `flatMap` | T → CompletableFuture<R> |
| `thenCombine` | zip | (T, U) → R |
| `thenAccept` / `thenRun` | forEach | T → void / () → void |
| `exceptionally` / `handle` | catch | Throwable → T |

> 💡 Methods ending in **`Async`** (`thenApplyAsync`) may run on another thread; the others run on the thread that
> completed the previous stage.

---

## 10. Concurrent Collections & Synchronizers

| Class | Use for |
|---|---|
| `ConcurrentHashMap` | a thread-safe map. Use `merge`, `compute`, `computeIfAbsent` for **atomic** updates |
| `CopyOnWriteArrayList` | read-mostly lists (listeners): each write copies the array |
| `ConcurrentLinkedQueue` | a non-blocking thread-safe queue |
| `ArrayBlockingQueue` / `LinkedBlockingQueue` | producer-consumer: `put()` / `take()` block |
| `PriorityBlockingQueue` / `DelayQueue` / `SynchronousQueue` | priority / delayed / direct hand-off |

| Synchronizer | Idea | Reusable? | Analogy |
|---|---|---|---|
| **`CountDownLatch(n)`** | threads `await()` until `countDown()` has been called n times | ❌ one-shot | a rocket launch waits for N system checks |
| **`CyclicBarrier(n)`** | n threads wait **for each other** at a barrier, then all proceed | ✅ | friends wait until everyone arrives, then leave together |
| **`Semaphore(n)`** | n permits: `acquire()` / `release()` | ✅ | a parking lot with n spaces (connection pools, rate limits) |
| `Phaser` | a flexible, multi-phase barrier | ✅ | |
| `Exchanger` | two threads swap objects | ✅ | |

### ThreadLocal

```java
private static final ThreadLocal<SimpleDateFormat> FMT =
        ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));   // each thread gets its own
FMT.get().format(new Date());
FMT.remove();   // ⚠️ ALWAYS remove in thread pools, or values leak into the next task
```

---

## 11. Virtual Threads (Java 21) ⭐

**One-liner:** lightweight threads **managed by the JVM** rather than the OS. When a virtual thread blocks
(I/O, sleep, lock), it **unmounts** from its carrier (platform) thread, which can then run another virtual thread.

```
  Platform threads (OS threads)      Virtual threads
  ~1 MB stack each, ~thousands max   ~few KB each, MILLIONS possible
  ┌──────┐ ┌──────┐                  v1 v2 v3 v4 v5 ... v1,000,000
  │ T1   │ │ T2   │   ◄── carriers ── mounted/unmounted onto a small ForkJoinPool of carriers
  └──────┘ └──────┘
```

```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 100_000; i++) {
        int id = i;                                                  // effectively final copy for the lambda
        executor.submit(() -> { Thread.sleep(1000); return id; });  // 100k concurrent "blocking" tasks → ~1 second
    }
}
```

| ✅ Do | ❌ Don't |
|---|---|
| use them for **I/O-bound**, blocking work (HTTP, DB, files) | expect speed-ups on **CPU-bound** work |
| create one per task (they're cheap) | **pool** them |
| use `ReentrantLock` around blocking calls (Java 21) | hold `synchronized` during long blocking ops → **pinning** (fixed in Java 24) |
| keep ThreadLocal usage light | store huge objects in ThreadLocals across millions of threads (→ Scoped Values) |

---

## 12. Making Code Thread-Safe: the Strategies

1. **Immutability**: no state changes means no races (`final` fields, records, `List.copyOf`).
2. **Confinement**: don't share (local variables, ThreadLocal, a single-threaded executor).
3. **Synchronization**: `synchronized` / `Lock` around compound actions.
4. **Atomic variables**: `AtomicInteger`, `LongAdder`, `AtomicReference`.
5. **Concurrent collections**: `ConcurrentHashMap`, `BlockingQueue`.
6. **Higher-level abstractions**: executors, CompletableFuture, parallel streams (stateless).

---

## 🧩 Memory Tricks

| Concept | Trick |
|---|---|
| Thread states | "**New Runners Block, Wait, Time-wait, Terminate**" |
| Deadlock conditions | "**My House Never Collapses**" |
| volatile vs synchronized | "**volatile = see · synchronized = see + lock**" |
| sleep vs wait | "**sleep holds the key, wait hands it back**" |
| wait rule | "**wait in a WHILE, inside SYNCHRONIZED**" |
| Latch vs barrier | "**Latch: wait for EVENTS · Barrier: wait for FRIENDS**" |
| Semaphore | "**Parking lot with N spaces**" |
| thenApply vs thenCompose | "**Apply = map, Compose = flatMap**" |
| Virtual threads | "**Cheap waiters, not faster workers**" |

---

## ❓ Self-Test

<details><summary>1. What happens if you call run() instead of start()?</summary>

The code runs synchronously on the current thread. No new thread is created.
</details>

<details><summary>2. Why is count++ not thread-safe, and give 3 fixes.</summary>

It's three steps (read, add, write), and threads can interleave between them and lose updates. Fixes: a
`synchronized` method/block, `AtomicInteger.incrementAndGet()`, or a `ReentrantLock` around it. (`LongAdder`
works too.)
</details>

<details><summary>3. Does volatile make an operation atomic?</summary>

No. It only guarantees visibility and ordering. `volatile int x; x++` is still a race.
</details>

<details><summary>4. Why must wait() be called in a while loop?</summary>

Spurious wakeups can happen, and another thread may consume the condition between the notify and this thread
re-acquiring the lock. So the condition must be re-checked after waking.
</details>

<details><summary>5. How do you prevent deadlock in a bank transfer between two accounts?</summary>

Always lock the accounts in a consistent global order (e.g. the lower account id first), whatever the transfer
direction. Alternatively, use `tryLock` with a timeout and retry.
</details>

<details><summary>6. CountDownLatch vs CyclicBarrier?</summary>

With a latch, one or more threads wait for N events (countDowns) and it can't be reset. With a barrier, N
threads wait for each other, it's reusable, and it can run a barrier action.
</details>

<details><summary>7. submit() vs execute()?</summary>

`execute(Runnable)` returns nothing, and exceptions go to the uncaught-exception handler. `submit()` returns a
Future, and exceptions are stored in it and rethrown as `ExecutionException` from `get()`.
</details>

<details><summary>8. thenApply vs thenCompose?</summary>

`thenApply` transforms the value with a plain function (like map). `thenCompose` chains a function that itself
returns a CompletableFuture and flattens it (like flatMap), which you use for dependent async calls.
</details>

<details><summary>9. When should you NOT use virtual threads?</summary>

For CPU-bound computation (no benefit), as a pool (they're meant to be cheap and disposable), and with long
blocking calls inside `synchronized` blocks on Java 21–23 (that pins the carrier thread).
</details>

<details><summary>10. What is a daemon thread?</summary>

A background service thread (the GC is an example). The JVM doesn't wait for daemon threads; it exits when only
daemon threads remain. Call `setDaemon(true)` before `start()`.
</details>

---

## ✅ Chapter Checklist

- [ ] Draw the thread state diagram
- [ ] Demonstrate a race condition and 3 fixes
- [ ] Write producer-consumer with wait/notify **and** with BlockingQueue
- [ ] Explain the 4 deadlock conditions and lock ordering
- [ ] Write a CompletableFuture pipeline with thenCompose, thenCombine and exceptionally
- [ ] Explain virtual threads and pinning
- [ ] Run [code/08_concurrency](../code/08_concurrency)

➡️ Next: [09 · JVM, Memory & Garbage Collection](09-jvm-memory-gc.md)
