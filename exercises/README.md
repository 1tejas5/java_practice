# 🏋️ Exercises: Learn by Doing

> **Rules for real learning:**
> 1. Read the problem → **write your own solution first** (15–20 minutes max per problem).
> 2. Stuck? Open the 💡 hint. Still stuck? Look at the solution, **close it**, and rewrite it from memory.
> 3. Run the solution file to see the expected results: every solution checks itself and prints ✅ PASS / ❌ FAIL.
>
> Difficulty: 🟢 easy · 🟡 medium · 🔴 hard

## How to practise

```powershell
# 1. Copy the practice template and write your solutions there
copy exercises\practice\Practice.java exercises\practice\MyBasics.java   # rename the class inside too
java exercises\practice\MyBasics.java

# 2. Compare with (or run) the reference solutions
java exercises\solutions\Ex01Basics.java
```

---

## 1. Basics
Solutions: [Ex01Basics.java](solutions/Ex01Basics.java)

| # | Problem | Example | Level |
|---|---|---|---|
| 1.1 | **FizzBuzz** → return a list for 1..n: "Fizz" for multiples of 3, "Buzz" for 5, "FizzBuzz" for both | n=5 → `[1, 2, Fizz, 4, Buzz]` | 🟢 |
| 1.2 | **Reverse an integer** (keep the sign) | `-1234 → -4321` | 🟢 |
| 1.3 | **Is prime?** (check divisors up to √n only) | `29 → true` | 🟢 |
| 1.4 | **First n Fibonacci numbers** (iteratively) | `7 → [0,1,1,2,3,5,8]` | 🟢 |
| 1.5 | **Sum of digits** | `9875 → 29` | 🟢 |
| 1.6 | **Armstrong number** (sum of each digit^numDigits == number) | `153 → true` | 🟢 |
| 1.7 | **Second largest** distinct element in an array | `[4,8,1,8,6] → 6` | 🟡 |
| 1.8 | **Rotate an array right by k** (in place, O(1) extra) | `[1,2,3,4,5], k=2 → [4,5,1,2,3]` | 🟡 |
| 1.9 | **Binary search** (iterative) | `[1,3,5,7,9], 7 → 3` | 🟡 |
| 1.10 | **Move zeros to the end**, keeping the order of the rest | `[0,1,0,3,12] → [1,3,12,0,0]` | 🟡 |
| 1.11 | **Matrix multiplication** | 2×2 × 2×2 | 🟡 |
| 1.12 | **Number pyramid** → return it as a String | n=3 → `"  1\n 1 2\n1 2 3"` | 🟢 |

<details><summary>💡 Hints</summary>

- 1.2: `rev = rev * 10 + n % 10; n /= 10;`. Handle the sign separately.
- 1.7: track `first` and `second` in one pass and skip values equal to `first`.
- 1.8: reverse the whole array, then reverse the first k, then the rest (k = k % n).
- 1.10: keep an `insertPos` pointer, copy the non-zeros forward, then fill with zeros.
</details>

## 2. OOP
Solutions: [Ex02Oop.java](solutions/Ex02Oop.java)

| # | Problem | Level |
|---|---|---|
| 2.1 | **Shape hierarchy**: an abstract `Shape` with `area()` and `perimeter()`; `Circle`, `Rectangle`, `Triangle` (3 sides, Heron's formula). Compute the total area of a mixed list. | 🟢 |
| 2.2 | **Accounts**: `Account` (private balance, deposit/withdraw with validation). `SavingsAccount` overrides `withdraw` to keep a minimum balance of 1000. `CurrentAccount` allows an overdraft of up to 5000. Show polymorphism with a `List<Account>`. | 🟡 |
| 2.3 | **Employee equality**: two employees are equal if their `id`s are equal. Override equals/hashCode and prove that a HashSet removes duplicates. | 🟡 |
| 2.4 | **Immutable `Money`** (amount as BigDecimal + currency). `add()` returns a new object and throws `IllegalArgumentException` on a currency mismatch. | 🟡 |
| 2.5 | **Interface `Payable`** (`double amountDue()`) implemented by the unrelated classes `Invoice` and `Salary`. Sum everything payable polymorphically. | 🟢 |
| 2.6 | **Parking lot design**: `Vehicle` (Bike, Car, Truck) needs a spot size (SMALL, MEDIUM, LARGE). A vehicle can park in a spot of its size or larger. `ParkingLot.park(vehicle)` returns the spot or throws when full. `leave(spot)` frees it. | 🔴 |

## 3. Strings
Solutions: [Ex03Strings.java](solutions/Ex03Strings.java)

| # | Problem | Example | Level |
|---|---|---|---|
| 3.1 | **Reverse the words** in a sentence | `"I love Java" → "Java love I"` | 🟢 |
| 3.2 | **Palindrome**, ignoring case and non-alphanumerics | `"A man, a plan, a canal: Panama" → true` | 🟢 |
| 3.3 | **Anagram check** | `listen/silent → true` | 🟢 |
| 3.4 | **Character frequency**, in order of first appearance | `"banana" → {b=1, a=3, n=2}` | 🟢 |
| 3.5 | **First non-repeating character** | `"swiss" → 'w'` | 🟡 |
| 3.6 | **Compress** runs of characters | `"aaabbc" → "a3b2c1"` | 🟡 |
| 3.7 | **Count vowels and consonants** | `"Hello World" → [3, 7]` | 🟢 |
| 3.8 | **Is it a rotation?** | `waterbottle / erbottlewat → true` | 🟡 |
| 3.9 | **Capitalize each word** | `"hello java world" → "Hello Java World"` | 🟢 |
| 3.10 | **Longest substring without repeating characters** (length) | `"abcabcbb" → 3` | 🔴 |

<details><summary>💡 Hints</summary>

- 3.5: count with a `LinkedHashMap`, or two passes over an `int[256]`.
- 3.8: `s2` is a rotation of `s1` iff `(s1 + s1).contains(s2)` and the lengths are equal.
- 3.10: sliding window. Keep the last index of each char in a map and move `start` past repeats.
</details>

## 4. Exceptions
Solutions: [Ex04Exceptions.java](solutions/Ex04Exceptions.java)

| # | Problem | Level |
|---|---|---|
| 4.1 | `parseIntOrDefault(String s, int def)`: never throws, returns `def` for null or invalid input | 🟢 |
| 4.2 | A custom **checked** `InvalidAgeException`. `validateAge(int)` throws it for age < 0 or > 150 | 🟢 |
| 4.3 | A class `TrackedResource implements AutoCloseable` that logs open/close into a shared list. Prove that two resources in one try-with-resources close in reverse order | 🟡 |
| 4.4 | **Retry helper**: `<T> T retry(Supplier<T> action, int attempts)` retries on RuntimeException and rethrows the last one, with the attempt count in its message | 🟡 |
| 4.5 | **Exception translation**: wrap a low-level `IOException` into your own unchecked `ConfigException`, **keeping the cause** | 🟡 |

## 5. Generics
Solutions: [Ex05Generics.java](solutions/Ex05Generics.java)

| # | Problem | Level |
|---|---|---|
| 5.1 | Generic `Pair<A, B>` with `swap()` returning `Pair<B, A>` | 🟢 |
| 5.2 | `<T extends Comparable<T>> T max(List<T>)` | 🟢 |
| 5.3 | Generic `ArrayStack<T>` (push, pop, peek, isEmpty, size) backed by an array that doubles when full | 🟡 |
| 5.4 | `double sum(List<? extends Number>)`: works for Integer, Double and Long lists | 🟢 |
| 5.5 | `<T> int countIf(Collection<T> items, Predicate<? super T> condition)` | 🟡 |
| 5.6 | `<T> void copyInto(List<? super T> dest, List<? extends T> src)` (PECS) | 🟡 |

## 6. Collections
Solutions: [Ex06Collections.java](solutions/Ex06Collections.java)

| # | Problem | Example | Level |
|---|---|---|---|
| 6.1 | **Remove duplicates**, keeping order | `[3,1,3,2,1] → [3,1,2]` | 🟢 |
| 6.2 | **Top-k frequent words** (ties → alphabetical) | k=2 | 🟡 |
| 6.3 | **Group anagrams** | `[eat,tea,tan,ate,nat,bat] → [[eat,tea,ate],[tan,nat],[bat]]` | 🟡 |
| 6.4 | **Two-sum** → the indices of two numbers adding to the target (O(n) with a HashMap) | `[2,7,11,15], 9 → [0,1]` | 🟡 |
| 6.5 | **Valid brackets** using a Deque | `"{[()]}" → true` | 🟡 |
| 6.6 | **Merge two sorted lists** | `[1,4,7] + [2,3,9] → [1,2,3,4,7,9]` | 🟢 |
| 6.7 | **Sort employees** by dept asc, then salary desc | | 🟢 |
| 6.8 | **LRU cache** with capacity n (LinkedHashMap) | | 🟡 |
| 6.9 | **Kth largest** with a PriorityQueue (min-heap of size k) | `[3,2,1,5,6,4], k=2 → 5` | 🟡 |
| 6.10 | **Intersection** of two lists, keeping duplicates as many times as they appear in both | `[1,2,2,1] ∩ [2,2] → [2,2]` | 🟢 |

## 7. Streams
Solutions: [Ex07Streams.java](solutions/Ex07Streams.java). All problems use this data:

```java
record Employee(String name, String dept, double salary, int age, String city) { }
List<Employee> emps = List.of(
    new Employee("Asha",  "IT",    85000, 29, "Pune"),
    new Employee("Ravi",  "IT",    65000, 35, "Mumbai"),
    new Employee("Meera", "HR",    50000, 41, "Pune"),
    new Employee("John",  "Sales", 55000, 31, "Delhi"),
    new Employee("Zara",  "IT",    95000, 38, "Pune"),
    new Employee("Karan", "HR",    48000, 26, "Mumbai"),
    new Employee("Asha",  "Sales", 60000, 33, "Delhi"));
```

| # | Problem | Level |
|---|---|---|
| 7.1 | Names of employees older than 30, sorted alphabetically | 🟢 |
| 7.2 | Average salary per department (`Map<String, Double>`, sorted by dept) | 🟢 |
| 7.3 | Name of the highest-paid employee in each department | 🟡 |
| 7.4 | Count of employees per city, sorted by count desc and then city name | 🟡 |
| 7.5 | Second-highest salary | 🟡 |
| 7.6 | Partition into salary ≥ 60k vs the rest (names only) | 🟢 |
| 7.7 | Distinct departments, sorted, joined with ", " | 🟢 |
| 7.8 | Names that appear more than once | 🟡 |
| 7.9 | The city with the highest total salary | 🟡 |
| 7.10 | `Map<String, List<String>>`: department → names sorted by salary desc | 🔴 |

## 8. Concurrency
Solutions: [Ex08Concurrency.java](solutions/Ex08Concurrency.java)

| # | Problem | Level |
|---|---|---|
| 8.1 | A counter incremented by 4 threads × 10,000 times. Implement it with `synchronized`, `AtomicInteger` and `ReentrantLock`, and show each gives exactly 40,000 | 🟢 |
| 8.2 | **Parallel sum**: split an array into 4 chunks, sum each with a `Callable` on an `ExecutorService`, and combine the Futures | 🟡 |
| 8.3 | **Three threads print 1..9 in turn** (T1:1, T2:2, T3:3, T1:4…) | 🔴 |
| 8.4 | **CompletableFuture**: fetch `price` and `stock` in parallel (simulated delays), combine them into "Rs.X, Y left", and fall back to "unavailable" on an exception | 🟡 |
| 8.5 | **Producer-consumer**: a producer puts 1..100 into a `BlockingQueue` followed by a poison pill; a consumer sums them (5050) | 🟡 |

## 9. Modern Java
Solutions: [Ex09Modern.java](solutions/Ex09Modern.java)

| # | Problem | Level |
|---|---|---|
| 9.1 | `record Temperature(double celsius)` that rejects values below -273.15 and has `fahrenheit()` and `static Temperature ofFahrenheit(double)` | 🟢 |
| 9.2 | `sealed interface Shape permits Circle, Square, Rect`, with `area(Shape)` written as an exhaustive switch and no default | 🟢 |
| 9.3 | An expression tree: `sealed interface Expr` with `Num`, `Add`, `Mul`, `Neg`. `eval` and `show` using record patterns | 🟡 |
| 9.4 | `describe(Object)` with a pattern switch: null, Integer (negative/positive guard), String (blank/non-blank), List (size), and anything else | 🟢 |
| 9.5 | A command parser: turn text like `"deposit 500"` / `"withdraw 200"` / `"balance"` into sealed `Command` records, then process them with a switch | 🟡 |

## 10. Design Patterns
Solutions: [Ex10Patterns.java](solutions/Ex10Patterns.java)

| # | Problem | Level |
|---|---|---|
| 10.1 | **Builder** for an immutable `HttpRequestSpec` (url required, method default GET, headers map, timeout default 30s). `build()` validates that the URL starts with `http` | 🟡 |
| 10.2 | **Strategy**: shipping cost as `Standard` (Rs.50), `Express` (Rs.120 + 10/kg) or `Free` above Rs.999 of order value, swappable at runtime | 🟢 |
| 10.3 | **Observer**: a `StockTicker` notifies its listeners when a price changes. An `AlertListener` records messages when the price drops by more than 5% | 🟡 |
| 10.4 | **Decorator**: a `Text` interface with `Plain`, plus `Bold` (`**x**`), `Italic` (`_x_`) and `Upper` decorators that can be combined in any order | 🟢 |
| 10.5 | **Singleton**: a lazy, thread-safe `IdGenerator` (holder idiom) handing out unique ids to 1,000 concurrent tasks with no duplicates | 🟡 |

---

✅ **When you finish them all**, you're ready for the big one: the [Mini Bank project](../project/README.md).
