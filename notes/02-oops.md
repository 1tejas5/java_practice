# 02 · OOP — Object-Oriented Programming (the heart of Java)

> ⏱️ **Revise in:** 90 min · 🎯 **Level:** Beginner → Advanced · ▶️ **Code:** [code/02_oops](../code/02_oops) · 🏋️ **Practice:** [Exercises → OOP](../exercises/README.md#2-oop)

## 🧠 Mind Map

```
02 OOP
├── Building blocks
│   ├── Class  = blueprint (fields + methods + constructors)
│   ├── Object = instance (state + behaviour + identity), created with new → heap
│   ├── Constructor: same name, no return type · default/no-arg/parameterized · this() / super()
│   ├── this  → current object      super → parent part of the object
│   ├── static → belongs to the CLASS (one copy) · static block · static nested class
│   └── Init order: static blocks (once) → instance blocks → constructor  (parent first!)
│
├── 4 PILLARS  ── "A PIE" ──
│   ├── A · Abstraction   → show WHAT, hide HOW → abstract class / interface
│   ├── P · Polymorphism  → one interface, many forms
│   │   ├── Compile-time = overloading (static binding)
│   │   └── Runtime = overriding (dynamic method dispatch)
│   ├── I · Inheritance   → IS-A · extends · reuse · single/multilevel/hierarchical
│   │   └── no multiple inheritance of classes (diamond problem) → use interfaces
│   └── E · Encapsulation → private fields + public methods · validation · data hiding
│
├── Relationships
│   ├── IS-A  → inheritance (Dog IS-A Animal)
│   └── HAS-A → Association ⊃ Aggregation (weak, can live alone) ⊃ Composition (strong, dies together)
│
├── Abstract class vs Interface
│   ├── abstract class: state + constructors + any methods · single inheritance · "IS-A"
│   └── interface: contract · default/static/private methods (8/9) · multiple · "CAN-DO"
│
├── Modifiers
│   ├── access: private < default < protected < public  ("Me → House → Family → World")
│   └── non-access: final · static · abstract · synchronized · volatile · transient · native
│
├── java.lang.Object: toString · equals · hashCode · getClass · clone · wait/notify · finalize(deprecated)
├── Nested classes: static nested · inner (member) · local · anonymous
└── Design quality: immutability · composition > inheritance · loose coupling · high cohesion · SOLID
```

## ⚡ 30-Second Summary

- A **class** is a blueprint; an **object** is a real thing built from it, living on the **heap**.
- **A PIE:** **A**bstraction (hide complexity), **P**olymorphism (same call, different behaviour),
  **I**nheritance (reuse via IS-A), **E**ncapsulation (protect data behind methods).
- **Overloading** = same name, different parameters (compile time). **Overriding** = same signature
  in a subclass (runtime).
- **Methods are polymorphic; fields and static methods are NOT** (they're resolved by the reference type).
- Interface = "what it can do" (contract); abstract class = "what it is" (partial implementation + state).
- Override `equals()` → **must** override `hashCode()`.
- Prefer **composition over inheritance**, and **program to interfaces**.

---

## 1. Why OOP?

| Procedural (C style) | Object-Oriented (Java) |
|---|---|
| Program = list of functions acting on data | Program = objects that **own** their data and behaviour |
| Data is global/exposed, anything can change it | Data is hidden; changed only through methods |
| Hard to scale, change ripples everywhere | Modular, reusable, easier to maintain and test |

**Analogy:** a car. You use `start()`, `accelerate()`, `brake()` (the interface) without knowing about fuel
injection (abstraction). The engine internals are sealed off (encapsulation). A `SportsCar` is a `Car`
with extras (inheritance). Pressing `accelerate()` behaves differently in an electric car and a petrol car
(polymorphism).

---

## 2. Class & Object

```java
class Car {                       // CLASS = blueprint
    String brand;                 // fields   → STATE
    int speed;

    Car(String brand) {           // constructor → INITIALIZATION
        this.brand = brand;
    }

    void accelerate(int by) {     // method   → BEHAVIOUR
        speed += by;
    }
}

Car c1 = new Car("Tata");          // OBJECT = instance
Car c2 = new Car("BMW");
Car c3 = c1;                       // no new object! c3 and c1 point to the SAME car
```

```
   STACK                HEAP
   c1 ──────────┬────► [Car: brand="Tata", speed=0]
   c3 ──────────┘
   c2 ───────────────► [Car: brand="BMW",  speed=0]
```

**Every object has:** **State** (field values) · **Behaviour** (methods) · **Identity** (its own memory
address; `==` compares identity).

**Ways to create an object:** `new` · reflection (`Constructor.newInstance()`) · `clone()` ·
deserialization · factory methods (`List.of()`, which call `new` internally).

---

## 3. Constructors

**One-liner:** a special block that initializes a new object. Same name as the class, **no return type**
(not even `void`).

```java
class Student {
    private final String name;
    private int age;

    Student() {                       // no-arg constructor
        this("Unknown", 18);          // constructor chaining (must be the FIRST statement)
    }

    Student(String name, int age) {   // parameterized constructor
        this.name = name;             // 'this' resolves the name clash between field and parameter
        this.age = age;
    }

    Student(Student other) {          // "copy constructor" (Java has no built-in one)
        this(other.name, other.age);
    }
}
```

| Rule | Detail |
|---|---|
| Default constructor | The compiler adds a no-arg one **only if you write no constructor at all** |
| Not inherited | Subclasses don't inherit constructors, they **call** them with `super(...)` |
| First line | Either `this(...)` or `super(...)`, never both; if neither, the compiler inserts `super()` |
| Can be `private` | Used for Singleton, utility classes and factory methods |
| Can't be | `static`, `final`, `abstract`, `synchronized` |
| Can overload | Yes, many constructors with different parameters |

> ⚠️ **Trap:** if the parent has **only** `Parent(int x)`, then `class Child extends Parent { }` fails to
> compile, because the implicit `super()` has no matching constructor. Fix it by calling `super(5)` explicitly.
> ⚠️ **Trap:** `void Student() { }` is a **method** that happens to share the class name, not a constructor.

---

## 4. The `this` keyword — 5 uses

| Use | Example |
|---|---|
| Refer to the current object's field | `this.name = name;` |
| Call another constructor of the same class | `this("Unknown", 18);` |
| Pass the current object as an argument | `register(this);` |
| Return the current object (fluent/builder style) | `return this;` |
| Call the current object's method (usually implicit) | `this.validate();` |

> `this` is **not available in a static context**, because there's no current object there.

---

## 5. `static` — belongs to the class, not the object

```java
class Counter {
    static int count = 0;     // ONE copy shared by all objects
    int id;                   // each object has its own

    Counter() { id = ++count; }

    static int getCount() {   // can be called as Counter.getCount() with no object
        // return id;          ❌ static can't touch instance members directly (which object's id?)
        return count;
    }

    static {                  // static block: runs ONCE, when the class is loaded
        System.out.println("Counter class loaded");
    }
}
```

| | static | instance |
|---|---|---|
| Belongs to | the class | each object |
| Memory | one copy | one copy per object |
| Access | `ClassName.member` | `object.member` |
| Can use `this`/`super`? | ❌ | ✅ |
| Can access instance members directly? | ❌ (needs an object) | ✅ |
| Overriding | ❌ hidden, not overridden | ✅ |

**Use static for:** constants (`static final`), utility methods (`Math.max`), counters, factory methods.

### Initialization order (a favourite interview question)

```
When the class is first used:     When `new Child()` runs:
 1. Parent static block           3. Parent instance initializer blocks / field initializers
 2. Child static block            4. Parent constructor
    (once per class load)         5. Child instance initializer blocks / field initializers
                                  6. Child constructor
```

> 🧩 **Memory trick:** "**Statics first, once. Then Parent builds before Child.** Blocks before bodies."
> See [InitializationOrder.java](../code/02_oops/InitializationOrder.java).

---

## 6. Pillar 1 — ENCAPSULATION 💊

**One-liner:** bundle data and the methods that use it into one unit, and **hide the data** behind those
methods (`private` fields + public getters/setters with validation).

**Analogy:** a medicine capsule, where the contents are sealed in. Or an ATM: you never touch the cash
vault directly, only the buttons.

```java
public class BankAccount {
    private double balance;                       // hidden

    public double getBalance() { return balance; }

    public void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        balance += amount;                        // controlled change
    }

    public void withdraw(double amount) {
        if (amount > balance) throw new IllegalStateException("Insufficient funds");
        balance -= amount;
    }
    // no setBalance() → nobody can set balance = 1_000_000 directly
}
```

**Benefits:** data validation · flexibility (change the internals without breaking callers) · read-only or
write-only fields · easier testing · security.

> 🔑 **Encapsulation vs abstraction:** encapsulation is *how* you hide (private + methods, the
> implementation level). Abstraction is *what* you expose (essential features only, the design level).
> Encapsulation helps you achieve abstraction.

---

## 7. Pillar 2 — INHERITANCE 🧬

**One-liner:** a child class acquires the fields and methods of a parent class (**IS-A** relationship)
using `extends`. It promotes reuse and makes polymorphism possible.

```java
class Animal {
    protected String name;
    Animal(String name) { this.name = name; }
    void eat()   { System.out.println(name + " eats"); }
    void sound() { System.out.println("..."); }
}

class Dog extends Animal {                  // Dog IS-A Animal
    Dog(String name) { super(name); }       // call the parent constructor
    @Override
    void sound() { System.out.println(name + " barks"); }   // override
    void fetch() { System.out.println(name + " fetches"); } // new behaviour
}
```

### Types of inheritance

```
 Single          Multilevel        Hierarchical         Multiple (❌ classes, ✅ interfaces)
   A                A                  A                  A     B
   │                │                ┌─┴─┐                 └──┬──┘
   B                B                B   C                    C
                    │
                    C
```

> ❓ **Why no multiple inheritance with classes? The Diamond Problem:** if `B` and `C` both override `A.hello()`
> and `D extends B, C`, which `hello()` does `D` get? Java avoids the ambiguity by allowing only one parent
> class. Interfaces can be multiply implemented, and if two default methods clash, the class **must**
> override and choose: `B.super.hello()`.

### `super` — 3 uses

| Use | Example |
|---|---|
| Call the parent constructor | `super(name);` (first line) |
| Call the parent's version of an overridden method | `super.sound();` |
| Access a parent field hidden by a child field | `super.name` |

### What is NOT inherited?

- **Constructors** (they're called, not inherited)
- **private members** (they exist inside the object but aren't accessible from the child)
- **static members** are accessible, but they belong to the parent class and can't be overridden

> 🔑 Every class implicitly extends **`java.lang.Object`**, the root of all classes.
> 🔑 `final class` can't be extended (e.g. `String`, `Integer`, `Math`).

---

## 8. Pillar 3 — POLYMORPHISM 🎭

**One-liner:** "many forms". The same method call behaves differently depending on the object.

```
                       POLYMORPHISM
           ┌───────────────┴───────────────┐
    Compile-time (static)            Runtime (dynamic)
    = Method OVERLOADING             = Method OVERRIDING
    decided by the compiler          decided by the JVM at runtime
    from the REFERENCE type          from the actual OBJECT type
    and the argument types           ("dynamic method dispatch")
```

### Runtime polymorphism (the important one)

```java
Animal a = new Dog("Tommy");    // UPCASTING: parent reference → child object (automatic)
a.sound();                      // "Tommy barks" → the JVM looks at the OBJECT (Dog)
a.eat();                        // inherited from Animal
// a.fetch();                   ❌ compile error: the REFERENCE type Animal has no fetch()

if (a instanceof Dog d) {       // safe DOWNCASTING (Java 16+ pattern)
    d.fetch();
}
Animal x = new Animal("Generic");
// Dog bad = (Dog) x;           ❌ ClassCastException at runtime
```

> 🧩 **Golden rule:** "**The compiler checks the REFERENCE, the JVM runs the OBJECT.**"
> - *Can I call it?* → decided by the reference type (compile time)
> - *Which version runs?* → decided by the object type (runtime), but **only for instance methods**.

### ⚠️ Not polymorphic: fields, static methods, private methods

```java
class Parent { String name = "Parent"; static String who() { return "static Parent"; } }
class Child extends Parent { String name = "Child"; static String who() { return "static Child"; } }

Parent p = new Child();
p.name;   // "Parent"         ← fields are resolved by the REFERENCE type (field hiding)
p.who();  // "static Parent"  ← static methods are resolved by the REFERENCE type (method hiding)
```

### Overloading vs overriding

| | Overloading | Overriding |
|---|---|---|
| Where | same class (or inherited) | parent ↔ child |
| Signature | **different** parameters | **same** name + parameters |
| Return type | anything | same or **covariant** (subtype) |
| Access | anything | same or **wider** (not more restrictive) |
| Checked exceptions | anything | same, narrower, or none (**not broader**) |
| static / private / final | can be overloaded | **can't** be overridden |
| Binding | compile time | runtime |
| `@Override` | n/a | recommended (the compiler checks it) |

> 🧩 **Overriding rules: "Return narrows, Access widens, Exceptions narrow."**

```java
class Shape {
    protected Shape copy() throws IOException { ... }
}
class Circle extends Shape {
    @Override
    public Circle copy() { ... }   // ✅ wider access, covariant return type, fewer exceptions
}
```

---

## 9. Pillar 4 — ABSTRACTION 🎨

**One-liner:** show **what** an object does and hide **how** it does it. Achieved with **abstract classes**
(0–100% abstraction) and **interfaces** (contracts).

### Abstract class

```java
abstract class Shape {                       // can't be instantiated: new Shape() ❌
    private final String color;              // ✅ can have state
    Shape(String color) { this.color = color; } // ✅ can have constructors (called via super)

    abstract double area();                  // no body → subclasses MUST implement it

    void describe() {                        // ✅ concrete method
        System.out.printf("%s %s with area %.2f%n", color, getClass().getSimpleName(), area());
    }
}

class Circle extends Shape {
    private final double r;
    Circle(String color, double r) { super(color); this.r = r; }
    @Override double area() { return Math.PI * r * r; }
}
```

**Rules:** a class with **any** abstract method must be `abstract`. An abstract class can have **zero** abstract methods.
A subclass must implement every abstract method or be abstract itself. `abstract` can't be combined with `final`,
`static` or `private` on a method (they'd prevent overriding).

### Interface

```java
interface Payment {
    double FEE = 0.02;                        // implicitly public static final (a constant)

    void pay(double amount);                  // implicitly public abstract

    default void receipt(double amount) {     // Java 8: default method (has a body, can be overridden)
        System.out.println("Paid " + amount + " (+fee " + amount * FEE + ")");
    }

    static Payment upi() {                    // Java 8: static method (call it as Payment.upi())
        return amt -> System.out.println("UPI paid " + amt);
    }

    private void log(String m) { }            // Java 9: private helper for default methods
}

class CardPayment implements Payment {
    @Override
    public void pay(double amount) {          // must be public!
        System.out.println("Card paid " + amount);
    }
}
```

**Interface evolution:** Java 7 = abstract methods + constants · Java 8 = **+ default & static methods** ·
Java 9 = **+ private methods**.

**Special interfaces:**
- **Functional interface:** exactly one abstract method → can be implemented by a lambda (`Runnable`, `Comparator`).
- **Marker interface:** no methods, just tags a class (`Serializable`, `Cloneable`, `RandomAccess`).

### ⭐ Abstract class vs Interface

| | Abstract class | Interface |
|---|---|---|
| Keyword | `abstract class` / `extends` | `interface` / `implements` |
| Multiple inheritance | ❌ only one | ✅ many |
| Fields | any (instance, static, final or not) | only `public static final` constants |
| Constructors | ✅ yes | ❌ no |
| Methods | abstract + concrete, any access | abstract, default, static, private |
| Default access of methods | package-private | `public` |
| State | ✅ can hold state | ❌ no instance state |
| Relationship | **IS-A** (Dog is an Animal) | **CAN-DO** (Dog can `Swim`, is `Comparable`) |
| Use when | related classes share code/state | unrelated classes share a capability or contract |

> 🧩 **Memory trick:** "**Abstract class = a half-built house** (walls exist, finish the rooms).
> **Interface = a job description** (what you must do, not how)."

---

## 10. Access Modifiers

| Modifier | Same class | Same package | Subclass (other package) | Everywhere |
|---|:-:|:-:|:-:|:-:|
| `private` | ✅ | ❌ | ❌ | ❌ |
| *default* (no keyword) | ✅ | ✅ | ❌ | ❌ |
| `protected` | ✅ | ✅ | ✅ (through inheritance) | ❌ |
| `public` | ✅ | ✅ | ✅ | ✅ |

> 🧩 **"Me → House → Family → World"**: private = only me, default = my house (package),
> protected = my house + my children living elsewhere (subclasses), public = the whole world.
> 🔑 Top-level classes can only be `public` or default. A file can have only **one public top-level class**,
> and its name must match the file name.

## 11. The `final` keyword

| Applied to | Meaning | Example |
|---|---|---|
| variable | value can't be reassigned (a constant) | `final int MAX = 10;` |
| reference variable | **the reference** can't change, **the object can still mutate** | `final List<String> l = new ArrayList<>(); l.add("ok");` ✅ `l = new ArrayList<>();` ❌ |
| method | can't be overridden | `final void lock()` |
| class | can't be extended | `final class String` |
| parameter | can't be reassigned in the method | `void f(final int x)` |

**Blank final:** declared without a value, then assigned exactly once in every constructor.
**Effectively final:** never reassigned after initialization (needed for lambdas/inner classes).

> 🔑 **`final` vs `finally` vs `finalize`:** keyword for constants/no-override/no-extend · the block that always
> runs after try · a deprecated Object method that was called before GC.

---

## 12. Relationships: IS-A vs HAS-A

```
ASSOCIATION (uses / knows about)  ─ Teacher ↔ Student
   └── AGGREGATION (has-a, WEAK)  ─ Department ◇── Teacher   (teacher survives if the dept closes)
         └── COMPOSITION (STRONG) ─ House ◆── Room           (rooms die with the house)
```

```java
class Engine { }
class Car {
    private final Engine engine = new Engine();   // COMPOSITION: Car creates and owns the Engine
}
class Department {
    private final List<Teacher> teachers;         // AGGREGATION: teachers are passed in, exist independently
    Department(List<Teacher> teachers) { this.teachers = teachers; }
}
```

> 🔑 **Composition over inheritance:** inheritance tightly couples you to the parent's implementation
> (the "fragile base class" problem). Composition lets you swap parts at runtime and keeps classes small.
> Inherit only for true IS-A relationships where the child can **fully substitute** the parent (Liskov).

---

## 13. The `Object` class — methods every object has

| Method | Purpose | Default behaviour |
|---|---|---|
| `toString()` | readable description | `ClassName@hexHashCode` |
| `equals(Object o)` | logical equality | same as `==` (identity) |
| `hashCode()` | int used by hash collections | derived from identity |
| `getClass()` | runtime class info | final, can't override |
| `clone()` | field-by-field copy | protected; needs `Cloneable`, else `CloneNotSupportedException` |
| `wait() / notify() / notifyAll()` | thread coordination | final |
| `finalize()` | cleanup before GC | **deprecated** (Java 9), don't use |

### ⭐ The equals() & hashCode() contract

1. If `a.equals(b)` then `a.hashCode() == b.hashCode()` — **mandatory**.
2. Equal hash codes do **not** imply `equals` (collisions are allowed).
3. `equals` must be reflexive, symmetric, transitive, consistent, and `x.equals(null) == false`.

> 🧩 **"Same object → same hash; same hash ≠ same object."**

**What breaks if you override only `equals`?** `HashSet`/`HashMap` first use `hashCode` to find the bucket.
Two "equal" objects with different hash codes land in **different buckets**, so the set keeps duplicates and
`map.get(equalKey)` returns `null`. → [ObjectMethodsDemo.java](../code/02_oops/ObjectMethodsDemo.java)

```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;                       // same reference
    if (!(o instanceof Employee e)) return false;     // null-safe type check + cast
    return id == e.id && Objects.equals(name, e.name);
}

@Override
public int hashCode() {
    return Objects.hash(id, name);                    // use the SAME fields as equals
}
```

> 💡 **Records** (Java 16+) generate `equals`, `hashCode` and `toString` for you. See ch 11.

### Shallow vs deep copy

| Shallow copy (`super.clone()` default) | Deep copy |
|---|---|
| copies field values; **referenced objects are shared** | also copies the referenced objects |
| changing a nested list in the copy affects the original | fully independent |

> 💡 Prefer **copy constructors** or static factories over `clone()`, which is widely considered broken
> (Effective Java, Item 13).

---

## 14. Nested (Inner) Classes

| Type | Declared | Needs an outer object? | Can access outer's | Typical use |
|---|---|---|---|---|
| **Static nested** | `static class X` inside a class | ❌ | only static members | helpers, `Map.Entry`, Builder |
| **Inner (member)** | `class X` inside a class | ✅ `outer.new Inner()` | all members, even private | iterators tied to their collection |
| **Local** | inside a method | ✅ (if in an instance method) | members + effectively final locals | one-off helper class |
| **Anonymous** | `new Interface() { ... }` | ✅ (if in an instance method) | members + effectively final locals | one-time implementation (mostly replaced by lambdas) |

```java
class Outer {
    private int x = 10;
    static class StaticNested { }                         // new Outer.StaticNested()
    class Inner { int get() { return x; } }               // outer.new Inner()
    void method() {
        int local = 5;                                    // effectively final
        class Local { int get() { return x + local; } }
        Runnable anon = new Runnable() {                  // anonymous class
            public void run() { System.out.println(local); }
        };
    }
}
```

> ⚠️ A non-static inner class holds a hidden reference to its outer object, which can cause **memory leaks**.
> Prefer `static` nested classes unless you really need the outer instance.

---

## 15. Immutable Classes

**One-liner:** an object whose state can never change after construction (`String`, `Integer`, `LocalDate`).

**Recipe:** "**Final class, Private final fields, No setters, Defensive copies**"

```java
public final class Employee {                          // 1. final: no subclass can break it
    private final String name;                         // 2. private final fields
    private final List<String> skills;

    public Employee(String name, List<String> skills) {
        this.name = name;
        this.skills = List.copyOf(skills);             // 4. defensive copy IN
    }
    public String getName() { return name; }           // 3. getters only, no setters
    public List<String> getSkills() { return skills; } //    List.copyOf is already unmodifiable
    public Employee withName(String n) { return new Employee(n, skills); } // "wither" returns a NEW object
}
```

**Why immutability?** Thread-safe for free · safe as HashMap keys · cacheable · simpler to reason about.

---

## 16. Coupling, Cohesion & "Program to an Interface"

| | Good | Bad |
|---|---|---|
| **Coupling** (dependency between classes) | **Loose**: depends on interfaces | **Tight**: depends on concrete classes |
| **Cohesion** (focus within a class) | **High**: one clear job | **Low**: does unrelated things |

```java
// ❌ tight coupling
class OrderService { private MySqlRepository repo = new MySqlRepository(); }

// ✅ loose coupling: depend on an abstraction and inject it (Dependency Injection, as in Spring)
class OrderService {
    private final OrderRepository repo;                       // interface
    OrderService(OrderRepository repo) { this.repo = repo; }  // any implementation works, including a test fake
}

List<String> names = new ArrayList<>();   // ✅ program to the interface (List), not ArrayList
```

> ➡️ SOLID principles are covered with examples in [12 · Design Patterns & SOLID](12-design-patterns-solid.md).

---

## 🧩 Memory Tricks Recap

| Concept | Trick |
|---|---|
| 4 pillars | **A PIE** |
| Polymorphism rule | **"Compiler checks the reference, JVM runs the object"** |
| Overriding rules | **"Return narrows, Access widens, Exceptions narrow"** |
| Access modifiers | **"Me → House → Family → World"** |
| Init order | **"Statics first, once. Parent before Child. Blocks before bodies."** |
| Abstract vs interface | **"Half-built house vs job description"** · IS-A vs CAN-DO |
| equals/hashCode | **"Same object → same hash; same hash ≠ same object"** |
| Immutable | **"Final class, Private final fields, No setters, Defensive copies"** |
| Composition vs aggregation | **"Composition: dies together; Aggregation: lives apart"** |

---

## ❓ Self-Test

<details><summary>1. Can we override a static method?</summary>

No. Declaring a static method with the same signature in a child class **hides** the parent's method; it
doesn't override it. Which one runs is decided by the reference type at compile time.
</details>

<details><summary>2. Can we override a private method? A final method?</summary>

No to both. A private method isn't visible to the child, so a same-named method there is a brand-new method.
A final method can't be overridden by definition.
</details>

<details><summary>3. <code>Parent p = new Child();</code> Both have a field <code>name</code>. What's <code>p.name</code>?</summary>

The **Parent's** field. Fields aren't polymorphic, so they're resolved by the reference type. Methods would
use the Child's version.
</details>

<details><summary>4. Can an abstract class have a constructor? Why?</summary>

Yes. You can't call it with `new` directly, but subclass constructors call it via `super(...)` to
initialize the abstract class's own fields.
</details>

<details><summary>5. Can an interface have a constructor or instance fields?</summary>

No. Interfaces have no instance state. Their fields are implicitly `public static final` constants.
</details>

<details><summary>6. Two interfaces have the same default method. A class implements both. What happens?</summary>

Compile error unless the class overrides the method. Inside the override it can pick one with `A.super.method()`.
</details>

<details><summary>7. What happens if you override equals() but not hashCode()?</summary>

Equal objects may get different hash codes, so they land in different buckets. A `HashSet` then keeps
"duplicates" and `HashMap.get()` can't find keys that are logically equal.
</details>

<details><summary>8. What's printed first: the static block, the instance block, or the constructor?</summary>

Static block (once, at class load), then the instance initializer block, then the constructor. With
inheritance: parent static, child static, parent instance block, parent constructor, child instance block,
child constructor.
</details>

<details><summary>9. Why can't a constructor be final, static or abstract?</summary>

It isn't inherited, so `final` and `abstract` are meaningless. It initializes an object, so it can't be
`static`.
</details>

<details><summary>10. Aggregation vs composition, with an example?</summary>

Both are HAS-A. In aggregation the part can exist independently (a Department has Teachers, and teachers
outlive the department). In composition the part's lifecycle is bound to the whole (a House has Rooms; no
house, no rooms).
</details>

<details><summary>11. Can a subclass override a method and make it <code>private</code>?</summary>

No. The overriding method can't have weaker access. `public` in the parent must stay `public`.
</details>

<details><summary>12. What does calling an overridable method from a constructor risk?</summary>

The child's override runs **before the child's fields are initialized**, so it sees `null`/`0` values.
See [InitializationOrder.java](../code/02_oops/InitializationOrder.java). One subtle exception: a `final` field
initialized with a compile-time constant (`final String TAG = "x";`) is inlined by the compiler, so it already appears
set (tricky Q52).
</details>

---

## ✅ Chapter Checklist

- [ ] Explain all 4 pillars with code (BankAccount, Animal/Dog, Shape, Payment)
- [ ] Write the init order for Parent/Child from memory
- [ ] Fill in the overloading vs overriding table from memory
- [ ] Fill in the abstract class vs interface table from memory
- [ ] Write correct `equals()` and `hashCode()` methods
- [ ] Write an immutable class
- [ ] Run every program in [code/02_oops](../code/02_oops) and predict the output first

➡️ Next: [03 · Strings, Wrappers & Enums](03-strings-wrappers-enums.md)
