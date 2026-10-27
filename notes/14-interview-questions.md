# 14 · Top 120 Java Interview Questions (with crisp answers)

> ⏱️ **Revise in:** 2 sessions of 60 min · 🎯 Freshers → 5+ years
>
> **How to use:** read the question, **answer out loud in 2–3 sentences**, then open the answer. Mark the ones you
> missed and revisit them after 1 day, 3 days and 7 days (spaced repetition). ⭐ = asked very frequently.

---

## 1. Core Java & Basics

<details><summary>⭐ 1. What makes Java platform-independent?</summary>

javac compiles source to **bytecode**, which runs on any OS that has a JVM. The JVM itself is platform-specific;
the bytecode isn't. That's "Write Once, Run Anywhere".
</details>

<details><summary>⭐ 2. JDK vs JRE vs JVM?</summary>

JVM executes bytecode. JRE = JVM + core libraries (enough to run). JDK = JRE + development tools (javac, jar,
jdb, javadoc). JDK ⊃ JRE ⊃ JVM.
</details>

<details><summary>3. Why is Java not 100% object-oriented?</summary>

Because of the primitive types (`int`, `char`…), which aren't objects, and static members, which belong to no object.
</details>

<details><summary>⭐ 4. Is Java pass-by-value or pass-by-reference?</summary>

Always **pass-by-value**. For objects, the value copied is the reference. A method can therefore mutate the
object, but it can't make the caller's variable refer to a different object.
</details>

<details><summary>5. What are the default values of instance variables? Do local variables have defaults?</summary>

0 / 0.0 / false / '\u0000' / null. Local variables have **no** defaults and must be initialized before use (a compile error otherwise).
</details>

<details><summary>6. Why is main() static? Can we overload or override it?</summary>

It's static so the JVM can call it without creating an object. It can be overloaded, but the JVM only calls
`main(String[])`. It can't be overridden, because static methods are hidden, not overridden.
</details>

<details><summary>7. What is widening vs narrowing conversion?</summary>

Widening (small → large, e.g. int → long) is automatic and safe. Narrowing (large → small, e.g. double → int)
needs an explicit cast and can lose data or precision.
</details>

<details><summary>8. What is the difference between <code>==</code> and <code>equals()</code>?</summary>

`==` compares primitive values or **object references** (identity). `equals()` compares logical content, as
defined by the class (String, wrappers and collections override it).
</details>

<details><summary>9. Can a switch work on a String? A long?</summary>

String: yes (Java 7+). long: no. Allowed types are byte/short/char/int and their wrappers, String and enum, plus
any reference type via Java 21 patterns.
</details>

<details><summary>10. What are varargs? Rules?</summary>

`void f(int... nums)` accepts 0..n arguments as an array. There can be only one varargs parameter, and it must be last.
</details>

<details><summary>11. What is the difference between <code>break</code>, <code>continue</code> and labeled break?</summary>

`break` exits the innermost loop or switch. `continue` skips to the next iteration. `break label` exits the loop
marked with that label, e.g. an outer loop.
</details>

<details><summary>12. What are the access modifiers? Most to least restrictive?</summary>

private (class only) → default (package) → protected (package + subclasses) → public (everyone).
</details>

## 2. OOP

<details><summary>⭐ 13. What are the 4 pillars of OOP?</summary>

Abstraction (expose what, hide how), Polymorphism (one interface, many forms), Inheritance (IS-A reuse),
Encapsulation (private data behind methods). Mnemonic: **A PIE**.
</details>

<details><summary>⭐ 14. Overloading vs overriding?</summary>

Overloading: same name with different parameters, in the same class, resolved at compile time. Overriding: same
signature in a subclass, resolved at runtime (dynamic dispatch). An override can't reduce visibility or throw broader
checked exceptions, and its return type must be the same or covariant.
</details>

<details><summary>⭐ 15. Abstract class vs interface?</summary>

An abstract class can have state, constructors and methods with any access, but you can extend only one (IS-A).
An interface has no instance state, only constants and abstract/default/static/private methods, and a class can
implement many (CAN-DO). Use an abstract class to share code among related classes, and an interface for a
contract or capability.
</details>

<details><summary>16. Can an abstract class have a constructor? Can it be instantiated?</summary>

It can have a constructor, which subclasses call via `super()`. It can't be instantiated with `new`
(an anonymous subclass is possible).
</details>

<details><summary>⭐ 17. Why doesn't Java support multiple inheritance of classes?</summary>

To avoid the diamond problem: if two parents had the same method, which one would the child inherit? Java allows
multiple interfaces. If two default methods conflict, the class must override and choose with `X.super.m()`.
</details>

<details><summary>18. Can we override static, private or final methods?</summary>

No. Static methods are hidden. Private methods aren't visible to the subclass, so a same-named method is a new
method. Final methods are explicitly non-overridable.
</details>

<details><summary>19. What is dynamic method dispatch?</summary>

The JVM picks the overridden method to run based on the **actual object type** at runtime, not the reference type.
This is runtime polymorphism.
</details>

<details><summary>20. What are the uses of <code>this</code> and <code>super</code>?</summary>

`this`: the current object's fields, calling another constructor (`this(...)`), passing or returning the current
object. `super`: the parent constructor (`super(...)`), parent methods and parent fields. `this()` or `super()`
must be the first statement of a constructor.
</details>

<details><summary>21. What is the order of initialization in a class hierarchy?</summary>

Parent static → child static (once, at class load), then for each object: parent instance initializers →
parent constructor → child instance initializers → child constructor.
</details>

<details><summary>22. Composition vs aggregation vs association?</summary>

Association is any "uses/knows" relationship. Aggregation is a weak HAS-A where the part can live independently
(Department–Teacher). Composition is a strong HAS-A where the part's lifecycle is tied to the whole (House–Room).
</details>

<details><summary>⭐ 23. Why prefer composition over inheritance?</summary>

Inheritance couples a subclass tightly to its parent's implementation (fragile base class) and is fixed at compile time.
Composition is flexible: you can swap parts at runtime, test them in isolation, and avoid deep hierarchies.
</details>

<details><summary>⭐ 24. What is the equals/hashCode contract?</summary>

Equal objects must have equal hash codes. Equal hash codes don't imply equality. equals must be reflexive,
symmetric, transitive, consistent, and return false for null. If you override equals, override hashCode too,
otherwise HashMap and HashSet break.
</details>

<details><summary>⭐ 25. How do you create an immutable class?</summary>

Make the class final, make all fields private final, provide no setters, initialize everything in the constructor,
make defensive copies of mutable inputs and outputs (`List.copyOf`), and return new objects for "modifications".
Or use a record, adding copies for mutable components.
</details>

<details><summary>26. Shallow copy vs deep copy?</summary>

A shallow copy copies field values, so referenced objects are shared. A deep copy also recursively copies the
referenced objects. `Object.clone()` is shallow by default. Prefer copy constructors.
</details>

<details><summary>27. What is a marker interface? Examples?</summary>

An interface with no methods that tags a class for special treatment: `Serializable`, `Cloneable`, `RandomAccess`.
Annotations are the modern alternative.
</details>

## 3. Strings, Wrappers, Enums

<details><summary>⭐ 28. Why is String immutable?</summary>

**SPTH**: Security (paths and credentials can't change after validation), the String Pool (sharing is safe),
Thread-safety, and HashCode caching (reliable map keys).
</details>

<details><summary>⭐ 29. What is the String pool?</summary>

A special area in the heap that stores unique string literals. Literals with the same content share one object.
`new String()` bypasses the pool, and `intern()` returns the pooled copy.
</details>

<details><summary>⭐ 30. String vs StringBuilder vs StringBuffer?</summary>

String is immutable. StringBuilder is mutable, unsynchronized and fastest. StringBuffer is mutable and
synchronized (thread-safe, slower, legacy). Use StringBuilder for building strings in loops.
</details>

<details><summary>31. How many objects does <code>String s = new String("hello")</code> create?</summary>

One or two: a heap object always, plus the literal `"hello"` in the pool if it isn't there already.
</details>

<details><summary>⭐ 32. What is the Integer cache?</summary>

`Integer.valueOf` (used by autoboxing) caches -128..127, so `==` is true for boxed values in that range and
usually false outside it. Always compare wrappers with `equals`.
</details>

<details><summary>33. What is autoboxing? Any pitfalls?</summary>

Automatic conversion between primitives and their wrappers. Pitfalls: unboxing null throws an NPE, `==` on
wrappers, performance cost in loops, and `list.remove(int)` vs `remove(Object)`.
</details>

<details><summary>34. Can an enum have constructors and methods? Can it extend a class?</summary>

Yes to constructors (always private), fields, methods (including abstract methods per constant), and interfaces.
It can't extend a class, because it already extends `java.lang.Enum`.
</details>

<details><summary>35. Why use BigDecimal for money?</summary>

`double` is binary floating point and can't represent 0.1 exactly. BigDecimal is exact decimal arithmetic
(create it from a String) and gives you control over rounding.
</details>

## 4. Exceptions

<details><summary>⭐ 36. Checked vs unchecked exceptions?</summary>

Checked exceptions (subclasses of Exception, but not RuntimeException) must be caught or declared, because the
compiler enforces it. They're for external, recoverable conditions like IOException. Unchecked exceptions
(RuntimeException) usually signal programming bugs (NPE, IllegalArgument), and handling them isn't forced.
</details>

<details><summary>37. Error vs Exception?</summary>

An Error is a serious JVM-level problem (OutOfMemoryError, StackOverflowError) that you shouldn't catch. An
Exception is an application-level condition you may handle.
</details>

<details><summary>⭐ 38. final vs finally vs finalize?</summary>

final: a keyword for constants, non-overridable methods and non-extendable classes. finally: a block that always
runs after try/catch. finalize: a deprecated Object method that used to be called before GC.
</details>

<details><summary>39. When doesn't finally execute?</summary>

On `System.exit()`, a JVM crash or kill, an infinite loop or deadlock inside the try, or when a daemon thread is
cut off because the JVM exits.
</details>

<details><summary>40. throw vs throws?</summary>

`throw` actually throws an exception instance inside a method. `throws` declares, in the signature, which checked
exceptions the method may throw.
</details>

<details><summary>⭐ 41. What is try-with-resources?</summary>

A try that automatically closes resources implementing AutoCloseable, in reverse order, even if an exception occurs.
Exceptions from close() are attached as suppressed exceptions.
</details>

<details><summary>42. How do you create a custom exception?</summary>

Extend `Exception` (checked) or `RuntimeException` (unchecked), and provide `(String message)` and
`(String message, Throwable cause)` constructors. Add fields for extra context if useful.
</details>

<details><summary>43. Exception rules with overriding?</summary>

An overriding method may throw the same checked exceptions, narrower ones, or none, but never broader or new
checked exceptions. Unchecked exceptions are unrestricted.
</details>

## 5. Collections

<details><summary>⭐ 44. How does HashMap work internally?</summary>

An array of buckets. `hash = h ^ (h >>> 16)` and `index = (n - 1) & hash`. Collisions are chained in a linked
list, which becomes a Red-Black tree when a bucket has 8 or more nodes and capacity is 64 or more. put/get use
hashCode to find the bucket and equals to find the key. Default capacity is 16, load factor 0.75, and the table
doubles when size exceeds the threshold.
</details>

<details><summary>⭐ 45. HashMap vs Hashtable vs ConcurrentHashMap?</summary>

HashMap: not thread-safe, allows one null key and null values. Hashtable: legacy, every method synchronized,
no nulls. ConcurrentHashMap: thread-safe with fine-grained locking and CAS, high concurrency, no nulls, and weakly
consistent iterators.
</details>

<details><summary>⭐ 46. ArrayList vs LinkedList?</summary>

ArrayList is a dynamic array: O(1) random access, O(n) middle inserts, compact in memory. LinkedList is a doubly
linked list: O(n) access and O(1) at the ends. ArrayList is the default choice.
</details>

<details><summary>⭐ 47. HashSet vs LinkedHashSet vs TreeSet?</summary>

HashSet: unordered, O(1). LinkedHashSet: insertion order, O(1). TreeSet: sorted, O(log n), uses compareTo.
(Hash = Hurry, Linked = Line, Tree = Tidy.)
</details>

<details><summary>48. How does HashSet ensure uniqueness?</summary>

It's backed by a HashMap: each element is a key with a dummy value. Duplicates are detected with hashCode() and equals().
</details>

<details><summary>⭐ 49. Comparable vs Comparator?</summary>

Comparable (`compareTo`) defines the natural order inside the class, and there's only one. Comparator (`compare`)
defines external custom orders, as many as you like, composable with `comparing().thenComparing().reversed()`.
</details>

<details><summary>⭐ 50. Fail-fast vs fail-safe iterators?</summary>

Fail-fast iterators (ArrayList, HashMap) throw ConcurrentModificationException on structural changes during
iteration, detected via modCount. Fail-safe or weakly consistent iterators (CopyOnWriteArrayList,
ConcurrentHashMap) never throw CME.
</details>

<details><summary>51. How do you remove elements while iterating?</summary>

Use `Iterator.remove()`, `Collection.removeIf(predicate)`, or iterate backwards by index. Never call
`list.remove()` inside a for-each.
</details>

<details><summary>52. What happens if a HashMap key is mutable?</summary>

If its fields change after insertion, its hashCode changes, so lookups search the wrong bucket. The entry is
effectively lost (wrong results and a memory leak). Keys should be immutable.
</details>

<details><summary>53. What is the load factor? Why 0.75?</summary>

It's the fill ratio that triggers a resize. 0.75 balances memory usage against collision probability (time vs space).
</details>

<details><summary>54. Collections.unmodifiableList vs List.of vs List.copyOf?</summary>

`unmodifiableList` is a read-only **view**, so changes to the backing list still show through. `List.of` is truly
immutable and rejects nulls. `List.copyOf` is an immutable snapshot copy.
</details>

<details><summary>55. How do you make a collection thread-safe?</summary>

Use the concurrent collections (ConcurrentHashMap, CopyOnWriteArrayList, BlockingQueue), the
`Collections.synchronizedX()` wrappers (synchronize iteration manually), or immutable collections.
</details>

<details><summary>56. How do you implement an LRU cache?</summary>

Extend LinkedHashMap with `accessOrder = true` and override `removeEldestEntry` to return `size() > capacity`.
From scratch, use a HashMap plus a doubly linked list.
</details>

<details><summary>57. PriorityQueue: ordering and use cases?</summary>

A binary min-heap by default (use `Comparator.reverseOrder()` for a max-heap). poll and offer are O(log n), peek
is O(1). Iteration is NOT sorted. Uses: top-K problems, scheduling, Dijkstra.
</details>

<details><summary>58. Why use ArrayDeque instead of Stack?</summary>

Stack extends Vector, so it's synchronized (slow) and legacy. ArrayDeque is faster and offers push/pop/peek as well as queue operations.
</details>

## 6. Java 8+ Functional

<details><summary>⭐ 59. What is a functional interface?</summary>

An interface with exactly one abstract method (default/static methods don't count), e.g. Runnable, Comparator,
Predicate. It can be implemented with a lambda or a method reference. `@FunctionalInterface` makes the compiler
enforce it.
</details>

<details><summary>⭐ 60. Predicate, Function, Consumer, Supplier?</summary>

Predicate<T>: test → boolean. Function<T,R>: apply → R. Consumer<T>: accept → void. Supplier<T>: get() → T.
</details>

<details><summary>⭐ 61. Intermediate vs terminal operations?</summary>

Intermediate operations (filter, map, sorted…) return a Stream and are lazy. Terminal operations (collect,
forEach, reduce, count…) produce a result and trigger execution. A stream is consumed after its terminal operation.
</details>

<details><summary>⭐ 62. map vs flatMap?</summary>

map transforms each element 1→1. flatMap maps each element to a stream and flattens the results, e.g.
List<List<T>> → Stream<T>.
</details>

<details><summary>63. What does lazy evaluation in streams mean?</summary>

Nothing runs until a terminal operation is called, and elements then flow through the pipeline one at a time,
which enables short-circuiting (findFirst, limit, anyMatch) and even infinite streams.
</details>

<details><summary>64. findFirst vs findAny? reduce?</summary>

findFirst respects encounter order. findAny can return any element (faster in parallel). reduce combines all
elements into one result: `reduce(0, Integer::sum)`.
</details>

<details><summary>⭐ 65. groupingBy vs partitioningBy?</summary>

groupingBy groups by any key into `Map<K, List<T>>`, with optional downstream collectors (counting, averaging,
mapping). partitioningBy splits by a predicate into exactly two groups, `Map<Boolean, List<T>>`.
</details>

<details><summary>66. What happens with Collectors.toMap on duplicate keys?</summary>

It throws IllegalStateException. Supply a merge function: `toMap(k, v, (a, b) -> a)`.
</details>

<details><summary>⭐ 67. What is Optional and how should it be used?</summary>

A container that may hold a value, making absence explicit. Use it as a method return type with
map/filter/orElse/orElseGet/orElseThrow. Don't use it for fields or parameters, and don't call get() without a check.
</details>

<details><summary>68. orElse vs orElseGet?</summary>

orElse evaluates its argument eagerly, always. orElseGet takes a Supplier that runs only when the Optional is
empty, which is better for expensive defaults.
</details>

<details><summary>69. What are the 4 types of method references?</summary>

Static (`Integer::parseInt`), bound instance (`System.out::println`), unbound instance (`String::toUpperCase`),
constructor (`ArrayList::new`).
</details>

<details><summary>70. When are parallel streams a bad idea?</summary>

Small data sets, I/O or blocking operations, shared mutable state, order-dependent logic, sources that are hard to
split (LinkedList), and server environments where the common ForkJoinPool is shared.
</details>

## 7. Multithreading & Concurrency

<details><summary>⭐ 71. Ways to create a thread? Which is preferred?</summary>

Extend Thread, implement Runnable or Callable, use an ExecutorService, or (Java 21) a virtual thread. Prefer
Runnable/Callable with an ExecutorService: it separates the task from the thread and reuses threads.
</details>

<details><summary>⭐ 72. start() vs run()?</summary>

start() creates a new thread that calls run(). Calling run() directly executes it on the current thread. Calling
start() twice throws IllegalThreadStateException.
</details>

<details><summary>73. What are the thread states?</summary>

NEW, RUNNABLE, BLOCKED (waiting for a monitor), WAITING, TIMED_WAITING, TERMINATED.
</details>

<details><summary>⭐ 74. sleep() vs wait()?</summary>

sleep is a static Thread method that keeps its locks and doesn't need synchronized. wait is an Object method that
releases the lock, must be called inside synchronized, and is woken by notify/notifyAll.
</details>

<details><summary>⭐ 75. What is synchronized? Method vs block?</summary>

It gives mutual exclusion and visibility through an object's monitor lock. A synchronized method locks `this`
(or the Class object, if static). A block locks any given object and keeps the critical section small.
</details>

<details><summary>⭐ 76. volatile vs synchronized vs Atomic?</summary>

volatile gives visibility and ordering only, with no atomicity for `x++`. synchronized gives atomicity and
visibility but blocks. Atomic classes do lock-free CAS for single-variable atomic updates.
</details>

<details><summary>⭐ 77. What is a deadlock? How do you prevent it?</summary>

Threads waiting forever on each other's locks. It needs mutual exclusion, hold-and-wait, no preemption and
circular wait. Prevent it with consistent lock ordering, tryLock with a timeout, fewer and coarser locks, and no
nested locking.
</details>

<details><summary>78. Livelock vs starvation?</summary>

Livelock: threads keep reacting to each other without making progress. Starvation: a thread never gets the CPU or
a lock (unfair scheduling, greedy threads).
</details>

<details><summary>⭐ 79. What is ExecutorService? Types of thread pools?</summary>

A framework that manages a pool of threads and a task queue. Executors.newFixedThreadPool, newCachedThreadPool,
newSingleThreadExecutor, newScheduledThreadPool, newWorkStealingPool, newVirtualThreadPerTaskExecutor. Shut it down
with shutdown()/shutdownNow().
</details>

<details><summary>80. Explain the ThreadPoolExecutor parameters.</summary>

corePoolSize, maximumPoolSize, keepAliveTime, workQueue, threadFactory, RejectedExecutionHandler. Flow: fill the
core threads, then the queue, then extra threads up to max, then reject (Abort, CallerRuns, Discard, DiscardOldest).
</details>

<details><summary>⭐ 81. Callable vs Runnable? What is a Future?</summary>

Callable returns a value and can throw checked exceptions. Runnable doesn't. A Future represents a pending result:
get() blocks, and there's also cancel, isDone and get(timeout).
</details>

<details><summary>⭐ 82. What is CompletableFuture? thenApply vs thenCompose?</summary>

It's a composable async computation (supplyAsync, thenApply, thenCombine, allOf, exceptionally). thenApply maps
the value. thenCompose chains a function that returns another CompletableFuture and flattens it.
</details>

<details><summary>83. CountDownLatch vs CyclicBarrier vs Semaphore?</summary>

A latch makes threads wait for N events and is one-shot. A barrier makes N threads wait for each other and is
reusable. A semaphore limits concurrent access to N permits.
</details>

<details><summary>84. What is ThreadLocal? Any risk?</summary>

A per-thread copy of a variable (user context, SimpleDateFormat). The risk is memory leaks and data bleeding
between tasks in thread pools if you don't call remove().
</details>

<details><summary>⭐ 85. What are virtual threads (Java 21)?</summary>

Lightweight, JVM-managed threads. They unmount from their carrier OS thread while blocked, so millions can run
concurrently, which is ideal for I/O-bound thread-per-request code. They're not faster for CPU work, and you
shouldn't pool them.
</details>

<details><summary>86. How does ConcurrentHashMap achieve thread-safety (Java 8+)?</summary>

CAS for inserting into empty bins, synchronized on the first node of a bin for updates, volatile reads (mostly
lock-free), and a concurrent resize that multiple threads help with. No global lock.
</details>

## 8. JVM & Memory

<details><summary>⭐ 87. Stack vs heap?</summary>

The stack is per-thread and holds method frames (primitive locals and references), LIFO, and fast; it overflows
with StackOverflowError. The heap is shared and holds all objects; it's managed by the GC and fills up with
OutOfMemoryError.
</details>

<details><summary>⭐ 88. What are the JVM memory areas?</summary>

Shared: method area/Metaspace (class metadata) and the heap. Per thread: the stack, the PC register and the native
method stack.
</details>

<details><summary>⭐ 89. How does garbage collection work? When is an object eligible?</summary>

The GC traces reachability from GC roots (stack references, statics, active threads). Unreachable objects are
garbage. Generational collection: Eden → Survivor → Old, with minor and major GCs, using mark-sweep-compact or
copying.
</details>

<details><summary>90. What are the types of garbage collectors?</summary>

Serial, Parallel (throughput), G1 (default, region-based, predictable pauses), ZGC and Shenandoah (sub-millisecond
pauses). CMS was removed in 14.
</details>

<details><summary>91. What is the class loading process and parent delegation?</summary>

Load → Link (verify, prepare, resolve) → Initialize. The loaders are Bootstrap, Platform and Application. Each asks
its parent first, which protects core classes.
</details>

<details><summary>92. Can Java have memory leaks?</summary>

Yes: objects that are still referenced but never used again. Typical causes are static collections, unclosed
resources, unremoved listeners and ThreadLocals, and inner classes holding their outer instance.
</details>

<details><summary>93. PermGen vs Metaspace?</summary>

PermGen (before Java 8) was a fixed-size heap area for class metadata. Metaspace (Java 8+) is native memory that
grows dynamically, which means fewer "PermGen space" OOMs.
</details>

<details><summary>94. What is the JIT compiler?</summary>

It compiles frequently executed bytecode into optimized native code at runtime (tiered C1/C2), using inlining,
escape analysis and other optimizations.
</details>

## 9. Modern Java

<details><summary>⭐ 95. What is a record?</summary>

A concise immutable data carrier (Java 16). It auto-generates private final fields, the canonical constructor,
accessors, equals, hashCode and toString. It's implicitly final, can implement interfaces, and can't extend a
class or declare extra instance fields.
</details>

<details><summary>⭐ 96. What are sealed classes?</summary>

Classes or interfaces that restrict which classes can extend or implement them (`permits`). The subclasses must be
final, sealed or non-sealed. Combined with switch, the compiler can check exhaustiveness.
</details>

<details><summary>97. Explain pattern matching for switch (Java 21).</summary>

Switch can match on types with binding variables (`case Integer i`), guards (`when i > 10`), `case null`, and record
deconstruction (`case Point(int x, int y)`). Over sealed types it's exhaustive with no default needed.
</details>

<details><summary>98. What is <code>var</code>? Limitations?</summary>

Local variable type inference (Java 10). It needs an initializer, and can't be used for fields, parameters or return
types, with null, or with array initializers. Java remains statically typed.
</details>

<details><summary>99. What changed in Java 8, 11, 17 and 21 (one line each)?</summary>

8: lambdas, streams, Optional, java.time, default methods. 11: HttpClient, String methods, var in lambdas.
17: sealed classes, plus everything from 12–16 (switch expressions, text blocks, records, instanceof patterns). 21: virtual threads, switch and record patterns, sequenced collections.
</details>

<details><summary>100. What are text blocks?</summary>

Multi-line string literals delimited by `"""` (Java 15). Indentation is stripped automatically, so they're great for
JSON, SQL and HTML.
</details>

## 10. Design & Best Practices

<details><summary>⭐ 101. What are the SOLID principles?</summary>

Single responsibility, Open/closed, Liskov substitution, Interface segregation, Dependency inversion.
</details>

<details><summary>⭐ 102. How do you write a thread-safe singleton?</summary>

An enum singleton (the best: reflection- and serialization-safe), the static holder idiom, or double-checked
locking with a volatile field.
</details>

<details><summary>103. Which design patterns are used in the JDK?</summary>

Decorator (java.io streams), Adapter (InputStreamReader, Arrays.asList), Strategy (Comparator), Iterator, Factory
(Calendar.getInstance, List.of), Builder (StringBuilder, HttpRequest), Singleton (Runtime), Observer (listeners),
Template Method (AbstractList), Proxy (reflect.Proxy).
</details>

<details><summary>104. What is dependency injection?</summary>

Objects receive their dependencies from outside (constructor or setter) instead of creating them. This gives loose
coupling and easy testing. Spring's IoC container does it automatically.
</details>

<details><summary>105. Factory vs Builder?</summary>

Factory decides **which** class to instantiate (hides the concrete types). Builder decides **how** to assemble one
complex object step by step (many optional parameters, readable, immutable result).
</details>

## 11. Scenario / Senior Questions

<details><summary>106. Your app throws OutOfMemoryError in production. What do you do?</summary>

Enable `-XX:+HeapDumpOnOutOfMemoryError`, analyze the dump with Eclipse MAT or VisualVM (dominator tree, leak
suspects), check GC logs, look for growing collections, caches, unclosed resources and ThreadLocals, and fix the
leak or size the heap properly.
</details>

<details><summary>107. The app hangs. How do you investigate?</summary>

Take thread dumps (`jstack <pid>` or `jcmd <pid> Thread.print`) several times, look for deadlocks
(reported automatically), threads BLOCKED on the same lock, or threads stuck in I/O, and check the pool queues.
</details>

<details><summary>108. How would you make a bank transfer thread-safe?</summary>

Lock both accounts in a consistent order (by id) to avoid deadlock, check the balance and update inside the
critical section, and in a DB use a transaction with an appropriate isolation level or row locks.
</details>

<details><summary>109. How would you process a 10 GB file?</summary>

Stream it: `Files.lines()` or a BufferedReader, line by line, never `readAllLines`. Optionally process in parallel
chunks with bounded memory, and write output incrementally.
</details>

<details><summary>110. How do you handle 10,000 concurrent HTTP calls efficiently?</summary>

Virtual threads (`newVirtualThreadPerTaskExecutor`) or async HttpClient with CompletableFuture. Add a semaphore to
limit concurrency toward the downstream service, plus timeouts and retries.
</details>

<details><summary>111. String concatenation in a loop: why is it slow?</summary>

Each `+=` creates a new String and copies everything, which is O(n²). StringBuilder appends into one growable buffer.
</details>

<details><summary>112. How do you avoid NullPointerException?</summary>

Validate early (`Objects.requireNonNull`), return empty collections instead of null, use Optional return types,
write `"constant".equals(var)`, use annotations (@NonNull) and static analysis, and initialize fields.
</details>

## 12. Must-Practise Coding Questions

| # | Problem | Key idea |
|---|---|---|
| 113 | Reverse a string / check a palindrome | two pointers or StringBuilder.reverse |
| 114 | Find duplicates in an array/list | HashSet.add returns false, or groupingBy counting |
| 115 | First non-repeated character | LinkedHashMap counts |
| 116 | Anagram check | sort the chars, or a frequency array of 26 |
| 117 | Second largest number | a single pass tracking first and second |
| 118 | Fibonacci / factorial (iterative and recursive) | base cases, memoization |
| 119 | Print odd/even with two threads | wait/notify on a shared monitor |
| 120 | Producer-consumer | BlockingQueue |

➡️ Solutions are in [exercises/solutions](../exercises/solutions) and [code/07_functional/StreamInterviewProblems.java](../code/07_functional/StreamInterviewProblems.java)

➡️ Next: [15 · Tricky Output Questions](15-tricky-output-questions.md)
