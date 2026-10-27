# 11 · Modern Java (9 → 21 → 25)

> ⏱️ **Revise in:** 50 min · 🎯 **Level:** Intermediate → Advanced · ▶️ **Code:** [code/11_modern](../code/11_modern) · 🏋️ **Practice:** [Exercises → Modern](../exercises/README.md#9-modern-java)

## 🧠 Mind Map

```
11 MODERN JAVA
├── var (10): local type inference · must initialize · not for fields/params/returns
├── Text blocks (15): """ multi-line strings """ · \ (no newline) · \s (keep a space)
├── Switch expressions (14): -> arrows · multiple labels · yield · exhaustive
├── Records (16): immutable data carriers
│   ├── auto: private final fields · canonical constructor · accessors x() · equals/hashCode/toString
│   ├── compact constructor for validation · static factories · implements interfaces
│   └── can't: extend a class · add instance fields · be mutable (shallowly immutable!)
├── Sealed classes (17): sealed ... permits A, B · subclasses are final | sealed | non-sealed
├── Pattern matching
│   ├── instanceof (16): if (o instanceof String s && s.length() > 3)
│   ├── switch patterns (21): case Integer i when i > 10 -> ... · case null · dominance order
│   ├── record patterns (21): case Point(int x, int y) · nested deconstruction · var
│   └── sealed + switch = EXHAUSTIVE, no default (algebraic data types)
├── Collections: List.of/Set.of/Map.of (9) · copyOf (10) · Stream.toList (16) · Sequenced (21)
├── APIs: String isBlank/strip/lines/repeat (11) · HttpClient (11) · Files.readString (11)
│         · Collectors.teeing (12) · Math.clamp (21) · StringBuilder.repeat (21)
├── Concurrency: Virtual threads (21) · Structured concurrency & Scoped values (previews → 25)
├── Modules / JPMS (9): module-info.java · requires · exports · opens · jlink
└── Beyond 21: unnamed _ (22) · Stream gatherers (24) · no pinning in synchronized (24)
              · instance main + compact source files, flexible constructors, module imports (25)
```

## ⚡ 30-Second Summary

- **Records** replace boilerplate DTOs. **Sealed** types restrict who can extend a type.
- **Pattern matching** + **switch** + **sealed** + **records** = clean, type-safe, exhaustive data handling.
- **`var`** reduces noise without losing static typing.
- **Text blocks** make JSON/SQL/HTML literals readable.
- Java **21 is LTS**: virtual threads, record patterns, switch patterns and sequenced collections are all final.

---

## 1. Version Feature Map (what to say in interviews)

| Version | Must-know features |
|---|---|
| **9** | Modules (JPMS), JShell, `List.of/Set.of/Map.of`, private interface methods, `takeWhile/dropWhile`, `Optional.ifPresentOrElse/or/stream`, G1 default |
| **10** | `var`, `List.copyOf`, `Optional.orElseThrow()` |
| **11 LTS** | String `isBlank/strip/lines/repeat`, `Files.readString/writeString`, **HttpClient**, `java File.java` launcher, `var` in lambdas, `Predicate.not` |
| **12–13** | `Collectors.teeing`, `String.indent/transform`, `Files.mismatch` (switch & text blocks previewed) |
| **14** | **Switch expressions**, helpful NPE messages |
| **15** | **Text blocks** |
| **16** | **Records**, **pattern matching for instanceof**, `Stream.toList()` |
| **17 LTS** | **Sealed classes**, strong encapsulation of JDK internals |
| **18** | UTF-8 by default, `jwebserver` |
| **21 LTS** | **Virtual threads**, **pattern matching for switch**, **record patterns**, **sequenced collections**, generational ZGC |
| **22** | Unnamed variables & patterns `_`, Foreign Function & Memory API |
| **24** | Stream gatherers, virtual threads no longer pinned by `synchronized` |
| **25 LTS** | Compact source files + instance `void main()`, flexible constructor bodies, module import declarations, scoped values |

---

## 2. `var`: Local Variable Type Inference (Java 10)

```java
var list = new ArrayList<String>();        // ArrayList<String>
var map = new HashMap<String, List<Integer>>();
for (var entry : map.entrySet()) { }        // Map.Entry<String, List<Integer>>
try (var reader = Files.newBufferedReader(path)) { }
BiFunction<Integer, Integer, Integer> add = (var a, var b) -> a + b;   // Java 11 (handy for annotations)
```

| ✅ Allowed | ❌ Not allowed |
|---|---|
| local variables **with an initializer** | fields, method parameters, return types |
| enhanced for / classic for loops | `var x;` (no initializer) |
| try-with-resources | `var x = null;` |
| lambda parameters (11) | `var arr = {1, 2};` (array initializer) |
| | `var a = 1, b = 2;` (compound declaration) |

> 🔑 Java is **still statically typed**: `var` is inferred at compile time, and the type never changes.
> ⚠️ `var list = new ArrayList<>();` infers **`ArrayList<Object>`**. Give the type argument when you use `var`.
> 💡 `var` is a *reserved type name*, not a keyword, so `int var = 5;` is still legal (please don't).

---

## 3. Text Blocks (Java 15)

```java
String json = """
        {
          "name": "Asha",
          "skills": ["Java", "SQL"]
        }
        """;                       // the indentation of the closing """ sets the left margin

String sql = """
        SELECT id, name
        FROM employees
        WHERE dept = '%s'
        """.formatted("IT");

String oneLine = """
        This is a long sentence \
        on one line.""";           // \ at the end of a line = no newline
String keepSpace = """
        trailing space kept:\s
        """;                       // \s = an explicit space
```

---

## 4. Records (Java 16) ⭐

```java
public record Point(int x, int y) { }
// The compiler generates:
//  private final int x; private final int y;
//  public Point(int x, int y) { ... }             ← canonical constructor
//  public int x() / public int y()                ← accessors (NO "get" prefix)
//  equals(), hashCode(), toString() → "Point[x=1, y=2]"

record Range(int low, int high) {
    Range {                                         // COMPACT constructor: validate/normalize
        if (low > high) throw new IllegalArgumentException(low + " > " + high);
    }                                               // the fields are assigned automatically afterwards
    Range(int single) { this(single, single); }     // other constructors must delegate to the canonical one
    static Range of(int a, int b) { return new Range(Math.min(a, b), Math.max(a, b)); }  // static factory
    int length() { return high - low; }             // instance methods ✅
    static final Range ZERO = new Range(0, 0);      // static fields ✅
}

record Order(String id, List<String> items) {
    Order { items = List.copyOf(items); }           // defensive copy → truly immutable
}
```

| Records CAN | Records CANNOT |
|---|---|
| implement interfaces | extend a class (they implicitly extend `java.lang.Record`) |
| have static fields/methods, instance methods | declare extra **instance** fields |
| have nested types, annotations, generics | be abstract, or be extended (implicitly **final**) |
| be declared **locally** inside a method | have setters (the fields are final) |

> ⚠️ Records are **shallowly immutable**: a `List` component can still be mutated unless you copy it.
> 💡 **Great for:** DTOs, API responses, map keys, multiple return values, stream intermediate results, events.

---

## 5. Sealed Classes & Interfaces (Java 17)

**One-liner:** "Only **these** classes may extend me." A **closed** hierarchy the compiler knows completely.

```java
public sealed interface Shape permits Circle, Square, Rectangle { }

public record Circle(double radius) implements Shape { }          // records are implicitly final ✅
public final class Square implements Shape { ... }                 // final: no further subclasses
public non-sealed class Rectangle implements Shape { ... }         // non-sealed: open to anyone again
```

**Rules:** every permitted subclass must **directly** extend the sealed type and be **`final`**, **`sealed`**, or
**`non-sealed`**. Permitted classes live in the same module (or the same package, in the unnamed module).
If they're in the same file, `permits` can be omitted.

> 🔑 **Why?** Domain modelling ("a Payment is EITHER Card, UPI or Cash, nothing else") + **exhaustive switch**
> (below) + safer APIs (nobody can add rogue subclasses).
> 🧩 "**Sealed = a guest list. Final = no guests. Non-sealed = open party.**"

---

## 6. Pattern Matching ⭐⭐

### 6.1 instanceof (Java 16)

```java
// Before
if (obj instanceof String) {
    String s = (String) obj;
    System.out.println(s.length());
}
// Now: test + cast + bind in one step
if (obj instanceof String s && s.length() > 3) {
    System.out.println(s.toUpperCase());
}
if (!(obj instanceof Integer n)) return;   // flow scoping: n is in scope after this line
System.out.println(n + 1);
```

### 6.2 Switch with patterns (Java 21)

```java
static String describe(Object o) {
    return switch (o) {
        case null                  -> "null!";                      // explicit null handling
        case Integer i when i > 100 -> "big int " + i;              // GUARD with `when`
        case Integer i             -> "int " + i;
        case String s              -> "string of length " + s.length();
        case int[] arr             -> "int[] of length " + arr.length;
        case List<?> list          -> "list of size " + list.size();
        default                    -> "something else: " + o;
    };
}
```

> ⚠️ **Dominance:** put **specific cases before general ones**. `case Number n` before `case Integer i` is a compile
> error, because the Integer case could never match.
> ⚠️ Without `case null`, a null selector still throws an NPE (as before).
> ⚠️ A `when` guard can only read **effectively final** local variables (like lambdas). Copy a changing local into a
> `final` snapshot first. See exercise 9.5.

### 6.3 Record patterns (Java 21): deconstruction

```java
record Point(int x, int y) { }
record Line(Point start, Point end) { }

if (obj instanceof Point(int x, int y)) System.out.println(x + y);   // pulls the components out

static String where(Object o) {
    return switch (o) {
        case Point(var x, var y) when x == 0 && y == 0 -> "origin";
        case Point(var x, var y)                        -> "point at " + x + "," + y;
        case Line(Point(var x1, var y1), Point(var x2, var y2)) ->     // NESTED patterns
                "line of length " + Math.hypot(x2 - x1, y2 - y1);
        default -> "unknown";
    };
}
```

### 6.4 The killer combo: sealed + records + exhaustive switch

```java
sealed interface Payment permits Card, Upi, Cash { }
record Card(String number, double amount) implements Payment { }
record Upi(String vpa, double amount) implements Payment { }
record Cash(double amount) implements Payment { }

static double fee(Payment p) {
    return switch (p) {                       // NO default needed: the compiler knows every case
        case Card c -> c.amount() * 0.02;
        case Upi u  -> 0;
        case Cash c -> c.amount() > 10_000 ? 50 : 0;
    };
}
// Add `record Crypto(...) implements Payment` → every non-exhaustive switch FAILS TO COMPILE
// → the compiler finds every place you forgot to update. 🎯
```

> 🧩 This is how Java does **algebraic data types**, the "data-oriented programming" style:
> **records = the data, sealed = the set of choices, switch patterns = the behaviour.**

---

## 7. Sequenced Collections (Java 21)

```java
interface SequencedCollection<E> extends Collection<E> {
    E getFirst(); E getLast(); void addFirst(E e); void addLast(E e);
    E removeFirst(); E removeLast(); SequencedCollection<E> reversed();
}
```
Implemented by `List`, `Deque`, `LinkedHashSet`, `SortedSet`. `SequencedMap` (`LinkedHashMap`, `SortedMap`) adds
`firstEntry()`, `lastEntry()`, `pollFirstEntry()`, `putFirst()`, `reversed()`.

```java
list.getLast();                // instead of list.get(list.size() - 1)
linkedHashSet.reversed();      // previously impossible without copying
```

---

## 8. Other Handy Modern APIs

```java
// Strings (11+)
"  ".isBlank();  " hi ".strip();  "a\nb".lines().count();  "ab".repeat(3);
"text".indent(4);  "abc".transform(s -> s + "!");  "%s=%d".formatted("x", 1);

// Collections & streams
List.of(1, 2); Set.of("a"); Map.of("k", 1); Map.entry("k", 1);   // immutable (9)
List.copyOf(mutable);                                            // immutable copy (10)
stream.toList();                                                 // unmodifiable (16)
Collectors.teeing(minBy(...), maxBy(...), (min, max) -> ...);    // two collectors at once (12)
Predicate.not(String::isBlank);                                  // (11)
optional.isEmpty(); optional.orElseThrow();                      // (11, 10)

// Java 21 goodies
Math.clamp(150, 0, 100);         // 100
new StringBuilder().repeat("ab", 3);
"a,b;c".splitWithDelimiters("[,;]", 0);   // [a, ",", b, ";", c]

// HttpClient (11)
HttpClient client = HttpClient.newHttpClient();
HttpRequest req = HttpRequest.newBuilder(URI.create("https://api.example.com/users/1")).GET().build();
HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
      .thenApply(HttpResponse::body).thenAccept(System.out::println);   // async with CompletableFuture
```

---

## 9. Modules (JPMS, Java 9) in Brief

```java
// src/com.bank.app/module-info.java
module com.bank.app {
    requires java.sql;                       // dependency
    requires transitive com.bank.model;      // consumers of this module get it too
    exports com.bank.api;                    // ONLY this package is visible to other modules
    opens com.bank.entity to com.fasterxml.jackson.databind;   // allow deep reflection
    uses com.bank.spi.Notifier;              // service consumer
    provides com.bank.spi.Notifier with com.bank.impl.SmsNotifier;   // service provider
}
```

**Benefits:** **strong encapsulation** (`public` no longer means "public to the world" unless exported) · **reliable
configuration** (missing modules are detected at startup) · **smaller runtimes** with `jlink`.
Most apps still use the classpath (the "unnamed module"), but the JDK itself is modular.

---

## 10. Beyond 21: a Peek at 22–25

```java
// Java 22: unnamed variables & patterns
case Circle(_) -> "a circle";                 // ignore components you don't need
for (var _ : list) count++;
try { ... } catch (Exception _) { ... }

// Java 25: compact source files & instance main (great for scripts and learning)
void main() {
    IO.println("Hello, Java 25!");
}

// Java 25: flexible constructor bodies: statements BEFORE super(...)
class Child extends Parent {
    Child(int value) {
        if (value < 0) throw new IllegalArgumentException();   // validate first
        super(value);
    }
}
```

---

## 🧩 Memory Tricks

| Concept | Trick |
|---|---|
| LTS versions | "**8 · 11 · 17 · 21 · 25**": every 2 years since 17 |
| Version anchors | "**8 lambdas · 11 HTTP · 17 sealed · 21 virtual**" |
| Records | "**A record is a named tuple with free equals/hashCode/toString**" |
| Sealed | "**Guest list · no guests (final) · open party (non-sealed)**" |
| Data-oriented trio | "**Records = data · Sealed = choices · Switch = behaviour**" |
| Pattern order | "**Specific before general**" (dominance) |

---

## ❓ Self-Test

<details><summary>1. What does the compiler generate for <code>record Point(int x, int y)</code>?</summary>

Private final fields `x` and `y`, a canonical constructor, the accessors `x()` and `y()`, and `equals()`,
`hashCode()` and `toString()`. The class is implicitly final and extends `java.lang.Record`.
</details>

<details><summary>2. How do you validate a record's fields?</summary>

Use a compact constructor: `record Age(int value) { Age { if (value < 0) throw new IllegalArgumentException(); } }`.
Assignments to the fields happen automatically at the end of it.
</details>

<details><summary>3. What modifiers must a permitted subclass of a sealed class have?</summary>

Exactly one of `final`, `sealed` (with its own permits) or `non-sealed`. Records and enums are implicitly final.
</details>

<details><summary>4. Why don't you need a default in a switch over a sealed interface?</summary>

The compiler knows all the permitted subtypes, so if every one is covered, the switch is exhaustive. Adding a
new subtype makes the switch fail to compile, which exposes the code that needs updating.
</details>

<details><summary>5. What's wrong with <code>var x = null;</code> and <code>var list = new ArrayList&lt;&gt;();</code>?</summary>

The first doesn't compile, because null has no type to infer. The second compiles but infers
`ArrayList<Object>`, so you lose type safety.
</details>

<details><summary>6. Is a record immutable?</summary>

Shallowly. The fields are final, but a mutable component like a `List` can still be changed. Make a defensive
copy (`List.copyOf`) in the compact constructor.
</details>

<details><summary>7. What does <code>case Integer i when i > 10</code> mean?</summary>

A type pattern with a guard: it matches only when the selector is an Integer **and** the condition is true.
The guard can use the binding variable `i`.
</details>

---

## ✅ Chapter Checklist

- [ ] Write a record with a compact constructor and a static factory
- [ ] Model a sealed hierarchy and write an exhaustive switch with no default
- [ ] Use a nested record pattern
- [ ] List the headline features of 8, 11, 17 and 21
- [ ] Run [code/11_modern](../code/11_modern)

➡️ Next: [12 · Design Patterns & SOLID](12-design-patterns-solid.md)
