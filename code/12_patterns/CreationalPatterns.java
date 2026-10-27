import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Set;

/*
 * TOPIC: Creational patterns - Singleton (4 ways + breaking it), Factory, Abstract Factory, Builder, Prototype
 * RUN:   java code/12_patterns/CreationalPatterns.java
 */
public class CreationalPatterns {

    public static void main(String[] args) throws Exception {
        section("1. Singleton - one instance, even under concurrency");
        Set<Integer> identities = ConcurrentHashMap.newKeySet();
        try (ExecutorService pool = Executors.newFixedThreadPool(16)) {
            for (int i = 0; i < 1000; i++) pool.submit(() -> identities.add(System.identityHashCode(DclSingleton.getInstance())));
        }
        System.out.println("double-checked locking: distinct instances seen by 1000 tasks = " + identities.size());
        System.out.println("holder idiom same object? " + (HolderSingleton.getInstance() == HolderSingleton.getInstance()));
        AppConfig.INSTANCE.set("env", "prod");
        System.out.println("enum singleton value: " + AppConfig.INSTANCE.get("env"));
        System.out.println("eager singleton: " + EagerSingleton.INSTANCE.hello());

        section("2. Breaking a singleton with reflection (and why enums are safer)");
        Constructor<HolderSingleton> ctor = HolderSingleton.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        try {
            ctor.newInstance();
        } catch (java.lang.reflect.InvocationTargetException e) {
            System.out.println("HolderSingleton guard: " + e.getCause().getMessage());
        }
        Constructor<GuardlessSingleton> c2 = GuardlessSingleton.class.getDeclaredConstructor();
        c2.setAccessible(true);
        System.out.println("GuardlessSingleton broken? " + (c2.newInstance() != GuardlessSingleton.getInstance()));
        try {
            Constructor<AppConfig> enumCtor = AppConfig.class.getDeclaredConstructor(String.class, int.class);
            enumCtor.setAccessible(true);
            enumCtor.newInstance("HACK", 1);
        } catch (IllegalArgumentException e) {
            System.out.println("enum via reflection -> " + e.getMessage());
        }

        section("3. Factory - clients ask for a type, never call new on concrete classes");
        for (String channel : List.of("EMAIL", "SMS", "PUSH")) {
            Notification n = NotificationFactory.create(channel);
            n.send("Your OTP is 4821");
        }

        section("4. Abstract Factory - families of related objects");
        for (UiFactory factory : List.of(new LightThemeFactory(), new DarkThemeFactory())) {
            Button b = factory.createButton();
            Checkbox cb = factory.createCheckbox();
            System.out.println(b.render() + " + " + cb.render());
        }

        section("5. Builder - readable construction with optional parts + validation");
        Pizza p1 = Pizza.builder("Large").cheese().olives().crust("thin").build();
        Pizza p2 = Pizza.builder("Small").build();
        System.out.println(p1);
        System.out.println(p2);
        try {
            Pizza.builder("Huge").build();
        } catch (IllegalStateException e) {
            System.out.println("build() validation: " + e.getMessage());
        }
        System.out.println("JDK builder: " + new StringBuilder("a").append("b").insert(0, "<").append(">"));

        section("6. Prototype - copy a configured object instead of rebuilding it");
        ReportTemplate base = new ReportTemplate("Quarterly", new ArrayList<>(List.of("Header", "Footer")));
        ReportTemplate q1 = base.copy();
        q1.sections.add("Q1 numbers");
        System.out.println("base = " + base);
        System.out.println("copy = " + q1 + "  (deep copy: base unaffected)");
    }

    static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}

// ---------- Singletons ----------
final class EagerSingleton {
    static final EagerSingleton INSTANCE = new EagerSingleton();   // created at class load

    private EagerSingleton() { }

    String hello() {
        return "created eagerly when the class loaded";
    }
}

final class DclSingleton {
    private static volatile DclSingleton instance;                 // volatile is essential

    private DclSingleton() { }

    static DclSingleton getInstance() {
        if (instance == null) {
            synchronized (DclSingleton.class) {
                if (instance == null) instance = new DclSingleton();
            }
        }
        return instance;
    }
}

final class HolderSingleton {
    private HolderSingleton() {
        if (Holder.INSTANCE != null) throw new IllegalStateException("use getInstance(), reflection blocked");
    }

    private static class Holder {                                   // loaded only on first getInstance()
        static final HolderSingleton INSTANCE = new HolderSingleton();
    }

    static HolderSingleton getInstance() {
        return Holder.INSTANCE;
    }
}

final class GuardlessSingleton {
    private static final GuardlessSingleton INSTANCE = new GuardlessSingleton();

    private GuardlessSingleton() { }

    static GuardlessSingleton getInstance() {
        return INSTANCE;
    }
}

enum AppConfig {
    INSTANCE;
    private final java.util.Map<String, String> values = new ConcurrentHashMap<>();

    void set(String k, String v) {
        values.put(k, v);
    }

    String get(String k) {
        return values.get(k);
    }
}

// ---------- Factory ----------
interface Notification {
    void send(String msg);
}

class EmailNotification implements Notification {
    public void send(String msg) {
        System.out.println("EMAIL -> " + msg);
    }
}

class SmsNotification implements Notification {
    public void send(String msg) {
        System.out.println("SMS   -> " + msg);
    }
}

class PushNotification implements Notification {
    public void send(String msg) {
        System.out.println("PUSH  -> " + msg);
    }
}

class NotificationFactory {
    static Notification create(String channel) {
        return switch (channel) {
            case "EMAIL" -> new EmailNotification();
            case "SMS" -> new SmsNotification();
            case "PUSH" -> new PushNotification();
            default -> throw new IllegalArgumentException("unknown channel " + channel);
        };
    }
}

// ---------- Abstract Factory ----------
interface Button { String render(); }

interface Checkbox { String render(); }

interface UiFactory {
    Button createButton();

    Checkbox createCheckbox();
}

class LightThemeFactory implements UiFactory {
    public Button createButton() { return () -> "[Light Button]"; }

    public Checkbox createCheckbox() { return () -> "[Light Checkbox]"; }
}

class DarkThemeFactory implements UiFactory {
    public Button createButton() { return () -> "[Dark Button]"; }

    public Checkbox createCheckbox() { return () -> "[Dark Checkbox]"; }
}

// ---------- Builder ----------
final class Pizza {
    private final String size;
    private final boolean cheese, olives;
    private final String crust;

    private Pizza(Builder b) {
        this.size = b.size;
        this.cheese = b.cheese;
        this.olives = b.olives;
        this.crust = b.crust;
    }

    static Builder builder(String size) {
        return new Builder(size);
    }

    @Override
    public String toString() {
        return size + " pizza [crust=" + crust + ", cheese=" + cheese + ", olives=" + olives + "]";
    }

    static final class Builder {
        private final String size;        // required
        private boolean cheese, olives;   // optional
        private String crust = "regular";

        private Builder(String size) {
            this.size = size;
        }

        Builder cheese() {
            cheese = true;
            return this;
        }

        Builder olives() {
            olives = true;
            return this;
        }

        Builder crust(String crust) {
            this.crust = crust;
            return this;
        }

        Pizza build() {
            if (!List.of("Small", "Medium", "Large").contains(size)) throw new IllegalStateException("invalid size " + size);
            return new Pizza(this);
        }
    }
}

// ---------- Prototype ----------
class ReportTemplate {
    final String title;
    final List<String> sections;

    ReportTemplate(String title, List<String> sections) {
        this.title = title;
        this.sections = sections;
    }

    ReportTemplate copy() {
        return new ReportTemplate(title, new ArrayList<>(sections));
    }

    @Override
    public String toString() {
        return title + " " + sections;
    }
}
