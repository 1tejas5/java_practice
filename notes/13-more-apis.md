# 13 · Annotations, Reflection, Date-Time API & JDBC

> ⏱️ **Revise in:** 45 min · 🎯 **Level:** Intermediate → Advanced · ▶️ **Code:** [code/13_more_apis](../code/13_more_apis)

## 🧠 Mind Map

```
13 MORE APIs
├── Annotations = metadata on code (@Something)
│   ├── Built-in: @Override · @Deprecated · @SuppressWarnings · @FunctionalInterface · @SafeVarargs
│   ├── Meta: @Retention(SOURCE | CLASS | RUNTIME) · @Target(TYPE, METHOD, FIELD...) · @Inherited · @Repeatable
│   ├── Custom: @interface MyAnno { String value() default ""; }
│   └── Processed: by the compiler · annotation processors (Lombok) · reflection at runtime (Spring, JUnit)
├── Reflection = inspect & modify classes at RUNTIME
│   ├── Class<?>: X.class · obj.getClass() · Class.forName("a.b.X")
│   ├── getDeclaredFields/Methods/Constructors · getAnnotation
│   ├── field.setAccessible(true) · method.invoke(obj, args) · ctor.newInstance()
│   └── Used by: Spring DI, Hibernate, JUnit, Jackson ·  ⚠️ slow, breaks encapsulation, no compile checks
├── Date-Time (java.time, Java 8) — IMMUTABLE & thread-safe
│   ├── LocalDate · LocalTime · LocalDateTime (no zone)
│   ├── ZonedDateTime (with zone) · Instant (UTC timestamp) · ZoneId("Asia/Kolkata")
│   ├── Period (date-based: 1y 2m 3d) vs Duration (time-based: 5h 30m)
│   ├── ChronoUnit.DAYS.between · TemporalAdjusters · DateTimeFormatter.ofPattern("dd-MM-yyyy")
│   └── Old Date/Calendar/SimpleDateFormat → mutable, not thread-safe, months 0-11 ❌
└── JDBC = talk to relational databases
    ├── Connection → PreparedStatement (? params) → ResultSet → close (try-with-resources)
    ├── Statement (SQL injection risk ❌) · PreparedStatement ✅ · CallableStatement (procedures)
    ├── executeQuery (SELECT → ResultSet) · executeUpdate (DML → row count) · batches
    ├── Transactions: setAutoCommit(false) · commit · rollback · isolation levels
    └── Connection pooling (HikariCP) · higher level: JdbcTemplate, JPA/Hibernate
```

---

## PART A: Annotations

**One-liner:** annotations are **metadata** attached to code. They don't change logic by themselves; **tools**
(the compiler, annotation processors, frameworks via reflection) read them and act on them.

### Built-in annotations

| Annotation | Purpose |
|---|---|
| `@Override` | the compiler verifies the method really overrides something (catches typos) |
| `@Deprecated(since="9", forRemoval=true)` | marks an API as obsolete → compiler warnings |
| `@SuppressWarnings("unchecked")` | silences specific compiler warnings |
| `@FunctionalInterface` | the compiler enforces exactly one abstract method |
| `@SafeVarargs` | suppresses heap-pollution warnings on generic varargs |
| `@Serial` (14) | marks serialization members (`serialVersionUID`, `readObject`) for compiler checks |

### Meta-annotations (annotations on annotations)

| Meta-annotation | Options |
|---|---|
| **`@Retention`** | `SOURCE` (discarded by the compiler, e.g. `@Override`) · `CLASS` (in the .class file, not at runtime: the default) · **`RUNTIME`** (readable via reflection) |
| **`@Target`** | `TYPE`, `METHOD`, `FIELD`, `PARAMETER`, `CONSTRUCTOR`, `LOCAL_VARIABLE`, `ANNOTATION_TYPE`, `TYPE_USE`, `RECORD_COMPONENT`… |
| `@Inherited` | subclasses inherit the class-level annotation |
| `@Documented` | included in the Javadoc |
| `@Repeatable` | can be applied more than once |

### Custom annotation + processing it with reflection

```java
@Retention(RetentionPolicy.RUNTIME)          // MUST be RUNTIME to read via reflection
@Target(ElementType.METHOD)
@interface Test {
    String name() default "";                // elements look like methods; defaults allowed
    int timeoutMs() default 1000;
}

class CalculatorTests {
    @Test(name = "adds two numbers") void add() { assert 2 + 2 == 4; }
    void helper() { }                        // not annotated → ignored
}

for (Method m : CalculatorTests.class.getDeclaredMethods()) {
    if (m.isAnnotationPresent(Test.class)) {
        Test t = m.getAnnotation(Test.class);
        m.invoke(new CalculatorTests());     // this is basically how JUnit works!
    }
}
```

> 🧩 "**SOURCE = compiler only · CLASS = in the file · RUNTIME = frameworks can see it.**"
> Element types are restricted to: primitives, String, Class, enums, other annotations, and arrays of these.
> An element named `value` can be set without its name: `@Table("users")`.

---

## PART B: Reflection

**One-liner:** the ability of a program to **inspect and manipulate its own classes, methods, fields and
constructors at runtime**, even private ones.

```java
// 3 ways to get a Class object
Class<?> c1 = String.class;
Class<?> c2 = "hello".getClass();
Class<?> c3 = Class.forName("java.lang.String");      // by name (dynamic loading)

Class<?> clazz = Employee.class;
clazz.getSimpleName(); clazz.getSuperclass(); clazz.getInterfaces(); Modifier.toString(clazz.getModifiers());

for (Field f : clazz.getDeclaredFields()) { }         // declared = all in this class, incl. private
for (Method m : clazz.getMethods()) { }               // public, incl. inherited
Constructor<?> ctor = clazz.getDeclaredConstructor(String.class, double.class);
Object emp = ctor.newInstance("Asha", 50_000);        // create an object

Field salary = clazz.getDeclaredField("salary");
salary.setAccessible(true);                           // bypass private
salary.set(emp, 99_000.0);

Method secret = clazz.getDeclaredMethod("secretBonus", int.class);
secret.setAccessible(true);
Object result = secret.invoke(emp, 10);               // call a private method
```

| `getFields()` / `getMethods()` | `getDeclaredFields()` / `getDeclaredMethods()` |
|---|---|
| **public** only, **including inherited** | **all** access levels, **this class only** |

**Who uses reflection?** Spring (DI, `@Autowired`, `@Component` scanning) · Hibernate/JPA (mapping fields to columns) ·
JUnit (finding `@Test` methods) · Jackson (JSON ⇄ objects) · IDEs/debuggers · dynamic proxies.

| ✅ Power | ❌ Price |
|---|---|
| frameworks, plugins, generic tools | slower than direct calls |
| works with classes unknown at compile time | breaks encapsulation (`setAccessible`) |
| | no compile-time type checking (errors at runtime) |
| | blocked by the module system unless the package is `opens` |

---

## PART C: Date-Time API (`java.time`, Java 8) ⭐

### Why the old API was bad
`java.util.Date`/`Calendar` are **mutable** (not thread-safe) · `SimpleDateFormat` is **not thread-safe** · months are
**0-indexed** (January = 0 🤦) · confusing time-zone handling · `Date` is really an instant, despite its name.
`java.time` classes are **immutable, thread-safe, clear, and fluent**.

### The classes

| Class | Represents | Example |
|---|---|---|
| `LocalDate` | a date, no time, no zone | `2026-10-27` (birthdays, holidays) |
| `LocalTime` | a time, no date | `09:30` (office opening time) |
| `LocalDateTime` | date + time, **no zone** | `2026-10-27T09:30` |
| `ZonedDateTime` | date + time + **zone** | `2026-10-27T09:30+05:30[Asia/Kolkata]` |
| `OffsetDateTime` | date + time + UTC offset | APIs, databases |
| `Instant` | a point on the UTC timeline (epoch-based) | timestamps, logs, `createdAt` |
| `Duration` | **time-based** amount (hours, minutes, seconds, nanos) | `PT2H30M` |
| `Period` | **date-based** amount (years, months, days) | `P1Y2M3D` |
| `ZoneId` | a time-zone id | `ZoneId.of("Asia/Kolkata")` |
| `YearMonth`, `MonthDay`, `DayOfWeek`, `Month` | helpers | card expiry, recurring dates |

```java
LocalDate today = LocalDate.now();
LocalDate dob = LocalDate.of(1998, Month.AUGUST, 15);           // months are 1-12 ✅
LocalDate parsed = LocalDate.parse("2026-12-25");               // ISO format by default

LocalDate due = today.plusDays(30).minusMonths(1).withDayOfMonth(1);   // returns NEW objects (immutable)
today.getDayOfWeek(); today.isLeapYear(); today.lengthOfMonth();
dob.isBefore(today); today.isAfter(dob); today.isEqual(LocalDate.now());

int age = Period.between(dob, today).getYears();                 // age calculation
long daysLeft = ChronoUnit.DAYS.between(today, LocalDate.of(2026, 12, 31));
Duration meeting = Duration.between(LocalTime.of(9, 0), LocalTime.of(10, 45));   // PT1H45M

LocalDate lastDay = today.with(TemporalAdjusters.lastDayOfMonth());
LocalDate nextFri = today.with(TemporalAdjusters.next(DayOfWeek.FRIDAY));

DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");   // thread-safe!
String s = LocalDateTime.now().format(fmt);                      // "27-Oct-2026 14:05"
LocalDateTime back = LocalDateTime.parse("27-Oct-2026 14:05", fmt);

ZonedDateTime india = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
ZonedDateTime ny = india.withZoneSameInstant(ZoneId.of("America/New_York"));  // same moment, other zone
Instant now = Instant.now();  long epochMs = now.toEpochMilli();

Date legacy = Date.from(now);  Instant fromLegacy = legacy.toInstant();      // interop with old code
```

> ⚠️ `plusDays()` etc. return a **new** object. `today.plusDays(1);` on its own does nothing (the same trap as
> Strings).
> 🧩 "**Period = Paper calendar (days/months/years) · Duration = Digital stopwatch (hours/min/sec).**"
> 💡 Store **`Instant`** (UTC) in databases/logs and convert to `ZonedDateTime` only for display.

| Pattern letter | Meaning | Example |
|---|---|---|
| `yyyy` / `yy` | year | 2026 / 26 |
| `MM` / `MMM` / `MMMM` | month | 10 / Oct / October |
| `dd` | day of month | 27 |
| `HH` / `hh` + `a` | hour 0-23 / 1-12 + AM/PM | 14 / 02 PM |
| `mm` / `ss` | minutes / seconds | 05 / 09 |
| `EEE` | day name | Tue |

---

## PART D: JDBC (Java Database Connectivity)

### The 6 steps

```java
String url = "jdbc:mysql://localhost:3306/bank";       // jdbc:<vendor>://host:port/db
String sql = "SELECT id, name, balance FROM accounts WHERE balance > ?";

try (Connection con = DriverManager.getConnection(url, "user", "pass");   // 1-2. driver auto-loads (JDBC 4) + connect
     PreparedStatement ps = con.prepareStatement(sql)) {                  // 3. prepare
    ps.setDouble(1, 10_000);                                              // 4. bind parameters (1-based!)
    try (ResultSet rs = ps.executeQuery()) {                              // 5. execute
        while (rs.next()) {                                               //    cursor starts BEFORE the first row
            System.out.println(rs.getInt("id") + " " + rs.getString("name") + " " + rs.getDouble(3));
        }
    }
}                                                                         // 6. auto-close (reverse order)
```

| Method | Use for | Returns |
|---|---|---|
| `executeQuery()` | SELECT | `ResultSet` |
| `executeUpdate()` | INSERT / UPDATE / DELETE / DDL | `int` (rows affected) |
| `execute()` | anything | `boolean` (true if a ResultSet came back) |
| `addBatch()` + `executeBatch()` | many inserts at once | `int[]` |

### Statement vs PreparedStatement vs CallableStatement

| | `Statement` | `PreparedStatement` ✅ | `CallableStatement` |
|---|---|---|---|
| SQL | concatenated strings | precompiled with `?` placeholders | stored procedures `{call proc(?, ?)}` |
| SQL injection | ❌ **vulnerable** | ✅ **safe** (parameters are bound as data) | ✅ |
| Performance (repeated) | re-parsed every time | precompiled once, reused | |

```java
// ❌ SQL INJECTION: input = "' OR '1'='1"  →  WHERE name = '' OR '1'='1'  → returns ALL rows!
stmt.executeQuery("SELECT * FROM users WHERE name = '" + input + "'");

// ✅ parameter binding: input is treated as plain data, never as SQL
PreparedStatement ps = con.prepareStatement("SELECT * FROM users WHERE name = ?");
ps.setString(1, input);
```

### Transactions (ACID)

```java
con.setAutoCommit(false);                     // start a manual transaction
try {
    debit.executeUpdate();                    // UPDATE accounts SET balance = balance - 500 WHERE id = 1
    credit.executeUpdate();                   // UPDATE accounts SET balance = balance + 500 WHERE id = 2
    con.commit();                             // both succeed → save
} catch (SQLException e) {
    con.rollback();                           // either fails → undo both
    throw e;
}
```

**ACID:** **A**tomicity (all or nothing) · **C**onsistency (valid state to valid state) · **I**solation (concurrent
transactions don't interfere) · **D**urability (once committed, it survives crashes).

| Isolation level | Dirty read | Non-repeatable read | Phantom read |
|---|:-:|:-:|:-:|
| `READ_UNCOMMITTED` | possible | possible | possible |
| `READ_COMMITTED` (common default) | ❌ | possible | possible |
| `REPEATABLE_READ` (MySQL default) | ❌ | ❌ | possible |
| `SERIALIZABLE` | ❌ | ❌ | ❌ |

> **Dirty read:** you see another transaction's *uncommitted* data. **Non-repeatable read:** the same row read twice gives
> different values. **Phantom read:** the same query returns *new rows* the second time.

**Connection pooling:** creating connections is expensive (network + auth), so a pool (**HikariCP**, Spring Boot's
default) keeps connections open and lends them out. Use a `DataSource` instead of `DriverManager` in real apps.
**Higher levels:** Spring `JdbcTemplate` (removes the boilerplate) → **JPA/Hibernate** (ORM: objects ↔ tables) →
**Spring Data JPA** (repositories with derived queries).

---

## ❓ Self-Test

<details><summary>1. What retention must an annotation have so that Spring/JUnit can read it at runtime?</summary>

`@Retention(RetentionPolicy.RUNTIME)`. The default is CLASS, which isn't visible to reflection.
</details>

<details><summary>2. getMethods() vs getDeclaredMethods()?</summary>

`getMethods()` returns all **public** methods, including inherited ones. `getDeclaredMethods()` returns methods
of **every** access level declared in **this class only**.
</details>

<details><summary>3. Why is java.time better than Date/Calendar?</summary>

It's immutable and thread-safe, months are 1-based, the API is clear and separates the concepts (date, time,
zone, instant, period, duration), formatters are thread-safe, and time-zone support is solid.
</details>

<details><summary>4. Period vs Duration?</summary>

A Period is date-based (years, months, days), used with LocalDate. A Duration is time-based (hours, minutes,
seconds, nanos), used with LocalTime, LocalDateTime and Instant.
</details>

<details><summary>5. How does PreparedStatement prevent SQL injection?</summary>

The SQL structure is compiled first with `?` placeholders, and the parameters are sent separately as data.
They can never be interpreted as SQL syntax.
</details>

<details><summary>6. How do you make two updates atomic in JDBC?</summary>

Call `setAutoCommit(false)`, run both updates, and `commit()`. On any exception, `rollback()`.
</details>

<details><summary>7. Why use a connection pool?</summary>

Opening a DB connection is slow (TCP plus authentication). A pool reuses open connections, which improves
latency and throughput and caps the total number of connections.
</details>

---

## ✅ Chapter Checklist

- [ ] Write a custom RUNTIME annotation and read it with reflection
- [ ] Call a private method through reflection
- [ ] Calculate an age and the days until an event with java.time
- [ ] Format and parse dates with DateTimeFormatter
- [ ] Write JDBC code with PreparedStatement, try-with-resources and a transaction
- [ ] Run [code/13_more_apis](../code/13_more_apis)

➡️ Next: [14 · Interview Questions](14-interview-questions.md)
