# ☕ Java Complete Revision Kit: Beginner → Advanced (+ OOP, Hands-On)

> Everything you need to **revise Java quickly and remember it**: visual mind maps, short notes with memory tricks,
> runnable demos, interview questions, tricky-output puzzles, exercises with self-checking solutions, an interactive
> flashcard app, and a real project to code.
>
> ✅ Every program here was **compiled and run on Java 21** (your installed JDK): 42 demos, 10 solution files,
> and a 30-file project with 21 passing tests.

---

## 🚀 Start Here (5 minutes)

1. **Open the big picture:** [notes/00-master-mindmap.md](notes/00-master-mindmap.md). In VS Code, press
   `Ctrl+Shift+V` for the formatted preview.
2. **Open the interactive app:** double-click [java-revision-app.html](java-revision-app.html) (works offline in any browser).
   It has 🧠 mind maps with *Recall mode*, 🃏 ~150 flashcards, and a 🎯 34-question quiz.
3. **Run your first demo:**
   ```powershell
   cd C:\java
   java code\02_oops\FourPillarsDemo.java
   ```
4. **Pick a study plan** (below) and go.

---

## 📦 What's Inside

```
C:\java
├── README.md                    ← you are here
├── java-revision-app.html       ← interactive mind maps + flashcards + quiz (open in a browser)
│
├── notes\                       ← 17 chapters: mind map → summary → concepts → tables → tricks → self-test
│   ├── 00-master-mindmap.md     the whole of Java on one page + all memory tricks
│   ├── 01-java-basics.md        JDK/JRE/JVM, types, casting, operators, control flow, arrays, methods
│   ├── 02-oops.md               ⭐ classes, constructors, static, A PIE pillars, interfaces, Object, immutability
│   ├── 03-strings-wrappers-enums.md
│   ├── 04-exceptions.md
│   ├── 05-generics.md
│   ├── 06-collections.md        ⭐ incl. HashMap internals
│   ├── 07-functional-java8.md   ⭐ lambdas, streams, collectors, Optional + 20 stream interview problems
│   ├── 08-multithreading-concurrency.md  ⭐ incl. CompletableFuture & virtual threads
│   ├── 09-jvm-memory-gc.md
│   ├── 10-io-nio-serialization.md
│   ├── 11-modern-java.md        records, sealed, pattern matching, Java 9 → 25
│   ├── 12-design-patterns-solid.md
│   ├── 13-more-apis.md          annotations, reflection, java.time, JDBC
│   ├── 14-interview-questions.md   120 Q&A (click to reveal)
│   ├── 15-tricky-output-questions.md   60 "predict the output" puzzles (answers verified by running them)
│   └── 16-cheatsheet.md         one page, night before the interview
│
├── code\                        ← 42 runnable demos (java File.java), one folder per chapter
│   ├── README.md                index of what each demo shows
│   └── run-all.cmd              runs everything, prints OK/FAILED
│
├── exercises\                   ← ~70 coding problems across 10 topics (easy → hard)
│   ├── README.md                problems + hints
│   ├── practice\Practice.java   template with a PASS/FAIL checker for YOUR solutions
│   └── solutions\Ex01..Ex10     reference solutions that test themselves
│
└── project\                     ← 🏦 MiniBank: a realistic app using almost every concept
    ├── README.md                architecture, concept map, 9 build-it-yourself milestones
    ├── run.cmd                  run.cmd | run.cmd cli | run.cmd test
    ├── src\com\minibank\...     model, service, repository, events, CLI, concurrency simulation
    └── test\com\minibank\...    a homemade JUnit (annotations + reflection) and 21 tests
```

---

## 🧠 The Revision Method (why this package is structured this way)

Reading alone is the *weakest* way to remember. This kit is built around 5 techniques that research shows work:

| Step | Technique | What you do here |
|---|---|---|
| 1️⃣ **See the whole** | Big-picture first | Look at the chapter's **mind map** and try to recall each branch *before* reading |
| 2️⃣ **Understand** | Analogies + examples | Read the notes: one-liners, analogies, tables, ⚠️ traps |
| 3️⃣ **See it run** | Concrete experience | Run the `code\` demo. **Predict the output first**, then compare |
| 4️⃣ **Recall** | Active recall + testing effect | Self-test `<details>` questions, flashcards, quiz, tricky outputs |
| 5️⃣ **Apply** | Deliberate practice | Exercises, then the MiniBank milestones |
| 🔁 **Repeat** | Spaced repetition | Revisit after **1 day → 3 days → 7 days → 21 days** (flashcards "only unknown" mode) |

> 🧩 **Mnemonics** are collected in [00-master-mindmap.md](notes/00-master-mindmap.md#-all-memory-tricks-in-one-place):
> *A PIE*, *PECS*, *Hash = Hurry / Linked = Line / Tree = Tidy*, *My House Never Collapses*, *Every Wise Boy Votes*…

---

## 📅 Study Plans

### ⚡ 7-Day Fast Track (2–3 hours/day), for when you already know Java and need a refresh

| Day | Notes | Run | Practise |
|---|---|---|---|
| 1 | 00, 01, 02 (OOP) | `code\01_basics`, `code\02_oops` | Exercises 1 + 2 · flashcards: Basics, OOP |
| 2 | 03, 04, 05 | `code\03_strings`, `04`, `05` | Exercises 3, 4, 5 · tricky Q1–Q41 |
| 3 | 06 Collections | `code\06_collections` (esp. `MyHashMap.java`) | Exercises 6 · tricky Q42–Q50 |
| 4 | 07 Functional | `code\07_functional` | Exercises 7 · stream problems from memory |
| 5 | 08 Concurrency, 09 JVM | `code\08_concurrency`, `code\09_jvm` | Exercises 8 · flashcards: Concurrency, JVM |
| 6 | 10, 11, 12, 13 | `code\10_io` … `code\13_more_apis` | Exercises 9, 10 · run MiniBank + read its code |
| 7 | 14 Interview Q&A, 15 Tricky, 16 Cheat sheet | `code\15_tricky` | Full quiz in the app · flashcards "only unknown" |

### 🐢 14-Day Thorough Plan (1.5–2 hours/day), from beginner to advanced, with the full project

| Day | Focus |
|---|---|
| 1 | 01 Basics + demos + exercises 1.1–1.6 |
| 2 | 02 OOP part 1 (classes → inheritance) + demos |
| 3 | 02 OOP part 2 (polymorphism → immutability) + exercises 2 · **review day 1** |
| 4 | 03 Strings/Wrappers/Enums + 04 Exceptions + exercises 3, 4 |
| 5 | 05 Generics + 06 Collections part 1 · **review days 2–3** |
| 6 | 06 Collections part 2 (HashMap internals, Comparators) + exercises 6 |
| 7 | 07 Functional + 20 stream problems · **weekly review: quiz + flashcards** |
| 8 | 08 Concurrency part 1 (threads, sync, volatile, wait/notify) |
| 9 | 08 Concurrency part 2 (executors, CompletableFuture, virtual threads) + exercises 8 |
| 10 | 09 JVM + 10 I/O · **MiniBank milestones 1–3** |
| 11 | 11 Modern Java + exercises 9 · **MiniBank milestones 4–6** |
| 12 | 12 Design Patterns + 13 More APIs + exercises 10 · **MiniBank milestones 7–9** |
| 13 | 14 Interview questions (all) + 15 Tricky outputs (all) |
| 14 | 16 Cheat sheet + full quiz + flashcards "only unknown" until the deck is empty 🎉 |

---

## 📚 Chapter Index

| # | Chapter | Level | Time | Demo code |
|---|---|---|---|---|
| 00 | [Master Mind Map](notes/00-master-mindmap.md) | all | 15 min | n/a |
| 01 | [Java Basics](notes/01-java-basics.md) | 🟢 | 45 min | [01_basics](code/01_basics) |
| 02 | [OOP](notes/02-oops.md) | 🟢→🔴 | 90 min | [02_oops](code/02_oops) |
| 03 | [Strings, Wrappers, Enums](notes/03-strings-wrappers-enums.md) | 🟢→🟡 | 45 min | [03_strings](code/03_strings) |
| 04 | [Exceptions](notes/04-exceptions.md) | 🟡 | 40 min | [04_exceptions](code/04_exceptions) |
| 05 | [Generics](notes/05-generics.md) | 🟡 | 30 min | [05_generics](code/05_generics) |
| 06 | [Collections](notes/06-collections.md) | 🟡→🔴 | 75 min | [06_collections](code/06_collections) |
| 07 | [Lambdas, Streams, Optional](notes/07-functional-java8.md) | 🟡→🔴 | 75 min | [07_functional](code/07_functional) |
| 08 | [Multithreading & Concurrency](notes/08-multithreading-concurrency.md) | 🔴 | 90 min | [08_concurrency](code/08_concurrency) |
| 09 | [JVM, Memory, GC](notes/09-jvm-memory-gc.md) | 🔴 | 45 min | [09_jvm](code/09_jvm) |
| 10 | [I/O, NIO, Serialization](notes/10-io-nio-serialization.md) | 🟡 | 35 min | [10_io](code/10_io) |
| 11 | [Modern Java 9 → 25](notes/11-modern-java.md) | 🟡→🔴 | 50 min | [11_modern](code/11_modern) |
| 12 | [SOLID & Design Patterns](notes/12-design-patterns-solid.md) | 🟡→🔴 | 70 min | [12_patterns](code/12_patterns) |
| 13 | [Annotations, Reflection, Date-Time, JDBC](notes/13-more-apis.md) | 🟡→🔴 | 45 min | [13_more_apis](code/13_more_apis) |
| 14 | [120 Interview Questions](notes/14-interview-questions.md) | all | 2 × 60 min | n/a |
| 15 | [60 Tricky Output Puzzles](notes/15-tricky-output-questions.md) | all | 40 min | [15_tricky](code/15_tricky) |
| 16 | [One-Page Cheat Sheet](notes/16-cheatsheet.md) | all | 15 min | n/a |

---

## 🛠️ Handy Commands

```powershell
java code\06_collections\MyHashMap.java          # run any single demo
code\run-all.cmd                                 # run all 42 demos (OK/FAILED summary)
java exercises\solutions\Ex07Streams.java        # see the reference solutions pass their checks
cd project ; .\run.cmd                           # MiniBank guided demo
.\run.cmd cli                                    # MiniBank interactive console
.\run.cmd test                                   # MiniBank's 21 unit tests
```

**VS Code tips:** `Ctrl+Shift+V` = markdown preview · `Ctrl+K V` = preview side by side · with the
*Extension Pack for Java* installed you can also click ▶ Run above any `main` method.

---

## 💡 Last-Mile Interview Advice

- **Explain in 3 layers:** a one-line definition → a real-world analogy → a code example or trade-off.
  (Every concept in the notes is written in this shape. Practise saying them out loud.)
- **Always mention the trade-off:** *"ArrayList is the default because of O(1) random access, but…"*
- **Use the MiniBank project as your story:** *"In my practice project I implemented deadlock-free transfers by
  locking accounts in id order, and verified money conservation with 20,000 concurrent transfers."*
- **Day before:** read [16-cheatsheet.md](notes/16-cheatsheet.md), run the quiz once, sleep well. 😴

Happy revising! ☕🚀
