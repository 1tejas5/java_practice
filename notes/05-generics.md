# 05 · Generics

> ⏱️ **Revise in:** 30 min · 🎯 **Level:** Intermediate · ▶️ **Code:** [code/05_generics](../code/05_generics) · 🏋️ **Practice:** [Exercises → Generics](../exercises/README.md#5-generics)

## 🧠 Mind Map

```
05 GENERICS  ("parameterized types": write once, type-safe for any type)
├── Why: compile-time type safety · no casts · reusable algorithms
├── Syntax
│   ├── Generic class      class Box<T> { T value; }
│   ├── Generic interface  interface Repository<T, ID>
│   ├── Generic method     static <T> T first(List<T> list)
│   └── Diamond            Box<String> b = new Box<>();
├── Naming: T type · E element · K key · V value · N number · R result
├── Bounded types
│   ├── <T extends Number>                (upper bound: T is Number or a subtype)
│   └── <T extends Number & Comparable<T>> (multiple bounds: class first, then interfaces)
├── Wildcards (?)
│   ├── List<?>               unknown type (read as Object)
│   ├── List<? extends T>     PRODUCER: read T, can't add
│   └── List<? super T>       CONSUMER: add T, read as Object
│   └── PECS = Producer Extends, Consumer Super
├── Invariance: List<Integer> is NOT a List<Number> (arrays ARE covariant → ArrayStoreException)
└── Type erasure (generics exist only at compile time)
    ├── ❌ new T()  ❌ new T[]  ❌ instanceof List<String>  ❌ List<int>
    ├── ❌ static T field  ❌ overload f(List<String>) + f(List<Integer>)
    └── the compiler inserts casts + bridge methods
```

## ⚡ 30-Second Summary

- Generics move type errors **from runtime to compile time**.
- `<T>` is a placeholder. The user of the class decides what T is (`Box<String>`).
- `<T extends X>` restricts T to X or its subtypes.
- **PECS:** read from `? extends T`, write into `? super T`.
- **Type erasure:** at runtime `List<String>` and `List<Integer>` are both just `List`.

---

## 1. Why Generics?

```java
// ❌ Before Java 5 (raw types)
List list = new ArrayList();
list.add("hello");
list.add(42);                         // no complaint
String s = (String) list.get(1);      // 💥 ClassCastException at RUNTIME

// ✅ With generics
List<String> names = new ArrayList<>();
names.add("hello");
// names.add(42);                     ❌ caught at COMPILE time
String first = names.get(0);          // no cast needed
```

> 🔑 **The 3 benefits:** **Type safety** (errors at compile time) · **No casting** · **Code reuse** (one
> algorithm for all types).

---

## 2. Generic Classes, Interfaces & Methods

```java
class Box<T> {                                   // T = type parameter
    private T value;
    void set(T value) { this.value = value; }
    T get() { return value; }
}
Box<String> b = new Box<>();                     // T = String (diamond <> infers it)
b.set("Java");
String v = b.get();

class Pair<K, V> {                               // multiple type parameters
    private final K key; private final V value;
    Pair(K key, V value) { this.key = key; this.value = value; }
    K getKey() { return key; }
    V getValue() { return value; }
}

interface Repository<T, ID> {                    // generic interface
    void save(T entity);
    Optional<T> findById(ID id);
}

class Util {
    static <T> void printAll(List<T> items) {    // generic METHOD: <T> goes BEFORE the return type
        for (T item : items) System.out.println(item);
    }
    static <T extends Comparable<T>> T max(List<T> list) {
        T best = list.get(0);
        for (T t : list) if (t.compareTo(best) > 0) best = t;
        return best;
    }
}
Util.max(List.of(3, 9, 2));          // 9  (T inferred as Integer)
Util.<String>printAll(List.of("a")); // explicit type witness (rarely needed)
```

| Convention | Meaning | Example |
|---|---|---|
| `T` | Type | `Box<T>` |
| `E` | Element | `List<E>` |
| `K`, `V` | Key, Value | `Map<K, V>` |
| `N` | Number | `<N extends Number>` |
| `R` | Result/Return | `Function<T, R>` |
| `S`, `U` | 2nd, 3rd types | `BiFunction<T, U, R>` |

---

## 3. Bounded Type Parameters

```java
// Upper bound: only Number or its subclasses (Integer, Double, ...)
static <N extends Number> double sum(List<N> nums) {
    double total = 0;
    for (N n : nums) total += n.doubleValue();   // ✅ can call Number's methods
    return total;
}
sum(List.of(1, 2, 3));        // ✅
sum(List.of(1.5, 2.5));       // ✅
// sum(List.of("a"));         ❌ String is not a Number

// Multiple bounds: at most ONE class, and it must come FIRST
static <T extends Number & Comparable<T>> T clampMax(T a, T b) { return a.compareTo(b) >= 0 ? a : b; }
```

> `extends` is used for **both** classes and interfaces in bounds. There's no `implements` in generics.

---

## 4. Invariance: the surprising rule

```java
Integer[] ints = {1, 2};
Number[] nums = ints;            // ✅ arrays are COVARIANT
nums[0] = 3.14;                  // 💥 ArrayStoreException at runtime

List<Integer> intList = new ArrayList<>();
// List<Number> numList = intList;   ❌ generics are INVARIANT: compile error
// If it were allowed: numList.add(3.14) would put a Double into a List<Integer>!
```

> 🔑 `Integer` IS-A `Number`, but **`List<Integer>` is NOT a `List<Number>`**. Use wildcards to get flexibility.

---

## 5. Wildcards & PECS ⭐

```
           List<? extends Number>                 List<? super Integer>
           "a list of SOME subtype of Number"     "a list of SOME supertype of Integer"
           accepts: List<Number>, List<Integer>,  accepts: List<Integer>, List<Number>,
                    List<Double> ...                       List<Object>
           READ  as Number ✅                     READ  as Object only
           WRITE ❌ (which subtype? unknown)      WRITE Integer ✅
           → it PRODUCES values for you          → it CONSUMES values from you
```

```java
// PRODUCER → extends (you only read from it)
static double total(List<? extends Number> list) {
    double sum = 0;
    for (Number n : list) sum += n.doubleValue();
    return sum;
}
total(List.of(1, 2));          // List<Integer> ✅
total(List.of(1.5));           // List<Double>  ✅

// CONSUMER → super (you only write into it)
static void fillWithInts(List<? super Integer> sink) {
    for (int i = 1; i <= 3; i++) sink.add(i);
}
List<Number> numbers = new ArrayList<>();
List<Object> objects = new ArrayList<>();
fillWithInts(numbers);         // ✅
fillWithInts(objects);         // ✅

// BOTH: the JDK's own signature
// Collections.copy(List<? super T> dest, List<? extends T> src)
```

> 🧩 **PECS = "Producer Extends, Consumer Super"** (Joshua Bloch, Effective Java).
> Memory picture: **extends = an exit** (data comes OUT to you) · **super = a supply drop** (you put data IN).
> Need to read *and* write? Use an exact type `<T>`, no wildcard.

| Wildcard | Read gives | Can add | Use for |
|---|---|---|---|
| `List<?>` | `Object` | only `null` | "any list", e.g. `printSize(List<?> l)` |
| `List<? extends T>` | `T` | only `null` | reading/producing |
| `List<? super T>` | `Object` | `T` and its subtypes | writing/consuming |
| `List<T>` | `T` | `T` | both |

---

## 6. Type Erasure

**One-liner:** the compiler checks generic types and then **erases** them: `T` becomes its bound (or `Object`),
and casts are inserted. The JVM never sees `List<String>`, only `List`.

```java
// What you write                      // What the JVM effectively sees
class Box<T> { T value; }             class Box { Object value; }
class NumBox<T extends Number> { T v; } class NumBox { Number v; }
String s = box.get();                 String s = (String) box.get();
```

**Consequences: what you CAN'T do**

| ❌ Not allowed | Why | Workaround |
|---|---|---|
| `new T()` | the type is unknown at runtime | pass a `Supplier<T>` or a `Class<T>` |
| `new T[10]` / `new List<String>[10]` | no runtime type for the array | `(T[]) new Object[10]` or use a `List<T>` |
| `obj instanceof List<String>` | erased | `obj instanceof List<?>` |
| `List<int>` | type params must be objects | `List<Integer>` (or `IntStream`) |
| `static T field;` | a static is shared across all `T`s | make it instance-level |
| overload `f(List<String>)` & `f(List<Integer>)` | same erasure → "name clash" | use different method names |
| `catch (T e)` / generic exception classes | erased at runtime | n/a |

```java
List<String> a = new ArrayList<>();
List<Integer> b = new ArrayList<>();
System.out.println(a.getClass() == b.getClass());   // true → both are just ArrayList
```

> 💡 **Bridge methods:** when a class implements `Comparable<Employee>`, the compiler adds a synthetic
> `compareTo(Object)` that casts and calls `compareTo(Employee)`, so polymorphism survives erasure.

---

## 7. Raw Types: don't

```java
List raw = new ArrayList<String>();   // raw type, so type checks are disabled (a warning, not an error)
raw.add(42);                           // compiles
```
Raw types exist only for backward compatibility with pre-Java-5 code.

---

## 🧩 Memory Tricks

| Concept | Trick |
|---|---|
| Wildcards | **PECS: Producer Extends, Consumer Super** |
| Invariance | "**A box of apples is not a box of fruit**" (you could put a banana into a box of fruit!) |
| Erasure | "**Generics are a compile-time costume; at runtime everyone's just Object**" |
| Generic method syntax | "**Declare `<T>` before you return T**": `static <T> T pick(...)` |

---

## ❓ Self-Test

<details><summary>1. Can you add an Integer to a <code>List&lt;? extends Number&gt;</code>?</summary>

No. The list might actually be a `List<Double>`, so the compiler can't allow any addition except `null`.
You can read Numbers from it.
</details>

<details><summary>2. Why is <code>List&lt;Object&gt; o = new ArrayList&lt;String&gt;();</code> a compile error?</summary>

Generics are invariant. If it were allowed, `o.add(42)` would put an Integer into a list of Strings.
</details>

<details><summary>3. What is type erasure? Give two consequences.</summary>

Generic type information is removed after compilation and replaced by bounds or `Object` plus casts.
Consequences: you can't write `new T()`, and you can't use `instanceof List<String>`. Also no generic arrays,
no primitives as type arguments, and no overloads that differ only in type parameters.
</details>

<details><summary>4. How would you create an instance of T inside a generic method?</summary>

Pass a factory: `static <T> T create(Supplier<T> s) { return s.get(); }` called as `create(ArrayList::new)`,
or pass a `Class<T>` and use `clazz.getDeclaredConstructor().newInstance()`.
</details>

<details><summary>5. Explain the signature <code>static &lt;T&gt; void copy(List&lt;? super T&gt; dest, List&lt;? extends T&gt; src)</code>.</summary>

`src` produces T values (extends) and `dest` consumes them (super). So you can copy a `List<Integer>` into a
`List<Number>` or a `List<Object>`.
</details>

---

## ✅ Chapter Checklist

- [ ] Write a generic class `Pair<K,V>` and a generic method `max`
- [ ] Explain PECS with the read/write table
- [ ] List 5 restrictions caused by type erasure
- [ ] Run [code/05_generics](../code/05_generics)

➡️ Next: [06 · Collections](06-collections.md)
