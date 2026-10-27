# 03 · Strings, Wrapper Classes, Math & Enums

> ⏱️ **Revise in:** 45 min · 🎯 **Level:** Beginner → Intermediate · ▶️ **Code:** [code/03_strings](../code/03_strings) · 🏋️ **Practice:** [Exercises → Strings](../exercises/README.md#3-strings)

## 🧠 Mind Map

```
03 STRINGS · WRAPPERS · ENUMS
├── String
│   ├── final class, IMMUTABLE, backed by byte[] (compact strings, Java 9)
│   ├── String Pool (in heap): literals are shared
│   │   ├── "a"            → pool
│   │   ├── new String("a")→ new heap object (+ pool literal if missing)
│   │   └── intern()       → returns the pooled copy
│   ├── == (same object?) vs equals() (same text?)
│   ├── Why immutable? Security · Pool sharing · Thread-safe · Cached hashCode  ("SPTH")
│   └── Methods: length charAt substring indexOf contains split replace strip isBlank join repeat
├── StringBuilder (fast, NOT synced) vs StringBuffer (synchronized, legacy)
├── Wrappers: Byte Short Integer Long Float Double Character Boolean
│   ├── Autoboxing int → Integer (Integer.valueOf) · Unboxing Integer → int (intValue)
│   ├── Integer cache -128..127 → == works by accident inside the range only!
│   ├── unboxing null → NullPointerException
│   └── parseInt → int · valueOf → Integer
├── Math: abs pow sqrt max min round floor ceil random · BigDecimal (money) · BigInteger
└── Enum
    ├── fixed set of constants, each a singleton object · implicitly final, extends Enum
    ├── values() · valueOf() · ordinal() · name()
    ├── fields + private constructor + methods + constant-specific bodies
    └── EnumMap · EnumSet · switch on enum · enum Singleton
```

## ⚡ 30-Second Summary

- **Strings are immutable.** Every "modification" creates a **new** String.
- **Always compare Strings with `equals()`**, never `==`.
- Building strings in a loop? Use **StringBuilder**.
- Wrapper `==` is a trap: `Integer 127 == 127` → true, `Integer 128 == 128` → **false**.
- Unboxing `null` → **NPE**.
- **Never use `double` for money**: use `BigDecimal` with the **String** constructor.
- Enums are full classes: they can have fields, constructors, methods, and implement interfaces.

---

## 1. String Basics & the String Pool

```java
String s1 = "Java";                 // literal → String Pool
String s2 = "Java";                 // reuses the SAME pooled object
String s3 = new String("Java");     // forces a NEW object on the heap
String s4 = s3.intern();            // returns the pooled one

s1 == s2        // true   (same pooled object)
s1 == s3        // false  (different objects)
s1.equals(s3)   // true   (same characters)
s1 == s4        // true
```

```
                 HEAP
 s1 ──┐   ┌──── String Pool ────┐
 s2 ──┼──►│   "Java"            │◄── s4 (intern)
      │   └─────────────────────┘
 s3 ─────► [ "Java" ]  (separate object)
```

> ❓ **How many objects does `new String("Java")` create?** **One or two:** the heap object always, plus
> the `"Java"` literal in the pool **if it isn't already there**.

### Compile-time vs runtime concatenation

```java
String a = "Ja" + "va";        // constant folding at COMPILE time → "Java" from the pool
System.out.println(a == "Java");          // true

String part = "Ja";
String b = part + "va";        // RUNTIME concat → new object
System.out.println(b == "Java");          // false

final String fpart = "Ja";     // final + constant → compile-time constant
String c = fpart + "va";
System.out.println(c == "Java");          // true
```

### Why is String immutable? → "**SPTH**"

| Reason | Explanation |
|---|---|
| **S**ecurity | file paths, URLs, class names, DB credentials can't be altered after validation |
| **P**ool | sharing one object among many references is only safe if nobody can change it |
| **T**hread-safety | immutable objects can be shared across threads without locks |
| **H**ashCode caching | computed once and cached → fast, reliable `HashMap` keys |

```java
String s = "hello";
s.toUpperCase();            // returns a NEW String, which is discarded here
System.out.println(s);      // "hello"
s = s.toUpperCase();        // re-point the reference
System.out.println(s);      // "HELLO"
```

---

## 2. String Methods Cheat Table

| Method | Example | Result |
|---|---|---|
| `length()` | `"Java".length()` | `4` |
| `charAt(i)` | `"Java".charAt(1)` | `'a'` |
| `substring(b, e)` | `"Hello".substring(1, 3)` | `"el"` (end **exclusive**) |
| `substring(b)` | `"Hello".substring(2)` | `"llo"` |
| `indexOf / lastIndexOf` | `"banana".indexOf('a')` / `lastIndexOf('a')` | `1` / `5` (`-1` if absent) |
| `contains` | `"Java".contains("av")` | `true` |
| `startsWith / endsWith` | `"file.txt".endsWith(".txt")` | `true` |
| `equals / equalsIgnoreCase` | `"a".equalsIgnoreCase("A")` | `true` |
| `compareTo` | `"apple".compareTo("banana")` | negative (`'a'-'b' = -1`) |
| `toUpperCase / toLowerCase` | `"Hi".toUpperCase()` | `"HI"` |
| `trim()` / `strip()` | `"  hi  ".strip()` | `"hi"` (strip is Unicode-aware, Java 11) |
| `isEmpty()` / `isBlank()` | `"  ".isEmpty()` / `"  ".isBlank()` | `false` / `true` (Java 11) |
| `replace` | `"a-b-c".replace("-", "+")` | `"a+b+c"` (literal) |
| `replaceAll` | `"a1b22".replaceAll("\\d", "")` | `"ab"` (**regex**) |
| `split` | `"a,b,c".split(",")` | `["a","b","c"]` (**regex**) |
| `String.join` | `String.join("-", "a", "b")` | `"a-b"` |
| `repeat` | `"ab".repeat(3)` | `"ababab"` (Java 11) |
| `toCharArray` | `"abc".toCharArray()` | `['a','b','c']` |
| `chars()` | `"abc".chars().count()` | `3` (an `IntStream`) |
| `String.valueOf` | `String.valueOf(42)` | `"42"` |
| `format / formatted` | `"%d%%".formatted(50)` | `"50%"` |
| `lines()` | `"a\nb".lines().count()` | `2` (Java 11) |

> ⚠️ **Trap:** `"a.b.c".split(".")` returns an **empty array**, because `.` is a regex meaning "any char".
> Use `split("\\.")`. The same goes for `|`, `*`, `+`, `?`, `$`.
> ⚠️ **Trap:** `null + "a"` → `"nulla"`, but `null.length()` → NPE.
> ⚠️ **Trap:** `"abc".substring(3)` → `""` (valid), `"abc".substring(4)` → `StringIndexOutOfBoundsException`.

**Useful `Character` methods:** `isDigit`, `isLetter`, `isLetterOrDigit`, `isUpperCase`, `isWhitespace`,
`toUpperCase`, `getNumericValue('7') → 7`, and the classic trick `ch - '0'` → digit value.

---

## 3. String vs StringBuilder vs StringBuffer

| | `String` | `StringBuilder` | `StringBuffer` |
|---|---|---|---|
| Mutable? | ❌ immutable | ✅ | ✅ |
| Thread-safe? | ✅ (immutable) | ❌ | ✅ (synchronized) |
| Speed | slow for repeated changes | **fastest** | slower (locking) |
| Since | 1.0 | 5 | 1.0 |
| Use for | fixed text, keys, constants | building strings in one thread (**99% of cases**) | legacy multi-threaded code |

```java
// ❌ O(n²): creates a new String every iteration
String s = "";
for (int i = 0; i < 10_000; i++) s += i;

// ✅ O(n): one growable buffer
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 10_000; i++) sb.append(i);
String result = sb.toString();
```

**StringBuilder API:** `append` · `insert(i, x)` · `delete(s, e)` · `deleteCharAt(i)` · `reverse()` ·
`replace(s, e, str)` · `setCharAt(i, c)` · `length()` · `capacity()` (default 16, grows to `(old*2)+2`).

> ⚠️ `StringBuilder` does **not** override `equals()`. Compare with `sb1.toString().equals(sb2.toString())`
> or `sb1.compareTo(sb2) == 0` (Java 11).

---

## 4. Wrapper Classes

| Primitive | Wrapper | Primitive | Wrapper |
|---|---|---|---|
| `byte` | `Byte` | `float` | `Float` |
| `short` | `Short` | `double` | `Double` |
| `int` | **`Integer`** | `char` | **`Character`** |
| `long` | `Long` | `boolean` | `Boolean` |

**Why do they exist?** Collections and generics need objects (`List<int>` ❌ → `List<Integer>` ✅) ·
they can be `null` · they carry utility methods (`Integer.parseInt`, `Character.isDigit`).

### Autoboxing & unboxing (Java 5)

```java
Integer boxed = 10;        // autoboxing → Integer.valueOf(10)
int raw = boxed;           // unboxing   → boxed.intValue()
List<Integer> list = new ArrayList<>();
list.add(5);               // autoboxing
int first = list.get(0);   // unboxing
```

### ⚠️ The Integer cache trap

```java
Integer a = 127, b = 127;
System.out.println(a == b);        // true   ← cached object (-128..127)
Integer c = 128, d = 128;
System.out.println(c == d);        // false  ← two different objects!
System.out.println(c.equals(d));   // true   ← ALWAYS use equals for wrappers
```

> 🔑 `Integer.valueOf()` caches **-128 to 127** (`Byte`, `Short`, `Long` too; `Character` caches 0–127;
> `Boolean` has TRUE/FALSE). `Float`/`Double` are **not** cached.

### Other wrapper traps

```java
Integer maybe = null;
int x = maybe;                         // 💥 NullPointerException (unboxing null)

Long big = 10L;
big.equals(10);                        // false! 10 autoboxes to Integer, and Long ≠ Integer
big.equals(10L);                       // true

List<Integer> nums = new ArrayList<>(List.of(10, 20, 30));
nums.remove(1);                        // removes INDEX 1 → [10, 30]
nums.remove(Integer.valueOf(10));      // removes the VALUE 10 → [30]

Long sum = 0L;                         // ❌ boxing in a loop is slow (creates many objects)
for (long i = 0; i < 1_000_000; i++) sum += i;   // use primitive long instead
```

| `Integer.parseInt("42")` | `Integer.valueOf("42")` |
|---|---|
| returns primitive `int` | returns an `Integer` object (may be cached) |
| both throw `NumberFormatException` for `"abc"` | |

**Handy:** `Integer.MAX_VALUE`, `Integer.toBinaryString(10)` → `"1010"`, `Integer.toHexString(255)` → `"ff"`,
`Integer.parseInt("ff", 16)` → `255`, `Integer.compare(a, b)`, `Character.getNumericValue('9')`.

---

## 5. Math, BigDecimal, BigInteger

| Method | Example | Result |
|---|---|---|
| `Math.abs` | `Math.abs(-5)` | `5` |
| `Math.pow` | `Math.pow(2, 10)` | `1024.0` (double!) |
| `Math.sqrt / cbrt` | `Math.sqrt(16)` | `4.0` |
| `Math.max / min` | `Math.max(3, 7)` | `7` |
| `Math.round` | `Math.round(2.5)` / `Math.round(-2.5)` | `3` / `-2` (rounds half **up**: `floor(x+0.5)`) |
| `Math.floor / ceil` | `Math.floor(-1.5)` / `Math.ceil(-1.5)` | `-2.0` / `-1.0` |
| `Math.random()` | | double in `[0.0, 1.0)` |
| `Math.floorMod` | `Math.floorMod(-7, 3)` | `2` (always non-negative for a positive divisor) |

**Random int in [min, max]:** `ThreadLocalRandom.current().nextInt(min, max + 1)` or `(int)(Math.random() * (max - min + 1)) + min`.

### BigDecimal: the only right way to do money

```java
System.out.println(0.1 + 0.2);                       // 0.30000000000000004 ❌

BigDecimal a = new BigDecimal("0.1");                // ✅ String constructor
BigDecimal b = new BigDecimal("0.2");
System.out.println(a.add(b));                        // 0.3 ✅

new BigDecimal(0.1);                                 // ❌ 0.1000000000000000055511151231257827...
BigDecimal.valueOf(0.1);                             // ✅ ok (uses Double.toString)

BigDecimal price = new BigDecimal("10");
price.divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP);   // 3.33  (without a scale → ArithmeticException)

new BigDecimal("2.0").equals(new BigDecimal("2.00"));         // false! (scale differs)
new BigDecimal("2.0").compareTo(new BigDecimal("2.00")) == 0; // true  ← compare values with compareTo
```

**BigInteger:** arbitrary-size integers, e.g. `BigInteger.valueOf(50)` factorials, cryptography.
Both are **immutable**: `a.add(b)` returns a new object.

---

## 6. Enums

**One-liner:** a type with a **fixed set of named constants**. Each constant is a single object
(an implicit `public static final` instance).

```java
enum Level { LOW, MEDIUM, HIGH }

Level l = Level.HIGH;
l.name();                 // "HIGH"
l.ordinal();              // 2 (position; avoid depending on it)
Level.valueOf("LOW");     // Level.LOW (IllegalArgumentException if no such constant)
Level.values();           // [LOW, MEDIUM, HIGH]
l == Level.HIGH;          // ✅ == is safe for enums (singletons)
```

### Enums with fields, constructors, methods and constant-specific behaviour

```java
enum Planet {
    MERCURY(3.303e+23, 2.4397e6),
    EARTH(5.976e+24, 6.37814e6);

    private final double mass, radius;          // fields
    Planet(double mass, double radius) {         // constructor is implicitly PRIVATE
        this.mass = mass;
        this.radius = radius;
    }
    double surfaceGravity() { return 6.67300E-11 * mass / (radius * radius); }
}

enum Operation {
    PLUS("+")  { int apply(int a, int b) { return a + b; } },   // constant-specific body
    TIMES("*") { int apply(int a, int b) { return a * b; } };

    private final String symbol;
    Operation(String symbol) { this.symbol = symbol; }
    abstract int apply(int a, int b);            // each constant MUST implement it
}
```

### Switch on an enum

```java
String msg = switch (level) {        // exhaustive: no default needed if every constant is covered
    case LOW -> "relax";
    case MEDIUM -> "watch";
    case HIGH -> "act now";
};
```

### Enum facts

| Fact | |
|---|---|
| Extends | `java.lang.Enum` implicitly → **can't extend another class** |
| Implements | ✅ interfaces |
| Constructor | always private (you can't `new` an enum) |
| Final? | implicitly final (unless constants have bodies, which are anonymous subclasses) |
| Comparable | ✅ by ordinal |
| Collections | **`EnumSet`** (bit vector, super fast) · **`EnumMap`** (array-based) |
| Singleton | `enum Config { INSTANCE; }` = the best singleton (thread-safe, serialization- and reflection-safe) |

---

## 🧩 Memory Tricks

| Concept | Trick |
|---|---|
| Why String is immutable | **SPTH**: Security, Pool, Thread-safety, HashCode caching |
| StringBuilder vs StringBuffer | "Buff**er** is old**er** and slow**er** (synchronized)" |
| Integer cache | "**One byte of cache: -128 to 127**" |
| Wrappers comparison | "**Wrappers? Use equals. Never ==.**" |
| Money | "**Money needs Big Decimals, from Strings**" |
| `split`/`replaceAll` | "**They speak regex**" (escape `.`, `|`, `*`) |

---

## ❓ Self-Test

<details><summary>1. <code>String s = new String("hi");</code> How many objects are created?</summary>

One or two: a new heap object always, plus the `"hi"` literal in the pool if it doesn't exist yet.
</details>

<details><summary>2. Output of <code>"Ja" + "va" == "Java"</code>? And with <code>String p = "Ja"; p + "va" == "Java"</code>?</summary>

`true` (folded into a constant at compile time) and `false` (concatenated at runtime into a new object).
If `p` were `final`, it would be `true` again.
</details>

<details><summary>3. Why is String a good HashMap key?</summary>

It's immutable, so its hashCode never changes and is cached after the first computation. It also correctly
overrides `equals()` and `hashCode()`.
</details>

<details><summary>4. <code>Integer a = 1000, b = 1000; a == b</code>?</summary>

`false`. 1000 is outside the -128..127 cache, so autoboxing creates two separate objects. Use `equals`.
</details>

<details><summary>5. What does <code>Math.round(-2.5)</code> return?</summary>

`-2`. `Math.round` computes `floor(x + 0.5)` = `floor(-2.0)` = `-2`.
</details>

<details><summary>6. Why prefer <code>new BigDecimal("0.1")</code> over <code>new BigDecimal(0.1)</code>?</summary>

The double literal 0.1 is already imprecise in binary, and the `double` constructor preserves that
imprecision exactly (0.1000000000000000055…). The String constructor represents 0.1 exactly.
</details>

<details><summary>7. Can an enum extend a class? Implement an interface? Have a public constructor?</summary>

It can't extend a class (it already extends `Enum`). It can implement interfaces. It can't have a public
constructor; enum constructors are always private.
</details>

<details><summary>8. StringBuilder vs StringBuffer?</summary>

Both are mutable. StringBuffer is synchronized, so it's thread-safe but slower. StringBuilder isn't
synchronized and is faster, which makes it the default choice.
</details>

---

## ✅ Chapter Checklist

- [ ] Draw the String pool diagram for `s1`, `s2`, `new String`, `intern`
- [ ] Explain SPTH
- [ ] Explain the Integer cache and demonstrate the `==` trap
- [ ] Write an enum with a field, a constructor and an abstract method
- [ ] Run [code/03_strings](../code/03_strings)

➡️ Next: [04 · Exceptions](04-exceptions.md)
