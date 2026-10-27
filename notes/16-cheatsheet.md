# 16 · The One-Page Cheat Sheet (night-before-the-interview revision)

> Read this in **15 minutes**. Each line is a fact you should be able to expand into a 1-minute explanation.

---

### ☕ Platform
`.java` →(javac)→ `.class` bytecode →(JVM: ClassLoader → Verifier → Interpreter + JIT)→ native code · **JDK ⊃ JRE ⊃ JVM** · bytecode is portable, the JVM isn't · `java File.java` runs a source file directly (11+).

### 🔢 Types
| byte 1 | short 2 | int 4 | long 8 | float 4 | double 8 | char 2 | boolean ? |
|---|---|---|---|---|---|---|---|

Widening: `byte→short→int→long→float→double` (char→int) · `byte+byte = int` · `x += 1` hides a cast · int overflow is silent · `(int)3.99 = 3` · `0.1+0.2 != 0.3` · `10/0` throws, `10.0/0 = Infinity` · locals have **no default**.

### 🔁 Operators & Flow
`i++` use-then-add · `++i` add-then-use · `i = i++` leaves i unchanged · `&&` short-circuits · `>>` keeps the sign, `>>>` fills with 0 · switch types: byte/short/char/int (+wrappers), String, enum, patterns (21) · `yield` returns a value from a switch-expression block · labeled `break outer;`.

### 🧱 OOP: "A PIE"
**A**bstraction (abstract class/interface) · **P**olymorphism (overload = compile time; override = runtime) · **I**nheritance (`extends`, IS-A, single class) · **E**ncapsulation (private + methods).
Override rules: **same signature · return same/covariant · access same/wider · checked exceptions same/narrower** · can't override static/private/final · **fields & statics resolve by reference type** · constructors aren't inherited · `this()`/`super()` go first.
Init order: **parent static → child static → parent instance+ctor → child instance+ctor**.

| Abstract class | Interface |
|---|---|
| state, constructors, any methods | constants + abstract/default/static/private |
| extend ONE | implement MANY |
| IS-A | CAN-DO |

Access: `private < default < protected < public` ("Me → House → Family → World").
`final`: var = constant, method = no override, class = no extend · **equals ⇒ same hashCode** · immutable = final class + private final + no setters + defensive copies.

### 🔤 Strings & Wrappers
String is immutable (**SPTH**: Security, Pool, Thread-safe, Hash cache) · literals → pool · `new String` → heap · `==` identity vs `equals` content · StringBuilder (fast) vs StringBuffer (synchronized) · `split`/`replaceAll` take a regex · Integer cache **-128..127** · unboxing null → NPE · `list.remove(1)` removes by index · BigDecimal from a **String** for money · `compareTo` for BigDecimal equality.

### 💥 Exceptions
`Throwable → Error | Exception → RuntimeException (unchecked)` · checked = compiler-enforced (IO, SQL) · catch specific → general · finally always runs (except `System.exit`) · **return in finally overrides** · try-with-resources closes in reverse order, suppressed exceptions · `throw` = do it, `throws` = declare it · overrides can't add broader checked exceptions.

### 🧬 Generics
Compile-time safety, no casts · `<T extends Number>` upper bound · **PECS**: `? extends T` read, `? super T` write · `List<Integer>` is NOT a `List<Number>` · **erasure**: no `new T()`, no `T[]`, no `instanceof List<String>`, no primitives.

### 📦 Collections
| Need | Use |
|---|---|
| list (default) | ArrayList (get O(1)) |
| unique | HashSet / LinkedHashSet (order) / TreeSet (sorted) |
| key→value | HashMap / LinkedHashMap / TreeMap |
| stack/queue | ArrayDeque |
| priority | PriorityQueue (min-heap) |
| thread-safe | ConcurrentHashMap, CopyOnWriteArrayList, BlockingQueue |

**HashMap:** 16 buckets · `(n-1) & (h ^ h>>>16)` · hashCode → bucket, equals → key · chain → tree at **8** (cap ≥ **64**) · resize ×2 at **0.75** · 1 null key · not thread-safe.
Comparable = natural (`compareTo`, inside) · Comparator = custom (`comparing().thenComparing().reversed()`) · fail-fast → CME, use `removeIf` · `List.of` is immutable, no nulls · `Arrays.asList` is fixed-size · `merge`/`computeIfAbsent` for counting/grouping · LRU = LinkedHashMap(accessOrder) + removeEldestEntry.

### λ Functional
Functional interface = 1 abstract method · **Predicate test→boolean · Function apply→R · Consumer accept→void · Supplier get→T** · method refs: `Class::static`, `obj::method`, `Class::instanceMethod`, `Class::new` · captured locals effectively final.
Stream = **Source → Intermediate (lazy) → Terminal** · single use · `map` 1→1, `flatMap` 1→many · `groupingBy(f, counting())` · `partitioningBy` → true/false · `toMap` needs a merge fn for duplicates · `toList()` is unmodifiable · Optional: `map/orElse/orElseGet(lazy)/orElseThrow`, return type only.

### 🧵 Concurrency
Runnable/Callable + ExecutorService · `start()` ≠ `run()` · states: NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED · `count++` isn't atomic → `synchronized` / `AtomicInteger` / `Lock` · **volatile = visibility only** · sleep keeps the lock, wait releases it (inside synchronized, in a `while`) · deadlock = **M**utual exclusion + **H**old & wait + **N**o preemption + **C**ircular wait → lock ordering / tryLock · CompletableFuture: `supplyAsync → thenApply(map) → thenCompose(flatMap) → thenCombine → exceptionally → join` · Latch = wait for events, Barrier = wait for each other, Semaphore = N permits · **virtual threads** (21): cheap, for blocking I/O, don't pool.

### 🧠 JVM
Shared: **Heap** (objects) + **Metaspace** (class metadata) · per thread: **Stack** (frames/locals), PC, native stack · loaders: Bootstrap → Platform → App (parent delegation) · Load → Link (Verify, Prepare, Resolve) → Init · GC eligible = unreachable from roots · Eden → S0/S1 → Old · minor vs full GC · **G1 default**, ZGC low-latency · `System.gc()` is a hint · Soft (caches) / Weak (WeakHashMap) refs · leaks = static collections, unclosed resources, listeners, ThreadLocal · tools: jstack, jmap, jcmd, VisualVM, JFR.

### 📁 I/O
Bytes: InputStream/OutputStream · chars: Reader/Writer · Buffered* wrappers = **Decorator** · prefer `Files.readString/writeString/lines/walk` + `Path.of` · try-with-resources · Serializable + `serialVersionUID`, `transient` skipped, static not saved.

### 🆕 Modern Java
`var` (10, locals only) · text blocks `"""` (15) · switch expressions (14) · **records** (16): immutable data, compact ctor · **sealed** (17): `permits`, final/sealed/non-sealed · instanceof pattern (16) · **switch patterns + record patterns + `when`** (21) · sealed + switch = exhaustive · Sequenced collections `getFirst/getLast/reversed` (21) · LTS: **8, 11, 17, 21, 25**.

### 🏛️ Design
**SOLID**: Single resp · Open/closed · Liskov · Interface segregation · Dependency inversion · Singleton → **enum** / holder / DCL + volatile · Builder (many optional params) · Factory (hide `new`) · Decorator (java.io) · Adapter (InputStreamReader) · Proxy (Spring AOP) · Strategy (Comparator) · Observer (listeners) · Template Method (AbstractList) · Command (Runnable).

### 🗄️ JDBC & Date-Time
Connection → PreparedStatement (`?`, stops SQL injection) → ResultSet (`next()`, 1-based) → close · transactions: `setAutoCommit(false)` → `commit()`/`rollback()` · ACID · pool with HikariCP.
`LocalDate/LocalTime/LocalDateTime` (no zone) · `ZonedDateTime` · `Instant` (UTC) · **Period = dates, Duration = time** · `ChronoUnit.DAYS.between` · immutable, so remember to **assign the result** · `DateTimeFormatter` is thread-safe.

---

### 🎯 Top 10 "say it in one breath" answers
1. **HashMap** → buckets by hash, equals in the bucket, tree at 8, resize at 0.75.
2. **String immutable** → security, pool, thread-safety, hash caching.
3. **equals/hashCode** → equal objects must have equal hashes.
4. **Checked vs unchecked** → compiler-forced external failures vs programming bugs.
5. **Abstract vs interface** → partial implementation + state (one) vs contract (many).
6. **volatile** → visibility, not atomicity.
7. **Deadlock** → 4 conditions; fix with lock ordering.
8. **Stream laziness** → no terminal, no execution; elements flow one at a time.
9. **GC** → reachability from roots; generational; G1 default.
10. **Records/sealed/patterns** → data, choices and behaviour with compiler-checked exhaustiveness.

🎉 **You've reached the end of the notes.** Now go build the [Mini Bank project](../project/README.md)!
