# 10 · File I/O, NIO.2 & Serialization

> ⏱️ **Revise in:** 35 min · 🎯 **Level:** Intermediate · ▶️ **Code:** [code/10_io](../code/10_io)

## 🧠 Mind Map

```
10 I/O
├── java.io (classic, stream-based, blocking)
│   ├── BYTE streams (binary: images, pdf, any file)
│   │   InputStream / OutputStream → FileInputStream · BufferedInputStream · DataInputStream
│   │                               · ObjectInputStream · ByteArrayInputStream
│   └── CHAR streams (text, with an encoding)
│       Reader / Writer → FileReader · BufferedReader (readLine) · InputStreamReader (bridge)
│                        · FileWriter · BufferedWriter · PrintWriter
│   └── Wrapping = DECORATOR pattern: new BufferedReader(new FileReader(...))
├── java.nio.file (NIO.2, Java 7+) ✅ PREFER
│   ├── Path.of("a", "b.txt") · resolve · getParent · getFileName · normalize
│   └── Files: readString · writeString · readAllLines · lines (lazy, close it!) · write
│              · exists · createDirectories · copy · move · delete · walk · list · size
├── Always: try-with-resources · specify the charset (UTF-8 default since Java 18)
├── Console input: Scanner (parses tokens) vs BufferedReader (fast lines)
├── Serialization: object ⇄ bytes
│   ├── implements Serializable (marker) · ObjectOutputStream.writeObject / readObject
│   ├── serialVersionUID (version check) · transient (skip a field) · static is not serialized
│   ├── non-serializable parent → its no-arg constructor runs on deserialization
│   └── Externalizable (full control) · security risk → prefer JSON
└── NIO (Java 1.4): Channels · Buffers · Selectors → non-blocking I/O (Netty, servers)
```

## ⚡ 30-Second Summary

- **Bytes** → `InputStream`/`OutputStream`. **Text** → `Reader`/`Writer`.
- Wrap streams in **Buffered*** for performance. That wrapping is the **Decorator pattern**.
- For everyday file work, use **`java.nio.file.Files`**: one-liners for reading, writing, copying and walking.
- **Always** use try-with-resources so files get closed.
- **Serialization** turns objects into bytes. Remember `Serializable`, `serialVersionUID` and `transient`.

---

## 1. The Two Families

| | Byte streams | Character streams |
|---|---|---|
| Base classes | `InputStream` / `OutputStream` | `Reader` / `Writer` |
| Unit | 8-bit bytes | 16-bit chars (decoded via a charset) |
| Use for | images, audio, zip, any binary | text files, CSV, logs |
| File | `FileInputStream` / `FileOutputStream` | `FileReader` / `FileWriter` |
| Buffered | `BufferedInputStream` / `BufferedOutputStream` | `BufferedReader` (`readLine()`) / `BufferedWriter` (`newLine()`) |
| Bridge | | `InputStreamReader` / `OutputStreamWriter` (bytes ⇄ chars, with a charset) |
| Objects/primitives | `ObjectInputStream`, `DataInputStream` | |
| Printing | `PrintStream` (`System.out`) | `PrintWriter` |

```java
// The Decorator pattern in action: each layer adds a feature
try (BufferedReader br = new BufferedReader(                     // + buffering + readLine()
        new InputStreamReader(                                    // + bytes → chars (charset)
            new FileInputStream("data.txt"), StandardCharsets.UTF_8))) {   // raw bytes from a file
    String line;
    while ((line = br.readLine()) != null) System.out.println(line);
}
```

> 🔑 **Why buffer?** Without a buffer, every `read()` can be a costly system call. A buffer (8 KB by default)
> reads big chunks at once.

---

## 2. NIO.2: the Modern Way (`java.nio.file`) ✅

```java
Path file = Path.of("data", "notes.txt");             // data/notes.txt (OS-independent separators)

Files.createDirectories(file.getParent());            // mkdir -p
Files.writeString(file, "line 1\nline 2\n");          // create/overwrite
Files.writeString(file, "line 3\n", StandardOpenOption.APPEND);
String all = Files.readString(file);                  // the whole file (small files)
List<String> lines = Files.readAllLines(file);        // all lines into memory

try (Stream<String> stream = Files.lines(file)) {     // LAZY: big files, must be closed
    long count = stream.filter(l -> l.contains("2")).count();
}

Files.copy(file, Path.of("backup.txt"), StandardCopyOption.REPLACE_EXISTING);
Files.move(src, dest, StandardCopyOption.ATOMIC_MOVE);
Files.deleteIfExists(file);
Files.exists(file); Files.isDirectory(dir); Files.size(file);

try (Stream<Path> tree = Files.walk(Path.of("."))) {  // recursive
    tree.filter(p -> p.toString().endsWith(".java")).forEach(System.out::println);
}
try (BufferedWriter w = Files.newBufferedWriter(file)) { w.write("buffered"); w.newLine(); }
```

| Path method | Example (`p = /home/asha/docs/a.txt`) |
|---|---|
| `getFileName()` | `a.txt` |
| `getParent()` | `/home/asha/docs` |
| `getRoot()` | `/` |
| `resolve("b.txt")` | `/home/asha/docs/a.txt/b.txt` (appends) |
| `resolveSibling("b.txt")` | `/home/asha/docs/b.txt` |
| `normalize()` | removes `.` and `..` |
| `toAbsolutePath()` | full path |
| `relativize(other)` | the relative path between two paths |

| `java.io.File` (legacy) | `java.nio.file.Path` + `Files` |
|---|---|
| methods return `false` on failure (silent) | throw descriptive exceptions |
| weak symlink/attribute support | full support |
| `file.listFiles()` | `Files.list`, `Files.walk` (streams) |
| convert: `file.toPath()` | convert: `path.toFile()` |

---

## 3. Reading Console Input

| `Scanner` | `BufferedReader` |
|---|---|
| parses tokens: `nextInt()`, `nextDouble()`, `next()` | reads whole lines: `readLine()` |
| slower (regex parsing), 1 KB buffer | **faster**, 8 KB buffer (competitive programming) |
| hides IOException | throws IOException |
| not synchronized | synchronized |
| ⚠️ the `nextInt()` + `nextLine()` leftover-newline trap | parse yourself: `Integer.parseInt(br.readLine())` |

---

## 4. Serialization

**One-liner:** converting an object's state into a **byte stream** (to save to a file, send over a network, or cache)
and later **deserializing** it back into an object.

```java
class User implements Serializable {                     // marker interface: no methods
    @Serial
    private static final long serialVersionUID = 1L;     // version stamp
    private String name;
    private transient String password;                   // transient → NOT serialized (null after reading)
    private static int count;                            // static → NOT serialized (belongs to the class)
}

try (var out = new ObjectOutputStream(Files.newOutputStream(Path.of("user.ser")))) {
    out.writeObject(new User("asha", "secret"));
}
try (var in = new ObjectInputStream(Files.newInputStream(Path.of("user.ser")))) {
    User u = (User) in.readObject();                     // password == null
}
```

| Rule | Detail |
|---|---|
| `Serializable` | a marker interface. Without it → `NotSerializableException` |
| All fields | must be serializable themselves (or `transient`) |
| `serialVersionUID` | if the class changes and the UIDs differ → `InvalidClassException`. **Declare it explicitly**, or the JVM computes one from the class structure (fragile) |
| `transient` | skipped; gets its default value (0/false/null) on deserialization |
| `static` | never serialized |
| Constructors | **not called** for Serializable classes. The **first non-serializable parent's no-arg constructor IS called** |
| Inheritance | a parent that's Serializable makes the child Serializable too |
| Custom logic | `private void writeObject(ObjectOutputStream)` / `readObject(ObjectInputStream)` |
| Singletons | implement `readResolve()` to return the instance (or use an enum singleton) |
| `Externalizable` | you write `writeExternal`/`readExternal` yourself; needs a **public no-arg constructor** |

> ⚠️ **Security:** deserializing **untrusted** data can execute malicious "gadget chains". Prefer JSON
> (Jackson/Gson) or protobuf for data exchange, and use `ObjectInputFilter` (Java 9) when you must deserialize.

---

## 5. Classic IO vs NIO (channels)

| `java.io` | `java.nio` (channels/buffers) |
|---|---|
| stream-oriented (byte by byte) | buffer-oriented (blocks) |
| **blocking** | can be **non-blocking** |
| one thread per connection | one thread can manage many channels via a **Selector** |
| simple | more complex. Used by Netty, Tomcat NIO connectors |

> 💡 With **virtual threads** (Java 21), simple blocking I/O code scales again, so you rarely need hand-written NIO.

---

## ❓ Self-Test

<details><summary>1. When would you use a byte stream instead of a character stream?</summary>

For binary data such as images, PDFs, zip files or serialized objects. Character streams decode bytes into
chars, which would corrupt binary content.
</details>

<details><summary>2. What design pattern does <code>new BufferedReader(new FileReader(f))</code> use?</summary>

The Decorator pattern. Each wrapper adds behaviour (buffering, readLine) while keeping the same Reader interface.
</details>

<details><summary>3. Files.readAllLines vs Files.lines?</summary>

`readAllLines` loads every line into a List in memory, which is fine for small files. `lines` returns a lazy
Stream that reads as you consume it, so it suits big files, but you must close it with try-with-resources.
</details>

<details><summary>4. What is serialVersionUID?</summary>

A version identifier for a Serializable class. During deserialization the stream's UID must match the class's
UID, otherwise you get `InvalidClassException`. Declare it explicitly so harmless changes don't break
compatibility.
</details>

<details><summary>5. What happens to transient and static fields during serialization?</summary>

Neither is written. Transient fields come back with default values. Static fields belong to the class, so they
just have whatever value the class currently has.
</details>

<details><summary>6. Is the constructor called during deserialization?</summary>

Not for the Serializable class itself. The no-arg constructor of the first **non-serializable** superclass is
called (Object's, at the very least).
</details>

---

## ✅ Chapter Checklist

- [ ] Draw the byte vs char stream table
- [ ] Read and write a file with `Files` in 3 lines
- [ ] Explain Decorator in Java I/O
- [ ] Serialize an object with a transient field and show what's lost
- [ ] Run [code/10_io](../code/10_io)

➡️ Next: [11 · Modern Java (9 → 21+)](11-modern-java.md)
