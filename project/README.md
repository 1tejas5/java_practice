# 🏦 MiniBank: the Hands-On Revision Project

> One small but realistic application that uses **nearly every concept in the notes**: OOP, exceptions, generics,
> collections, streams, file I/O, concurrency, modern Java (records, sealed types, pattern matching), design patterns,
> annotations and reflection.
>
> You can use it in two ways:
> 1. **Run and read** the finished code. Every file has a `CONCEPTS:` header saying what it teaches.
> 2. **Build it yourself** by following the 9 milestones below. This is the best way to revise. 💪

---

## ▶️ Run it

```powershell
cd C:\java\project
run.cmd            # guided demo: walks through all 8 concept areas (~2 seconds)
run.cmd cli        # interactive banking console with sample data
run.cmd test       # 21 unit tests (custom @Test annotation + reflection-based runner)
```

<details><summary>Other ways to run (Git Bash, manual javac)</summary>

```bash
./run.sh            # Git Bash / Linux / macOS
./run.sh cli
./run.sh test
```

```powershell
# Manual build, to understand what the script does:
dir /s /b src\*.java test\*.java > sources.txt
javac -d out\classes @sources.txt
java -cp out\classes com.minibank.App            # demo
java -cp out\classes com.minibank.App cli        # interactive
java -cp out\classes com.minibank.testing.TestRunner
```
</details>

**Try in CLI mode:**
```
accounts
deposit ACC-1001 2500
withdraw ACC-1001 999999          ← rejected: InsufficientFundsException (checked)
transfer ACC-1003 ACC-1002 750
register Priya priya@mail.com
open CUST-1005 current 3000
statement ACC-1002
report
simulate 20000                    ← 20,000 parallel transfers on virtual threads, money conserved
export ACC-1001                   ← writes out/statements/ACC-1001.csv
```

---

## 🏗️ Architecture

```
                         ┌──────────────────────────────┐
     run.cmd ──────────► │ App (composition root)       │  wires everything (what Spring would do)
                         └──────┬──────────────┬────────┘
                                │              │
                    ┌───────────▼───┐     ┌────▼─────────────┐
                    │ cli           │     │ sim              │
                    │ ConsoleApp    │     │ Concurrency-     │
                    │ CommandParser │     │ Simulation       │
                    │ Command(sealed)│    └────┬─────────────┘
                    └───────┬───────┘          │
                            ▼                  ▼
                    ┌────────────────────────────────────┐      ┌───────────────────┐
                    │ service                            │─────►│ event (Observer)  │
                    │ BankService · ReportService        │      │ TransactionListener│
                    │ AccountFactory                     │      │ FraudDetector     │
                    └───────┬───────────────────┬────────┘      └───────────────────┘
                            ▼                   ▼
                    ┌───────────────┐   ┌───────────────────────────────────────────┐
                    │ repository    │   │ model                                     │
                    │ Repository<T,ID>│ │ Account (abstract) ◄── SavingsAccount      │
                    │ InMemoryRepo  │   │                    ◄── CurrentAccount      │
                    └───────────────┘   │ Customer (Builder) · Transaction (record)  │
                                        │ AccountType · TransactionType (enums)      │
                                        └───────────────────────────────────────────┘
                    ┌───────────────────────────┐   ┌──────────────────────────────┐
                    │ exception                 │   │ util                         │
                    │ BankException (checked)   │   │ Money (BigDecimal helpers)   │
                    │  ├ InsufficientFunds...   │   │ IdGenerator (enum Singleton) │
                    │  ├ AccountNotFound...     │   │ CsvExporter (NIO.2)          │
                    │  └ CustomerNotFound...    │   └──────────────────────────────┘
                    │ InvalidAmountException    │
                    │   (unchecked)             │
                    └───────────────────────────┘
```

---

## 🗺️ Concept Map: where each topic lives

| Concept | Where to look |
|---|---|
| **Abstract class, template method** | `model/Account.java` (`withdraw` is final, `canWithdraw` is the hook) |
| **Inheritance & overriding** | `SavingsAccount` (minimum balance), `CurrentAccount` (overdraft) |
| **Runtime polymorphism** | `BankService.applyMonthlyInterest()` calls `monthlyInterest()` on any Account |
| **Encapsulation** | `Account.balance` is private and only changes via `deposit`/`withdraw` |
| **Immutable class + Builder** | `model/Customer.java` |
| **equals/hashCode** | `Customer` (by id), and records (automatic) |
| **Enums with fields/behaviour** | `AccountType` (interest rate), `TransactionType` (credit/debit sign) |
| **Records + compact constructor** | `model/Transaction.java`, `ReportService.CustomerWealth`, `cli/Command.java` |
| **Checked vs unchecked exceptions** | `exception/` package: `BankException` hierarchy vs `InvalidAmountException` |
| **Exception translation** | `CsvExporter` (IOException → UncheckedIOException) |
| **Generics + bounded wildcards** | `repository/Repository<T, ID>`, `InMemoryRepository(Function<? super T, ? extends ID>)` |
| **Optional** | `Repository.findById` + `orElseThrow(() -> new AccountNotFoundException(id))` |
| **Collections** | `ConcurrentHashMap`, `CopyOnWriteArrayList`, `EnumMap`, `List.copyOf` |
| **Streams & Collectors** | `ReportService`: groupingBy + reducing, partitioningBy, flatMap, sorted, max |
| **Text blocks** | `ReportService.summary()`, `CommandParser.help()` |
| **Sealed interface + pattern matching switch + record patterns** | `cli/Command.java` + `cli/ConsoleApp.execute()` |
| **Switch expression on strings, multi-catch** | `cli/CommandParser.java` |
| **Factory pattern** | `service/AccountFactory.java` |
| **Singleton (enum)** | `util/IdGenerator.java` |
| **Observer pattern (functional interface)** | `event/TransactionListener`, `FraudDetector`, and the lambda listener in `App` |
| **Dependency inversion / injection** | `BankService` constructor takes `Repository` interfaces, wired in `App.main` |
| **synchronized, deadlock-free lock ordering** | `BankService.transfer()` (locks by account id order) |
| **ExecutorService, virtual threads, Atomic counters** | `sim/ConcurrencySimulation.java` |
| **BigDecimal money maths** | `util/Money.java`, `SavingsAccount.monthlyInterest()` |
| **NIO.2 file I/O** | `util/CsvExporter.java` |
| **java.time** | `Transaction.timestamp` + `DateTimeFormatter` |
| **Annotations + reflection** | `test/.../testing/Test.java` + `TestRunner.java` (a mini JUnit) |
| **Unit testing mindset** | `test/.../BankServiceTest.java`, `ModelAndParserTest.java` |

---

## 🧗 Build It Yourself: 9 Milestones

> Create a new folder (e.g. `C:\java\my-bank\src\com\mybank\...`) and build these step by step. **Only look at the
> reference code when a milestone is done**, or when you've been stuck for 20+ minutes. Tick ✅ as you go.

### Milestone 1: The domain model (OOP) ⏱️ 60 min
- [ ] `enum AccountType { SAVINGS, CURRENT }` with an `annualInterestRate` field
- [ ] `record Transaction(id, accountId, type, amount, balanceAfter, timestamp, note)` with a compact constructor that rejects nulls
- [ ] `final class Customer` with a **Builder** (id and name required; email validated)
- [ ] `abstract class Account` with a private `BigDecimal balance`, `deposit()`, and `withdraw()` that calls an abstract `canWithdraw()`
- [ ] `SavingsAccount` (keeps a minimum of 1000, earns 4% p.a. monthly) and `CurrentAccount` (overdraft up to 10,000, no interest)
- **Check:** a `main` that creates both, deposits, and prints `describe()` for each through a `List<Account>` (polymorphism!)
- 📚 Revise: [02 OOP](../notes/02-oops.md), [03 Enums](../notes/03-strings-wrappers-enums.md), [11 Records](../notes/11-modern-java.md)

### Milestone 2: Exceptions ⏱️ 30 min
- [ ] Checked `BankException` → `InsufficientFundsException` (with `requested`, `available`, `shortfall()`), `AccountNotFoundException`
- [ ] Unchecked `InvalidAmountException` for amounts ≤ 0
- [ ] `withdraw` throws `InsufficientFundsException` **without changing the balance**
- **Check:** catch the specific exception in one place and the base `BankException` in another
- 📚 Revise: [04 Exceptions](../notes/04-exceptions.md)

### Milestone 3: Generic repository ⏱️ 30 min
- [ ] `interface Repository<T, ID>` with `save`, `findById → Optional<T>`, `findAll`, `deleteById`, and a default `findWhere(Predicate<? super T>)`
- [ ] `InMemoryRepository<T, ID>` backed by a `ConcurrentHashMap`, taking a `Function<T, ID>` such as `Account::id`
- **Check:** store customers and accounts in two repositories and look one up with `orElseThrow`
- 📚 Revise: [05 Generics](../notes/05-generics.md), [06 Collections](../notes/06-collections.md)

### Milestone 4: Business service + patterns ⏱️ 60 min
- [ ] `BankService(Repository<Customer,String>, Repository<Account,String>)`: **constructor injection**
- [ ] `registerCustomer`, `openAccount` (via `AccountFactory`), `deposit`, `withdraw`, `transfer`, `applyMonthlyInterest`
- [ ] `IdGenerator` as an **enum singleton** producing `ACC-1001`, `TX-1001`…
- [ ] An **Observer**: `@FunctionalInterface TransactionListener`, plus a `FraudDetector` that flags amounts ≥ 50,000
- **Check:** a transfer creates TRANSFER_OUT/TRANSFER_IN transactions and both listeners get notified
- 📚 Revise: [12 Patterns](../notes/12-design-patterns-solid.md)

### Milestone 5: Reports with streams ⏱️ 45 min
- [ ] Total balance per `AccountType` (`groupingBy` + `reducing` for BigDecimal)
- [ ] Top N customers by combined balance (group by owner, then sort)
- [ ] All transactions with `flatMap`, the count per type, the largest one (`max`), overdrawn accounts (`partitioningBy`)
- [ ] A `summary()` built with a **text block**
- 📚 Revise: [07 Streams](../notes/07-functional-java8.md)

### Milestone 6: File export ⏱️ 20 min
- [ ] `CsvExporter.exportStatement(account, dir)` with `Files.createDirectories` + `Files.write`
- [ ] Translate `IOException` into `UncheckedIOException`, keeping the cause
- 📚 Revise: [10 I/O](../notes/10-io-nio-serialization.md)

### Milestone 7: Concurrency ⏱️ 60 min
- [ ] Make `Account` methods `synchronized`
- [ ] `transfer`: lock **both** accounts in a fixed order (by id) to avoid deadlock. Think about why!
- [ ] `ConcurrencySimulation`: 20,000 random transfers on a fixed pool **and** on virtual threads, counting successes with `AtomicInteger`
- [ ] **Invariant:** total money before == total money after
- 🧪 **Experiment:** remove the ordered locking (lock `from` then `to`) and run `simulate` a few times. Does it hang? Use `jstack` to see the deadlock!
- 📚 Revise: [08 Concurrency](../notes/08-multithreading-concurrency.md)

### Milestone 8: Interactive CLI with modern Java ⏱️ 45 min
- [ ] `sealed interface Command` with one record per command (`Deposit(accountId, amount)`, …, `Invalid(input, reason)`)
- [ ] `CommandParser.parse(String)`: a switch expression, with bad input becoming `Invalid` (no crashes)
- [ ] `ConsoleApp`: a `Scanner` loop plus an **exhaustive pattern-matching switch** with record patterns
- 📚 Revise: [11 Modern Java](../notes/11-modern-java.md)

### Milestone 9: Your own test framework ⏱️ 45 min
- [ ] `@Retention(RUNTIME) @Target(METHOD) @interface Test`
- [ ] `Assert.assertEquals`, `assertThrows(Class<T>, ThrowingRunnable)`
- [ ] A `TestRunner` that finds `@Test` methods via **reflection**, creates a fresh instance per test, and reports PASS/FAIL
- [ ] Write at least 10 tests covering the rules above
- 📚 Revise: [13 Annotations & Reflection](../notes/13-more-apis.md)

---

## 🚀 Stretch Challenges (pick any)

| Challenge | Concepts practised |
|---|---|
| Add a `FixedDepositAccount` (7% interest, no withdrawal before its maturity date) | inheritance, `LocalDate`, sealed hierarchy |
| Persist to **H2 via JDBC**: a `JdbcAccountRepository implements Repository<Account,String>` | JDBC, PreparedStatement, transactions, and **DIP pays off**: no service changes! |
| Apply interest automatically every 10 seconds with `ScheduledExecutorService` | scheduling, daemon threads |
| A REST API using the JDK's built-in `com.sun.net.httpserver.HttpServer` (`GET /accounts/{id}`) | HTTP, JSON by hand, virtual-thread executor |
| Use `ReentrantLock.tryLock(timeout)` for transfers instead of `synchronized` | Lock API, deadlock avoidance |
| Load/save the whole bank state with serialization or JSON | I/O, `Serializable`, `transient` |
| A monthly statement PDF/HTML report grouped by month (`YearMonth`) | streams + java.time |
| Rewrite it with **Spring Boot + Spring Data JPA** | see how DI, repositories and annotations map 1:1 |

---

## 💬 Interview Talking Points from This Project

- *"How did you make transfers thread-safe?"* → Both monitors are acquired in a global order by account id, which
  prevents the circular wait. The balance check and update happen inside the lock, listeners are notified
  outside it, and a simulation verifies that money is conserved.
- *"Why BigDecimal?"* → It's exact decimal arithmetic. I build values from Strings, use scale 2 with HALF_EVEN
  rounding, and compare with `compareTo`.
- *"Checked or unchecked?"* → Business rule violations are checked (`InsufficientFundsException`) because the
  caller must decide what to do. Bad input or programmer errors are unchecked (`InvalidAmountException`).
- *"Why the Repository interface?"* → Dependency inversion. The service doesn't know whether the data lives in a
  map, H2 or Postgres, and tests use the in-memory version.
- *"Why a sealed Command?"* → The compiler checks that every command is handled. Adding one breaks the build
  until it's implemented.
