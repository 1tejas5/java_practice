import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * TOPIC: NIO.2 Files/Path, buffered readers/writers, byte streams, Data streams,
 *        CSV parsing into records, directory walking, serialization with transient.
 * RUN:   java code/10_io/FileIoDemo.java
 * NOTE:  everything happens inside a TEMP directory that is deleted at the end.
 */
public class FileIoDemo {

    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("java-revision-io");
        try {
            run(root);
        } finally {
            deleteRecursively(root);
            System.out.println("\n(temp directory " + root.getFileName() + " deleted)");
        }
    }

    static void run(Path root) throws IOException, ClassNotFoundException {
        section("1. Path basics");
        Path file = root.resolve("data").resolve("notes.txt");
        System.out.println("fileName=" + file.getFileName() + ", parent=" + file.getParent().getFileName()
                + ", sibling=" + file.resolveSibling("other.txt").getFileName());
        System.out.println("normalize(a/./b/../c) = " + Path.of("a", ".", "b", "..", "c").normalize());

        section("2. Write & read with Files (one-liners)");
        Files.createDirectories(file.getParent());
        Files.writeString(file, "first line\nsecond line\n");
        Files.writeString(file, "third line (appended)\n", StandardOpenOption.APPEND);
        System.out.print(Files.readString(file));
        List<String> lines = Files.readAllLines(file);
        System.out.println("readAllLines -> " + lines.size() + " lines, size on disk = " + Files.size(file) + " bytes");

        section("3. Lazy Files.lines (close it with try-with-resources)");
        try (Stream<String> stream = Files.lines(file)) {
            System.out.println("lines containing 'line': " + stream.filter(l -> l.contains("line")).count());
        }

        section("4. Buffered writer/reader");
        Path log = root.resolve("app.log");
        try (BufferedWriter w = Files.newBufferedWriter(log, StandardCharsets.UTF_8)) {
            for (int i = 1; i <= 3; i++) {
                w.write("INFO request " + i + " served");
                w.newLine();
            }
            w.write("ERROR database timeout");
            w.newLine();
        }
        try (BufferedReader r = Files.newBufferedReader(log)) {
            String line;
            int n = 0;
            while ((line = r.readLine()) != null) {
                if (line.startsWith("ERROR")) System.out.println("found at line " + (n + 1) + ": " + line);
                n++;
            }
        }

        section("5. CSV -> records -> report");
        Path csv = root.resolve("employees.csv");
        Files.write(csv, List.of(
                "name,dept,salary",
                "Asha,IT,85000",
                "Ravi,IT,65000",
                "Meera,HR,50000",
                "John,Sales,55000"));
        List<Employee> emps;
        try (Stream<String> rows = Files.lines(csv)) {
            emps = rows.skip(1)                                 // skip the header
                    .map(row -> row.split(","))
                    .map(c -> new Employee(c[0], c[1], Double.parseDouble(c[2])))
                    .toList();
        }
        Map<String, Double> totalByDept = emps.stream()
                .collect(Collectors.groupingBy(Employee::dept, TreeMap::new, Collectors.summingDouble(Employee::salary)));
        System.out.println("parsed " + emps.size() + " employees, total salary by dept = " + totalByDept);
        Path report = root.resolve("report.txt");
        Files.write(report, totalByDept.entrySet().stream().map(e -> e.getKey() + " = " + e.getValue()).toList());
        System.out.println("report written: " + Files.readAllLines(report));

        section("6. Byte streams: copy a binary file manually with a buffer");
        Path bin = root.resolve("random.bin");
        byte[] data = new byte[10_000];
        new java.util.Random(42).nextBytes(data);
        Files.write(bin, data);
        Path binCopy = root.resolve("random-copy.bin");
        try (InputStream in = Files.newInputStream(bin); OutputStream out = Files.newOutputStream(binCopy)) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
        }
        System.out.println("copied " + Files.size(binCopy) + " bytes, identical = " + (Files.mismatch(bin, binCopy) == -1));

        section("7. Data streams: write/read primitives in binary");
        Path dat = root.resolve("values.dat");
        try (var out = new DataOutputStream(Files.newOutputStream(dat))) {
            out.writeInt(42);
            out.writeDouble(3.14);
            out.writeUTF("hello");
            out.writeBoolean(true);
        }
        try (var in = new DataInputStream(Files.newInputStream(dat))) {     // read in the SAME order
            System.out.println("int=" + in.readInt() + " double=" + in.readDouble()
                    + " utf=" + in.readUTF() + " boolean=" + in.readBoolean());
        }

        section("8. Copy, move, delete, list, walk");
        Path backupDir = Files.createDirectories(root.resolve("backup"));
        Files.copy(file, backupDir.resolve("notes.bak"), StandardCopyOption.REPLACE_EXISTING);
        Files.move(report, backupDir.resolve("report.txt"), StandardCopyOption.REPLACE_EXISTING);
        System.out.println("report.txt still in root? " + Files.exists(report));
        try (Stream<Path> tree = Files.walk(root)) {
            tree.filter(Files::isRegularFile)
                    .map(p -> root.relativize(p).toString().replace('\\', '/'))
                    .sorted()
                    .forEach(p -> System.out.println("  " + p));
        }

        section("9. Serialization: transient and static fields are not saved");
        User.loginCount = 7;
        User original = new User("asha", "s3cr3t", 30);
        Path ser = root.resolve("user.ser");
        try (var out = new ObjectOutputStream(Files.newOutputStream(ser))) {
            out.writeObject(original);
        }
        User.loginCount = 99;                                 // change the static after saving
        User restored;
        try (var in = new ObjectInputStream(Files.newInputStream(ser))) {
            restored = (User) in.readObject();
        }
        System.out.println("original : " + original);
        System.out.println("restored : " + restored + "  <- password (transient) is null");
        System.out.println("static loginCount after restore = " + User.loginCount + " (current class value, not 7)");
        System.out.println("same object? " + (original == restored) + ", serialized size = " + Files.size(ser) + " bytes");
    }

    static void deleteRecursively(Path root) throws IOException {
        try (Stream<Path> walk = Files.walk(root)) {
            for (Path p : walk.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(p);   // children first
        }
    }

    static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}

record Employee(String name, String dept, double salary) { }

class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    static int loginCount;                       // static: belongs to the class, never serialized

    private final String username;
    private final transient String password;     // transient: skipped
    private final int age;

    User(String username, String password, int age) {
        this.username = username;
        this.password = password;
        this.age = age;
    }

    @Override
    public String toString() {
        return "User{username=" + username + ", password=" + password + ", age=" + age + "}";
    }
}
