# 07 · Functional Java: Lambdas, Streams & Optional (Java 8+)

> ⏱️ **Revise in:** 75 min · 🎯 **Level:** Intermediate → Advanced · ▶️ **Code:** [code/07_functional](../code/07_functional) · 🏋️ **Practice:** [Exercises → Streams](../exercises/README.md#7-streams)

## 🧠 Mind Map

```
07 FUNCTIONAL JAVA
├── Lambda: (params) -> expression | { statements }
│   ├── implements a FUNCTIONAL INTERFACE (exactly 1 abstract method, @FunctionalInterface)
│   ├── captures only EFFECTIVELY FINAL local variables
│   └── `this` = the enclosing object (unlike an anonymous class)
├── Built-in functional interfaces (java.util.function)
│   ├── Predicate<T>   test(T)   → boolean   "tests"
│   ├── Function<T,R>  apply(T)  → R         "transforms"
│   ├── Consumer<T>    accept(T) → void      "eats"
│   ├── Supplier<T>    get()     → T         "gives"
│   ├── UnaryOperator<T> · BinaryOperator<T> · Bi* versions · Int/Long/Double* primitives
│   └── compose: and/or/negate · andThen/compose · Comparator.comparing
├── Method references  ::
│   ├── Static              Integer::parseInt
│   ├── Bound instance      System.out::println
│   ├── Unbound instance    String::toUpperCase
│   └── Constructor         ArrayList::new
├── Streams  (Source → Intermediate* → Terminal)   "Streams SIT"
│   ├── Source: list.stream() · Arrays.stream · Stream.of · IntStream.range · iterate · generate · Files.lines
│   ├── Intermediate (LAZY, return Stream): filter map flatMap distinct sorted peek limit skip
│   │                                       takeWhile dropWhile mapToInt boxed
│   ├── Terminal (EAGER, trigger work): forEach collect toList reduce count min max
│   │                                   anyMatch allMatch noneMatch findFirst findAny toArray
│   ├── Collectors: toList toSet toMap joining groupingBy partitioningBy counting
│   │               summingInt averagingDouble mapping collectingAndThen teeing
│   ├── Properties: lazy · single-use · doesn't modify the source · can be infinite · parallelizable
│   └── Primitive streams: IntStream (sum, average, max, range, boxed)
└── Optional<T>: a box that may be empty → avoid null checks
    ├── create: of · ofNullable · empty
    ├── use: map · flatMap · filter · ifPresent · ifPresentOrElse
    └── unwrap: orElse (EAGER) · orElseGet (LAZY) · orElseThrow
```

## ⚡ 30-Second Summary

- A **lambda** is an anonymous function, a concise implementation of a **functional interface**.
- Know the **big four**: **Predicate** (test → boolean), **Function** (transform), **Consumer** (use, no
  return), **Supplier** (produce).
- A **stream** is a pipeline. Nothing happens until a **terminal** operation runs (laziness).
- **map** = 1-to-1 transform. **flatMap** = 1-to-many, then flatten.
- **groupingBy** builds `Map<K, List<V>>`; add a downstream collector (`counting()`, `averagingDouble()`) for aggregates.
- **Optional** = "maybe a value". Use it as a **return type**; prefer `orElseGet` for expensive defaults.

---

## 1. Lambda Expressions

```java
// Evolution of the same code
Comparator<String> c1 = new Comparator<String>() {          // anonymous class (Java 7)
    @Override public int compare(String a, String b) { return a.length() - b.length(); }
};
Comparator<String> c2 = (String a, String b) -> { return a.length() - b.length(); };
Comparator<String> c3 = (a, b) -> a.length() - b.length();  // types inferred, expression body
Comparator<String> c4 = Comparator.comparingInt(String::length);   // method reference (best)
```

| Syntax | Example |
|---|---|
| no params | `() -> System.out.println("hi")` |
| one param (parens optional) | `x -> x * 2` |
| multiple params | `(a, b) -> a + b` |
| block body (needs `return`) | `(a, b) -> { int s = a + b; return s; }` |
| explicit types / `var` | `(int a, int b) -> a + b` · `(var a, var b) -> a + b` |

### Effectively final capture

```java
int base = 10;
Function<Integer, Integer> add = x -> x + base;   // ✅ base is effectively final
// base = 20;                                     ❌ would make the lambda above a compile error
```

> ❓ **Why?** A lambda may run later or on another thread, so it captures a **copy** of the value. Allowing
> changes would make that copy confusing and racy. (Fields and array elements *can* be mutated, but that's
> rarely a good idea.)

### Lambda vs anonymous class

| | Lambda | Anonymous class |
|---|---|---|
| Works for | functional interfaces only | any interface/abstract class, many methods |
| `this` means | the **enclosing** instance | the anonymous object itself |
| Can have state (fields)? | ❌ | ✅ |
| Compiled to | `invokedynamic` (no extra .class file) | `Outer$1.class` |
| Shadow local variables? | ❌ (same scope) | ✅ (new scope) |

---

## 2. Functional Interfaces

| Interface | Abstract method | Shape | Example |
|---|---|---|---|
| **`Predicate<T>`** | `boolean test(T t)` | T → boolean | `s -> s.isEmpty()` |
| **`Function<T,R>`** | `R apply(T t)` | T → R | `s -> s.length()` |
| **`Consumer<T>`** | `void accept(T t)` | T → void | `s -> System.out.println(s)` |
| **`Supplier<T>`** | `T get()` | () → T | `() -> new ArrayList<>()` |
| `UnaryOperator<T>` | `T apply(T t)` | T → T | `s -> s.toUpperCase()` |
| `BinaryOperator<T>` | `T apply(T a, T b)` | (T,T) → T | `(a, b) -> a + b`, `Integer::sum` |
| `BiFunction<T,U,R>` | `R apply(T t, U u)` | (T,U) → R | `(name, age) -> name + age` |
| `BiConsumer<T,U>` | `void accept(T t, U u)` | (T,U) → void | `map.forEach((k, v) -> ...)` |
| `BiPredicate<T,U>` | `boolean test(T t, U u)` | (T,U) → boolean | `(s, n) -> s.length() > n` |
| `Runnable` | `void run()` | () → void | threads |
| `Callable<V>` | `V call() throws Exception` | () → V | executors |
| `Comparator<T>` | `int compare(T a, T b)` | (T,T) → int | sorting |

**Primitive specializations** avoid boxing: `IntPredicate`, `IntFunction<R>`, `ToIntFunction<T>`,
`IntUnaryOperator`, `IntBinaryOperator`, `IntSupplier`, `IntConsumer` (plus Long/Double versions).

> 🧩 "**Predicate tests, Function transforms, Consumer eats, Supplier gives.**"

### Composing functions

```java
Predicate<String> notEmpty = s -> !s.isEmpty();
Predicate<String> shortWord = s -> s.length() < 5;
Predicate<String> both = notEmpty.and(shortWord);          // also .or(), .negate(), Predicate.not(...)

Function<Integer, Integer> doubleIt = x -> x * 2;
Function<Integer, Integer> plus3 = x -> x + 3;
doubleIt.andThen(plus3).apply(5);   // (5*2)+3 = 13   ← andThen: this first, then that
doubleIt.compose(plus3).apply(5);   // (5+3)*2 = 16   ← compose: that first, then this
Function.identity();                // x -> x
```

---

## 3. Method References `::`

| Type | Syntax | Lambda equivalent |
|---|---|---|
| **Static method** | `Integer::parseInt` | `s -> Integer.parseInt(s)` |
| **Instance method of a particular object** (bound) | `System.out::println` | `x -> System.out.println(x)` |
| **Instance method of an arbitrary object** (unbound) | `String::toUpperCase` | `s -> s.toUpperCase()` |
| | `String::compareTo` | `(a, b) -> a.compareTo(b)` |
| **Constructor** | `ArrayList::new` | `() -> new ArrayList<>()` |
| | `int[]::new` | `n -> new int[n]` |

> 🧩 Use a method reference when the lambda **only calls one existing method** with the same arguments.

---

## 4. Streams: the Pipeline

```
 SOURCE                  INTERMEDIATE OPS (lazy, 0..n)                   TERMINAL OP (1, eager)
 ──────                  ────────────────────────────                    ─────────────────────
 list.stream()  ──►  filter(e -> e.salary > 50k) ──► map(Employee::name) ──► sorted() ──► collect(toList())
                     nothing runs until here ───────────────────────────────────────────────▲
```

> 🧩 **"Streams SIT"**: **S**ource → **I**ntermediate → **T**erminal. Without a Terminal, nothing happens.

**Key properties:**
1. **Not a data structure**: it doesn't store elements; it carries them from a source.
2. **Doesn't modify the source** collection.
3. **Lazy**: intermediate ops run only when a terminal op is invoked, and elements flow through **one at
   a time** (vertically), not stage by stage.
4. **Single-use**: reusing a consumed stream throws `IllegalStateException`.
5. **Short-circuiting**: `limit`, `findFirst`, `anyMatch`… can stop early, which even works on **infinite** streams.
6. **Internal iteration**: you say *what*, the library decides *how* (it can go parallel).

```java
Stream.of("a", "b", "c")
      .filter(s -> { System.out.println("filter " + s); return true; })
      .map(s -> { System.out.println("map " + s); return s.toUpperCase(); })
      .findFirst();
// Prints: filter a, map a   ← only ONE element processed (lazy + short-circuit)
```

### Creating streams

```java
list.stream();  set.stream();  map.entrySet().stream();
Arrays.stream(array);            Stream.of(1, 2, 3);
IntStream.range(0, 5);           // 0,1,2,3,4      rangeClosed(1, 5) → 1..5
Stream.iterate(1, x -> x * 2).limit(10);        // 1,2,4,...,512
Stream.iterate(1, x -> x <= 100, x -> x * 3);   // Java 9: with a condition
Stream.generate(Math::random).limit(3);
"hello".chars();                 // IntStream of chars
Files.lines(Path.of("a.txt"));   // lazy lines (close it: try-with-resources)
```

### Intermediate operations

| Op | Does | Example |
|---|---|---|
| `filter(Predicate)` | keep the matches | `.filter(n -> n % 2 == 0)` |
| `map(Function)` | transform 1→1 | `.map(String::length)` |
| `flatMap(Function→Stream)` | transform 1→many, flatten | `.flatMap(List::stream)` |
| `distinct()` | remove duplicates (equals) | |
| `sorted()` / `sorted(cmp)` | sort (stateful) | `.sorted(Comparator.reverseOrder())` |
| `peek(Consumer)` | look without changing (debugging) | `.peek(System.out::println)` |
| `limit(n)` / `skip(n)` | first n / drop n | pagination: `.skip(page*size).limit(size)` |
| `takeWhile` / `dropWhile` (9) | take/drop **while** the condition holds | on sorted data |
| `mapToInt/Long/Double` | to a primitive stream | `.mapToInt(Integer::intValue).sum()` |
| `boxed()` | primitive → wrapper stream | `IntStream.range(0,3).boxed()` |
| `mapMulti` (16) | flexible 1→many | rare |

### map vs flatMap ⭐

```java
List<List<Integer>> nested = List.of(List.of(1, 2), List.of(3, 4));
nested.stream().map(List::size).toList();               // [2, 2]          (1 → 1)
nested.stream().flatMap(List::stream).toList();         // [1, 2, 3, 4]    (1 → many, flattened)

List<String> sentences = List.of("hello world", "java streams");
sentences.stream().flatMap(s -> Arrays.stream(s.split(" "))).toList();   // [hello, world, java, streams]
```

> 🧩 "**map wraps, flatMap unwraps**": `map` would give `Stream<Stream<String>>`; `flatMap` gives `Stream<String>`.

### Terminal operations

| Op | Returns | Example |
|---|---|---|
| `forEach` / `forEachOrdered` | void | `.forEach(System.out::println)` |
| `collect(Collector)` | anything | `.collect(Collectors.toSet())` |
| `toList()` (16) | **unmodifiable** List | `.toList()` |
| `reduce` | T or Optional<T> | `.reduce(0, Integer::sum)` |
| `count()` | long | |
| `min(cmp)` / `max(cmp)` | Optional<T> | `.max(Comparator.naturalOrder())` |
| `anyMatch` / `allMatch` / `noneMatch` | boolean | `.anyMatch(n -> n > 10)` |
| `findFirst` / `findAny` | Optional<T> | `findAny` is faster in parallel |
| `toArray()` | Object[] / T[] | `.toArray(String[]::new)` |
| IntStream: `sum` `average` `summaryStatistics` | int / OptionalDouble / stats | |

```java
int sum = nums.stream().reduce(0, (a, b) -> a + b);      // identity + accumulator
Optional<Integer> max = nums.stream().reduce(Integer::max);
```

> ⚠️ On an **empty** stream, `allMatch` returns **true** and `anyMatch` returns **false** (vacuous truth).

---

## 5. Collectors ⭐⭐

```java
import static java.util.stream.Collectors.*;

List<String> names = emps.stream().map(Employee::name).collect(toList());
Set<String> depts  = emps.stream().map(Employee::dept).collect(toSet());
String csv         = emps.stream().map(Employee::name).collect(joining(", ", "[", "]"));

// toMap: key mapper, value mapper (+ merge function for duplicate keys!)
Map<Integer, String> byId = emps.stream().collect(toMap(Employee::id, Employee::name));
Map<String, Double> maxSalByDept = emps.stream()
        .collect(toMap(Employee::dept, Employee::salary, Math::max));   // without the merge fn: IllegalStateException on duplicates

// groupingBy: the SQL "GROUP BY" of streams
Map<String, List<Employee>> byDept = emps.stream().collect(groupingBy(Employee::dept));
Map<String, Long> countByDept      = emps.stream().collect(groupingBy(Employee::dept, counting()));
Map<String, Double> avgSalByDept   = emps.stream().collect(groupingBy(Employee::dept, averagingDouble(Employee::salary)));
Map<String, List<String>> namesByDept = emps.stream()
        .collect(groupingBy(Employee::dept, mapping(Employee::name, toList())));
Map<String, Optional<Employee>> topByDept = emps.stream()
        .collect(groupingBy(Employee::dept, maxBy(Comparator.comparingDouble(Employee::salary))));
Map<String, Long> sortedCounts = emps.stream()
        .collect(groupingBy(Employee::dept, TreeMap::new, counting()));  // choose the map type

// partitioningBy: always 2 groups, true/false
Map<Boolean, List<Employee>> richOrNot = emps.stream().collect(partitioningBy(e -> e.salary() > 60_000));

// statistics
DoubleSummaryStatistics stats = emps.stream().collect(summarizingDouble(Employee::salary));
stats.getMax(); stats.getAverage(); stats.getCount();

// post-processing
List<String> unmodifiable = emps.stream().map(Employee::name).collect(collectingAndThen(toList(), Collections::unmodifiableList));
```

| Collector | Result shape | SQL analogy |
|---|---|---|
| `groupingBy(f)` | `Map<K, List<T>>` | `GROUP BY` |
| `groupingBy(f, counting())` | `Map<K, Long>` | `COUNT(*) GROUP BY` |
| `groupingBy(f, averagingDouble(g))` | `Map<K, Double>` | `AVG() GROUP BY` |
| `groupingBy(f, mapping(g, toList()))` | `Map<K, List<R>>` | `SELECT g ... GROUP BY` |
| `partitioningBy(p)` | `Map<Boolean, List<T>>` | `CASE WHEN` split |
| `toMap(k, v, merge)` | `Map<K, V>` | |
| `joining(sep)` | `String` | `STRING_AGG` |

---

## 6. Parallel Streams

```java
long count = bigList.parallelStream().filter(this::isPrime).count();   // uses ForkJoinPool.commonPool()
```

| ✅ Good for | ❌ Bad for |
|---|---|
| large data + CPU-heavy, independent work | small collections (overhead > gain) |
| stateless, non-interfering operations | I/O or blocking calls |
| easily splittable sources (ArrayList, arrays, IntStream.range) | LinkedList, `Stream.iterate`, order-dependent work |
| | shared mutable state (race conditions!) |

> ⚠️ `forEach` in parallel gives an unpredictable order. Use `forEachOrdered` if order matters.

---

## 7. Optional ⭐

**One-liner:** a container that **may or may not** hold a non-null value. It makes "might be absent"
explicit in an API and helps avoid `NullPointerException`.

```java
Optional<String> a = Optional.of("Java");        // value must be non-null (else NPE)
Optional<String> b = Optional.ofNullable(maybe); // null → empty Optional
Optional<String> c = Optional.empty();

// ✅ functional style
String upper = findUser(id)
        .map(User::name)                          // transform if present
        .filter(n -> !n.isBlank())
        .map(String::toUpperCase)
        .orElse("UNKNOWN");

findUser(id).ifPresent(u -> System.out.println(u));
findUser(id).ifPresentOrElse(System.out::println, () -> System.out.println("not found"));  // Java 9
User u = findUser(id).orElseThrow(() -> new UserNotFoundException(id));
```

### ⚠️ `orElse` vs `orElseGet`

```java
opt.orElse(createDefault());        // createDefault() runs ALWAYS (eager), even when a value is present!
opt.orElseGet(() -> createDefault()); // runs ONLY when empty (lazy) ✅ for expensive defaults
```

| Method | Behaviour |
|---|---|
| `get()` | value, or `NoSuchElementException` (avoid; prefer `orElseThrow()`) |
| `isPresent()` / `isEmpty()` (11) | boolean |
| `orElse(v)` | value or v (**v is always evaluated**) |
| `orElseGet(supplier)` | value or supplier result (lazy) |
| `orElseThrow()` (10) / `orElseThrow(supplier)` | value or exception |
| `map` / `flatMap` | transform (use flatMap when the function itself returns an Optional) |
| `or(supplier)` (9) | an alternative Optional |
| `stream()` (9) | 0- or 1-element stream |

**Best practices:** ✅ use it as a **method return type**. ❌ Don't use it for fields, method parameters,
collections (return an empty list instead), or serialization. ❌ Never write `if (opt.isPresent()) opt.get()`;
use `map`/`orElse`/`ifPresent`. ❌ Never return `null` from a method declared to return Optional.

---

## 8. ⭐ Top 20 Stream Interview Problems (cover the answers and try them!)

```java
List<Integer> nums = List.of(5, 3, 8, 3, 1, 9, 8);
List<String> words = List.of("apple", "banana", "kiwi", "cherry", "fig");
String s = "programming";

// 1. Even numbers, squared
nums.stream().filter(n -> n % 2 == 0).map(n -> n * n).toList();                     // [64, 64]

// 2. Sum, max, average
nums.stream().mapToInt(Integer::intValue).sum();                                    // 37
nums.stream().max(Integer::compare).get();                                          // 9
nums.stream().mapToInt(Integer::intValue).average().orElse(0);                      // 5.2857...

// 3. Remove duplicates, preserving order
nums.stream().distinct().toList();                                                  // [5, 3, 8, 1, 9]

// 4. Find duplicate elements
Set<Integer> seen = new HashSet<>();
nums.stream().filter(n -> !seen.add(n)).collect(Collectors.toSet());                // [3, 8]

// 5. Second highest number
nums.stream().distinct().sorted(Comparator.reverseOrder()).skip(1).findFirst();     // Optional[8]

// 6. Character frequency (insertion order)
s.chars().mapToObj(c -> (char) c)
 .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()));
// {p=1, r=2, o=1, g=2, a=1, m=2, i=1, n=1}

// 7. First non-repeated character
s.chars().mapToObj(c -> (char) c)
 .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()))
 .entrySet().stream().filter(e -> e.getValue() == 1).map(Map.Entry::getKey).findFirst();   // Optional[p]

// 8. First repeated character
Set<Character> seenChars = new HashSet<>();
s.chars().mapToObj(c -> (char) c).filter(c -> !seenChars.add(c)).findFirst();       // Optional[r]

// 9. Longest word
words.stream().max(Comparator.comparingInt(String::length)).get();                  // "banana" (first max)

// 10. Sort by length, then alphabetically
words.stream().sorted(Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder())).toList();
// [fig, kiwi, apple, banana, cherry]

// 11. Join with commas, uppercase
words.stream().map(String::toUpperCase).collect(Collectors.joining(", "));

// 12. Word → length map
words.stream().collect(Collectors.toMap(Function.identity(), String::length));

// 13. Group words by length
words.stream().collect(Collectors.groupingBy(String::length));                      // {3=[fig], 4=[kiwi], 5=[apple], 6=[banana, cherry]}

// 14. Partition numbers into even/odd
nums.stream().collect(Collectors.partitioningBy(n -> n % 2 == 0));

// 15. Flatten a list of lists
List.of(List.of(1, 2), List.of(3)).stream().flatMap(List::stream).toList();         // [1, 2, 3]

// 16. Reverse each word of a sentence
Arrays.stream("I love Java".split(" ")).map(w -> new StringBuilder(w).reverse().toString())
      .collect(Collectors.joining(" "));                                            // "I evol avaJ"

// 17. Palindrome check
IntStream.range(0, s.length() / 2).allMatch(i -> s.charAt(i) == s.charAt(s.length() - 1 - i));

// 18. Employees: highest salary per department
emps.stream().collect(Collectors.groupingBy(Employee::dept,
        Collectors.collectingAndThen(Collectors.maxBy(Comparator.comparingDouble(Employee::salary)), Optional::get)));

// 19. Names of the top 3 earners
emps.stream().sorted(Comparator.comparingDouble(Employee::salary).reversed()).limit(3).map(Employee::name).toList();

// 20. Word frequency, sorted by count descending
Arrays.stream(text.split("\\s+"))
      .collect(Collectors.groupingBy(String::toLowerCase, Collectors.counting()))
      .entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed()).toList();
```

All 20 are runnable in [StreamInterviewProblems.java](../code/07_functional/StreamInterviewProblems.java).

---

## 🧩 Memory Tricks

| Concept | Trick |
|---|---|
| Big 4 interfaces | "**Predicate tests, Function transforms, Consumer eats, Supplier gives**" |
| Pipeline | "**Streams SIT**": Source, Intermediate, Terminal |
| Laziness | "**No terminal, no party**" |
| map vs flatMap | "**map wraps, flatMap unwraps**" |
| andThen vs compose | "**andThen = me first · compose = you first**" |
| orElse vs orElseGet | "**orElse always cooks dinner; orElseGet cooks only if you're hungry**" |
| Collectors | "**groupingBy = GROUP BY, partitioningBy = split in two**" |

---

## ❓ Self-Test

<details><summary>1. What is a functional interface? Is <code>Comparator</code> one, even though it has many methods?</summary>

An interface with exactly one abstract method. Comparator is one: `compare` is its only abstract method.
`equals` comes from Object and doesn't count, and the rest are default or static methods.
</details>

<details><summary>2. Difference between intermediate and terminal operations?</summary>

Intermediate operations return a Stream and are lazy. They only describe the pipeline. Terminal operations
produce a result or a side effect and trigger execution. A stream can have only one terminal operation.
</details>

<details><summary>3. What happens if you call a terminal operation twice on the same stream?</summary>

`IllegalStateException: stream has already been operated upon or closed`. Streams are single-use.
</details>

<details><summary>4. map vs flatMap?</summary>

`map` applies a one-to-one function (`Stream<T>` → `Stream<R>`). `flatMap` applies a function that returns a
stream for each element and flattens the results into one stream (`Stream<List<T>>` → `Stream<T>`).
</details>

<details><summary>5. Collectors.toMap with duplicate keys?</summary>

It throws `IllegalStateException: Duplicate key`. Pass a merge function as the third argument, e.g.
`(a, b) -> a` or `Integer::sum`.
</details>

<details><summary>6. orElse vs orElseGet?</summary>

`orElse(x)` always evaluates `x`, even if the Optional has a value. `orElseGet(supplier)` only calls the
supplier when the Optional is empty, so it's better for expensive defaults.
</details>

<details><summary>7. findFirst vs findAny?</summary>

`findFirst` returns the first element in encounter order. `findAny` may return any element and can be faster
in parallel streams.
</details>

<details><summary>8. Why must captured local variables be effectively final?</summary>

The lambda captures a copy of the value. It may run later or on another thread, so mutation would make that
copy inconsistent and lead to race conditions.
</details>

<details><summary>9. Is <code>stream.toList()</code> the same as <code>collect(Collectors.toList())</code>?</summary>

Almost. `toList()` (Java 16) returns an **unmodifiable** list. `Collectors.toList()` currently returns an
ArrayList, but that isn't guaranteed.
</details>

---

## ✅ Chapter Checklist

- [ ] Name the big 4 functional interfaces with their method names
- [ ] Give the 4 kinds of method references, with examples
- [ ] Demonstrate laziness with `peek`
- [ ] Use groupingBy with 3 different downstream collectors
- [ ] Solve all 20 interview problems without looking
- [ ] Run [code/07_functional](../code/07_functional)

➡️ Next: [08 · Multithreading & Concurrency](08-multithreading-concurrency.md)
