# ▶️ Runnable Code Demos

Every file is a **standalone program**. No build tool, no IDE setup, just Java 21:

```powershell
cd C:\java
java code\01_basics\DataTypesAndCasting.java        # any file, run directly (Java 11+ source launcher)
code\run-all.cmd                                     # run every demo and report OK / FAILED
```

> 🧠 **How to study with these:** open the file, read one `section(...)`, **predict the output**, run it, and compare.
> Then change something (a value, a modifier, a lock) and predict again. Breaking code on purpose is how you learn.

| Folder | File | What you'll see | Notes |
|---|---|---|---|
| **01_basics** | `DataTypesAndCasting.java` | ranges of the 8 primitives, literals, widening/narrowing, overflow, floating-point traps | [01](../notes/01-java-basics.md) |
| | `OperatorsAndControlFlow.java` | `i++` vs `++i`, short-circuit, shifts, ternary trap, 3 kinds of switch, loops, labels | [01](../notes/01-java-basics.md) |
| | `ArraysDemo.java` | defaults, 2D/jagged arrays, `Arrays.*`, `asList` view, AIOOBE, ArrayStoreException | [01](../notes/01-java-basics.md) |
| | `MethodsDemo.java` | pass-by-value proof, overload resolution order, varargs, recursion, StackOverflowError | [01](../notes/01-java-basics.md) |
| **02_oops** | `ClassesAndObjects.java` | references, constructor chaining, `this`, fluent API, static counter, utility class | [02](../notes/02-oops.md) |
| | `InitializationOrder.java` | the exact static/instance/constructor order, and the overridable-method-in-constructor trap | [02](../notes/02-oops.md) |
| | `FourPillarsDemo.java` | **A PIE**: BankAccount, Animal/Dog/Cat, Shape, Payment | [02](../notes/02-oops.md) |
| | `PolymorphismDeepDive.java` | field hiding, static hiding, private methods, covariant returns, compile-time overloads | [02](../notes/02-oops.md) |
| | `InterfacesDemo.java` | multiple interfaces, the diamond with defaults, static/private methods, functional & marker interfaces | [02](../notes/02-oops.md) |
| | `ObjectMethodsDemo.java` | equals without hashCode breaks HashSet, mutable keys, shallow vs deep copy | [02](../notes/02-oops.md) |
| | `InnerClassesAndImmutability.java` | 4 nested class types, an inner-class Iterator, an immutable class, composition | [02](../notes/02-oops.md) |
| **03_strings** | `StringsDemo.java` | the pool, `==` vs `equals`, `intern`, immutability, methods, split regex traps, StringBuilder speed | [03](../notes/03-strings-wrappers-enums.md) |
| | `WrappersAndMathDemo.java` | autoboxing, the Integer cache, unboxing NPE, `remove(int)` vs `remove(Object)`, BigDecimal, BigInteger | [03](../notes/03-strings-wrappers-enums.md) |
| | `EnumsDemo.java` | fields/constructors, constant-specific methods, EnumSet/EnumMap, enum singleton | [03](../notes/03-strings-wrappers-enums.md) |
| **04_exceptions** | `ExceptionsDemo.java` | multi-catch, finally traps, try-with-resources order + suppressed, custom exceptions, chaining, helpful NPE | [04](../notes/04-exceptions.md) |
| **05_generics** | `GenericsDemo.java` | generic class/interface/method, bounds, PECS, invariance vs arrays, erasure, `Supplier` as factory | [05](../notes/05-generics.md) |
| **06_collections** | `ListsSetsQueuesDemo.java` | ArrayList vs LinkedList timing, Hash/Linked/Tree sets, TreeSet navigation, Deque, PriorityQueue, top-K | [06](../notes/06-collections.md) |
| | `MapsDemo.java` | Java 8 map methods (merge, computeIfAbsent), map ordering, TreeMap slabs, LRU cache, sort by value | [06](../notes/06-collections.md) |
| | `MyHashMap.java` ⭐ | **a hand-written HashMap printing its own hashing, collisions and resizes** | [06](../notes/06-collections.md) |
| | `ComparableComparatorDemo.java` | natural vs custom ordering, comparator chains, nullsFirst, the subtraction-overflow trap | [06](../notes/06-collections.md) |
| | `IteratorsAndImmutability.java` | fail-fast vs fail-safe, safe removal, ListIterator, unmodifiable vs immutable, Java 21 sequenced | [06](../notes/06-collections.md) |
| **07_functional** | `LambdasAndFunctionalInterfaces.java` | the big 4, composition, 4 kinds of method references, `this` in lambdas, currying | [07](../notes/07-functional-java8.md) |
| | `StreamsDemo.java` | laziness proof, all the major operations, Collectors (groupingBy, toMap, teeing), parallel | [07](../notes/07-functional-java8.md) |
| | `OptionalDemo.java` | map/flatMap/filter, orElse (eager) vs orElseGet (lazy), anti-patterns | [07](../notes/07-functional-java8.md) |
| | `StreamInterviewProblems.java` ⭐ | **26 classic interview stream problems with answers** | [07](../notes/07-functional-java8.md) |
| **08_concurrency** | `ThreadBasics.java` | 5 ways to create threads, start vs run, **every thread state observed live**, join, interrupt, daemon | [08](../notes/08-multithreading-concurrency.md) |
| | `RaceConditionAndSync.java` | lost updates in action + 4 fixes timed, volatile flag, reentrancy, class vs instance locks | [08](../notes/08-multithreading-concurrency.md) |
| | `ProducerConsumer.java` | wait/notifyAll buffer, BlockingQueue with a poison pill, odd/even printer | [08](../notes/08-multithreading-concurrency.md) |
| | `ExecutorsAndCompletableFuture.java` | pools, Future timeouts, invokeAll/Any, scheduling, **rejection policy**, CompletableFuture pipelines | [08](../notes/08-multithreading-concurrency.md) |
| | `DeadlockDemo.java` ⭐ | **creates a real deadlock, detects it with ThreadMXBean, then fixes it twice** | [08](../notes/08-multithreading-concurrency.md) |
| | `SynchronizersAndVirtualThreads.java` | CountDownLatch, CyclicBarrier, Semaphore, ThreadLocal, **virtual vs platform threads race** | [08](../notes/08-multithreading-concurrency.md) |
| **09_jvm** | `MemoryAndGcDemo.java` | heap numbers, stack depth, the class loader chain, weak/soft refs, WeakHashMap, GC pools, OOM | [09](../notes/09-jvm-memory-gc.md) |
| **10_io** | `FileIoDemo.java` | Files/Path, buffered I/O, CSV → records → report, byte & data streams, walk, serialization + transient | [10](../notes/10-io-nio-serialization.md) |
| **11_modern** | `ModernJavaDemo.java` | var, text blocks, switch expressions, records, sealed + exhaustive switch, record patterns, Java 21 APIs | [11](../notes/11-modern-java.md) |
| **12_patterns** | `CreationalPatterns.java` | 4 singletons (+ breaking them with reflection), factory, abstract factory, builder, prototype | [12](../notes/12-design-patterns-solid.md) |
| | `StructuralPatterns.java` | adapter, decorator, proxy (manual + **JDK dynamic proxy, the way Spring AOP works**), facade, composite | [12](../notes/12-design-patterns-solid.md) |
| | `BehavioralPatterns.java` | strategy, observer, template method, command with undo, chain of responsibility, state | [12](../notes/12-design-patterns-solid.md) |
| | `SolidPrinciples.java` | each SOLID principle, done right | [12](../notes/12-design-patterns-solid.md) |
| **13_more_apis** | `AnnotationsAndReflection.java` ⭐ | **builds a mini JUnit, a validator and a mini Spring DI container** | [13](../notes/13-more-apis.md) |
| | `DateTimeDemo.java` | LocalDate/Time, Period vs Duration, adjusters, formatting, time zones, legacy interop | [13](../notes/13-more-apis.md) |
| | `JdbcDemo.java` | batch insert, **SQL injection vs PreparedStatement**, commit/rollback (needs the H2 jar, see the file header) | [13](../notes/13-more-apis.md) |
| **15_tricky** | `TrickyOutputs.java` | prints the verified answers to all 60 "predict the output" puzzles | [15](../notes/15-tricky-output-questions.md) |

### Running the JDBC demo against a real database

```powershell
# download h2-2.3.232.jar from https://h2database.com (or Maven Central), then:
java -cp path\to\h2-2.3.232.jar code\13_more_apis\JdbcDemo.java
```
