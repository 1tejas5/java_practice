# 09 · JVM Internals, Memory & Garbage Collection

> ⏱️ **Revise in:** 45 min · 🎯 **Level:** Advanced · ▶️ **Code:** [code/09_jvm](../code/09_jvm)

## 🧠 Mind Map

```
09 JVM · MEMORY · GC
├── Architecture: ClassLoader Subsystem → Runtime Data Areas → Execution Engine ⇄ JNI ⇄ native libs
├── Class loading
│   ├── Loaders: Bootstrap (core, java.base) → Platform (java.sql...) → Application (your classpath)
│   ├── Parent delegation: ask the parent first → core classes can't be spoofed
│   └── Phases: Load → Link (Verify, Prepare, Resolve) → Initialize (static blocks)
├── Runtime data areas  "Mad Hens Steal Popcorn Nightly"
│   ├── SHARED:     Method area/Metaspace (class metadata) · Heap (objects, arrays, String pool)
│   └── PER THREAD: Stack (frames: locals, operand stack) · PC register · Native method stack
├── Heap generations: Young (Eden, S0, S1) → Old (Tenured)  "most objects die young"
│   └── Minor GC (young, frequent, fast) · Major/Full GC (old, slower)
├── GC
│   ├── Eligible = unreachable from GC ROOTS (stack locals, statics, active threads, JNI)
│   ├── Mark → Sweep → Compact (copying in the young gen)
│   ├── Collectors: Serial · Parallel · G1 (default since 9) · ZGC · Shenandoah
│   └── System.gc() is only a hint · finalize() deprecated → try-with-resources / Cleaner
├── References: Strong · Soft (caches) · Weak (WeakHashMap) · Phantom (cleanup)
├── Errors: OutOfMemoryError (heap/metaspace) · StackOverflowError (deep recursion)
├── Memory leaks: static collections · unclosed resources · listeners · ThreadLocal · inner classes
├── JIT: interpreter → C1 → C2 (tiered) · inlining · escape analysis
└── Flags & tools: -Xms -Xmx -Xss · -XX:+UseG1GC · jps jstack jmap jcmd jstat · VisualVM · JFR
```

## ⚡ 30-Second Summary

- **Objects live on the heap** (shared). **Method calls and local variables live on the stack** (per thread).
- An object becomes garbage when no chain of references from a **GC root** reaches it.
- **Generational GC:** new objects go to **Eden**, survivors move through the **Survivor** spaces, and long-lived
  ones get promoted to the **Old** generation.
- **G1** is the default collector. **ZGC** gives sub-millisecond pauses on huge heaps.
- Java still has **memory leaks**: objects that stay *reachable* but are never used again.

---

## 1. JVM Architecture

```
         .class files (bytecode)
                │
 ┌──────────────▼─────────────── CLASS LOADER SUBSYSTEM ───────────────────────────┐
 │  LOADING                    LINKING                          INITIALIZATION      │
 │  Bootstrap → Platform →     Verify → Prepare → Resolve  →    static blocks,      │
 │  Application                (safe?)  (static defaults) (refs) static field values │
 └──────────────┬───────────────────────────────────────────────────────────────────┘
                │
 ┌──────────────▼──────────────── RUNTIME DATA AREAS ──────────────────────────────┐
 │   SHARED BY ALL THREADS            │   ONE PER THREAD                            │
 │   ┌─────────────────────────────┐  │   ┌────────┐ ┌──────────┐ ┌───────────────┐  │
 │   │ Method Area (Metaspace)     │  │   │ Stack  │ │ PC       │ │ Native Method │  │
 │   │ class metadata, bytecode,   │  │   │ frames │ │ Register │ │ Stack         │  │
 │   │ runtime constant pool       │  │   └────────┘ └──────────┘ └───────────────┘  │
 │   ├─────────────────────────────┤  │                                              │
 │   │ HEAP: objects, arrays,      │  │                                              │
 │   │ String pool, static fields* │  │                                              │
 │   └─────────────────────────────┘  │                                              │
 └──────────────┬──────────────────────────────────────────────────────────────────┘
                │
 ┌──────────────▼────── EXECUTION ENGINE ─────┐     ┌────────┐     ┌──────────────────┐
 │ Interpreter · JIT compiler · Garbage Coll. │ ◄──►│  JNI   │ ◄──►│ Native libraries │
 └────────────────────────────────────────────┘     └────────┘     └──────────────────┘
```
\* In HotSpot since Java 8, static fields live with the `Class` object on the heap. Interviews often
just say "method area", and that's fine as long as you know the nuance.

---

## 2. Class Loading

### The 3 built-in class loaders

| Loader | Loads | Written in |
|---|---|---|
| **Bootstrap** | core classes (`java.lang.*`, `java.util.*`) from the runtime image (formerly `rt.jar`) | native code (`getClassLoader()` returns **null**) |
| **Platform** (Java 9+, formerly *Extension*) | platform modules like `java.sql` | Java |
| **Application / System** | your classes from the classpath/module path | Java |

### Parent delegation model

```
  "Load com.myapp.Hello"  →  Application ──asks──► Platform ──asks──► Bootstrap
                                                                     can't find it
                                          can't find it   ◄──────────┘
               found on the classpath ✅ ◄──┘
```

> 🔑 **Why delegate?** **Security & consistency.** Your own fake `java.lang.String` can never replace the real
> one, because Bootstrap always gets the first chance to load it.

### Phases

| Phase | What happens |
|---|---|
| **Load** | find the `.class` bytes and create a `Class` object |
| **Link → Verify** | the bytecode verifier checks the code is valid and safe |
| **Link → Prepare** | allocate static fields with **default** values (0, null) |
| **Link → Resolve** | replace symbolic references with direct references (can be lazy) |
| **Initialize** | run static initializers & static blocks, **top to bottom** |

> 🧩 "**Load → Link (Verify, Prepare, Resolve) → Initialize**" = "**L-L-I, V-P-R**".
> 🔑 Classes are loaded **lazily**, on first active use (`new`, a static method call, a non-constant static field,
> `Class.forName`, subclass initialization).

---

## 3. Stack vs Heap ⭐

```java
class Person { String name; int age; Person(String n, int a) { name = n; age = a; } }

public static void main(String[] args) {
    int x = 10;
    Person p = new Person("Asha", 25);
    greet(p);
}
static void greet(Person who) { String msg = "Hi " + who.name; }
```

```
        STACK (thread "main")                          HEAP
 ┌──────────────────────────────┐
 │ greet() frame                │
 │   who ───────────────────────┼──────┐
 │   msg ───────────────────────┼──┐   │
 ├──────────────────────────────┤  │   │      ┌───────────────────────┐
 │ main() frame                 │  │   └────► │ Person                │
 │   args ──► String[]          │  │   ┌────► │  name ──► "Asha" (pool)│
 │   x = 10   (value itself)    │  │   │      │  age = 25             │
 │   p ─────────────────────────┼──┼───┘      └───────────────────────┘
 └──────────────────────────────┘  └────────► "Hi Asha" (new String)
```

| | Stack | Heap |
|---|---|---|
| Stores | method frames: **primitive locals + references** | **objects** and arrays (and their fields) |
| Scope | one per **thread** | **shared** by all threads |
| Lifetime | a frame is popped when the method returns | until the GC collects it |
| Speed | very fast (LIFO pointer bump) | slower (allocation + GC) |
| Size | small (`-Xss`, ~512KB–1MB) | large (`-Xms`/`-Xmx`) |
| Thread-safe? | ✅ (private) | ❌ (needs synchronization) |
| Error when full | `StackOverflowError` | `OutOfMemoryError: Java heap space` |

> 🔑 "**Primitives and references live where they're declared**": a local `int` is on the stack, but an `int`
> **field** lives inside its object on the heap.

---

## 4. Heap Generations & How GC Flows

```
 ┌──────────────────────────────── HEAP ─────────────────────────────────────┐
 │  YOUNG GENERATION (~1/3)                       │  OLD GENERATION (~2/3)    │
 │  ┌──────────────────────┬─────────┬─────────┐  │  ┌──────────────────────┐ │
 │  │        EDEN          │   S0    │   S1    │  │  │      TENURED          │ │
 │  │  (new objects born)  │(survivor│(survivor│  │  │ (long-lived objects)  │ │
 │  └──────────────────────┴─────────┴─────────┘  │  └──────────────────────┘ │
 └────────────────────────────────────────────────────────────────────────────┘
   METASPACE (native memory, outside the heap): class metadata  (replaced PermGen in Java 8)
```

1. `new` → the object is allocated in **Eden**.
2. Eden fills up → **Minor GC**: live objects are copied to the empty survivor space (S0 ⇄ S1) and their **age** goes up.
3. Objects surviving enough minor GCs (age threshold, up to 15) get **promoted** to **Old**.
4. Old fills up → **Major/Full GC** (more expensive, longer pauses).

> 🧩 "**Most objects die young**" (the *weak generational hypothesis*), so cleaning the small young
> generation often is cheap and effective.
> 💡 G1 divides the heap into many equal **regions** (each tagged Eden/Survivor/Old/Humongous) instead of big
> contiguous areas, and collects the regions with the **most garbage first** (hence "Garbage-First").

---

## 5. Garbage Collection

### When is an object eligible for GC?

When it's **unreachable from any GC root**. **GC roots:** local variables in active stack frames · active threads ·
static fields · JNI references · monitors held by synchronized blocks.

```java
Person a = new Person("A", 1);
a = null;                                // 1. nulling the reference
Person b = new Person("B", 2);
b = new Person("C", 3);                  // 2. reassigning: "B" is now unreachable
void m() { Person local = new Person("D", 4); }   // 3. out of scope after m() returns
// 4. "Island of isolation": x ↔ y reference each other, but nothing references x or y → BOTH collectable
```

> 🔑 Java GC is **reachability-based (tracing)**, not reference counting, so **cycles are collected**.

**Algorithm phases:** **Mark** (trace the live objects from the roots) → **Sweep** (free the dead ones) →
**Compact** (defragment). The young generation uses **copying** (live objects are copied to a survivor space,
and the rest of Eden is wiped).
**Stop-the-world (STW):** application threads pause during some GC phases. Modern collectors keep these pauses tiny.

### Garbage collectors

| Collector | Idea | Best for | Flag |
|---|---|---|---|
| **Serial** | single-threaded, STW | small heaps, single CPU, containers | `-XX:+UseSerialGC` |
| **Parallel** | multi-threaded, STW, max **throughput** | batch jobs (default in Java 8) | `-XX:+UseParallelGC` |
| **G1** | region-based, **predictable pauses** | general purpose, **default since Java 9** | `-XX:+UseG1GC -XX:MaxGCPauseMillis=200` |
| **ZGC** | concurrent, **< 1 ms pauses**, TB heaps (generational since 21) | latency-critical | `-XX:+UseZGC` |
| **Shenandoah** | concurrent compaction, low pause | latency-critical (Red Hat) | `-XX:+UseShenandoahGC` |
| ~~CMS~~ | deprecated in 9, **removed in 14** | | |

> ⚠️ `System.gc()` / `Runtime.getRuntime().gc()` only **request** a GC; the JVM may ignore it.
> ⚠️ `finalize()` is deprecated (Java 9) and slated for removal. Use **try-with-resources** or `java.lang.ref.Cleaner`.

---

## 6. Reference Types

| Type | Collected when | Use case |
|---|---|---|
| **Strong** (`Person p = new Person()`) | never, while it's strongly reachable | normal code |
| **Soft** (`SoftReference`) | only when the JVM is **low on memory** | memory-sensitive caches |
| **Weak** (`WeakReference`) | at the **next GC** if only weakly reachable | `WeakHashMap`, canonicalizing maps, listeners |
| **Phantom** (`PhantomReference`) | after the object is finalized; `get()` always returns null | post-mortem cleanup (`Cleaner`) |

> 🧩 "**Strong never lets go · Soft lets go when hungry · Weak lets go at the first chance · Phantom is already a ghost.**"

---

## 7. Memory Leaks in Java (yes, they happen)

**Definition:** objects that are **still reachable** (so the GC can't collect them) but **will never be used again**.

| Leak source | Fix |
|---|---|
| ever-growing `static` collections / caches | bound them, evict (LRU), use `WeakHashMap` |
| unclosed resources (streams, connections, sessions) | try-with-resources |
| listeners/callbacks never unregistered | unregister them, or use weak listeners |
| `ThreadLocal` in thread pools never removed | `threadLocal.remove()` in `finally` |
| non-static inner classes/anonymous classes holding the outer instance | `static` nested classes |
| mutable HashMap keys / broken `hashCode` | immutable keys, correct `equals`/`hashCode` |

### OutOfMemoryError flavours

| Message | Cause |
|---|---|
| `Java heap space` | heap full: a leak, or `-Xmx` too small |
| `GC overhead limit exceeded` | the GC spends >98% of the time recovering <2% of the heap |
| `Metaspace` | too many classes loaded (classloader leaks, heavy dynamic proxies) |
| `unable to create native thread` | too many platform threads / OS limits |
| `Requested array size exceeds VM limit` | an array larger than the JVM allows |

> **`StackOverflowError`** (an Error, not an Exception) = the stack depth was exceeded, usually by infinite/deep
> recursion. Fix the recursion, or raise `-Xss`.

---

## 8. JIT Compilation

```
 bytecode ──► Interpreter (starts immediately, slow)
                  │ profiling: "this method is HOT" (invocation/loop counters)
                  ▼
              C1 compiler (fast compile, light optimizations)
                  │ still hot
                  ▼
              C2 compiler (slow compile, aggressive optimizations) ──► fast native code
              (TIERED COMPILATION, default)
```

**Key optimizations:** method **inlining** · **escape analysis** (an object that never escapes a method can be
stack-allocated or scalar-replaced) · loop unrolling · dead-code elimination · lock elision.
If an assumption breaks, the JVM **deoptimizes** back to the interpreter.
**Warm-up:** Java apps get faster after running a while. *GraalVM native-image* (AOT) trades peak performance for
instant startup.

---

## 9. JVM Flags & Diagnostic Tools

| Flag | Meaning |
|---|---|
| `-Xms512m` / `-Xmx2g` | initial / max heap |
| `-Xss1m` | thread stack size |
| `-XX:MaxMetaspaceSize=256m` | cap the metaspace |
| `-XX:+UseG1GC` / `-XX:+UseZGC` | choose a GC |
| `-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=dump.hprof` | auto heap dump on OOM |
| `-Xlog:gc*` | GC logging (Java 9+ unified logging) |
| `-XX:+PrintFlagsFinal` | show every flag's value |

| Tool | Use |
|---|---|
| `jps` | list running JVMs (PIDs) |
| `jstack <pid>` | **thread dump**: deadlocks, stuck threads |
| `jmap -dump:live,format=b,file=heap.hprof <pid>` | **heap dump** |
| `jstat -gc <pid> 1000` | live GC statistics |
| `jcmd <pid> GC.heap_info / Thread.print / GC.class_histogram` | the Swiss-army knife |
| **VisualVM**, **JConsole**, **JDK Mission Control + Flight Recorder (JFR)** | GUI profiling/monitoring |
| **Eclipse MAT** | analyze heap dumps (find leak suspects) |

---

## 🧩 Memory Tricks

| Concept | Trick |
|---|---|
| Runtime areas | "**Mad Hens Steal Popcorn Nightly**": Method, Heap, Stack, PC, Native |
| Class loading | "**L-L-I, V-P-R**" |
| Loaders | "**Boot → Platform → App**", and "**ask your parent first**" |
| Generations | "**Most objects die young**", "**Eden → Survivor → Old**" |
| References | "**Strong holds · Soft when hungry · Weak at first chance · Phantom is a ghost**" |
| Stack vs heap | "**Stack = method calls & locals · Heap = objects**" |

---

## ❓ Self-Test

<details><summary>1. What's stored on the stack vs the heap?</summary>

The stack holds method frames with primitive local variables and references (one stack per thread). The heap
holds all objects and arrays, including their fields, and is shared by all threads.
</details>

<details><summary>2. When is an object eligible for garbage collection?</summary>

When it isn't reachable through any chain of references from a GC root (active stack locals, static fields,
active threads, JNI refs). Cycles that are unreachable are collected too.
</details>

<details><summary>3. What is the parent delegation model and why does it exist?</summary>

A classloader asks its parent to load a class before trying itself (App → Platform → Bootstrap). This prevents
core classes from being replaced by malicious or duplicate versions and keeps class identity consistent.
</details>

<details><summary>4. What replaced PermGen in Java 8 and why?</summary>

Metaspace, which lives in native memory and grows automatically. PermGen had a fixed size and caused frequent
`OutOfMemoryError: PermGen space`.
</details>

<details><summary>5. Minor GC vs Major GC?</summary>

A minor GC cleans the young generation (Eden plus survivors). It's frequent and fast. A major/full GC cleans
the old generation (or the whole heap), which is slower and has longer pauses.
</details>

<details><summary>6. Can Java have memory leaks? Give an example.</summary>

Yes: objects that remain reachable but unused. A classic example is a static `List` that keeps getting items
added and is never cleared. Other examples are unremoved listeners and ThreadLocals in thread pools.
</details>

<details><summary>7. What does System.gc() do?</summary>

It suggests that the JVM run a garbage collection. It isn't guaranteed and can be disabled with
`-XX:+DisableExplicitGC`.
</details>

<details><summary>8. Which GC is the default today?</summary>

G1, the default since Java 9 on server-class machines. The JVM may choose Serial on very small machines or
containers.
</details>

---

## ✅ Chapter Checklist

- [ ] Draw the JVM architecture (3 blocks)
- [ ] Draw the stack/heap diagram for a small program
- [ ] Explain the generations and an object's journey from Eden to Old
- [ ] Name 5 GC collectors and when to use each
- [ ] List 5 sources of memory leaks
- [ ] Run [code/09_jvm](../code/09_jvm)

➡️ Next: [10 · File I/O, NIO & Serialization](10-io-nio-serialization.md)
