# 00 · The Master Mind Map — All of Java on One Page

> **How to use this page:** Before opening any chapter, look at its branch below and try to *say out loud* what you
> remember about each leaf. Gaps you find = what to revise first. Do this again after finishing the chapter.
> (This "recall first, read second" habit is the single most effective memory technique.)

---

## 🗺️ The Big Picture

```
                                   ☕ JAVA
                                      │
   ┌──────────────┬──────────────┬────┴─────────┬───────────────┬────────────────┐
   │              │              │              │               │                │
 BASICS          OOP         CORE APIs      ADVANCED CORE     RUNTIME         MODERN & DESIGN
 (ch 01)       (ch 02)      (ch 03-06)       (ch 07-08)      (ch 09-10)        (ch 11-13)
```

```
☕ JAVA
│
├── 01 BASICS ─────────────────────────────────────────────────────────────────
│   ├── Platform: JDK ⊃ JRE ⊃ JVM · .java → javac → .class (bytecode) → JVM → JIT
│   ├── Data types: 8 primitives (byte short int long float double char boolean) + references
│   ├── Casting: widening (auto) · narrowing (explicit, may lose data) · promotion (byte+byte=int)
│   ├── Operators: arithmetic · ++/-- · && vs & · bitwise · >> vs >>> · ternary
│   ├── Control flow: if · switch (old/arrow/expression) · for · while · do-while · labeled break
│   ├── Arrays: fixed size · .length · default values · 2D/jagged · Arrays utility class
│   └── Methods: ALWAYS pass-by-value · overloading · varargs · recursion
│
├── 02 OOP ───────────────────────────────────────────────────────────────────
│   ├── Class (blueprint) → Object (instance in heap) · constructors · this · static · init order
│   ├── 4 Pillars "A PIE"
│   │   ├── Abstraction    → hide HOW, show WHAT (abstract class, interface)
│   │   ├── Polymorphism   → one name, many forms (overload = compile time, override = runtime)
│   │   ├── Inheritance    → IS-A, extends, super, no multiple inheritance of classes
│   │   └── Encapsulation  → private data + public methods (capsule)
│   ├── Relationships: IS-A (inheritance) vs HAS-A (association → aggregation → composition)
│   ├── Modifiers: private < default < protected < public · final · abstract · static
│   ├── Object class: equals · hashCode · toString · clone · getClass · wait/notify
│   ├── Nested classes: static nested · inner · local · anonymous
│   └── Immutability · Coupling/Cohesion · SOLID
│
├── 03 STRINGS · WRAPPERS · ENUMS ──────────────────────────────────────────────
│   ├── String: immutable · String pool · == vs equals · StringBuilder (fast) vs StringBuffer (sync)
│   ├── Wrappers: autoboxing · Integer cache (-128..127) · unboxing null → NPE
│   ├── Math · BigDecimal (money!) · BigInteger
│   └── Enum: type-safe constants · fields · constructors · methods · EnumMap/EnumSet
│
├── 04 EXCEPTIONS ─────────────────────────────────────────────────────────────
│   ├── Throwable → Error (don't catch) | Exception → Checked | RuntimeException (unchecked)
│   ├── try · catch · finally · multi-catch · try-with-resources
│   ├── throw (do it) vs throws (declare it) · custom exceptions · chaining
│   └── Rules: specific catch first · finally return trap · overriding can't widen checked
│
├── 05 GENERICS ───────────────────────────────────────────────────────────────
│   ├── Type safety at compile time · no casts · class<T> · method <T> · bounded <T extends X>
│   ├── Wildcards: ? · ? extends T (read) · ? super T (write) → PECS
│   └── Type erasure: no new T(), no T[], no primitives, no instanceof List<String>
│
├── 06 COLLECTIONS ────────────────────────────────────────────────────────────
│   ├── List (order + duplicates): ArrayList · LinkedList
│   ├── Set (unique): HashSet · LinkedHashSet · TreeSet
│   ├── Queue/Deque: PriorityQueue (heap) · ArrayDeque (stack + queue)
│   ├── Map (key → value): HashMap · LinkedHashMap · TreeMap · ConcurrentHashMap
│   ├── "Hash = Hurry · Linked = Line · Tree = Tidy"
│   ├── HashMap internals: buckets · hash · equals · load factor 0.75 · treeify at 8
│   └── Comparable (natural, inside) vs Comparator (custom, outside) · fail-fast iterators
│
├── 07 FUNCTIONAL JAVA (Java 8+) ─────────────────────────────────────────────
│   ├── Lambda: (params) -> body · Functional interface = exactly 1 abstract method
│   ├── Predicate (test) · Function (apply) · Consumer (accept) · Supplier (get)
│   ├── Method refs: Class::static · obj::method · Class::instanceMethod · Class::new
│   ├── Streams: Source → Intermediate (lazy) → Terminal (eager) · single use
│   ├── Collectors: toList · toMap · groupingBy · partitioningBy · joining · counting
│   └── Optional: ofNullable · map · orElse (eager) vs orElseGet (lazy) · orElseThrow
│
├── 08 CONCURRENCY ────────────────────────────────────────────────────────────
│   ├── Create: Thread · Runnable · Callable · ExecutorService · Virtual threads (21)
│   ├── Lifecycle: NEW → RUNNABLE → BLOCKED/WAITING/TIMED_WAITING → TERMINATED
│   ├── Safety: synchronized · volatile (visibility) · Atomic* (CAS) · Locks
│   ├── Coordination: wait/notify · BlockingQueue · CountDownLatch · Semaphore
│   ├── Async: Future · CompletableFuture
│   └── Problems: race condition · deadlock (4 conditions) · starvation · livelock
│
├── 09 JVM · MEMORY · GC ──────────────────────────────────────────────────────
│   ├── ClassLoader: Bootstrap → Platform → Application (parent delegation)
│   ├── Memory: Heap (objects) · Stack (frames, locals) · Metaspace (class meta) · PC · Native
│   ├── GC: reachability from GC roots · Young (Eden, S0, S1) → Old · G1 default
│   └── Errors: OutOfMemoryError (heap) · StackOverflowError (deep recursion)
│
├── 10 I/O · NIO · SERIALIZATION ───────────────────────────────────────────────
│   ├── Byte streams (InputStream/OutputStream) vs Char streams (Reader/Writer)
│   ├── Buffered* wrappers (Decorator pattern) · try-with-resources
│   ├── NIO.2: Path · Files.readString/writeString/lines/walk
│   └── Serialization: Serializable · serialVersionUID · transient
│
├── 11 MODERN JAVA (9 → 21+) ──────────────────────────────────────────────────
│   ├── var · text blocks """ · switch expressions + yield
│   ├── record (immutable data carrier) · sealed (closed hierarchy)
│   ├── Pattern matching: instanceof binding · switch patterns · record patterns · when guards
│   └── Sequenced collections · Virtual threads · Modules (module-info.java)
│
├── 12 DESIGN PATTERNS & SOLID ────────────────────────────────────────────────
│   ├── SOLID: Single resp · Open/closed · Liskov · Interface segregation · Dependency inversion
│   ├── Creational: Singleton · Factory · Builder · Prototype
│   ├── Structural: Adapter · Decorator · Proxy · Facade
│   └── Behavioral: Strategy · Observer · Template Method · Command · Iterator
│
└── 13 MORE APIs ──────────────────────────────────────────────────────────────
    ├── Annotations: @Retention · @Target · custom annotations
    ├── Reflection: Class<?> · getDeclaredMethods · invoke · setAccessible
    ├── Date-Time: LocalDate · LocalDateTime · ZonedDateTime · Instant · Duration vs Period
    └── JDBC: Connection · PreparedStatement (stops SQL injection) · ResultSet · transactions
```

---

## 📖 The Java Story in 12 Sentences (read this once, it ties everything together)

1. You write **source code** in a `.java` file, organized into **classes** (blueprints).
2. `javac` (from the **JDK**) compiles it into **bytecode** (`.class`) — not machine code, but a universal JVM language.
3. The **JVM** runs that bytecode on any OS → *Write Once, Run Anywhere*.
4. The **ClassLoader** loads classes on demand; the **JIT compiler** turns hot code into fast machine code.
5. When you call `new`, an **object** is created on the **heap**; your variable on the **stack** holds a *reference* to it.
6. Objects model the world using **OOP**: hide data (**encapsulation**), reuse (**inheritance**), vary behaviour
   (**polymorphism**), and expose only essentials (**abstraction**).
7. Text is a special immutable object — **String** — shared through the **String pool**.
8. Groups of objects live in **collections** (List / Set / Queue / Map), made type-safe by **generics**.
9. Things go wrong → the JVM throws **exceptions**; you **catch** what you can recover from.
10. Since Java 8 you can pass behaviour as data (**lambdas**) and process collections declaratively with **streams**.
11. To do many things at once you use **threads**; shared data needs **synchronization** to stay correct.
12. Objects no longer reachable are cleaned up automatically by the **Garbage Collector** — no `free()`, no `delete`.

---

## 🧗 Learning Path (Beginner → Advanced)

```
LEVEL 1 · BEGINNER        LEVEL 2 · INTERMEDIATE        LEVEL 3 · ADVANCED
─────────────────         ──────────────────────        ──────────────────
01 Basics                 04 Exceptions                 08 Concurrency
02 OOP                    05 Generics                   09 JVM, Memory, GC
03 Strings/Wrappers       06 Collections                11 Modern Java 17-21
                          07 Lambdas & Streams          12 Design Patterns
                          10 File I/O                   13 Reflection, JDBC
        │                          │                            │
        └────────── 14 Interview Q&A · 15 Tricky Outputs · 16 Cheat Sheet ──┘
```

---

## 📅 Java Version Timeline (what came when)

| Version | Year | Headline features (remember the **bold** ones) |
|---|---|---|
| 1.0 | 1996 | The beginning — "Write Once, Run Anywhere" |
| 1.2 | 1998 | **Collections Framework**, Swing, JIT |
| 5 | 2004 | **Generics**, **autoboxing**, **enums**, **varargs**, **annotations**, enhanced for, `java.util.concurrent` |
| 7 | 2011 | **try-with-resources**, **multi-catch**, String in switch, diamond `<>`, NIO.2 (`Path`, `Files`) |
| **8** (LTS) | 2014 | **Lambdas**, **Streams**, **Optional**, **default methods**, **java.time**, Metaspace |
| 9 | 2017 | **Modules (JPMS)**, `List.of/Set.of/Map.of`, JShell, private interface methods |
| 10 | 2018 | **`var`** (local type inference) |
| **11** (LTS) | 2018 | String `isBlank/strip/lines/repeat`, **HttpClient**, run `java File.java` directly |
| 14 | 2020 | **Switch expressions**, helpful NullPointerException messages |
| 15 | 2020 | **Text blocks** `"""` |
| 16 | 2021 | **Records**, **pattern matching for instanceof**, `Stream.toList()` |
| **17** (LTS) | 2021 | **Sealed classes** |
| **21** (LTS) | 2023 | **Virtual threads**, **pattern matching for switch**, **record patterns**, **sequenced collections** |
| 22 | 2024 | Unnamed variables `_` |
| **25** (LTS) | 2025 | Compact source files & instance `main`, flexible constructor bodies, module imports, scoped values |

> 💡 **LTS = Long-Term Support.** Companies mostly run 8, 11, 17, 21 or 25. You have **Java 21** installed,
> and every program in this package runs on it.

---

## 🧩 All Memory Tricks in One Place

| # | Topic | Trick | Meaning |
|---|---|---|---|
| 1 | Primitive sizes | **"1-2-4-8, 4-8, 2"** | byte 1, short 2, int 4, long 8 · float 4, double 8 · char 2 (bytes) |
| 2 | Primitive order | **"Big Sharks Invade Lakes, Fish Dive, Crabs Burrow"** | byte short int long float double char boolean |
| 3 | Widening path | **"b → s → i → l → f → d"** (char joins at int) | byte→short→int→long→float→double |
| 4 | OOP pillars | **"A PIE"** | Abstraction, Polymorphism, Inheritance, Encapsulation |
| 5 | Access modifiers | **"Me → House → Family → World"** | private → default → protected → public |
| 6 | Overriding rules | **"Return narrows, Access widens, Exceptions narrow"** | covariant return, same-or-wider access, same-or-fewer checked exceptions |
| 7 | Overload resolution | **"Every Wise Boy Votes"** | Exact → Widening → Boxing → Varargs |
| 8 | equals/hashCode | **"Same object → same hash; same hash ≠ same object"** | the contract |
| 9 | Checked vs unchecked | **"Checked = world's fault, Unchecked = your fault"** | IOException vs NullPointerException |
| 10 | Generics wildcards | **"PECS"** | Producer Extends, Consumer Super |
| 11 | Hash/Linked/Tree | **"Hurry · Line · Tidy"** | fast-no-order · insertion order · sorted |
| 12 | Functional interfaces | **"Predicate tests, Function transforms, Consumer eats, Supplier gives"** | boolean · R · void · T |
| 13 | Streams | **"Streams SIT until a Terminal arrives"** | Source, Intermediate (lazy), Terminal |
| 14 | Thread states | **"New Runners Block, Wait, Time-wait, Terminate"** | NEW RUNNABLE BLOCKED WAITING TIMED_WAITING TERMINATED |
| 15 | Deadlock conditions | **"My House Never Collapses"** | Mutual exclusion, Hold & wait, No preemption, Circular wait |
| 16 | JVM memory areas | **"Mad Hens Steal Popcorn Nightly"** | Method area, Heap, Stack, PC register, Native stack |
| 17 | Class loading | **"Load → Link (Verify, Prepare, Resolve) → Initialize"** | the 3 phases |
| 18 | SOLID | **S-O-L-I-D** | Single resp, Open/closed, Liskov, Interface segregation, Dependency inversion |
| 19 | Immutable class | **"Final class, Private final fields, No setters, Defensive copies"** | 4-step recipe |
| 20 | length vs length() vs size() | **"Arrays: field, Strings: method, Collections: size()"** | `arr.length`, `str.length()`, `list.size()` |
| 21 | GC generations | **"Most objects die young"** | Eden → Survivor → Old |
| 22 | volatile vs synchronized | **"volatile = see, synchronized = see + lock"** | visibility vs visibility + atomicity |

---

## ✅ Self-check: can you explain each in ONE sentence?

- [ ] Why is Java platform independent but the JVM is platform dependent?
- [ ] What are the 4 pillars of OOP, and an example of each?
- [ ] Why is String immutable?
- [ ] What happens inside `HashMap.put()`?
- [ ] Checked vs unchecked exceptions?
- [ ] `map()` vs `flatMap()`?
- [ ] `synchronized` vs `volatile`?
- [ ] What makes an object eligible for garbage collection?
- [ ] What is a record? What is a sealed class?
- [ ] What are the SOLID principles?

If you can't explain one, its chapter is your next stop. ➡️ Start with [01 · Java Basics](01-java-basics.md)
