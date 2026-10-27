import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/*
 * TOPIC: Structural patterns - Adapter, Decorator, Proxy (manual + JDK dynamic proxy), Facade, Composite
 * RUN:   java code/12_patterns/StructuralPatterns.java
 */
public class StructuralPatterns {

    public static void main(String[] args) {
        section("1. Adapter - legacy API behind the interface our app expects");
        PaymentGateway gateway = new PayPalAdapter(new LegacyPayPal());
        gateway.pay(499.0);

        section("2. Decorator - stack behaviours at runtime");
        Coffee order = new Caramel(new Milk(new Milk(new Espresso())));
        System.out.println(order.desc() + " = Rs." + order.cost());
        Coffee plain = new Espresso();
        System.out.println(plain.desc() + " = Rs." + plain.cost());

        section("3. Proxy - lazy loading (virtual proxy)");
        Image img = new LazyImageProxy("holiday.png");
        System.out.println("proxy created, real image NOT loaded yet");
        img.display();
        img.display();                       // second call reuses the loaded image

        section("4. JDK dynamic proxy - logging around ANY interface (how Spring AOP works)");
        Calculator real = new SimpleCalculator();
        Calculator logged = (Calculator) Proxy.newProxyInstance(
                Calculator.class.getClassLoader(),
                new Class<?>[]{Calculator.class},
                (proxy, method, methodArgs) -> {
                    long start = System.nanoTime();
                    Object result = method.invoke(real, methodArgs);
                    System.out.printf("  [LOG] %s%s -> %s (%d us)%n", method.getName(),
                            java.util.Arrays.toString(methodArgs), result, (System.nanoTime() - start) / 1000);
                    return result;
                });
        logged.add(2, 3);
        logged.multiply(4, 5);

        section("5. Facade - one call hides a whole subsystem");
        new OrderFacade().placeOrder("Java Book", 799);

        section("6. Composite - files and folders treated uniformly");
        Folder root = new Folder("project");
        Folder src = new Folder("src");
        src.add(new FileItem("Main.java", 1200));
        src.add(new FileItem("Utils.java", 800));
        root.add(src);
        root.add(new FileItem("README.md", 300));
        root.print("");
        System.out.println("total size = " + root.size() + " bytes");
    }

    static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}

// ---------- Adapter ----------
interface PaymentGateway {
    void pay(double rupees);
}

class LegacyPayPal {
    void makePayment(String currency, double amount) {
        System.out.println("LegacyPayPal charged " + amount + " " + currency);
    }
}

class PayPalAdapter implements PaymentGateway {
    private final LegacyPayPal legacy;

    PayPalAdapter(LegacyPayPal legacy) {
        this.legacy = legacy;
    }

    @Override
    public void pay(double rupees) {
        legacy.makePayment("INR", rupees);   // translate the call
    }
}

// ---------- Decorator ----------
interface Coffee {
    double cost();

    String desc();
}

class Espresso implements Coffee {
    public double cost() { return 100; }

    public String desc() { return "Espresso"; }
}

abstract class CoffeeDecorator implements Coffee {
    protected final Coffee inner;

    CoffeeDecorator(Coffee inner) {
        this.inner = inner;
    }
}

class Milk extends CoffeeDecorator {
    Milk(Coffee c) { super(c); }

    public double cost() { return inner.cost() + 30; }

    public String desc() { return inner.desc() + " + Milk"; }
}

class Caramel extends CoffeeDecorator {
    Caramel(Coffee c) { super(c); }

    public double cost() { return inner.cost() + 45; }

    public String desc() { return inner.desc() + " + Caramel"; }
}

// ---------- Proxy ----------
interface Image {
    void display();
}

class RealImage implements Image {
    private final String file;

    RealImage(String file) {
        this.file = file;
        System.out.println("  loading " + file + " from disk (expensive!)");
    }

    public void display() {
        System.out.println("  displaying " + file);
    }
}

class LazyImageProxy implements Image {
    private final String file;
    private RealImage real;

    LazyImageProxy(String file) {
        this.file = file;
    }

    public void display() {
        if (real == null) real = new RealImage(file);
        real.display();
    }
}

interface Calculator {
    int add(int a, int b);

    int multiply(int a, int b);
}

class SimpleCalculator implements Calculator {
    public int add(int a, int b) { return a + b; }

    public int multiply(int a, int b) { return a * b; }
}

// ---------- Facade ----------
class Inventory {
    void reserve(String item) { System.out.println("  inventory: reserved " + item); }
}

class PaymentService {
    void charge(double amount) { System.out.println("  payment: charged Rs." + amount); }
}

class Shipping {
    void schedule(String item) { System.out.println("  shipping: courier booked for " + item); }
}

class OrderFacade {
    private final Inventory inventory = new Inventory();
    private final PaymentService payment = new PaymentService();
    private final Shipping shipping = new Shipping();

    void placeOrder(String item, double amount) {
        System.out.println("placeOrder(" + item + ")");
        inventory.reserve(item);
        payment.charge(amount);
        shipping.schedule(item);
    }
}

// ---------- Composite ----------
interface FileSystemItem {
    long size();

    void print(String indent);
}

record FileItem(String name, long bytes) implements FileSystemItem {
    public long size() { return bytes; }

    public void print(String indent) { System.out.println(indent + "- " + name + " (" + bytes + ")"); }
}

class Folder implements FileSystemItem {
    private final String name;
    private final List<FileSystemItem> children = new ArrayList<>();

    Folder(String name) {
        this.name = name;
    }

    void add(FileSystemItem item) {
        children.add(item);
    }

    public long size() {
        return children.stream().mapToLong(FileSystemItem::size).sum();   // recursion through the tree
    }

    public void print(String indent) {
        System.out.println(indent + "+ " + name + "/");
        for (FileSystemItem c : children) c.print(indent + "   ");
    }
}
