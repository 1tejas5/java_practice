import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/*
 * SOLUTIONS - Exercises section 4: Exceptions
 * RUN: java exercises/solutions/Ex04Exceptions.java
 */
public class Ex04Exceptions {

    public static void main(String[] args) {
        // 4.1
        check("4.1 valid", 42, parseIntOrDefault("42", -1));
        check("4.1 invalid", -1, parseIntOrDefault("4x2", -1));
        check("4.1 null", 0, parseIntOrDefault(null, 0));
        check("4.1 spaces", 7, parseIntOrDefault("  7 ", 0));

        // 4.2
        check("4.2 valid age", "ok", ageResult(30));
        check("4.2 negative", "InvalidAgeException: age -5 is not between 0 and 150", ageResult(-5));
        check("4.2 too old", "InvalidAgeException: age 200 is not between 0 and 150", ageResult(200));

        // 4.3
        List<String> log = new ArrayList<>();
        try (TrackedResource db = new TrackedResource("db", log);
             TrackedResource file = new TrackedResource("file", log)) {
            log.add("work");
        }
        check("4.3 close order", List.of("open db", "open file", "work", "close file", "close db"), log);

        // 4.4
        int[] calls = {0};
        String result = retry(() -> {
            if (++calls[0] < 3) throw new IllegalStateException("flaky #" + calls[0]);
            return "success on attempt " + calls[0];
        }, 5);
        check("4.4 retry succeeds", "success on attempt 3", result);
        try {
            retry(() -> { throw new IllegalStateException("always down"); }, 3);
            check("4.4 retry gives up", "exception", "none");
        } catch (RuntimeException e) {
            check("4.4 retry gives up", "failed after 3 attempts", e.getMessage());
            check("4.4 keeps last cause", "always down", e.getCause().getMessage());
        }

        // 4.5
        try {
            loadConfig(Path.of("definitely-missing-config.properties"));
        } catch (ConfigException e) {
            check("4.5 translated", true, e.getMessage().startsWith("cannot load config"));
            check("4.5 cause kept", true, e.getCause() instanceof IOException);
        }
        summary();
    }

    // 4.1
    static int parseIntOrDefault(String s, int def) {
        if (s == null) return def;
        try {
            return Integer.parseInt(s.strip());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    // 4.2
    static class InvalidAgeException extends Exception {            // checked
        InvalidAgeException(String message) {
            super(message);
        }
    }

    static void validateAge(int age) throws InvalidAgeException {
        if (age < 0 || age > 150) throw new InvalidAgeException("age " + age + " is not between 0 and 150");
    }

    static String ageResult(int age) {
        try {
            validateAge(age);
            return "ok";
        } catch (InvalidAgeException e) {
            return e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }

    // 4.3
    static class TrackedResource implements AutoCloseable {
        private final String name;
        private final List<String> log;

        TrackedResource(String name, List<String> log) {
            this.name = name;
            this.log = log;
            log.add("open " + name);
        }

        @Override
        public void close() {
            log.add("close " + name);
        }
    }

    // 4.4
    static <T> T retry(Supplier<T> action, int attempts) {
        RuntimeException last = null;
        for (int i = 1; i <= attempts; i++) {
            try {
                return action.get();
            } catch (RuntimeException e) {
                last = e;                                           // remember and try again
            }
        }
        throw new RuntimeException("failed after " + attempts + " attempts", last);
    }

    // 4.5
    static class ConfigException extends RuntimeException {
        ConfigException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    static String loadConfig(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new ConfigException("cannot load config from " + path, e);   // translate, keep the cause
        }
    }

    // ---------------- test helper ----------------
    private static int passed, failed;

    static void check(String name, Object expected, Object actual) {
        boolean ok = Objects.deepEquals(expected, actual);
        if (ok) passed++;
        else failed++;
        System.out.printf("%s %-28s %s%n", ok ? "PASS" : "FAIL", name, ok ? "" : "expected=" + expected + " actual=" + actual);
    }

    static void summary() {
        System.out.printf("%n%d passed, %d failed%n", passed, failed);
    }
}
