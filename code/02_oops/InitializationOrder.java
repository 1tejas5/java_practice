/*
 * TOPIC: Exact order of static blocks, instance blocks, field initializers and constructors,
 *        plus the classic trap of calling an overridable method from a constructor.
 * RUN:   java code/02_oops/InitializationOrder.java
 *
 * PREDICT FIRST: write down the order of the lines before running!
 */
public class InitializationOrder {

    public static void main(String[] args) {
        System.out.println("---- main starts ----");
        System.out.println("\n>>> new Child()  (first time: classes get loaded)");
        new Child();
        System.out.println("\n>>> new Child()  (second time: no static blocks!)");
        new Child();

        System.out.println("\n>>> TRAP: overridable method called from the parent constructor");
        new Derived();
    }
}

class Parent {
    static String sField = log("1. Parent static field initializer");

    static {
        log("2. Parent static block");
    }

    String iField = log("5. Parent instance field initializer");

    {
        log("6. Parent instance initializer block");
    }

    Parent() {
        log("7. Parent constructor");
    }

    static String log(String msg) {
        System.out.println("   " + msg);
        return msg;
    }
}

class Child extends Parent {
    static String sField = log("3. Child static field initializer");

    static {
        log("4. Child static block");
    }

    String iField = log("8. Child instance field initializer");

    {
        log("9. Child instance initializer block");
    }

    Child() {
        super();                       // implicit anyway - parent is built first
        log("10. Child constructor");
    }
}

class Base {
    Base() {
        System.out.println("   Base constructor calls describe() ...");
        describe();                    // dynamic dispatch -> Derived.describe() runs!
    }

    void describe() {
        System.out.println("   Base.describe");
    }
}

class Derived extends Base {
    private String name = "Derived";   // NOT assigned yet when Base() runs
    private final int size;

    Derived() {
        super();
        size = 42;
        System.out.println("   Derived constructor done: name=" + name + ", size=" + size);
    }

    @Override
    void describe() {
        System.out.println("   Derived.describe sees name=" + name + ", size=" + size + "  <- not initialized yet!");
    }
}
