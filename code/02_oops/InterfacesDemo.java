/*
 * TOPIC: Interfaces in depth - multiple implementation, default/static/private methods,
 *        the diamond problem with defaults, functional & marker interfaces,
 *        and abstract class vs interface side by side.
 * RUN:   java code/02_oops/InterfacesDemo.java
 */
public class InterfacesDemo {

    public static void main(String[] args) {
        section("1. A class can implement MANY interfaces (CAN-DO capabilities)");
        Duck duck = new Duck("Donald");
        duck.fly();
        duck.swim();
        Flyable f = duck;               // the same object seen through different contracts
        Swimmable s = duck;
        System.out.println("Flyable? " + (f != null) + ", Swimmable? " + (s != null));

        section("2. Diamond problem with default methods -> must override and choose");
        new SmartPhone().start();

        section("3. Static interface methods are called on the INTERFACE");
        System.out.println(Flyable.describe());
        // duck.describe();             // compile error: static interface methods are not inherited

        section("4. Functional interface -> lambda");
        Calculator add = (a, b) -> a + b;
        Calculator mul = (a, b) -> a * b;
        System.out.println("add(3,4)=" + add.compute(3, 4) + ", mul(3,4)=" + mul.compute(3, 4));
        System.out.println("add.twice(3,4)=" + add.twice(3, 4) + "  (default method on a functional interface)");

        section("5. Marker interface");
        Object doc = new Document();
        if (doc instanceof Auditable) {
            System.out.println("Document is tagged Auditable -> audit log written");
        }

        section("6. Abstract class (IS-A + shared state) + interface (CAN-DO)");
        Employee dev = new Developer("Riya", 120_000);
        dev.work();
        System.out.println(dev.summary());
        if (dev instanceof Promotable p) {
            p.promote();
            System.out.println(dev.summary());
        }
    }

    static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}

interface Flyable {
    void fly();

    static String describe() {
        return "Flyable: anything that can fly";
    }
}

interface Swimmable {
    void swim();
}

class Duck implements Flyable, Swimmable {
    private final String name;

    Duck(String name) {
        this.name = name;
    }

    @Override
    public void fly() {
        System.out.println(name + " flies");
    }

    @Override
    public void swim() {
        System.out.println(name + " swims");
    }
}

interface Camera {
    default void start() {
        System.out.println("Camera starting");
    }
}

interface MusicPlayer {
    default void start() {
        System.out.println("MusicPlayer starting");
    }
}

class SmartPhone implements Camera, MusicPlayer {
    @Override
    public void start() {              // without this override: compile error (ambiguous)
        Camera.super.start();          // choose one, or both, or neither
        MusicPlayer.super.start();
        System.out.println("SmartPhone ready");
    }
}

@FunctionalInterface                   // compiler enforces exactly ONE abstract method
interface Calculator {
    int compute(int a, int b);

    default int twice(int a, int b) {  // default methods don't count
        return 2 * compute(a, b);
    }
}

interface Auditable { }                // marker: no methods, just a tag

class Document implements Auditable { }

abstract class Employee {
    protected final String name;       // shared STATE lives in the abstract class
    protected double salary;

    Employee(String name, double salary) {
        this.name = name;
        this.salary = salary;
    }

    abstract void work();

    String summary() {
        return name + " earns " + salary;
    }
}

interface Promotable {
    void promote();
}

class Developer extends Employee implements Promotable {
    Developer(String name, double salary) {
        super(name, salary);
    }

    @Override
    void work() {
        System.out.println(name + " writes code");
    }

    @Override
    public void promote() {
        salary *= 1.2;
        System.out.println(name + " promoted!");
    }
}
