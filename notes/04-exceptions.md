# 04 · Exception Handling

> ⏱️ **Revise in:** 40 min · 🎯 **Level:** Intermediate · ▶️ **Code:** [code/04_exceptions](../code/04_exceptions) · 🏋️ **Practice:** [Exercises → Exceptions](../exercises/README.md#4-exceptions)

## 🧠 Mind Map

```
04 EXCEPTIONS
├── What: an object describing an abnormal event that disrupts normal flow
├── Hierarchy
│   └── Throwable
│       ├── Error  (JVM trouble, don't catch) → OutOfMemoryError · StackOverflowError
│       └── Exception
│           ├── CHECKED (compiler forces handling) → IOException · SQLException
│           │       · ClassNotFoundException · InterruptedException · FileNotFoundException
│           └── RuntimeException = UNCHECKED (programming bugs) → NullPointerException
│                   · ArrayIndexOutOfBounds · ClassCast · Arithmetic · NumberFormat
│                   · IllegalArgument · IllegalState · ConcurrentModification
├── Keywords: try · catch · finally · throw · throws
├── Features: multi-catch (A | B) · try-with-resources (AutoCloseable) · suppressed · chaining (cause)
├── Rules
│   ├── catch order: most specific → most general
│   ├── finally runs always (except System.exit / JVM crash)
│   ├── return in finally OVERRIDES the try's return or exception (never do it)
│   └── overriding method can't throw broader/new CHECKED exceptions
├── Custom exceptions: extends Exception (checked) or RuntimeException (unchecked)
└── Best practices: catch specific · don't swallow · log OR rethrow · fail fast · clean up resources
```

## ⚡ 30-Second Summary

- **Checked** exceptions (`IOException`) **must** be caught or declared with `throws`; the compiler checks.
- **Unchecked** exceptions (`RuntimeException` and subclasses) are usually **bugs**; fix the code.
- **Errors** (`OutOfMemoryError`) are serious JVM problems; don't catch them.
- `finally` always runs; use **try-with-resources** for anything closeable.
- `throw` **throws** an exception object; `throws` **declares** that a method might throw.

---

## 1. The Exception Hierarchy

```
                          java.lang.Object
                                 │
                          java.lang.Throwable
                 ┌───────────────┴───────────────┐
               Error                          Exception
         (abnormal JVM state)         ┌──────────┴───────────────┐
      OutOfMemoryError         (checked ones)              RuntimeException  ← UNCHECKED
      StackOverflowError       IOException                  NullPointerException
      NoClassDefFoundError       └ FileNotFoundException    ArithmeticException
      AssertionError           SQLException                 ClassCastException
                               ClassNotFoundException       IndexOutOfBoundsException
                               InterruptedException           ├ ArrayIndexOutOfBounds...
                               CloneNotSupportedException     └ StringIndexOutOfBounds...
                               (your custom checked ones)   IllegalArgumentException
                                                              └ NumberFormatException
                                                            IllegalStateException
                                                            UnsupportedOperationException
                                                            ConcurrentModificationException
```

### Checked vs unchecked

| | Checked | Unchecked |
|---|---|---|
| Subclass of | `Exception` (but not `RuntimeException`) | `RuntimeException` (or `Error`) |
| Compiler forces handling? | ✅ catch or declare | ❌ |
| Typical cause | **external world**: file missing, network down, DB error | **programmer bug**: null, bad index, bad cast |
| Recoverable? | often (retry, fallback, ask the user) | fix the code instead |
| Examples | `IOException`, `SQLException`, `InterruptedException` | `NullPointerException`, `IllegalArgumentException` |

> 🧩 **Memory trick:** "**Checked = the world's fault (compiler Checks you planned for it). Unchecked = your fault.**"

> ❓ **`ClassNotFoundException` vs `NoClassDefFoundError`?** The first is checked and thrown when you load a
> class **dynamically by name** (`Class.forName("x")`) and it doesn't exist. The second is an Error: the class
> **was present at compile time** but is missing at runtime.

---

## 2. try / catch / finally

```java
try {
    int[] a = new int[2];
    a[5] = 10;                                   // throws → jumps to the matching catch
    System.out.println("never printed");
} catch (ArrayIndexOutOfBoundsException e) {     // specific first
    System.out.println("Bad index: " + e.getMessage());
} catch (RuntimeException e) {                   // more general later
    System.out.println("Something else");
} finally {
    System.out.println("Always runs: close files, release locks");
}
```

**Valid combinations:** `try-catch` · `try-finally` · `try-catch-finally` · `try-with-resources` (catch/finally optional).
A `try` alone is ❌.

**Catch order rule:** subclasses before superclasses. `catch (Exception e)` followed by `catch (IOException e)` is a
**compile error**, because the second block is unreachable.

### Multi-catch (Java 7)

```java
try { ... }
catch (NumberFormatException | ArithmeticException e) {   // the types must NOT be parent/child of each other
    // e is implicitly final here
}
```

### When does finally NOT run?

`System.exit()` · a JVM crash or kill (`kill -9`, power cut) · an infinite loop/deadlock in try · the thread
being a daemon thread when the JVM exits.

### ⚠️ The return-in-finally trap

```java
static int test() {
    try {
        return 1;
    } finally {
        return 2;            // overrides the 1, and would even swallow an exception!
    }
}
// test() returns 2

static int test2() {
    int x = 1;
    try {
        return x;            // the return VALUE (1) is saved now
    } finally {
        x = 99;              // changing x doesn't change the saved value
    }
}
// test2() returns 1
```

---

## 3. try-with-resources (Java 7) ⭐

Any object implementing **`AutoCloseable`** (or `Closeable`) is closed automatically, even if an exception
occurs.

```java
try (BufferedReader br = Files.newBufferedReader(Path.of("data.txt"));
     BufferedWriter bw = Files.newBufferedWriter(Path.of("out.txt"))) {
    bw.write(br.readLine());
} catch (IOException e) {
    System.err.println("I/O failed: " + e.getMessage());
}
// bw closes first, then br  →  resources close in REVERSE order of declaration
```

**Suppressed exceptions:** if the try block throws and `close()` throws too, the try's exception is the one
propagated, and close's exception is attached to it via `e.getSuppressed()`.

> 🔑 Java 9+: you can use an existing **effectively final** variable: `try (reader) { ... }`.

---

## 4. throw vs throws

| `throw` | `throws` |
|---|---|
| **does** it: throws one exception object | **declares** it: lists possible exceptions |
| inside a method body | in the method signature |
| `throw new IllegalArgumentException("age < 0");` | `void read() throws IOException, SQLException` |
| followed by an instance | followed by class names |

```java
void setAge(int age) {
    if (age < 0) throw new IllegalArgumentException("Age can't be negative: " + age);
    this.age = age;
}

void loadConfig() throws IOException {         // caller must handle it
    Files.readString(Path.of("config.txt"));
}
```

**Propagation:** an uncaught exception travels **up the call stack** (method → caller → … → `main`). If no one
catches it, the thread dies and the stack trace is printed.

---

## 5. Custom Exceptions

```java
// Checked: the caller MUST deal with it (a recoverable business condition)
public class InsufficientFundsException extends Exception {
    private final double shortBy;
    public InsufficientFundsException(String message, double shortBy) {
        super(message);
        this.shortBy = shortBy;
    }
    public double getShortBy() { return shortBy; }
}

// Unchecked: an invalid input or programming error
public class InvalidAccountException extends RuntimeException {
    public InvalidAccountException(String message, Throwable cause) {
        super(message, cause);                   // keep the root cause!
    }
}
```

**Naming:** end with `Exception`. **Provide constructors** `(String message)` and `(String message, Throwable cause)`.
**Choose checked** when the caller can reasonably recover, **unchecked** otherwise. Modern frameworks like Spring
favour unchecked.

### Exception chaining (wrapping)

```java
try {
    repository.save(user);
} catch (SQLException e) {
    throw new DataAccessException("Could not save user " + user.id(), e);   // e becomes the "cause"
}
// Stack trace shows: DataAccessException ... Caused by: SQLException ...
```

---

## 6. Exceptions & Overriding

| Parent method throws | Child override may throw |
|---|---|
| nothing | nothing checked (unchecked is always fine) |
| `IOException` | `IOException`, `FileNotFoundException` (subclass), or nothing |
| `IOException` | ❌ **not** `Exception` (broader) or `SQLException` (new checked) |

> 🧩 "**Children can't be scarier than their parents.**" (Liskov: code written for the parent must still compile.)

---

## 7. The Most Common Runtime Exceptions (and the usual cause)

| Exception | Typical cause | Fix |
|---|---|---|
| `NullPointerException` | calling a method on `null`, unboxing `null` | null checks, `Optional`, `Objects.requireNonNull` |
| `ArrayIndexOutOfBoundsException` | `a[a.length]`, off-by-one loops | `i < a.length` |
| `StringIndexOutOfBoundsException` | `substring`/`charAt` beyond length | check lengths |
| `ClassCastException` | bad downcast | `instanceof` check first |
| `ArithmeticException` | integer `/ 0` | validate the divisor |
| `NumberFormatException` | `Integer.parseInt("12a")` | validate input |
| `IllegalArgumentException` | invalid method argument | validate early (fail fast) |
| `IllegalStateException` | object in the wrong state (e.g. `iterator.remove()` twice) | check state |
| `ConcurrentModificationException` | modifying a collection while iterating | `Iterator.remove()` / `removeIf` |
| `UnsupportedOperationException` | `List.of(...).add(x)`, `Arrays.asList(...).add(x)` | use a mutable copy |

> 💡 **Helpful NPE messages (Java 14+):** `Cannot invoke "String.length()" because "user.name" is null`
> tells you exactly what was null.

---

## 8. Best Practices ✅ / Anti-patterns ❌

| ✅ Do | ❌ Don't |
|---|---|
| Catch the **most specific** exception | `catch (Exception e)` everywhere; never `catch (Throwable t)` |
| Use try-with-resources | close manually in finally (easy to get wrong) |
| Include context: `"User 42 not found"` | `throw new Exception("error")` |
| Keep the cause when wrapping (`new X(msg, e)`) | lose the root cause |
| **Log OR rethrow** (once) | log-and-rethrow at every layer (duplicate logs) |
| Validate arguments early (fail fast) | let bad data travel deep |
| Restore the interrupt flag: `Thread.currentThread().interrupt()` | swallow `InterruptedException` |
| Use exceptions for exceptional cases | use exceptions for normal control flow |

```java
catch (IOException e) { }      // ❌ the worst: swallowed silently, the bug becomes invisible
```

---

## 🧩 Memory Tricks

| Concept | Trick |
|---|---|
| Checked vs unchecked | "**World's fault vs your fault**" |
| Catch order | "**Small fish before big fish**" (specific → general) |
| throw vs throws | "**throw = do it · throws = warn about it**" |
| Overriding | "**Children can't be scarier than parents**" |
| Resource closing | "**Last opened, first closed**" |
| final/finally/finalize | "**final = fixed · finally = always · finalize = deprecated GC hook**" |

---

## ❓ Self-Test

<details><summary>1. Is it possible to have try without catch?</summary>

Yes. Use `try-finally`, or try-with-resources, which needs neither catch nor finally.
</details>

<details><summary>2. What's returned by <code>try { return 1; } finally { return 2; }</code>?</summary>

`2`. The finally block's return overrides the try's return. It would also swallow an exception thrown in
try, which is why you should never return from finally.
</details>

<details><summary>3. Can we catch an Error?</summary>

Technically yes, since it's a Throwable, but you shouldn't. Errors like `OutOfMemoryError` mean the JVM is in
a state where recovery usually isn't possible.
</details>

<details><summary>4. Why is <code>catch (Exception e) {} catch (IOException e) {}</code> a compile error?</summary>

The `IOException` catch is unreachable because `Exception` already catches it. Catch blocks must go from
specific to general.
</details>

<details><summary>5. What happens to an exception thrown in close() inside try-with-resources?</summary>

If the try block also threw, the close() exception is added as a suppressed exception (`getSuppressed()`)
and the original exception propagates. Otherwise, the close() exception itself propagates.
</details>

<details><summary>6. Checked or unchecked for a custom "UserNotFound" exception?</summary>

It depends on whether the caller can recover. Many modern codebases (Spring) use unchecked exceptions for
cleaner APIs. Use checked when you want to force the caller to handle the case, e.g. `InsufficientFundsException`
in a banking API.
</details>

<details><summary>7. Can a child's overriding method throw a RuntimeException the parent didn't declare?</summary>

Yes. Unchecked exceptions are unrestricted. Only new or broader **checked** exceptions are forbidden.
</details>

---

## ✅ Chapter Checklist

- [ ] Draw the hierarchy (Throwable → Error / Exception → RuntimeException)
- [ ] Give 4 checked and 6 unchecked examples
- [ ] Write a custom checked exception with a cause constructor
- [ ] Explain the try-with-resources closing order and suppressed exceptions
- [ ] Run [code/04_exceptions](../code/04_exceptions)

➡️ Next: [05 · Generics](05-generics.md)
