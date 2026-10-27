# 01 · Java Basics — The Foundation

> ⏱️ **Revise in:** 45 min · 🎯 **Level:** Beginner · ▶️ **Code:** [code/01_basics](../code/01_basics) · 🏋️ **Practice:** [Exercises → Basics](../exercises/README.md#1-basics)

## 🧠 Mind Map

```
01 JAVA BASICS
├── Platform
│   ├── JDK = JRE + dev tools (javac, jar, javadoc, jdb, jshell)
│   ├── JRE = JVM + core libraries
│   ├── JVM = runs bytecode (platform DEPENDENT)
│   └── Bytecode (.class) = platform INDEPENDENT  → WORA
├── Program anatomy: class · main(String[] args) · statements
├── Variables: local (no default!) · instance · static
├── Data types
│   ├── Primitive (8): byte short int long | float double | char | boolean
│   └── Reference: class · interface · array · enum · record
├── Type casting: widening (auto) · narrowing (cast) · promotion (byte+byte → int)
├── Operators: + - * / % · ++ -- · == != · && || ! · & | ^ ~ · << >> >>> · ?: · instanceof
├── Control flow
│   ├── if / else-if / else
│   ├── switch: classic (fall-through) · arrow (->) · expression (yield)
│   ├── loops: for · while · do-while · for-each
│   └── jumps: break · continue · labeled break · return
├── Arrays: fixed size · .length · defaults · 2D · jagged · Arrays.* utils
├── Methods: signature · return · overloading · varargs · recursion
│   └── Java is ALWAYS pass-by-value (copies of references for objects)
└── I/O basics: System.out.printf · Scanner (nextInt + nextLine trap)
```

## ⚡ 30-Second Summary

- Java code → **javac** → **bytecode** → runs on any **JVM** (Write Once, Run Anywhere).
- **8 primitives** hold raw values; everything else is a **reference** to an object on the heap.
- Small type → big type is **automatic** (widening); big → small needs a **cast** and can lose data.
- `byte + byte`, `short + char` … all become **int** in arithmetic.
- **Local variables have no default value** — the compiler forces you to initialize them.
- Java is **always pass-by-value**. For objects the *reference* is copied, so you can change the object's
  state but never make the caller's variable point to something else.

---

## 1. What is Java? Why is it popular?

**One-liner:** A class-based, object-oriented, strongly-typed language that compiles to bytecode and runs on the JVM.

| Feature | What it means | Remember it as |
|---|---|---|
| Platform independent | Bytecode runs on any OS that has a JVM | "WORA — Write Once, Run Anywhere" |
| Object-oriented | Everything is modeled with classes & objects | (not 100% OOP — primitives & static exist) |
| Robust | Strong typing, exceptions, garbage collection, no pointer arithmetic | "Hard to crash" |
| Secure | No pointers, bytecode verifier, sandboxing | "Can't poke memory" |
| Multithreaded | Built-in thread support | "Does many things at once" |
| High performance | JIT compiles hot code to native machine code | "Starts interpreted, ends compiled" |

> ❓ **Is Java 100% object-oriented?** No — it has **primitive types** (`int`, `char`…) that are not objects,
> and **static** members that belong to no object.

---

## 2. JDK vs JRE vs JVM

```
┌──────────────────────────── JDK (Java Development Kit) ─────────────────────────┐
│  Dev tools: javac (compiler), java (launcher), jar, javadoc, jdb, jshell, jconsole│
│  ┌─────────────────────── JRE (Java Runtime Environment) ───────────────────────┐│
│  │  Core libraries: java.lang, java.util, java.io, java.time ...                 ││
│  │  ┌──────────────────── JVM (Java Virtual Machine) ─────────────────────────┐  ││
│  │  │  ClassLoader · Bytecode Verifier · Interpreter · JIT · Garbage Collector │  ││
│  │  └──────────────────────────────────────────────────────────────────────────┘  ││
│  └────────────────────────────────────────────────────────────────────────────────┘│
└────────────────────────────────────────────────────────────────────────────────────┘
```

| | JDK | JRE | JVM |
|---|---|---|---|
| Purpose | **Develop** + run | **Run** only | **Execute** bytecode |
| Contains | JRE + tools | JVM + libraries | Interpreter, JIT, GC |
| Who needs it | Developers | End users (older setups) | Everyone (inside JRE) |

> 🔑 **Remember:** *JDK ⊃ JRE ⊃ JVM* — "Develop ⊃ Run ⊃ Execute".
> 🔑 **Bytecode is platform-independent; the JVM itself is platform-dependent** (there's a different JVM for
> Windows, Linux, macOS). That's exactly how WORA works.
> 💡 Since Java 11, Oracle ships only the JDK (no separate JRE download). `jlink` can build a slim runtime.

### How a program runs

```
 Hello.java ──javac──► Hello.class ──java──► ClassLoader ──► Bytecode Verifier ──► Execution Engine
 (source)             (bytecode)             (loads)          (safety check)      ├─ Interpreter (line by line)
                                                                                  └─ JIT (hot code → native)
```

```powershell
javac Hello.java      # compile → Hello.class
java Hello            # run the class
java Hello.java       # Java 11+: compile in memory & run in one step (single-file programs)
```

---

## 3. Anatomy of `main`

```java
public class Hello {                          // file must be named Hello.java (if class is public)
    public static void main(String[] args) {  // entry point the JVM looks for
        System.out.println("Hello, Java!");
    }
}
```

| Word | Why it's there |
|---|---|
| `public` | JVM calls it from outside the class |
| `static` | JVM can call it **without creating an object** |
| `void` | returns nothing to the JVM (use `System.exit(code)` for an exit code) |
| `main` | the exact name the JVM searches for |
| `String[] args` | command-line arguments (`java Hello a b` → `args = ["a","b"]`) |

> ✅ Also valid: `static public void main(String... args)`, `public static void main(final String[] a)`.
> ❌ A missing `static` or a different signature compiles fine, but the program won't launch as an entry point.
> 💡 You can overload `main`, but the JVM only calls `main(String[])`.
> 🆕 Java 25 allows a simpler `void main()` (instance main, compact source files). In Java 21 that's a preview
> feature, so use the classic form.

---

## 4. Variables

| Kind | Declared | Lives in | Default value? | Lifetime |
|---|---|---|---|---|
| **Local** | inside method/block | Stack | ❌ **No — must initialize** | until block ends |
| **Instance** | in class, no `static` | Heap (inside object) | ✅ Yes | as long as the object |
| **Static (class)** | in class, with `static` | One copy per class | ✅ Yes | as long as the class is loaded |

**Default values (fields only):** `0` for numbers · `0.0` for float/double · `false` for boolean ·
`'\u0000'` for char · `null` for references.

```java
class Demo {
    int count;              // instance → default 0
    static String app;      // static   → default null
    void test() {
        int x;              // local    → NO default
        // System.out.println(x);   ❌ compile error: variable x might not have been initialized
    }
}
```

**Naming rules:** start with a letter, `$` or `_` · no digits first · no keywords · case-sensitive.
**Conventions:** `ClassName` · `methodName` / `variableName` · `MAX_SIZE` · `com.company.project`.

---

## 5. Data Types

```
                       Data Types
            ┌──────────────┴──────────────┐
        Primitive (8)                 Reference
   ┌────────┼─────────┬────────┐      class · interface · array · enum · record
 Integer  Floating  Character Boolean   (variable holds an ADDRESS, default null)
 byte      float     char      boolean
 short     double
 int
 long
```

| Type | Size | Range | Default | Literal example |
|---|---|---|---|---|
| `byte` | 1 byte | -128 to 127 | 0 | `byte b = 10;` |
| `short` | 2 bytes | -32,768 to 32,767 | 0 | `short s = 1000;` |
| `int` | 4 bytes | ≈ ±2.1 billion (-2³¹ to 2³¹-1) | 0 | `int i = 1_000_000;` |
| `long` | 8 bytes | ≈ ±9.2 × 10¹⁸ | 0L | `long l = 9_000_000_000L;` |
| `float` | 4 bytes | ~6-7 significant digits | 0.0f | `float f = 3.14f;` |
| `double` | 8 bytes | ~15-16 significant digits | 0.0d | `double d = 3.14;` |
| `char` | 2 bytes | 0 to 65,535 (unsigned, UTF-16) | '\u0000' | `char c = 'A';` |
| `boolean` | JVM-dependent | `true` / `false` | false | `boolean ok = true;` |

> 🧩 **Memory trick:** sizes **"1-2-4-8, 4-8, 2"** · order **"Big Sharks Invade Lakes, Fish Dive, Crabs Burrow"**.

**Literals you should recognise:**

```java
int dec = 26, hex = 0x1A, oct = 032, bin = 0b11010;   // all equal 26
long big = 10_000_000_000L;   // L needed: number is outside int range
float f = 1.5f;               // f needed: decimals default to double
char c1 = 'A', c2 = 65, c3 = 'A';                // all 'A'
```

> ⚠️ **Trap:** `float f = 1.5;` ❌ (1.5 is a `double`). `long x = 10000000000;` ❌ (int literal too big — add `L`).
> ⚠️ **Trap:** `0.1 + 0.2 == 0.3` is **false** (binary floating point). Use `BigDecimal` for money.

---

## 6. Type Casting & Promotion

```
WIDENING (automatic, safe)  ───────────────────────────────────►
   byte → short → int → long → float → double
                   ▲
                 char
◄─────────────────────────────────── NARROWING (explicit cast, may lose data)
```

```java
int i = 100;
long l = i;              // widening: automatic
double d = l;            // widening: automatic

double pi = 3.99;
int n = (int) pi;        // narrowing: 3 (decimal part CUT, not rounded)
byte b = (byte) 130;     // -126 (overflow wraps around: 130 - 256)
```

**Type promotion in expressions:** `byte`, `short`, `char` are **promoted to `int`** before arithmetic;
the result is the type of the *largest* operand.

```java
byte a = 10, b = 20;
// byte c = a + b;       ❌ a + b is an int
byte c = (byte) (a + b); // ✅
a += 5;                  // ✅ compound assignment has a hidden cast: a = (byte)(a + 5)
char ch = 'A';
System.out.println(ch + 1);        // 66  (int)
System.out.println((char) (ch + 1)); // B
```

> ⚠️ **Trap:** `long → float` is "widening" but **can lose precision** (float has only ~7 digits).
> ⚠️ **Trap:** `int` overflow is silent: `Integer.MAX_VALUE + 1 == Integer.MIN_VALUE`. Use `Math.addExact()` to get an exception instead.

---

## 7. Operators

| Category | Operators | Notes |
|---|---|---|
| Arithmetic | `+ - * / %` | `7/2 = 3` (int division) · `7.0/2 = 3.5` · `-7 % 3 = -1` (sign follows the dividend) |
| Unary | `++ -- + - !` | `i++` uses then increments · `++i` increments then uses |
| Relational | `== != > < >= <=` | `==` on objects compares **references** |
| Logical | `&& \|\| !` | **short-circuit**: right side skipped if result is known |
| Bitwise | `& \| ^ ~` | on booleans `&` and `\|` evaluate **both** sides |
| Shift | `<< >> >>>` | `>>` keeps the sign · `>>>` fills with 0 |
| Assignment | `= += -= *= /= %=` | compound ops include an implicit cast |
| Ternary | `cond ? a : b` | an expression, not a statement |
| Type | `instanceof` | `obj instanceof String s` (Java 16+ binds `s`) |

```java
int x = 5;
int y = x++ + ++x;   // 5 + 7 = 12, x is now 7
int i = 0;
i = i++;             // i stays 0! (old value is assigned back)

int a = -8;
System.out.println(a >> 1);   // -4          (sign kept)
System.out.println(a >>> 1);  // 2147483644  (zero-filled)

String s = null;
if (s != null && s.length() > 0) { }  // ✅ safe: && short-circuits
// if (s != null & s.length() > 0)    ❌ NPE: & evaluates both sides
```

**Division by zero:** `10 / 0` → `ArithmeticException` · `10.0 / 0` → `Infinity` · `0.0 / 0` → `NaN` (and `NaN != NaN`).

> 🧩 **Precedence (high → low):** `postfix ++ --` → `unary` → `* / %` → `+ -` → `shift` → `relational` →
> `== !=` → `&` → `^` → `|` → `&&` → `||` → `?:` → `=`.  When in doubt, **use parentheses**.

---

## 8. Control Flow

### if / else

```java
if (marks >= 90) grade = 'A';
else if (marks >= 75) grade = 'B';
else grade = 'C';
```

### switch — three generations

```java
// 1) Classic switch — falls through until it hits break
switch (day) {
    case 1: case 7: System.out.println("Weekend"); break;
    case 2:         System.out.println("Monday");  break;
    default:        System.out.println("Weekday");
}

// 2) Arrow switch (Java 14+) — no fall-through, no break needed
switch (day) {
    case 1, 7 -> System.out.println("Weekend");
    case 2    -> System.out.println("Monday");
    default   -> System.out.println("Weekday");
}

// 3) Switch EXPRESSION — returns a value; yield for blocks
String type = switch (day) {
    case 1, 7 -> "Weekend";
    case 2 -> {
        System.out.println("Ugh");
        yield "Monday";          // yield = "return a value from this case"
    }
    default -> "Weekday";
};
```

> 🔑 **Allowed switch types:** `byte short char int` (+ their wrappers), `String`, `enum` — and since
> Java 21, **any type** via pattern matching (see ch 11). ❌ Not `long`, `float`, `double`, `boolean`.
> ⚠️ Classic switch on a `null` String → `NullPointerException`.

### Loops

| Loop | Use when | Runs at least once? |
|---|---|---|
| `for (init; cond; update)` | you know the count | no |
| `while (cond)` | you loop until a condition changes | no |
| `do { } while (cond);` | the body must run first (menus, input validation) | **yes** |
| `for (T x : collection)` | reading every element | no |

```java
outer:                                   // label
for (int i = 0; i < 3; i++) {
    for (int j = 0; j < 3; j++) {
        if (j == 1) continue;            // skip rest of THIS iteration
        if (i == 2) break outer;         // exit BOTH loops
        System.out.println(i + "," + j);
    }
}
```

> ⚠️ for-each **can't** modify the array/list structure or tell you the index. Modifying a list inside
> for-each → `ConcurrentModificationException` (see ch 06).

---

## 9. Arrays

**One-liner:** a fixed-size, indexed container of same-type elements. **An array is an object** (lives on the heap).

```java
int[] a = new int[5];              // [0, 0, 0, 0, 0]  defaults applied
int[] b = {3, 1, 2};               // literal
String[] names = new String[3];    // [null, null, null]

int[][] grid = new int[3][4];      // 3 rows x 4 columns
int[][] jagged = new int[3][];     // rows can have different lengths
jagged[0] = new int[]{1};
jagged[1] = new int[]{1, 2, 3};

System.out.println(b.length);      // 3   ← field, no ()
// b[3] = 9;                       ❌ ArrayIndexOutOfBoundsException
```

**`java.util.Arrays` toolbox:**

| Method | Does |
|---|---|
| `Arrays.toString(a)` / `deepToString(grid)` | readable printing (printing `a` directly shows `[I@1b6d3586`) |
| `Arrays.sort(a)` | sorts (dual-pivot quicksort for primitives, TimSort for objects) |
| `Arrays.binarySearch(a, key)` | O(log n) search — **array must be sorted** |
| `Arrays.fill(a, 7)` | fill every slot |
| `Arrays.copyOf(a, n)` / `copyOfRange(a, from, to)` | resize/copy |
| `Arrays.equals(a, b)` | compares content (`a == b` compares references) |
| `Arrays.asList(...)` | fixed-size `List` view (can `set`, can't `add`/`remove`) |
| `Arrays.stream(a)` | turns the array into a stream |

> 🧩 **"Arrays: field, Strings: method, Collections: size()"** → `arr.length`, `str.length()`, `list.size()`.
> ⚠️ Arrays are **covariant**: `Object[] o = new String[2]; o[0] = 1;` compiles but throws `ArrayStoreException`.

---

## 10. Methods

```java
//  modifier  return  name     parameters        ← name + parameter types = SIGNATURE
    public    int     add     (int a, int b) {
        return a + b;
    }
```

### Overloading — same name, different parameter list

```java
int    add(int a, int b)          { return a + b; }
double add(double a, double b)    { return a + b; }
int    add(int a, int b, int c)   { return a + b + c; }
// int add(int x, int y)  ❌ same signature — parameter NAMES don't matter
// long add(int a, int b) ❌ return type alone is NOT enough
```

> 🧩 **Which overload wins? "Every Wise Boy Votes":** **E**xact match → **W**idening → **B**oxing → **V**arargs.
> `void f(long x)` vs `void f(Integer x)` → `f(5)` calls `f(long)` (widening beats boxing).

### Varargs — variable number of arguments

```java
static int sum(int... nums) {        // nums is an int[]
    int total = 0;
    for (int n : nums) total += n;
    return total;
}
sum(); sum(1); sum(1, 2, 3);          // all valid
```

Rules: only **one** varargs parameter, and it must be **last**.

### Pass-by-value (the #1 confusion)

```java
static void change(int x, StringBuilder sb, StringBuilder sb2) {
    x = 99;                 // changes only the local copy
    sb.append(" World");    // changes the OBJECT (both refs point to it)  → visible to caller
    sb2 = new StringBuilder("New"); // re-points the local copy only       → NOT visible
}

int n = 1;
StringBuilder a = new StringBuilder("Hello"), b = new StringBuilder("Hi");
change(n, a, b);
// n == 1, a == "Hello World", b == "Hi"
```

```
 caller stack        method stack           heap
 n  = 1              x  = 99
 a  ──────────┐      sb ──────┐
              └───────────────┴──► [ "Hello World" ]
 b  ──────────────────────────────► [ "Hi" ]
                     sb2 ─────────► [ "New" ]   (garbage after return)
```

> 🔑 **Java passes a copy of the value. For objects, the value IS the reference.**
> So you can *mutate* the object, but you can't *swap* the caller's references. A `swap(a, b)` method never works in Java.

### Recursion

```java
static long factorial(int n) {
    if (n <= 1) return 1;          // base case — without it: StackOverflowError
    return n * factorial(n - 1);   // recursive case
}
```

---

## 11. Console I/O

```java
System.out.printf("%-10s|%5d|%8.2f|%n", "Apple", 42, 3.14159);  // "Apple     |   42|    3.14|"
String s = String.format("%,d", 1234567);                         // "1,234,567"
```

| Format | Meaning | Format | Meaning |
|---|---|---|---|
| `%d` | integer | `%s` | string |
| `%f`, `%.2f` | decimal (2 places) | `%c` | char |
| `%b` | boolean | `%n` | newline (portable) |
| `%5d` / `%-5s` | width right / left aligned | `%05d` | zero padded |
| `%x` | hex | `%e` | scientific |

```java
Scanner sc = new Scanner(System.in);
int age = sc.nextInt();
sc.nextLine();                 // ⚠️ consume the leftover newline!
String name = sc.nextLine();   // otherwise this reads "" immediately
```

---

## 📊 Quick Comparison Tables

| `i++` | `++i` |
|---|---|
| use the value, then increment | increment, then use the value |
| `int a = 5; int b = a++;` → b=5, a=6 | `int a = 5; int b = ++a;` → b=6, a=6 |

| `&&` / `\|\|` | `&` / `\|` (on booleans) |
|---|---|
| short-circuit (may skip right side) | always evaluate both sides |
| use for conditions | rarely needed; also bitwise on ints |

| `break` | `continue` | `return` |
|---|---|---|
| exits the loop/switch | skips to the next iteration | exits the method |

---

## ❓ Self-Test (answer first, then click)

<details><summary>1. Why is Java platform-independent but the JVM platform-dependent?</summary>

`javac` produces bytecode, which is the same for every OS. Each OS has its own JVM implementation that
translates that same bytecode into its own native instructions.
</details>

<details><summary>2. What does <code>System.out.println(1 + 2 + "3" + 4 + 5);</code> print?</summary>

`3345`. Evaluation is left to right: `1+2=3` (numeric), then `3+"3"="33"` (string), then `"33"+4="334"`, then `"3345"`.
</details>

<details><summary>3. Will <code>byte b = 10; b = b + 1;</code> compile? What about <code>b += 1;</code>?</summary>

No. `b + 1` is an `int` and needs a cast. `b += 1` compiles because compound assignment includes an implicit cast.
</details>

<details><summary>4. What is printed by <code>int i = 0; i = i++; System.out.println(i);</code>?</summary>

`0`. `i++` evaluates to the old value (0), increments i to 1, and then the assignment writes 0 back.
</details>

<details><summary>5. Is Java pass-by-value or pass-by-reference?</summary>

Always pass-by-value. For object arguments the value that gets copied is the reference, so a method can change
the object's state but can't make the caller's variable point to a different object.
</details>

<details><summary>6. What is the output of <code>(int) 3.99</code> and <code>(byte) 200</code>?</summary>

`3` (truncation, not rounding) and `-56` (200 − 256).
</details>

<details><summary>7. Which overload is called: <code>f(long)</code> or <code>f(Integer)</code> for <code>f(5)</code>?</summary>

`f(long)`, because widening beats boxing (Exact → Widening → Boxing → Varargs).
</details>

<details><summary>8. Do local variables get default values?</summary>

No. Only fields (instance and static) and array elements get defaults. Reading a local variable before
assigning it is a compile error.
</details>

<details><summary>9. Can you switch on a <code>long</code>?</summary>

No. Classic switch works on `byte/short/char/int` (and their wrappers), `String` and `enum`. Java 21 pattern
matching allows any reference type, but primitive `long`, `float`, `double` and `boolean` still aren't allowed.
</details>

<details><summary>10. Difference between <code>a.length</code>, <code>s.length()</code> and <code>list.size()</code>?</summary>

An array has a `length` field, a String has a `length()` method, and a Collection has a `size()` method.
</details>

---

## ✅ Chapter Checklist

- [ ] Draw JDK ⊃ JRE ⊃ JVM from memory
- [ ] List all 8 primitives with sizes
- [ ] Explain widening vs narrowing with an example of data loss
- [ ] Write all 3 forms of switch
- [ ] Explain pass-by-value with the StringBuilder example
- [ ] Run [`code/01_basics`](../code/01_basics) demos and predict outputs **before** running

➡️ Next: [02 · OOP](02-oops.md)
