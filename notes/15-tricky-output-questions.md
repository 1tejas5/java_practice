# 15 · Tricky "Predict the Output" Questions

> ⏱️ **Revise in:** 40 min · 🎯 **Level:** All levels · ▶️ **Verify:** `java code/15_tricky/TrickyOutputs.java`
>
> Every answer below was **produced by actually running** [TrickyOutputs.java](../code/15_tricky/TrickyOutputs.java)
> on Java 21. **How to practise:** read the code, say your answer out loud, *then* open the answer. Wrong answers are
> the most valuable ones: write them down in your "mistakes list".

---

## A. Operators & Types

<details><summary><b>Q1.</b> <code>System.out.println(1 + 2 + "3" + 4 + 5);</code></summary>

**`3345`**: evaluation is left to right. `1+2=3` (int), then `3+"3"="33"`, and once a String is involved, everything after is concatenated.
</details>

<details><summary><b>Q2–Q4.</b> <code>"" + 'a' + 'b'</code> · <code>'a' + 'b'</code> · <code>'a' + 'b' + "c"</code></summary>

**`ab`** · **`195`** · **`195c`**. Adding two chars is **int** arithmetic (97 + 98). A String on the left forces concatenation.
</details>

<details><summary><b>Q5.</b> <code>char c = 'A'; c += 1; System.out.println(c);</code></summary>

**`B`**. Compound assignment includes an implicit cast back to char. (`c = c + 1` would not compile.)
</details>

<details><summary><b>Q6.</b> <code>int i = 0; i = i++ + ++i;</code></summary>

**`2`**. `i++` yields 0 (i becomes 1), then `++i` makes i 2 and yields 2, so 0 + 2 = 2.
</details>

<details><summary><b>Q7.</b> <code>int x = 5; x = x++ * 2 + x;</code></summary>

**`16`**. `x++` yields 5 (x becomes 6), so 5 * 2 + 6 = 16.
</details>

<details><summary><b>Q8.</b> <code>int y = 10; y = y++;</code></summary>

**`10`**. The old value is saved, y is incremented, and then the saved 10 is assigned back.
</details>

<details><summary><b>Q9.</b> <code>System.out.println(010);</code></summary>

**`8`**. A leading 0 means **octal**.
</details>

<details><summary><b>Q10.</b> <code>System.out.println('7' + 1);</code></summary>

**`56`**. `'7'` is char code 55. To get the digit, use `'7' - '0'`.
</details>

<details><summary><b>Q11.</b> <code>System.out.println((byte) 200);</code></summary>

**`-56`**. A byte ranges from -128 to 127, and 200 − 256 = −56.
</details>

<details><summary><b>Q12–Q13.</b> <code>Integer.MAX_VALUE + 1</code> · <code>Math.abs(Integer.MIN_VALUE)</code></summary>

**`-2147483648`** both times. Overflow wraps silently, and MIN_VALUE has no positive counterpart in int.
</details>

<details><summary><b>Q14–Q16.</b> <code>0.1 + 0.2</code> · <code>0.1 + 0.2 == 0.3</code> · <code>0.1f + 0.2f == 0.3f</code></summary>

**`0.30000000000000004`** · **`false`** · **`true`** (by luck of float rounding). Never compare floating point with `==`:
use a tolerance or `BigDecimal`.
</details>

<details><summary><b>Q17–Q18.</b> <code>10.0 / 0</code> · <code>0.0/0 == 0.0/0</code></summary>

**`Infinity`** · **`false`** (NaN is never equal to anything, even itself). Integer `10 / 0` would throw `ArithmeticException`.
</details>

<details><summary><b>Q19.</b> <code>Math.round(2.5)</code>, <code>Math.round(-2.5)</code>, <code>Math.round(-2.51)</code></summary>

**`3 -2 -3`**. `Math.round(x)` = `floor(x + 0.5)`.
</details>

<details><summary><b>Q20.</b> <code>Math.min(Double.MIN_VALUE, 0.0)</code></summary>

**`0.0`**. `Double.MIN_VALUE` is the smallest **positive** double (4.9E-324), not the most negative one.
</details>

<details><summary><b>Q21.</b> <code>Object o = true ? 1 : 2.0; System.out.println(o);</code></summary>

**`1.0`**. Both ternary branches undergo numeric promotion to double.
</details>

<details><summary><b>Q22–Q23.</b> <code>true ? 'a' : 0</code> vs <code>true ? 'a' : k</code> (k is an int variable)</summary>

**`a`** vs **`97`**. With a constant int that fits in char, the result type is char. With a non-constant int, it's promoted to int.
</details>

<details><summary><b>Q24.</b> <code>boolean flag = false; if (flag = true) ...</code></summary>

**`yes`**. `=` is assignment, so the condition becomes `true`. (Only compiles because the type is boolean.)
</details>

<details><summary><b>Q25.</b> <code>-7 % 3</code> and <code>7 % -3</code></summary>

**`-1 1`**. The result takes the sign of the **dividend** (the left operand).
</details>

## B. Strings

<details><summary><b>Q26–Q29.</b> <code>("Ja"+"va") == "Java"</code> · <code>(part+"va") == "Java"</code> · <code>new String("Java") == "Java"</code> · <code>new String("Java").intern() == "Java"</code></summary>

**`true` · `false` · `false` · `true`**. Constants are folded at compile time into the pooled literal. Runtime
concatenation and `new` create heap objects. `intern()` returns the pooled one.
</details>

<details><summary><b>Q30.</b> <code>String s = "a"; s.concat("b"); System.out.println(s);</code></summary>

**`a`**. Strings are immutable. `concat` returns a new String, and it's discarded here.
</details>

<details><summary><b>Q31.</b> <code>String n = null; System.out.println(n + "x");</code></summary>

**`nullx`**. Concatenation converts null to `"null"`. (Calling `n.length()` would throw an NPE.)
</details>

<details><summary><b>Q32.</b> <code>"abc".substring(3)</code> and <code>"abc".substring(1,1)</code></summary>

Both are **empty strings** (valid). `substring(4)` would throw `StringIndexOutOfBoundsException`.
</details>

<details><summary><b>Q33.</b> <code>"a,b,,c,,".split(",").length</code></summary>

**`4`** → `[a, b, "", c]`. Trailing empty strings are removed, but middle ones stay.
</details>

<details><summary><b>Q34.</b> <code>"a.b.c".split(".").length</code></summary>

**`0`**. `.` is a regex that matches every character, so everything is a separator and only trailing empties are left, which get removed. Use `split("\\.")`.
</details>

<details><summary><b>Q35.</b> <code>new StringBuilder("a").equals(new StringBuilder("a"))</code></summary>

**`false`**. StringBuilder doesn't override `equals`, so this is a reference comparison.
</details>

<details><summary><b>Q36.</b> <code>"" + 1 + 2</code> vs <code>(1 + 2) + ""</code></summary>

**`12`** vs **`3`**.
</details>

## C. Wrappers

<details><summary><b>Q37.</b> <code>Integer a=127, b=127, c=128, d=128;</code> <code>a==b</code>, <code>c==d</code></summary>

**`true false`**. The Integer cache covers -128..127. Always use `equals` for wrappers.
</details>

<details><summary><b>Q38–Q39.</b> <code>Long.valueOf(1).equals(1)</code> · <code>Integer.valueOf(1).equals(Long.valueOf(1))</code></summary>

**`false` · `false`**. `equals` checks the type too: Integer ≠ Long.
</details>

<details><summary><b>Q40.</b> <code>new BigDecimal("2.0").equals(new BigDecimal("2.00"))</code></summary>

**`false`**. The scale differs. Use `compareTo(...) == 0` to compare numeric values.
</details>

<details><summary><b>Q41.</b> <code>Character.getNumericValue('a')</code></summary>

**`10`**. Letters a–z map to 10–35 (as digits in base 36). Not -1!
</details>

## D. Collections

<details><summary><b>Q42.</b> <code>List&lt;Integer&gt; l = new ArrayList&lt;&gt;(List.of(1,2,3)); l.remove(1);</code></summary>

**`[1, 3]`**. `remove(int index)` is chosen over `remove(Object)`. To remove the value, use `remove(Integer.valueOf(1))`.
</details>

<details><summary><b>Q43.</b> <code>int[] prim = {1,2,3}; Arrays.asList(prim).size()</code> vs <code>Arrays.asList(1,2,3).size()</code></summary>

**`1 vs 3`**. With a primitive array, `T` becomes `int[]`, so you get a list with **one** element (the array itself).
</details>

<details><summary><b>Q44.</b> <code>Arrays.asList(1,2,3).add(4)</code></summary>

**`UnsupportedOperationException`**. The list is fixed-size (backed by the array).
</details>

<details><summary><b>Q45.</b> <code>List.of(1,2,3).contains(null)</code></summary>

**`NullPointerException`**. Immutable `List.of` collections reject null even in queries.
</details>

<details><summary><b>Q46.</b> The Set&lt;Short&gt; puzzle: add <code>sh</code>, then <code>remove(sh - 1)</code>, 100 times. Size?</summary>

**`100`**. `sh - 1` is an **int**, which autoboxes to `Integer`, and an Integer is never equal to a Short, so nothing is ever removed.
</details>

<details><summary><b>Q47–Q48.</b> <code>hashMap.put(null, 1); hashMap.get(null)</code> · <code>treeMap.put(null, 1)</code></summary>

**`1`** · **`NullPointerException`**. HashMap allows one null key. TreeMap must compare keys, so null fails.
</details>

<details><summary><b>Q49.</b> Adding to an ArrayList inside a for-each over it</summary>

**`ConcurrentModificationException`**. The fail-fast iterator detects the structural modification.
</details>

<details><summary><b>Q50.</b> <code>Object[] arr = new Integer[1]; arr[0] = "s";</code></summary>

**`ArrayStoreException`**. Arrays are covariant, but the runtime still checks the element type.
</details>

## E. OOP

<details><summary><b>Q51.</b> <code>P p = new C();</code> (both have a field <code>name</code>, an overridden <code>getName()</code>, and a static <code>who()</code>). Print <code>p.name + p.getName() + p.who()</code></summary>

**`P C P.who`**. Fields and static methods use the **reference** type; overridden instance methods use the **object** type.
</details>

<details><summary><b>Q52.</b> The parent constructor calls an overridden <code>hook()</code> that reads the child's fields <code>tag = "derived"</code> (normal) and <code>final constTag = "derived"</code></summary>

**`tag=null, constTag=derived`**. The child's field initializers haven't run yet when the parent constructor runs, **but**
a `final` field initialized with a compile-time constant is **inlined** by the compiler, so it already appears to have its value.
</details>

<details><summary><b>Q53.</b> Overloads <code>call(Object)</code> and <code>call(String)</code>: what does <code>call(null)</code> pick?</summary>

**`String`**, the most specific type. (If you added `call(StringBuilder)`, `call(null)` would fail to compile as ambiguous.)
</details>

<details><summary><b>Q54.</b> Overloads <code>pick(long)</code>, <code>pick(Integer)</code>, <code>pick(int...)</code>: what does <code>pick(5)</code> pick?</summary>

**`long`**. The order is Exact → **Widening** → Boxing → Varargs ("Every Wise Boy Votes").
</details>

## F. Exceptions

<details><summary><b>Q55.</b> <code>try { return 1; } finally { return 2; }</code></summary>

**`2`**. The finally block's return overrides the try's return.
</details>

<details><summary><b>Q56.</b> <code>catch</code> appends "catch " and returns <code>sb.append("return").toString()</code>; <code>finally</code> appends " + finally ran"</summary>

**`catch return`**. The return **value** (a String) is computed before finally runs. Finally still runs, but it changes
the StringBuilder, not the String that was already created.
</details>

<details><summary><b>Q57.</b> <code>int v = 10; try { return v; } finally { v = 20; }</code></summary>

**`10`**. The primitive return value was already saved.
</details>

<details><summary><b>Bonus.</b> <code>try { System.exit(0); } finally { System.out.println("finally"); }</code></summary>

Prints **nothing**. `System.exit` stops the JVM, so finally doesn't run.
</details>

## G. Streams & Syntax

<details><summary><b>Q58.</b> <code>Stream.of(1,2,3).peek(trace::append);</code> (no terminal op). What's in <code>trace</code>?</summary>

**`''`** (empty). Without a terminal operation, nothing executes.
</details>

<details><summary><b>Q59.</b> <code>Stream.of(1,2,3,4).peek(log).map(n -> n*2).filter(n -> n > 2).findFirst()</code>. Which elements were peeked?</summary>

**`Optional[4] after p1 p2`**. Elements flow one at a time, and `findFirst` short-circuits after the first match, so 3 and 4 are never touched.
</details>

<details><summary><b>Q60.</b> <code>for (int n = 0; n < 3; n++) ; { loop.append("x"); }</code></summary>

**`x`** (once). The `;` is the empty loop body, and the braces are just a block that runs once afterwards.
</details>

---

## 🏆 Score Yourself

| Score | Verdict |
|---|---|
| 55–60 | Interview-ready on core Java gotchas 🔥 |
| 45–54 | Strong. Revisit the sections where you slipped |
| 30–44 | Good base. Re-read chapters 01, 03 and 06 |
| < 30 | Do chapters 01–06 again, then retry in 3 days (spaced repetition!) |

➡️ Next: [16 · One-Page Cheat Sheet](16-cheatsheet.md)
