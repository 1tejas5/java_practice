# 12 · SOLID Principles & Design Patterns

> ⏱️ **Revise in:** 70 min · 🎯 **Level:** Intermediate → Advanced · ▶️ **Code:** [code/12_patterns](../code/12_patterns) · 🏋️ **Practice:** [Exercises → Patterns](../exercises/README.md#10-design-patterns)

## 🧠 Mind Map

```
12 DESIGN
├── SOLID
│   ├── S  Single Responsibility  → one reason to change
│   ├── O  Open/Closed            → extend with new code, don't edit old code
│   ├── L  Liskov Substitution    → a subtype must work wherever its parent works
│   ├── I  Interface Segregation  → small, focused interfaces
│   └── D  Dependency Inversion   → depend on abstractions (inject them)
├── Other principles: DRY · KISS · YAGNI · composition > inheritance · program to an interface
│                     · Law of Demeter · high cohesion / low coupling
└── GoF patterns (23)
    ├── CREATIONAL (how objects are made)
    │   ├── Singleton  → one instance (enum ✅, holder, double-checked locking)
    │   ├── Factory    → create without exposing `new`/the concrete class
    │   ├── Abstract Factory → families of related objects
    │   ├── Builder    → step-by-step construction, many optional params
    │   └── Prototype  → clone an existing object
    ├── STRUCTURAL (how objects are composed)
    │   ├── Adapter    → convert one interface into another (plug adapter)
    │   ├── Decorator  → add behaviour by wrapping (java.io!)
    │   ├── Proxy      → a stand-in controlling access (lazy, security, logging)
    │   ├── Facade     → a simple front for a complex subsystem
    │   ├── Composite  → a tree of parts and wholes treated alike
    │   └── Flyweight  → share common objects (Integer cache, String pool)
    └── BEHAVIORAL (how objects communicate)
        ├── Strategy   → swappable algorithms (Comparator)
        ├── Observer   → publish/subscribe (listeners)
        ├── Template Method → fixed skeleton, overridable steps
        ├── Command    → a request as an object (undo, queues)
        ├── Iterator   → traverse without exposing internals
        ├── Chain of Responsibility → pass along handlers (filters)
        └── State      → behaviour changes with internal state
```

## ⚡ 30-Second Summary

- **SOLID** keeps code changeable. Most violations come down to doing too much (S) or depending on concrete classes (D).
- **Patterns** are named, proven solutions to recurring problems. Learn the **intent** first, then the code.
- Many patterns are **already inside the JDK**: Decorator in java.io, Strategy as Comparator, Iterator in collections,
  Flyweight in the Integer cache.
- **Modern Java shrinks many patterns:** lambdas implement Strategy/Command/Observer in one line, records make Builders
  less necessary, and enums give the best Singleton.

---

## PART A: SOLID

### S: Single Responsibility Principle
> "A class should have **one, and only one, reason to change**."

```java
// ❌ Invoice does 3 jobs: business logic + printing + persistence
class Invoice { double total() {...}  void print() {...}  void saveToDb() {...} }

// ✅ split by reason-to-change
class Invoice { double total() {...} }
class InvoicePrinter { void print(Invoice i) {...} }
class InvoiceRepository { void save(Invoice i) {...} }
```

### O: Open/Closed Principle
> "Open for **extension**, closed for **modification**."

```java
// ❌ every new shape forces you to edit this method
double area(Object s) {
    if (s instanceof Circle c) return Math.PI * c.r * c.r;
    else if (s instanceof Square q) return q.side * q.side;   // + edit again for Triangle...
}

// ✅ new shapes = new classes; existing code untouched
interface Shape { double area(); }
record Circle(double r) implements Shape { public double area() { return Math.PI * r * r; } }
record Square(double side) implements Shape { public double area() { return side * side; } }
double total(List<Shape> shapes) { return shapes.stream().mapToDouble(Shape::area).sum(); }
```

### L: Liskov Substitution Principle
> "Objects of a subclass must be usable **wherever the parent is expected**, without surprises."

```java
// ❌ Classic violation: Square extends Rectangle
class Rectangle { int w, h; void setWidth(int w) { this.w = w; } void setHeight(int h) { this.h = h; } }
class Square extends Rectangle {
    @Override void setWidth(int w) { this.w = this.h = w; }    // changes the height too!
}
Rectangle r = new Square(); r.setWidth(5); r.setHeight(4);  // the caller expects area 20 → gets 16 💥

// ❌ Another one: Penguin extends Bird { void fly() { throw new UnsupportedOperationException(); } }
// ✅ Fix: model capabilities separately → interface Flyable; Sparrow implements Flyable; Penguin doesn't
```
> 🔑 Smells: overriding methods that **throw UnsupportedOperationException**, **strengthen preconditions**, or
> **weaken postconditions**.

### I: Interface Segregation Principle
> "Clients shouldn't be forced to depend on methods they **don't use**."

```java
// ❌ fat interface
interface Worker { void work(); void eat(); void sleep(); }
class Robot implements Worker { public void eat() { /* meaningless */ } ... }

// ✅ role interfaces
interface Workable { void work(); }
interface Eatable  { void eat(); }
class Human implements Workable, Eatable { ... }
class Robot implements Workable { ... }
```

### D: Dependency Inversion Principle
> "High-level modules should depend on **abstractions**, not on concrete low-level modules."

```java
// ❌ tightly coupled to MySQL; can't test without a real DB
class OrderService { private final MySqlOrderRepo repo = new MySqlOrderRepo(); }

// ✅ depend on an interface, inject the implementation (constructor injection, which is what Spring does)
interface OrderRepository { void save(Order o); }
class OrderService {
    private final OrderRepository repo;
    OrderService(OrderRepository repo) { this.repo = repo; }   // MySQL, Mongo, or an in-memory fake for tests
}
```

> 🧩 **SOLID in one breath:** "**One job · add, don't edit · subs can stand in · small interfaces · depend on abstractions.**"

### Other principles you should name-drop

| Principle | Meaning |
|---|---|
| **DRY** | Don't Repeat Yourself: one source of truth |
| **KISS** | Keep It Simple, Stupid |
| **YAGNI** | You Aren't Gonna Need It: don't build for imaginary futures |
| **Composition over inheritance** | HAS-A is more flexible than IS-A |
| **Program to an interface** | `List<X> l = new ArrayList<>()` |
| **Law of Demeter** | "Talk to friends, not strangers": avoid `a.getB().getC().doIt()` |
| **Tell, don't ask** | tell objects what to do instead of pulling their data out and deciding for them |

---

## PART B: Creational Patterns

### 1. Singleton: exactly one instance + a global access point

```java
// ✅ BEST: enum singleton (thread-safe, serialization-safe, reflection-safe)
public enum AppConfig {
    INSTANCE;
    private final Properties props = new Properties();
    public String get(String key) { return props.getProperty(key); }
}

// ✅ Bill Pugh / holder idiom: lazy + thread-safe without locks (the JVM guarantees class init is thread-safe)
public final class Registry {
    private Registry() { }
    private static class Holder { static final Registry INSTANCE = new Registry(); }
    public static Registry getInstance() { return Holder.INSTANCE; }
}

// ✅ Double-checked locking: volatile is REQUIRED (prevents seeing a half-constructed object)
public final class Db {
    private static volatile Db instance;
    private Db() { }
    public static Db getInstance() {
        if (instance == null) {                    // 1st check (no lock, fast path)
            synchronized (Db.class) {
                if (instance == null) {            // 2nd check (inside the lock)
                    instance = new Db();
                }
            }
        }
        return instance;
    }
}
```

| Variant | Lazy? | Thread-safe? | Notes |
|---|---|---|---|
| Eager (`static final X I = new X()`) | ❌ | ✅ | simple, created even if unused |
| Lazy, unsynchronized | ✅ | ❌ | broken with threads |
| `synchronized getInstance()` | ✅ | ✅ | slow: a lock on every call |
| Double-checked + `volatile` | ✅ | ✅ | the classic interview answer |
| Holder idiom | ✅ | ✅ | elegant |
| **Enum** | ❌ (at class load) | ✅ | **safest**: immune to reflection & serialization attacks |

**How singletons get broken (and the fixes):** reflection calls the private constructor (throw if the instance
already exists) · serialization creates a new copy (add `readResolve()` returning INSTANCE) · cloning (don't
implement Cloneable / throw). **Enum handles all three automatically.**
**JDK:** `Runtime.getRuntime()`. ⚠️ Singletons are global state and hard to test. Prefer DI-managed singletons (Spring beans).

### 2. Factory (Method): let a method decide which class to instantiate

```java
interface Notification { void send(String msg); }
class EmailNotification implements Notification { public void send(String m) { ... } }
class SmsNotification   implements Notification { public void send(String m) { ... } }

class NotificationFactory {
    static Notification create(String channel) {
        return switch (channel) {
            case "EMAIL" -> new EmailNotification();
            case "SMS"   -> new SmsNotification();
            default -> throw new IllegalArgumentException("Unknown channel " + channel);
        };
    }
}
Notification n = NotificationFactory.create("SMS");   // the client never calls `new SmsNotification()`
```
**JDK:** `Calendar.getInstance()`, `NumberFormat.getInstance()`, `List.of()`, `Integer.valueOf()`, `Executors.newFixedThreadPool()`.
**Abstract Factory** = a factory of factories for **families** (e.g. `DarkThemeFactory` → DarkButton + DarkCheckbox).

### 3. Builder: build complex objects step by step

```java
// Problem: telescoping constructors → new Pizza("L", true, false, true, false, "thin") ???
public class Pizza {
    private final String size; private final boolean cheese, olives; private final String crust;
    private Pizza(Builder b) { size = b.size; cheese = b.cheese; olives = b.olives; crust = b.crust; }

    public static Builder builder(String size) { return new Builder(size); }

    public static class Builder {
        private final String size;              // required
        private boolean cheese, olives;         // optional
        private String crust = "regular";
        private Builder(String size) { this.size = size; }
        public Builder cheese()            { cheese = true; return this; }
        public Builder olives()            { olives = true; return this; }
        public Builder crust(String crust) { this.crust = crust; return this; }
        public Pizza build() { /* validate here */ return new Pizza(this); }
    }
}
Pizza p = Pizza.builder("L").cheese().crust("thin").build();   // readable, immutable result
```
**JDK:** `StringBuilder`, `HttpRequest.newBuilder()`, `Stream.builder()`, `Locale.Builder`. Lombok: `@Builder`.

### 4. Prototype: create new objects by copying an existing one
`Object.clone()` or copy constructors. Useful when creation is expensive (e.g. a loaded configuration template).

---

## PART C: Structural Patterns

### 5. Adapter: make incompatible interfaces work together 🔌

```java
interface PaymentGateway { void pay(double rupees); }              // what our app expects
class LegacyPayPal { void makePayment(String currency, double amt) { ... } }   // what we have

class PayPalAdapter implements PaymentGateway {
    private final LegacyPayPal paypal = new LegacyPayPal();
    public void pay(double rupees) { paypal.makePayment("INR", rupees); }   // translate the call
}
```
**JDK:** `Arrays.asList()` (array → List), `InputStreamReader` (byte stream → char stream).

### 6. Decorator: add responsibilities dynamically by wrapping ☕

```java
interface Coffee { double cost(); String desc(); }
class Espresso implements Coffee { public double cost() { return 100; } public String desc() { return "Espresso"; } }

abstract class CoffeeDecorator implements Coffee {
    protected final Coffee inner;
    CoffeeDecorator(Coffee inner) { this.inner = inner; }
}
class Milk extends CoffeeDecorator {
    Milk(Coffee c) { super(c); }
    public double cost() { return inner.cost() + 30; }
    public String desc() { return inner.desc() + " + Milk"; }
}
Coffee order = new Caramel(new Milk(new Espresso()));   // stack behaviours at runtime
```
**JDK:** `new BufferedReader(new InputStreamReader(new FileInputStream(f)))`, `Collections.unmodifiableList()`,
`Collections.synchronizedList()`.

> **Decorator vs inheritance:** inheritance adds behaviour at compile time to **all** instances. A decorator adds
> it at runtime to **one** object, and decorators combine freely (avoiding a class explosion like `MilkCaramelEspresso`).

### 7. Proxy: a stand-in that controls access

Types: **virtual** (lazy-load something expensive), **protection** (access checks), **remote** (RMI), **logging/caching**.

```java
interface Image { void display(); }
class RealImage implements Image { RealImage(String f) { /* expensive load */ } public void display() {...} }
class LazyImageProxy implements Image {
    private final String file; private RealImage real;
    LazyImageProxy(String file) { this.file = file; }
    public void display() {
        if (real == null) real = new RealImage(file);   // created only when first needed
        real.display();
    }
}
```
**JDK/frameworks:** `java.lang.reflect.Proxy` (dynamic proxies), **Spring AOP** (`@Transactional`), Hibernate lazy entities.

> **Proxy vs Decorator:** same structure, different **intent**. A proxy **controls access** to the object; a
> decorator **adds behaviour**.

### 8. Facade: one simple entry point to a complex subsystem

```java
class OrderFacade {
    private final Inventory inventory = new Inventory();
    private final PaymentService payment = new PaymentService();
    private final Shipping shipping = new Shipping();
    public void placeOrder(String item, double amount) {   // the client calls ONE method
        inventory.reserve(item);
        payment.charge(amount);
        shipping.schedule(item);
    }
}
```
**Examples:** Spring `JdbcTemplate` (hides JDBC boilerplate), SLF4J.

### 9. Composite: treat individual objects and groups the same way 🌳
`File` and `Folder` both implement `FileSystemItem.size()`; a folder's size is the sum of its children's sizes.
**JDK:** AWT `Container` contains `Component`s.

### 10. Flyweight: share objects to save memory
**JDK:** the `Integer` cache (-128..127), the **String pool**, `Boolean.TRUE`.

---

## PART D: Behavioral Patterns

### 11. Strategy: define a family of algorithms and make them interchangeable

```java
interface DiscountStrategy { double apply(double amount); }
class Checkout {
    private DiscountStrategy strategy;
    Checkout(DiscountStrategy s) { this.strategy = s; }
    void setStrategy(DiscountStrategy s) { this.strategy = s; }   // swap at runtime
    double total(double amount) { return strategy.apply(amount); }
}
// With lambdas, no classes needed:
Checkout c = new Checkout(amt -> amt);                // no discount
c.setStrategy(amt -> amt * 0.9);                      // festive 10% off
c.setStrategy(amt -> amt > 1000 ? amt - 200 : amt);   // flat 200 off above 1000
```
**JDK:** `Comparator` (sorting strategy), `ThreadPoolExecutor` rejection policies.

> **Strategy vs State:** Strategy is chosen **by the client**. State changes **itself** internally.

### 12. Observer: one-to-many notification (publish/subscribe) 📣

```java
interface Subscriber { void update(String video); }
class Channel {
    private final List<Subscriber> subs = new ArrayList<>();
    void subscribe(Subscriber s) { subs.add(s); }
    void unsubscribe(Subscriber s) { subs.remove(s); }
    void upload(String video) { subs.forEach(s -> s.update(video)); }   // notify all
}
Channel ch = new Channel();
ch.subscribe(v -> System.out.println("Asha watches " + v));
ch.upload("Java Streams in 10 min");
```
**JDK/frameworks:** event listeners (`ActionListener`), `PropertyChangeListener`, Spring `ApplicationEvent`,
reactive streams (`Flow`).

### 13. Template Method: a fixed skeleton with overridable steps

```java
abstract class DataExporter {
    public final void export() {          // final: the algorithm order can't change
        connect(); var data = fetch(); write(format(data)); close();
    }
    protected abstract List<String> fetch();      // subclasses fill in these steps
    protected abstract String format(List<String> data);
    protected void connect() { }                  // hook with a default
    protected void close() { }
    private void write(String s) { System.out.println(s); }
}
class CsvExporter extends DataExporter { ... }
```
**JDK:** `AbstractList` (implement `get` + `size`), `HttpServlet.service()` → `doGet()/doPost()`, `InputStream.read(byte[])`.

### 14. Command: wrap a request as an object (undo/redo, queues, macros)

```java
interface Command { void execute(); void undo(); }
record AddText(StringBuilder doc, String text) implements Command {
    public void execute() { doc.append(text); }
    public void undo() { doc.setLength(doc.length() - text.length()); }
}
Deque<Command> history = new ArrayDeque<>();
Command c = new AddText(doc, "Hello"); c.execute(); history.push(c);
history.pop().undo();                         // undo!
```
**JDK:** `Runnable`, `Callable` (tasks submitted to executors are commands).

### 15. Chain of Responsibility: pass a request along a chain of handlers

```java
abstract class Approver {
    private Approver next;
    Approver linkWith(Approver next) { this.next = next; return next; }
    void approve(double amount) {
        if (canApprove(amount)) System.out.println(getClass().getSimpleName() + " approved " + amount);
        else if (next != null) next.approve(amount);
        else System.out.println("Rejected " + amount);
    }
    abstract boolean canApprove(double amount);
}
// TeamLead (≤ 10k) → Manager (≤ 1L) → Director (≤ 10L)
```
**Examples:** servlet `FilterChain`, the Spring Security filter chain, logging levels, exception handling up the call stack.

### 16. Iterator & State, briefly
- **Iterator:** traverse a collection without exposing its structure. `Iterator<T>`, the for-each loop.
- **State:** an object changes its behaviour when its internal state changes (vending machine: `NoCoin` → `HasCoin` →
  `Dispensing`). Each state is a class, which removes giant `if (state == ...)` blocks. In modern Java, a sealed
  interface of states works nicely.

---

## 📊 Pattern Cheat Table

| Pattern | Category | One-line intent | In the JDK / frameworks |
|---|---|---|---|
| Singleton | Creational | one instance | `Runtime.getRuntime()` |
| Factory Method | Creational | create without `new` | `Calendar.getInstance()`, `List.of()` |
| Abstract Factory | Creational | families of objects | `DocumentBuilderFactory` |
| Builder | Creational | step-by-step construction | `StringBuilder`, `HttpRequest.newBuilder()` |
| Prototype | Creational | copy an existing object | `clone()` |
| Adapter | Structural | convert an interface | `Arrays.asList`, `InputStreamReader` |
| Decorator | Structural | add behaviour by wrapping | `BufferedReader`, `Collections.unmodifiableList` |
| Proxy | Structural | control access | `reflect.Proxy`, Spring AOP |
| Facade | Structural | simplify a subsystem | `JdbcTemplate` |
| Composite | Structural | part-whole trees | AWT `Container` |
| Flyweight | Structural | share instances | Integer cache, String pool |
| Strategy | Behavioral | swap algorithms | `Comparator` |
| Observer | Behavioral | publish/subscribe | listeners, `Flow` |
| Template Method | Behavioral | skeleton + hooks | `AbstractList`, `HttpServlet` |
| Command | Behavioral | request as an object | `Runnable` |
| Iterator | Behavioral | traverse | `Iterator` |
| Chain of Responsibility | Behavioral | handler chain | servlet `FilterChain` |
| State | Behavioral | behaviour per state | (enum/sealed state machines) |

---

## 🧩 Memory Tricks

| Concept | Trick |
|---|---|
| SOLID | "**One job · add don't edit · subs stand in · small interfaces · depend on abstractions**" |
| 3 categories | "**Creational = birth · Structural = body · Behavioral = behaviour**" |
| Decorator | "**Toppings on a coffee**" |
| Adapter | "**Travel plug adapter**" |
| Proxy | "**A security guard / receptionist in front of the boss**" |
| Facade | "**The hotel concierge**": one person handles everything |
| Observer | "**YouTube subscribe + bell icon**" |
| Strategy | "**Google Maps route: car / bike / walk**" |
| Chain of Responsibility | "**Leave approval: Lead → Manager → Director**" |
| Template Method | "**A recipe with fixed steps where you choose the filling**" |

---

## ❓ Self-Test

<details><summary>1. Why must the instance field be volatile in double-checked locking?</summary>

`instance = new Db()` isn't atomic: allocate, construct, assign can be reordered. Without volatile, another thread
could see a non-null reference to a not-yet-constructed object. Volatile prevents that reordering and guarantees
visibility.
</details>

<details><summary>2. What's the best way to implement a Singleton in Java?</summary>

An enum with a single constant. The JVM guarantees one instance, and it's thread-safe, serialization-safe and
reflection-safe. (The holder idiom is a good lazy alternative.)
</details>

<details><summary>3. Decorator vs Proxy?</summary>

They have the same wrapping structure but different intent. A decorator adds behaviour, and you can stack several.
A proxy controls access to the real object (lazy loading, security, remoting).
</details>

<details><summary>4. Which pattern does java.io use heavily? Give an example.</summary>

Decorator, e.g. `new BufferedReader(new InputStreamReader(new FileInputStream("f")))`. Each wrapper adds a
capability. `InputStreamReader` is also an Adapter (bytes → chars).
</details>

<details><summary>5. Explain Liskov with an example.</summary>

Code that works with a parent type must work with any subtype. Square extending Rectangle breaks it: setting the
width also changes the height, so a caller expecting independent width and height gets wrong results.
</details>

<details><summary>6. Strategy vs State?</summary>

Both delegate behaviour to interchangeable objects. In Strategy the client picks the algorithm. In State the object
switches its own state object internally as transitions happen.
</details>

<details><summary>7. How does Dependency Inversion relate to Spring?</summary>

Spring's IoC container creates beans and injects dependencies (usually by constructor) typed as interfaces. Your
high-level services depend on abstractions, and Spring wires in the concrete implementations.
</details>

---

## ✅ Chapter Checklist

- [ ] Explain each SOLID letter with a bad → good example
- [ ] Write 3 singleton versions and explain how each one can break
- [ ] Code Builder, Decorator, Strategy and Observer from memory
- [ ] Name a JDK example for 8 different patterns
- [ ] Run [code/12_patterns](../code/12_patterns)

➡️ Next: [13 · Annotations, Reflection, Date-Time & JDBC](13-more-apis.md)
