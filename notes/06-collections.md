# 06 · Collections Framework

> ⏱️ **Revise in:** 75 min · 🎯 **Level:** Intermediate → Advanced · ▶️ **Code:** [code/06_collections](../code/06_collections) · 🏋️ **Practice:** [Exercises → Collections](../exercises/README.md#6-collections)

## 🧠 Mind Map

```
06 COLLECTIONS  (java.util)
│
├── Iterable ─► Collection
│               ├── List   (ordered, index, duplicates OK)
│               │    ├── ArrayList   → dynamic array · fast get(i) · DEFAULT CHOICE
│               │    ├── LinkedList  → doubly linked · fast add/remove at ends · also a Deque
│               │    └── Vector / Stack (legacy, synchronized → avoid)
│               ├── Set    (unique elements)
│               │    ├── HashSet       → no order · O(1) · backed by HashMap
│               │    ├── LinkedHashSet → insertion order
│               │    └── TreeSet       → sorted · O(log n) · Red-Black tree · NavigableSet
│               └── Queue  (processing order)
│                    ├── PriorityQueue → heap · smallest first (by default)
│                    └── Deque → ArrayDeque (best stack AND queue) · LinkedList
│
├── Map (NOT a Collection!) key → value, unique keys
│    ├── HashMap       → no order · O(1) · 1 null key OK
│    ├── LinkedHashMap → insertion / access order (LRU cache!)
│    ├── TreeMap       → sorted keys · O(log n) · NavigableMap
│    ├── Hashtable     → legacy synchronized, no nulls
│    └── ConcurrentHashMap → thread-safe, high concurrency, no nulls
│
├── "Hash = Hurry (fast, no order) · Linked = Line (insertion order) · Tree = Tidy (sorted)"
│
├── HashMap internals: table of buckets (16) · hash ^ (h>>>16) · index = (n-1) & hash
│    · collision → linked list → Red-Black tree (≥8 in a bucket, capacity ≥64) · load factor 0.75 → resize ×2
│
├── Ordering: Comparable (compareTo, natural, inside the class)
│             Comparator (compare, custom, outside: comparing().thenComparing().reversed())
├── Iteration: Iterator (hasNext/next/remove) · ListIterator (both directions)
│              fail-fast (CME) vs fail-safe / weakly consistent (concurrent collections)
├── Utilities: Collections.sort/reverse/shuffle/max/frequency/unmodifiable*/synchronized*
├── Immutable: List.of · Set.of · Map.of · List.copyOf (no nulls!) · Arrays.asList = fixed-size
└── Java 21 Sequenced: getFirst · getLast · addFirst · addLast · reversed()
```

## ⚡ 30-Second Summary

- **List** = ordered with duplicates. **Set** = unique. **Queue** = processing order. **Map** = key→value.
- Defaults: **ArrayList**, **HashSet**, **HashMap**, **ArrayDeque**.
- Need order? → **Linked***. Need sorted? → **Tree***. Need thread-safe? → **Concurrent***.
- HashMap relies on **hashCode() to find the bucket** and **equals() to find the key** in that bucket.
- `Comparable` = one natural order built into the class. `Comparator` = any number of external orders.
- Never modify a collection while iterating with for-each. Use `Iterator.remove()` or `removeIf()`.

---

## 1. Which Collection Should I Use? (decision flow)

```
                    Do you need key → value pairs?
                    ├── YES → MAP
                    │         ├── sorted by key?          → TreeMap
                    │         ├── keep insertion order?   → LinkedHashMap
                    │         ├── multi-threaded?         → ConcurrentHashMap
                    │         └── otherwise               → HashMap ✅
                    └── NO
                         ├── Duplicates allowed / need index? → LIST
                         │         ├── mostly read by index   → ArrayList ✅
                         │         └── many add/remove at ends → ArrayDeque / LinkedList
                         ├── Unique elements only?          → SET
                         │         ├── sorted                 → TreeSet
                         │         ├── insertion order        → LinkedHashSet
                         │         └── otherwise              → HashSet ✅
                         └── Process in an order (FIFO/LIFO/priority)? → QUEUE
                                   ├── FIFO queue or LIFO stack → ArrayDeque ✅
                                   ├── by priority              → PriorityQueue
                                   └── producer-consumer        → BlockingQueue (ch 08)
```

---

## 2. Big-O Cheat Sheet

| Collection | add | get / contains | remove | Ordering | Nulls | Backed by |
|---|---|---|---|---|---|---|
| **ArrayList** | O(1)* | get(i) **O(1)** · contains O(n) | O(n) | insertion | ✅ | `Object[]` |
| **LinkedList** | O(1) at ends | get(i) **O(n)** | O(1) at ends | insertion | ✅ | doubly linked nodes |
| **HashSet** | O(1) | O(1) | O(1) | none | 1 null | HashMap |
| **LinkedHashSet** | O(1) | O(1) | O(1) | insertion | 1 null | LinkedHashMap |
| **TreeSet** | O(log n) | O(log n) | O(log n) | **sorted** | ❌ | TreeMap (Red-Black) |
| **PriorityQueue** | O(log n) | peek O(1) · contains O(n) | poll O(log n) | heap (head = min) | ❌ | array heap |
| **ArrayDeque** | O(1)* | peek O(1) | O(1) at ends | insertion | ❌ | circular array |
| **HashMap** | O(1) | O(1) | O(1) | none | 1 null key, null values | array of buckets |
| **LinkedHashMap** | O(1) | O(1) | O(1) | insertion/access | ✅ | HashMap + linked list |
| **TreeMap** | O(log n) | O(log n) | O(log n) | **sorted keys** | no null key | Red-Black tree |
| **ConcurrentHashMap** | O(1) | O(1) | O(1) | none | ❌ | buckets + CAS |

\* amortized (occasional resize). HashMap worst case: O(log n) with treeified buckets (O(n) before Java 8).

---

## 3. List

### ArrayList vs LinkedList ⭐

| | ArrayList | LinkedList |
|---|---|---|
| Structure | resizable array | doubly linked list |
| `get(i)` | **O(1)** (random access) | O(n) (walks nodes) |
| add at end | O(1) amortized | O(1) |
| add/remove in the middle | O(n) (shifts elements) | O(n) to find + O(1) to link |
| Memory | compact | extra prev/next pointers per element |
| Implements | `List`, `RandomAccess` | `List`, **`Deque`** |
| Use | almost always ✅ | rarely. Even as a queue, `ArrayDeque` is faster |

> 🔑 **ArrayList internals:** default capacity **10** (allocated lazily on the first add), grows by **1.5×**
> (`new = old + old/2`) by copying into a new array. `trimToSize()` releases unused capacity. Pre-size it with
> `new ArrayList<>(expectedSize)` when you know the size.

```java
List<String> list = new ArrayList<>();
list.add("A"); list.add(0, "Z"); list.addAll(List.of("B", "C"));
list.get(1); list.set(1, "X"); list.remove("C"); list.remove(0);
list.contains("B"); list.indexOf("B"); list.size(); list.isEmpty();
list.sort(Comparator.naturalOrder());
list.subList(0, 1);          // a VIEW of the range [0, 1)
```

> ⚠️ `Vector` (synchronized, grows ×2) and `Stack extends Vector` are **legacy**. Use `ArrayList`, plus
> `ArrayDeque` for stacks.

---

## 4. Set

```java
Set<String> hash   = new HashSet<>(List.of("banana", "apple", "cherry"));   // e.g. [banana, cherry, apple] (order unspecified)
Set<String> linked = new LinkedHashSet<>(List.of("banana", "apple"));       // [banana, apple]
Set<String> tree   = new TreeSet<>(List.of("banana", "apple", "cherry"));   // [apple, banana, cherry]

hash.add("apple");   // returns false: already present, the set is unchanged
```

**TreeSet / NavigableSet superpowers:**

```java
TreeSet<Integer> ts = new TreeSet<>(List.of(10, 20, 30, 40));
ts.first();        // 10          ts.last();         // 40
ts.floor(25);      // 20  (≤)     ts.ceiling(25);    // 30  (≥)
ts.lower(20);      // 10  (<)     ts.higher(20);     // 30  (>)
ts.headSet(30);    // [10, 20]    ts.tailSet(30);    // [30, 40]
ts.subSet(15, 35); // [20, 30]    ts.descendingSet();// [40, 30, 20, 10]
ts.pollFirst();    // removes and returns 10
```

> 🔑 **HashSet internally is a HashMap**: each element is stored as a **key** with a dummy constant value
> (`PRESENT`). Uniqueness comes from `hashCode()` + `equals()`.
> ⚠️ **TreeSet uses `compareTo`/`compare` for uniqueness, not `equals`!** A `TreeSet` with a comparator that
> compares by length treats `"cat"` and `"dog"` as duplicates.

---

## 5. Queue & Deque

| Operation | Throws an exception on failure | Returns a special value (`false`/`null`) |
|---|---|---|
| Insert | `add(e)` | `offer(e)` ✅ |
| Remove head | `remove()` | `poll()` ✅ |
| Examine head | `element()` | `peek()` ✅ |

> 🧩 "**offer/poll/peek are polite (return null/false); add/remove/element are loud (throw)**".

```java
Queue<String> queue = new ArrayDeque<>();      // FIFO
queue.offer("A"); queue.offer("B");
queue.poll();                                  // "A"

Deque<Integer> stack = new ArrayDeque<>();     // LIFO, the modern replacement for Stack
stack.push(1); stack.push(2);
stack.pop();                                   // 2
stack.peek();                                  // 1

PriorityQueue<Integer> minHeap = new PriorityQueue<>(List.of(5, 1, 3));
minHeap.poll();                                // 1 (smallest first)
PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Comparator.reverseOrder());
PriorityQueue<Task> byPriority = new PriorityQueue<>(Comparator.comparingInt(Task::priority));
```

> ⚠️ Iterating or printing a `PriorityQueue` does **not** give sorted order. Only repeated `poll()` does.
> 💡 Classic uses of `PriorityQueue`: top-K elements, Dijkstra's shortest path, scheduling, merging K sorted lists.

---

## 6. Map

```java
Map<String, Integer> stock = new HashMap<>();
stock.put("apple", 10);
stock.put("apple", 15);                  // same key → value REPLACED (returns old value 10)
stock.get("apple");                      // 15
stock.get("kiwi");                       // null
stock.getOrDefault("kiwi", 0);           // 0
stock.containsKey("apple"); stock.containsValue(15);
stock.remove("apple");
stock.putIfAbsent("pear", 5);            // only if absent (or mapped to null)

// Iterating: entrySet is the most efficient (key + value in one pass)
for (Map.Entry<String, Integer> e : stock.entrySet()) {
    System.out.println(e.getKey() + " → " + e.getValue());
}
stock.forEach((k, v) -> System.out.println(k + " → " + v));
```

### Java 8 Map methods ⭐ (interview favourites)

```java
// Word frequency in one line
Map<String, Integer> freq = new HashMap<>();
for (String w : words) freq.merge(w, 1, Integer::sum);

// Group into lists (multimap)
Map<String, List<String>> byDept = new HashMap<>();
byDept.computeIfAbsent("IT", k -> new ArrayList<>()).add("Asha");

map.computeIfPresent("apple", (k, v) -> v + 1);   // update only if present
map.compute("apple", (k, v) -> v == null ? 1 : v + 1);
map.replaceAll((k, v) -> v * 2);
map.remove("apple", 15);                          // remove only if the key maps to 15
```

### TreeMap navigation

```java
TreeMap<Integer, String> grades = new TreeMap<>(Map.of(90, "A", 75, "B", 60, "C", 40, "D"));
grades.floorEntry(82).getValue();    // "B"  (largest key ≤ 82): perfect for slab/range lookups
grades.firstKey(); grades.lastKey(); grades.headMap(75); grades.tailMap(75, true); grades.descendingMap();
```

### LinkedHashMap as an LRU cache ⭐

```java
class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;
    LRUCache(int capacity) {
        super(capacity, 0.75f, true);          // true = ACCESS order (get moves an entry to the end)
        this.capacity = capacity;
    }
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;              // evict the least recently used entry
    }
}
```

---

## 7. ⭐⭐ How HashMap Works Internally (the #1 Collections interview question)

```
 map.put("Asha", 90)
        │
        ▼
 1. h = "Asha".hashCode()                 → e.g. 2050019
 2. hash = h ^ (h >>> 16)                  → spreads the high bits into the low bits
 3. index = (n - 1) & hash                 → n = table length (16) → bucket 0..15  (fast modulo: n is a power of 2)
        │
        ▼
 table (Node<K,V>[16])
 ┌───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┐
 │ 0 │ 1 │ 2 │ 3 │ 4 │ 5 │ 6 │ 7 │ 8 │ 9 │10 │11 │12 │13 │14 │15 │
 └───┴───┴───┴─┬─┴───┴───┴───┴───┴───┴───┴───┴─┬─┴───┴───┴───┴───┘
               │                               │
          [Asha=90]                      [Ravi=70] → [Meera=88] → ...   ← COLLISION: same bucket
                                         (linked list; becomes a Red-Black TREE when ≥ 8 nodes
                                          and table size ≥ 64 → lookup O(log n) instead of O(n))
 4. Bucket empty?          → create a new Node(hash, key, value, next)
 5. Bucket occupied?       → walk the nodes: same hash AND (same ref OR equals()) → REPLACE the value
                             no match → append a new node at the tail
 6. ++size > threshold?    → RESIZE: capacity × 2, rehash (each node stays at index i or moves to i + oldCap)
                             threshold = capacity × loadFactor = 16 × 0.75 = 12
```

**`get(key)`:** compute the hash → index → check the first node → walk the list/tree comparing `hash` and then
`equals()` → return the value or `null`.

| Fact | Value |
|---|---|
| Default capacity | **16** (always a power of 2) |
| Load factor | **0.75** (the time vs space trade-off) |
| Resize | when size > capacity × 0.75 → **double** |
| Treeify threshold | **8** nodes in one bucket (if the table is ≥ **64**, otherwise it resizes instead) |
| Untreeify threshold | **6** |
| Null key | allowed **once**, always stored in bucket **0** |
| Thread-safe? | ❌ (use ConcurrentHashMap) |
| Order | not guaranteed, and can change after a resize |

> 🔑 **Why must keys be immutable?** If a key's fields change after insertion, its `hashCode` changes, so
> lookups go to a different bucket and the entry is "lost" (a memory leak + wrong results).
> 🔑 **What if `hashCode()` always returns 1?** Everything lands in one bucket and performance degrades to
> O(log n) (tree) or O(n) (list). It's still *correct*, because `equals()` decides equality.

### HashMap vs Hashtable vs ConcurrentHashMap vs synchronizedMap

| | HashMap | Hashtable | `Collections.synchronizedMap` | ConcurrentHashMap |
|---|---|---|---|---|
| Thread-safe | ❌ | ✅ (every method locked) | ✅ (one lock for the whole map) | ✅ (CAS + per-bucket locking) |
| Performance under concurrency | n/a | poor | poor | **excellent** |
| Null key/values | 1 null key, null values ✅ | ❌ | same as the wrapped map | ❌ |
| Iterator | fail-fast | fail-fast (Enumeration not) | fail-fast (sync manually!) | **weakly consistent** (no CME) |
| Since | 1.2 | 1.0 (legacy) | 1.2 | 5 |

> 💡 **Why does ConcurrentHashMap forbid null?** Ambiguity: `get(k) == null` could mean "absent" or "mapped to
> null", and in a concurrent map you can't safely double-check with `containsKey`.
> 💡 **Java 7 CHM** used 16 *segments* (lock striping). **Java 8+** locks only the head node of a single
> bucket and uses CAS for empty buckets, so reads are mostly lock-free.

---

## 8. Comparable vs Comparator ⭐

| | `Comparable<T>` | `Comparator<T>` |
|---|---|---|
| Package | `java.lang` | `java.util` |
| Method | `int compareTo(T other)` | `int compare(T a, T b)` |
| Where | **inside** the class | **outside** (a separate class/lambda) |
| How many orders | **one** natural order | **unlimited** |
| Modify the class? | yes | no (works on classes you don't own) |
| Used by | `Collections.sort(list)`, `TreeSet`, `TreeMap` by default | `list.sort(cmp)`, `new TreeSet<>(cmp)` |

**Return value:** negative → `a` comes first · 0 → equal · positive → `b` comes first.

```java
class Employee implements Comparable<Employee> {
    String name; String dept; double salary;
    @Override
    public int compareTo(Employee o) {
        return Integer.compare(this.id, o.id);       // ✅ natural order: by id
        // return this.id - o.id;                    ❌ can overflow for large/negative values
    }
}

// Comparator: modern, composable style
list.sort(Comparator.comparing(Employee::getDept)              // by dept
                    .thenComparing(Employee::getSalary, Comparator.reverseOrder()) // then salary desc
                    .thenComparing(Employee::getName));          // then name

list.sort(Comparator.comparingDouble(Employee::getSalary).reversed());
list.sort(Comparator.nullsFirst(Comparator.naturalOrder()));
```

> 🧩 "**Comparable: I compare MYSELF to another. Comparator: a JUDGE compares two others.**"

---

## 9. Iterators: fail-fast vs fail-safe

```java
List<Integer> nums = new ArrayList<>(List.of(1, 2, 3, 4));

for (Integer n : nums) if (n % 2 == 0) nums.remove(n);    // 💥 ConcurrentModificationException

Iterator<Integer> it = nums.iterator();                    // ✅ option 1
while (it.hasNext()) if (it.next() % 2 == 0) it.remove();

nums.removeIf(n -> n % 2 == 0);                            // ✅ option 2 (best)
```

| Fail-fast | Fail-safe / weakly consistent |
|---|---|
| throws `ConcurrentModificationException` if the structure changes during iteration | never throws CME |
| checks a `modCount` counter | iterates a snapshot (`CopyOnWriteArrayList`) or the live, weakly consistent view (`ConcurrentHashMap`) |
| `ArrayList`, `HashMap`, `HashSet`… | `CopyOnWriteArrayList`, `ConcurrentHashMap`, `ConcurrentLinkedQueue` |

| Iterator | ListIterator | Enumeration (legacy) |
|---|---|---|
| any Collection | Lists only | Vector/Hashtable |
| forward only | forward **and** backward | forward only |
| `remove()` | `remove()`, `set()`, `add()`, indexes | read-only |

---

## 10. The `Collections` Utility Class & Immutable Collections

```java
Collections.sort(list);                    Collections.reverse(list);
Collections.shuffle(list);                 Collections.swap(list, 0, 1);
Collections.max(list); Collections.min(list, cmp);
Collections.frequency(list, "a");          Collections.nCopies(3, "x");   // [x, x, x]
Collections.unmodifiableList(list);        // read-only VIEW: changes to `list` still show through!
Collections.synchronizedList(list);        // thread-safe wrapper
Collections.emptyList(); Collections.singletonList(x);
```

| Factory | Mutable? | Nulls? | Notes |
|---|---|---|---|
| `new ArrayList<>(...)` | ✅ | ✅ | normal |
| `Arrays.asList(a, b)` | **fixed size**: `set` ✅, `add/remove` ❌ | ✅ | a view over the array |
| `Collections.unmodifiableList(l)` | ❌ through the view | ✅ | the underlying list can still change |
| `List.of(a, b)` / `Set.of` / `Map.of` (Java 9) | ❌ **immutable** | ❌ **NPE** | `Set.of` duplicates → `IllegalArgumentException` |
| `List.copyOf(coll)` (Java 10) | ❌ immutable copy | ❌ | a snapshot, independent of the source |
| `stream.toList()` (Java 16) | ❌ unmodifiable | ✅ | |

### Java 21: Sequenced Collections

```java
List<String> l = new ArrayList<>(List.of("a", "b", "c"));
l.getFirst();  l.getLast();          // "a", "c"   (instead of l.get(0), l.get(l.size()-1))
l.addFirst("z"); l.removeLast();
l.reversed();                        // a reversed VIEW
LinkedHashMap<String,Integer> m = ...; m.firstEntry(); m.lastEntry(); m.pollFirstEntry();
```

---

## 🧩 Memory Tricks

| Concept | Trick |
|---|---|
| Implementation prefixes | "**Hash = Hurry, Linked = Line, Tree = Tidy**" |
| Queue methods | "**offer/poll/peek are polite; add/remove/element are loud**" |
| HashMap numbers | "**16 · 0.75 · 8 · 64**": capacity, load factor, treeify, min table for treeify |
| HashMap lookup | "**hashCode finds the street, equals finds the house**" |
| Comparable vs Comparator | "**I compare myself vs a judge compares two**" |
| Map is not a Collection | "**Map is a dictionary, not a bag**" |
| Stack | "**Don't use Stack, use ArrayDeque push/pop**" |

---

## ❓ Self-Test

<details><summary>1. Explain how HashMap.put works.</summary>

It computes `hash = h ^ (h >>> 16)` from the key's hashCode, then finds the bucket with `(n-1) & hash`. An empty
bucket gets a new node. Otherwise it walks the bucket: if a node has the same hash and an equal key, the value
is replaced; if not, a node is appended. A bucket with 8 or more nodes in a table of 64 or more is converted to
a Red-Black tree. When size exceeds capacity × 0.75, the table doubles and entries are rehashed.
</details>

<details><summary>2. Why should HashMap keys be immutable?</summary>

If a key's state changes, its hashCode changes, so a later `get` searches the wrong bucket and can't find the
entry. It also can't be removed, which leaks memory.
</details>

<details><summary>3. ArrayList vs LinkedList: when would you pick LinkedList?</summary>

Almost never. LinkedList only wins with frequent insertions/removals through an iterator in the middle of a
long list. For queues and stacks, `ArrayDeque` is faster.
</details>

<details><summary>4. HashSet vs TreeSet?</summary>

HashSet is O(1), unordered, allows one null, and uses hashCode/equals. TreeSet is O(log n), sorted, doesn't
allow null, and uses compareTo/compare (even for uniqueness).
</details>

<details><summary>5. Why does ConcurrentHashMap not allow null keys or values?</summary>

In a concurrent setting, a `null` from `get()` would be ambiguous (absent, or mapped to null?), and you
couldn't safely check `containsKey` afterwards because another thread may have changed the map in between.
</details>

<details><summary>6. Fail-fast vs fail-safe iterator?</summary>

A fail-fast iterator (ArrayList, HashMap) throws ConcurrentModificationException if the collection is
structurally modified during iteration, detected via `modCount`. A fail-safe or weakly consistent iterator
(CopyOnWriteArrayList, ConcurrentHashMap) never throws CME: it works on a snapshot or tolerates changes.
</details>

<details><summary>7. How do you sort a HashMap by value?</summary>

`map.entrySet().stream().sorted(Map.Entry.comparingByValue()).toList()`. To keep it as a map, collect into a
`LinkedHashMap`: `.collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (a,b)->a, LinkedHashMap::new))`.
</details>

<details><summary>8. What does <code>List.of(1,2,3).add(4)</code> do? And <code>List.of(1, null)</code>?</summary>

The first throws `UnsupportedOperationException` (immutable). The second throws `NullPointerException`
(nulls aren't allowed).
</details>

<details><summary>9. How would you implement an LRU cache in Java?</summary>

Extend `LinkedHashMap` with `accessOrder = true` and override `removeEldestEntry` to return
`size() > capacity`. The interview-from-scratch version uses a HashMap plus a doubly linked list.
</details>

---

## ✅ Chapter Checklist

- [ ] Draw the hierarchy (Collection → List/Set/Queue, Map separate)
- [ ] Recite the HashMap put flow and its 4 magic numbers (16, 0.75, 8, 64)
- [ ] Fill in ArrayList vs LinkedList and the HashMap/Hashtable/CHM tables from memory
- [ ] Write a multi-level Comparator chain
- [ ] Write an LRU cache with LinkedHashMap
- [ ] Run [code/06_collections](../code/06_collections) (especially `MyHashMap.java`!)

➡️ Next: [07 · Functional Java: Lambdas & Streams](07-functional-java8.md)
