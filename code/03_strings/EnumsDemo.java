import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

/*
 * TOPIC: Enums - basics, fields + constructor, constant-specific methods, interfaces,
 *        switch, EnumMap/EnumSet, enum singleton
 * RUN:   java code/03_strings/EnumsDemo.java
 */
public class EnumsDemo {

    public static void main(String[] args) {
        section("1. Basics");
        Level l = Level.HIGH;
        System.out.println("name=" + l.name() + ", ordinal=" + l.ordinal() + ", toString=" + l);
        System.out.println("values()=" + Arrays.toString(Level.values()));
        System.out.println("valueOf(\"LOW\")=" + Level.valueOf("LOW"));
        System.out.println("HIGH == Level.valueOf(\"HIGH\") ? " + (l == Level.valueOf("HIGH")) + " (singletons, == is safe)");
        System.out.println("LOW.compareTo(HIGH)=" + Level.LOW.compareTo(Level.HIGH) + " (by ordinal)");
        try {
            Level.valueOf("ULTRA");
        } catch (IllegalArgumentException e) {
            System.out.println("valueOf(\"ULTRA\") -> IllegalArgumentException");
        }

        section("2. Switch expression on an enum (exhaustive, no default)");
        for (Level level : Level.values()) {
            String advice = switch (level) {
                case LOW -> "relax";
                case MEDIUM -> "keep an eye on it";
                case HIGH -> "act now!";
            };
            System.out.println(level + " -> " + advice);
        }

        section("3. Fields, constructor, methods");
        for (Coffee c : Coffee.values()) {
            System.out.printf("%-10s %3d ml  Rs.%d%n", c, c.getMl(), c.price(2));
        }

        section("4. Constant-specific behaviour (abstract method per constant)");
        for (Operation op : Operation.values()) {
            System.out.println("8 " + op.symbol() + " 2 = " + op.apply(8, 2));
        }

        section("5. Enum implementing an interface");
        Describable d = Season.WINTER;
        System.out.println(d.describe());

        section("6. EnumSet and EnumMap");
        EnumSet<Day> weekend = EnumSet.of(Day.SAT, Day.SUN);
        EnumSet<Day> weekdays = EnumSet.complementOf(weekend);
        System.out.println("weekend  = " + weekend);
        System.out.println("weekdays = " + weekdays);
        System.out.println("range    = " + EnumSet.range(Day.TUE, Day.THU));
        Map<Day, String> plan = new EnumMap<>(Day.class);
        plan.put(Day.FRI, "Movie");
        plan.put(Day.MON, "Gym");
        System.out.println("EnumMap keeps declaration order: " + plan);

        section("7. Enum singleton");
        AppConfig.INSTANCE.set("theme", "dark");
        System.out.println("theme = " + AppConfig.INSTANCE.get("theme"));
    }

    static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}

enum Level { LOW, MEDIUM, HIGH }

enum Day { MON, TUE, WED, THU, FRI, SAT, SUN }

enum Coffee {
    ESPRESSO(30, 120), LATTE(240, 180), CAPPUCCINO(180, 170);

    private final int ml;
    private final int pricePerCup;

    Coffee(int ml, int pricePerCup) {         // implicitly private
        this.ml = ml;
        this.pricePerCup = pricePerCup;
    }

    int getMl() {
        return ml;
    }

    int price(int cups) {
        return pricePerCup * cups;
    }
}

enum Operation {
    PLUS("+") {
        int apply(int a, int b) { return a + b; }
    },
    MINUS("-") {
        int apply(int a, int b) { return a - b; }
    },
    TIMES("*") {
        int apply(int a, int b) { return a * b; }
    },
    DIVIDE("/") {
        int apply(int a, int b) { return a / b; }
    };

    private final String symbol;

    Operation(String symbol) {
        this.symbol = symbol;
    }

    String symbol() {
        return symbol;
    }

    abstract int apply(int a, int b);
}

interface Describable {
    String describe();
}

enum Season implements Describable {
    SUMMER, WINTER;

    @Override
    public String describe() {
        return name().charAt(0) + name().substring(1).toLowerCase() + " is season #" + (ordinal() + 1);
    }
}

enum AppConfig {
    INSTANCE;                                 // thread-safe, serialization-safe singleton

    private final Map<String, String> settings = new java.util.HashMap<>();

    void set(String k, String v) {
        settings.put(k, v);
    }

    String get(String k) {
        return settings.get(k);
    }
}
