/*
 * TOPIC: The tricky corners of polymorphism
 *   - fields and static methods are NOT polymorphic (hiding)
 *   - private methods are not overridden
 *   - covariant return types, access widening
 *   - overloading is chosen at COMPILE time, overriding at RUNTIME
 * RUN:   java code/02_oops/PolymorphismDeepDive.java
 *
 * RULE: "The compiler checks the REFERENCE, the JVM runs the OBJECT" (instance methods only)
 */
public class PolymorphismDeepDive {

    public static void main(String[] args) {
        Parent p = new Child();

        section("1. Instance method -> runtime (object type)");
        System.out.println("p.greet()      = " + p.greet());

        section("2. Field -> compile time (reference type) = FIELD HIDING");
        System.out.println("p.name         = " + p.name);
        System.out.println("((Child)p).name= " + ((Child) p).name);
        System.out.println("p.getName()    = " + p.getName() + "  <- via an overridden method we see Child's field");

        section("3. Static method -> compile time (reference type) = METHOD HIDING");
        System.out.println("p.who()        = " + p.who() + "  (calling static via an instance is legal but misleading)");
        System.out.println("Child.who()    = " + Child.who());

        section("4. Private methods are not overridden");
        p.callSecret();

        section("5. Covariant return type");
        Parent copy = p.copy();
        System.out.println("p.copy() returned a " + copy.getClass().getSimpleName());
        Child childCopy = new Child().copy();   // no cast needed thanks to the covariant return
        System.out.println("Child.copy() static type is Child: " + childCopy.greet());

        section("6. Overloading is resolved at COMPILE time by the REFERENCE type");
        Printer printer = new Printer();
        Parent asParent = new Child();
        Child asChild = new Child();
        printer.print(asParent);   // chooses print(Parent) even though the object is a Child
        printer.print(asChild);    // chooses print(Child)

        section("7. Double dispatch: overriding + overloading together");
        Printer fancy = new FancyPrinter();
        fancy.print(asParent);     // overload print(Parent) at compile time, FancyPrinter's version at runtime
    }

    static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}

class Parent {
    String name = "Parent-field";

    String greet() {
        return "Hello from Parent";
    }

    String getName() {
        return name;
    }

    static String who() {
        return "static Parent.who()";
    }

    private String secret() {
        return "Parent.secret()";
    }

    void callSecret() {
        System.out.println("callSecret() -> " + secret() + "  (private: always Parent's own)");
    }

    protected Parent copy() {
        return new Parent();
    }
}

class Child extends Parent {
    String name = "Child-field";   // hides Parent.name (bad practice, shown for learning)

    @Override
    String greet() {
        return "Hello from Child";
    }

    @Override
    String getName() {
        return name;
    }

    static String who() {          // hides, does NOT override (@Override here would not compile)
        return "static Child.who()";
    }

    private String secret() {      // a brand-new method, unrelated to Parent.secret()
        return "Child.secret()";
    }

    @Override
    public Child copy() {          // covariant return + wider access (protected -> public)
        return new Child();
    }
}

class Printer {
    void print(Parent p) {
        System.out.println("Printer.print(Parent)");
    }

    void print(Child c) {
        System.out.println("Printer.print(Child)");
    }
}

class FancyPrinter extends Printer {
    @Override
    void print(Parent p) {
        System.out.println("FancyPrinter.print(Parent)");
    }
}
