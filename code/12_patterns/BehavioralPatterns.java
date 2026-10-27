import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/*
 * TOPIC: Behavioral patterns - Strategy, Observer, Template Method, Command (undo), Chain of Responsibility, State
 * RUN:   java code/12_patterns/BehavioralPatterns.java
 */
public class BehavioralPatterns {

    public static void main(String[] args) {
        section("1. Strategy - swap algorithms at runtime (lambdas make it tiny)");
        Checkout checkout = new Checkout(amount -> amount);
        System.out.println("no discount      : " + checkout.total(1500));
        checkout.setStrategy(amount -> amount * 0.9);
        System.out.println("festive 10% off  : " + checkout.total(1500));
        checkout.setStrategy(new FlatDiscount(200, 1000));
        System.out.println("flat 200 over 1k : " + checkout.total(1500));

        section("2. Observer - subscribers are notified on every upload");
        Channel channel = new Channel("JavaWithAsha");
        Subscriber ravi = video -> System.out.println("  Ravi got a notification: " + video);
        channel.subscribe(ravi);
        channel.subscribe(video -> System.out.println("  Meera adds to watch-later: " + video));
        channel.upload("Streams in 10 minutes");
        channel.unsubscribe(ravi);
        channel.upload("Virtual threads explained");

        section("3. Template Method - fixed algorithm, customizable steps");
        new CsvExporter().export();
        new JsonExporter().export();

        section("4. Command - requests as objects, with undo");
        StringBuilder doc = new StringBuilder();
        Editor editor = new Editor();
        editor.run(new AppendCommand(doc, "Hello"));
        editor.run(new AppendCommand(doc, ", World"));
        editor.run(new AppendCommand(doc, "!!!"));
        System.out.println("doc = '" + doc + "'");
        editor.undo();
        System.out.println("after undo  = '" + doc + "'");
        editor.undo();
        System.out.println("after undo  = '" + doc + "'");

        section("5. Chain of Responsibility - expense approvals");
        Approver chain = new TeamLead();
        chain.linkWith(new Manager()).linkWith(new Director());
        for (double amount : new double[]{5_000, 75_000, 4_50_000, 20_00_000}) chain.approve(amount);

        section("6. State - a traffic light changes behaviour as its state changes");
        TrafficLight light = new TrafficLight();
        for (int i = 0; i < 4; i++) {
            System.out.println("  light is " + light.state().name() + " -> " + light.state().instruction());
            light.next();
        }
    }

    static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}

// ---------- Strategy ----------
@FunctionalInterface
interface DiscountStrategy {
    double apply(double amount);
}

record FlatDiscount(double off, double minimum) implements DiscountStrategy {
    public double apply(double amount) {
        return amount >= minimum ? amount - off : amount;
    }
}

class Checkout {
    private DiscountStrategy strategy;

    Checkout(DiscountStrategy strategy) {
        this.strategy = strategy;
    }

    void setStrategy(DiscountStrategy strategy) {
        this.strategy = strategy;
    }

    double total(double amount) {
        return strategy.apply(amount);
    }
}

// ---------- Observer ----------
@FunctionalInterface
interface Subscriber {
    void update(String video);
}

class Channel {
    private final String name;
    private final List<Subscriber> subscribers = new ArrayList<>();

    Channel(String name) {
        this.name = name;
    }

    void subscribe(Subscriber s) {
        subscribers.add(s);
    }

    void unsubscribe(Subscriber s) {
        subscribers.remove(s);
    }

    void upload(String video) {
        System.out.println(name + " uploaded '" + video + "' -> notifying " + subscribers.size() + " subscriber(s)");
        subscribers.forEach(s -> s.update(video));
    }
}

// ---------- Template Method ----------
abstract class DataExporter {
    final void export() {                       // final: the skeleton can't be changed
        System.out.println(getClass().getSimpleName() + ":");
        List<String> rows = fetch();
        String out = format(rows);
        System.out.println(out);
        afterExport();
    }

    private List<String> fetch() {
        return List.of("Asha,IT", "Ravi,HR");
    }

    protected abstract String format(List<String> rows);   // step chosen by subclasses

    protected void afterExport() { }                        // optional hook
}

class CsvExporter extends DataExporter {
    protected String format(List<String> rows) {
        return "  name,dept\n  " + String.join("\n  ", rows);
    }
}

class JsonExporter extends DataExporter {
    protected String format(List<String> rows) {
        StringBuilder sb = new StringBuilder("  [");
        for (String r : rows) {
            String[] p = r.split(",");
            if (sb.length() > 3) sb.append(", ");
            sb.append("{\"name\":\"").append(p[0]).append("\",\"dept\":\"").append(p[1]).append("\"}");
        }
        return sb.append("]").toString();
    }

    @Override
    protected void afterExport() {
        System.out.println("  (hook: JSON validated)");
    }
}

// ---------- Command ----------
interface Command {
    void execute();

    void undo();
}

record AppendCommand(StringBuilder doc, String text) implements Command {
    public void execute() {
        doc.append(text);
    }

    public void undo() {
        doc.setLength(doc.length() - text.length());
    }
}

class Editor {
    private final Deque<Command> history = new ArrayDeque<>();

    void run(Command c) {
        c.execute();
        history.push(c);
    }

    void undo() {
        if (!history.isEmpty()) history.pop().undo();
    }
}

// ---------- Chain of Responsibility ----------
abstract class Approver {
    private Approver next;

    Approver linkWith(Approver next) {
        this.next = next;
        return next;
    }

    void approve(double amount) {
        if (canApprove(amount)) {
            System.out.printf("  %-9s approved Rs.%,.0f%n", getClass().getSimpleName(), amount);
        } else if (next != null) {
            next.approve(amount);
        } else {
            System.out.printf("  REJECTED  Rs.%,.0f (above every limit)%n", amount);
        }
    }

    abstract boolean canApprove(double amount);
}

class TeamLead extends Approver {
    boolean canApprove(double amount) { return amount <= 10_000; }
}

class Manager extends Approver {
    boolean canApprove(double amount) { return amount <= 1_00_000; }
}

class Director extends Approver {
    boolean canApprove(double amount) { return amount <= 10_00_000; }
}

// ---------- State (enum-based state machine) ----------
enum LightState {
    RED { LightState next() { return GREEN; } String instruction() { return "STOP"; } },
    GREEN { LightState next() { return YELLOW; } String instruction() { return "GO"; } },
    YELLOW { LightState next() { return RED; } String instruction() { return "SLOW DOWN"; } };

    abstract LightState next();

    abstract String instruction();
}

class TrafficLight {
    private LightState state = LightState.RED;

    LightState state() {
        return state;
    }

    void next() {
        state = state.next();   // the STATE decides the transition, not a big if/else
    }
}
