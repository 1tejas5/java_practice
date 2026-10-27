/*
 * TOPIC: Class vs object, references, constructors, constructor chaining, this, static
 * RUN:   java code/02_oops/ClassesAndObjects.java
 */
public class ClassesAndObjects {

    public static void main(String[] args) {
        section("1. Objects and references");
        Car c1 = new Car("Tata");
        Car c2 = new Car("BMW");
        Car c3 = c1;                         // NOT a new object - another reference to c1's car
        c3.accelerate(50);
        System.out.println("c1 = " + c1 + "   (changed through c3!)");
        System.out.println("c2 = " + c2);
        System.out.println("c1 == c3 ? " + (c1 == c3) + "  (same identity)");
        System.out.println("c1 == c2 ? " + (c1 == c2));

        section("2. Constructors and chaining with this(...)");
        Student s1 = new Student();
        Student s2 = new Student("Asha", 21);
        Student s3 = new Student(s2);        // copy constructor
        System.out.println(s1);
        System.out.println(s2);
        System.out.println(s3 + "  <- copy, different object: " + (s2 != s3));

        section("3. this for fluent APIs (return this)");
        Pizza pizza = new Pizza().size("Large").addTopping("Cheese").addTopping("Olives");
        System.out.println(pizza);

        section("4. static: one copy per CLASS");
        System.out.println("Before any object, Counter.count = " + Counter.getCount());
        Counter a = new Counter();
        Counter b = new Counter();
        Counter c = new Counter();
        System.out.println("ids: " + a.id + ", " + b.id + ", " + c.id);
        System.out.println("Counter.count = " + Counter.getCount() + "  (shared by all objects)");
        System.out.println("MathUtil.square(7) = " + MathUtil.square(7) + "  (no object needed)");
    }

    static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}

class Car {
    String brand;   // state
    int speed;

    Car(String brand) {               // constructor
        this.brand = brand;           // this.brand = field, brand = parameter
    }

    void accelerate(int by) {         // behaviour
        speed += by;
    }

    @Override
    public String toString() {
        return "Car[" + brand + ", speed=" + speed + "]";
    }
}

class Student {
    private final String name;
    private final int age;

    Student() {
        this("Unknown", 18);          // must be the FIRST statement
        System.out.println("  (no-arg constructor finished after delegating)");
    }

    Student(String name, int age) {
        this.name = name;
        this.age = age;
    }

    Student(Student other) {
        this(other.name, other.age);
    }

    @Override
    public String toString() {
        return "Student[" + name + ", " + age + "]";
    }
}

class Pizza {
    private String size = "Medium";
    private final StringBuilder toppings = new StringBuilder();

    Pizza size(String size) {
        this.size = size;
        return this;                  // returning this enables chaining
    }

    Pizza addTopping(String t) {
        if (!toppings.isEmpty()) toppings.append(", ");
        toppings.append(t);
        return this;
    }

    @Override
    public String toString() {
        return size + " pizza with " + toppings;
    }
}

class Counter {
    private static int count = 0;     // shared
    final int id;                     // per object

    static {
        System.out.println("  [static block] Counter class loaded (runs once)");
    }

    Counter() {
        id = ++count;
    }

    static int getCount() {
        // return id;                 // compile error: no 'this' in a static context
        return count;
    }
}

final class MathUtil {
    private MathUtil() { }            // private constructor: a utility class can't be instantiated

    static int square(int x) {
        return x * x;
    }
}
