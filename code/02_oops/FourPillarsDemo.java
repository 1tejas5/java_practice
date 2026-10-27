import java.util.List;

/*
 * TOPIC: The 4 pillars of OOP - "A PIE"
 *        Abstraction (Shape, Payment) . Polymorphism (dynamic dispatch)
 *        Inheritance (Animal -> Dog/Cat) . Encapsulation (BankAccount)
 * RUN:   java code/02_oops/FourPillarsDemo.java
 */
public class FourPillarsDemo {

    public static void main(String[] args) {
        section("E - ENCAPSULATION: data is private, changed only through validated methods");
        BankAccount acc = new BankAccount("ACC-1");
        acc.deposit(1000);
        acc.withdraw(300);
        System.out.println("balance = " + acc.getBalance());
        try {
            acc.withdraw(5000);
        } catch (IllegalStateException e) {
            System.out.println("rejected: " + e.getMessage());
        }
        try {
            acc.deposit(-50);
        } catch (IllegalArgumentException e) {
            System.out.println("rejected: " + e.getMessage());
        }
        // acc.balance = 1_000_000;   // compile error: balance has private access

        section("I - INHERITANCE: Dog IS-A Animal, reuses eat(), overrides sound()");
        Dog dog = new Dog("Tommy");
        dog.eat();                     // inherited
        dog.sound();                   // overridden
        dog.fetch();                   // Dog-only
        dog.parentSound();             // super.sound()

        section("P - POLYMORPHISM: one call, many behaviours (decided at runtime)");
        List<Animal> zoo = List.of(new Dog("Rex"), new Cat("Kitty"), new Animal("Generic"));
        for (Animal a : zoo) {
            a.sound();                 // JVM picks the method of the actual OBJECT
        }
        Animal ref = new Dog("Bruno"); // upcasting (automatic)
        // ref.fetch();                // compile error: the REFERENCE type Animal has no fetch()
        if (ref instanceof Dog d) {    // safe downcast with a pattern
            d.fetch();
        }
        try {
            Animal generic = new Animal("Plain");
            Dog wrong = (Dog) generic; // compiles, fails at runtime
        } catch (ClassCastException e) {
            System.out.println("ClassCastException: an Animal object is not a Dog");
        }

        section("A - ABSTRACTION: callers use WHAT (area, pay), not HOW");
        List<Shape> shapes = List.of(new Circle("red", 1), new Rectangle("blue", 2, 3));
        double total = 0;
        for (Shape s : shapes) {
            s.describe();              // concrete method in the abstract class calls the abstract area()
            total += s.area();
        }
        System.out.printf("total area = %.2f%n", total);
        // new Shape("x");             // compile error: Shape is abstract

        List<Payment> methods = List.of(new CardPayment(), new WalletPayment(), Payment.upi());
        for (Payment p : methods) {
            p.pay(100);
        }
        new CardPayment().receipt(100); // default method from the interface
    }

    static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}

// ---------------- ENCAPSULATION ----------------
class BankAccount {
    private final String id;
    private double balance;            // hidden state

    BankAccount(String id) {
        this.id = id;
    }

    public double getBalance() {       // read access
        return balance;
    }

    public void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("deposit must be positive");
        balance += amount;
    }

    public void withdraw(double amount) {
        if (amount > balance) throw new IllegalStateException("insufficient funds in " + id);
        balance -= amount;
    }
}

// ---------------- INHERITANCE + POLYMORPHISM ----------------
class Animal {
    protected final String name;

    Animal(String name) {
        this.name = name;
    }

    void eat() {
        System.out.println(name + " eats");
    }

    void sound() {
        System.out.println(name + " makes a generic sound");
    }
}

class Dog extends Animal {
    Dog(String name) {
        super(name);                   // parent constructor must run first
    }

    @Override
    void sound() {
        System.out.println(name + " barks: Woof!");
    }

    void fetch() {
        System.out.println(name + " fetches the ball");
    }

    void parentSound() {
        System.out.print("super.sound() -> ");
        super.sound();
    }
}

class Cat extends Animal {
    Cat(String name) {
        super(name);
    }

    @Override
    void sound() {
        System.out.println(name + " meows: Meow!");
    }
}

// ---------------- ABSTRACTION: abstract class ----------------
abstract class Shape {
    private final String color;

    Shape(String color) {              // abstract classes CAN have constructors
        this.color = color;
    }

    abstract double area();            // subclasses MUST implement

    void describe() {                  // template: concrete code calling an abstract method
        System.out.printf("%s %s -> area %.2f%n", color, getClass().getSimpleName(), area());
    }
}

class Circle extends Shape {
    private final double r;

    Circle(String color, double r) {
        super(color);
        this.r = r;
    }

    @Override
    double area() {
        return Math.PI * r * r;
    }
}

class Rectangle extends Shape {
    private final double w, h;

    Rectangle(String color, double w, double h) {
        super(color);
        this.w = w;
        this.h = h;
    }

    @Override
    double area() {
        return w * h;
    }
}

// ---------------- ABSTRACTION: interface ----------------
interface Payment {
    double FEE_RATE = 0.02;            // public static final

    void pay(double amount);           // public abstract

    default void receipt(double amount) {
        System.out.println("receipt: " + amount + " + fee " + fee(amount));
    }

    static Payment upi() {             // static factory - a lambda works: Payment is functional
        return amount -> System.out.println("UPI paid " + amount);
    }

    private double fee(double amount) { // Java 9 private helper
        return amount * FEE_RATE;
    }
}

class CardPayment implements Payment {
    @Override
    public void pay(double amount) {
        System.out.println("Card paid " + amount);
    }
}

class WalletPayment implements Payment {
    @Override
    public void pay(double amount) {
        System.out.println("Wallet paid " + amount);
    }
}
