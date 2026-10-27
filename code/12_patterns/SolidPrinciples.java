import java.util.ArrayList;
import java.util.List;

/*
 * TOPIC: SOLID - each principle shown as a GOOD design (the bad version is described in comments)
 * RUN:   java code/12_patterns/SolidPrinciples.java
 */
public class SolidPrinciples {

    public static void main(String[] args) {
        section("S - Single Responsibility: Invoice computes, Printer prints, Repository saves");
        Invoice invoice = new Invoice(List.of(new Line("Keyboard", 1500, 2), new Line("Mouse", 700, 1)));
        new InvoicePrinter().print(invoice);
        new InvoiceRepository().save(invoice);

        section("O - Open/Closed: add Triangle without touching AreaCalculator");
        List<Shape> shapes = List.of(new Circle(1), new Square(2), new Triangle(3, 4));
        System.out.printf("total area = %.2f%n", AreaCalculator.total(shapes));

        section("L - Liskov: Penguin is a Bird, but only Sparrow is a FlyingBird");
        List<Bird> birds = List.of(new Sparrow(), new Penguin());
        for (Bird b : birds) {
            System.out.print(b.name() + " eats. ");
            if (b instanceof FlyingBird f) f.fly();
            else System.out.println("(does not pretend to fly - no UnsupportedOperationException)");
        }

        section("I - Interface Segregation: Robot implements only what it needs");
        List<Workable> workers = List.of(new Human(), new Robot());
        for (Workable w : workers) {
            w.work();
            if (w instanceof Eatable e) e.eat();
        }

        section("D - Dependency Inversion: OrderService depends on an interface");
        OrderService prod = new OrderService(new SqlOrderRepository());
        prod.place("ORD-1");
        InMemoryOrderRepository fake = new InMemoryOrderRepository();
        OrderService test = new OrderService(fake);           // swap in a fake for unit tests
        test.place("ORD-TEST");
        System.out.println("fake repo now holds: " + fake.saved);
    }

    static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}

// ---------- S ----------
// BAD: class Invoice { total(); print(); saveToDb(); }  -> 3 reasons to change
record Line(String item, double price, int qty) { }

record Invoice(List<Line> lines) {
    double total() {
        return lines.stream().mapToDouble(l -> l.price() * l.qty()).sum();
    }
}

class InvoicePrinter {
    void print(Invoice inv) {
        inv.lines().forEach(l -> System.out.printf("  %-10s %d x %.0f%n", l.item(), l.qty(), l.price()));
        System.out.printf("  TOTAL      %.0f%n", inv.total());
    }
}

class InvoiceRepository {
    void save(Invoice inv) {
        System.out.println("  saved invoice with " + inv.lines().size() + " lines");
    }
}

// ---------- O ----------
// BAD: double area(Object s) { if (s instanceof Circle) ... else if (s instanceof Square) ... }
interface Shape {
    double area();
}

record Circle(double r) implements Shape {
    public double area() { return Math.PI * r * r; }
}

record Square(double side) implements Shape {
    public double area() { return side * side; }
}

record Triangle(double base, double height) implements Shape {   // NEW shape, zero edits elsewhere
    public double area() { return 0.5 * base * height; }
}

class AreaCalculator {
    static double total(List<Shape> shapes) {
        return shapes.stream().mapToDouble(Shape::area).sum();
    }
}

// ---------- L ----------
// BAD: class Penguin extends Bird { void fly() { throw new UnsupportedOperationException(); } }
interface Bird {
    String name();
}

interface FlyingBird extends Bird {
    void fly();
}

class Sparrow implements FlyingBird {
    public String name() { return "Sparrow"; }

    public void fly() { System.out.println("Sparrow flies."); }
}

class Penguin implements Bird {
    public String name() { return "Penguin"; }
}

// ---------- I ----------
// BAD: interface Worker { work(); eat(); }  -> Robot forced to implement eat()
interface Workable {
    void work();
}

interface Eatable {
    void eat();
}

class Human implements Workable, Eatable {
    public void work() { System.out.print("Human works. "); }

    public void eat() { System.out.println("Human eats lunch."); }
}

class Robot implements Workable {
    public void work() { System.out.println("Robot works 24x7."); }
}

// ---------- D ----------
// BAD: class OrderService { private final MySqlRepo repo = new MySqlRepo(); }
interface OrderRepository {
    void save(String orderId);
}

class SqlOrderRepository implements OrderRepository {
    public void save(String orderId) { System.out.println("  INSERT INTO orders VALUES ('" + orderId + "')"); }
}

class InMemoryOrderRepository implements OrderRepository {
    final List<String> saved = new ArrayList<>();

    public void save(String orderId) { saved.add(orderId); }
}

class OrderService {
    private final OrderRepository repo;          // abstraction

    OrderService(OrderRepository repo) {         // constructor injection
        this.repo = repo;
    }

    void place(String orderId) {
        System.out.println("placing " + orderId + " via " + repo.getClass().getSimpleName());
        repo.save(orderId);
    }
}
