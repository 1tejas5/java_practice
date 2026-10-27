import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/*
 * TOPIC: 4 kinds of nested classes, an immutable class, composition vs inheritance,
 *        and "program to an interface" (loose coupling).
 * RUN:   java code/02_oops/InnerClassesAndImmutability.java
 */
public class InnerClassesAndImmutability {

    public static void main(String[] args) {
        section("1. Static nested class (no outer object needed)");
        Outer.StaticNested sn = new Outer.StaticNested();
        sn.hello();

        section("2. Inner (member) class (needs an outer object)");
        Outer outer = new Outer(10);
        Outer.Inner inner = outer.new Inner();      // unusual syntax: outerObject.new Inner()
        inner.show();

        section("3. Local and anonymous classes");
        outer.localAndAnonymous();

        section("4. Inner class in real life: an Iterator");
        NumberBag bag = new NumberBag();
        bag.add(3);
        bag.add(5);
        for (int n : bag) System.out.print(n + " ");   // for-each works because NumberBag is Iterable
        System.out.println();

        section("5. Immutable class");
        List<String> skills = new ArrayList<>(List.of("Java"));
        ImmutableEmployee e = new ImmutableEmployee("Asha", skills);
        skills.add("Hacking");                       // caller mutates its own list...
        System.out.println(e + "  <- unaffected (defensive copy)");
        try {
            e.skills().add("SQL");
        } catch (UnsupportedOperationException ex) {
            System.out.println("e.skills().add -> UnsupportedOperationException (unmodifiable)");
        }
        ImmutableEmployee renamed = e.withName("Asha K");
        System.out.println(renamed + " is a NEW object; original still " + e.name());

        section("6. Composition + programming to an interface");
        Car electric = new Car(new ElectricEngine());
        Car petrol = new Car(new PetrolEngine());
        electric.drive();
        petrol.drive();
        petrol.swapEngine(new ElectricEngine());     // change behaviour at runtime - impossible with inheritance
        petrol.drive();
    }

    static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}

class Outer {
    private final int x;
    private static final String TAG = "OUTER";

    Outer(int x) {
        this.x = x;
    }

    static class StaticNested {
        void hello() {
            System.out.println("StaticNested sees static TAG=" + TAG + " (but not instance x)");
        }
    }

    class Inner {
        void show() {
            System.out.println("Inner sees private x=" + x + " via hidden ref " + Outer.this.getClass().getSimpleName() + ".this");
        }
    }

    void localAndAnonymous() {
        int bonus = 5;                               // effectively final -> can be captured
        class Local {
            int total() {
                return x + bonus;
            }
        }
        System.out.println("Local class total = " + new Local().total());

        Runnable anonymous = new Runnable() {        // anonymous class implementing Runnable
            @Override
            public void run() {
                System.out.println("Anonymous class run(), bonus=" + bonus);
            }
        };
        anonymous.run();

        Runnable lambda = () -> System.out.println("Lambda does the same in one line, bonus=" + bonus);
        lambda.run();
        // bonus++;                                  // would break both: captured variables must be effectively final
    }
}

class NumberBag implements Iterable<Integer> {
    private final int[] data = new int[10];
    private int size;

    void add(int n) {
        data[size++] = n;
    }

    @Override
    public Iterator<Integer> iterator() {
        return new BagIterator();
    }

    private class BagIterator implements Iterator<Integer> {   // inner class reads the outer's private data
        private int cursor;

        @Override
        public boolean hasNext() {
            return cursor < size;
        }

        @Override
        public Integer next() {
            return data[cursor++];
        }
    }
}

final class ImmutableEmployee {                      // 1. final class
    private final String name;                       // 2. private final fields
    private final List<String> skills;

    ImmutableEmployee(String name, List<String> skills) {
        this.name = name;
        this.skills = List.copyOf(skills);           // 4. defensive copy (and unmodifiable)
    }

    String name() {                                  // 3. getters only
        return name;
    }

    List<String> skills() {
        return skills;
    }

    ImmutableEmployee withName(String newName) {
        return new ImmutableEmployee(newName, skills);
    }

    @Override
    public String toString() {
        return "ImmutableEmployee[" + name + ", " + skills + "]";
    }
}

interface Engine {                                   // abstraction
    String start();
}

class ElectricEngine implements Engine {
    public String start() {
        return "silent hum";
    }
}

class PetrolEngine implements Engine {
    public String start() {
        return "vroom vroom";
    }
}

class Car {
    private Engine engine;                           // HAS-A (composition), typed as the interface

    Car(Engine engine) {
        this.engine = engine;
    }

    void swapEngine(Engine e) {
        this.engine = e;
    }

    void drive() {
        System.out.println("Car drives: " + engine.start());
    }
}
