import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/*
 * TOPIC: java.lang.Object methods - toString, equals, hashCode (the contract!), clone (shallow vs deep)
 * RUN:   java code/02_oops/ObjectMethodsDemo.java
 */
public class ObjectMethodsDemo {

    public static void main(String[] args) throws Exception {
        section("1. Default toString / equals");
        NoOverrides n1 = new NoOverrides(1), n2 = new NoOverrides(1);
        System.out.println("toString(): " + n1 + "  <- ClassName@hash");
        System.out.println("n1.equals(n2): " + n1.equals(n2) + "  <- default equals is ==");

        section("2. equals WITHOUT hashCode -> HashSet/HashMap break");
        Set<OnlyEquals> broken = new HashSet<>();
        broken.add(new OnlyEquals(7));
        broken.add(new OnlyEquals(7));
        System.out.println("equal? " + new OnlyEquals(7).equals(new OnlyEquals(7)));
        System.out.println("HashSet size = " + broken.size() + "  <- duplicates! (different buckets)");

        section("3. equals AND hashCode -> correct behaviour");
        Set<Employee> good = new HashSet<>();
        good.add(new Employee(1, "Asha"));
        good.add(new Employee(1, "Asha"));
        System.out.println("HashSet size = " + good.size());
        Map<Employee, String> roles = new HashMap<>();
        roles.put(new Employee(2, "Ravi"), "Lead");
        System.out.println("lookup with an equal NEW key -> " + roles.get(new Employee(2, "Ravi")));
        System.out.println("toString(): " + new Employee(3, "Meera"));

        section("4. Mutating a key after inserting it into a HashSet");
        MutableKey key = new MutableKey("A");
        Set<MutableKey> set = new HashSet<>();
        set.add(key);
        key.value = "B";                     // hashCode changes -> object is 'lost' in the wrong bucket
        System.out.println("contains(key) after mutation: " + set.contains(key) + "  <- keys should be immutable!");

        section("5. clone(): shallow vs deep");
        Team original = new Team("Alpha", new ArrayList<>(List.of("Ann", "Bob")));
        Team shallow = original.shallowClone();
        Team deep = original.deepCopy();
        shallow.members.add("Eve");          // shared list!
        System.out.println("original after modifying the SHALLOW copy: " + original.members);
        deep.members.add("Zed");
        System.out.println("original after modifying the DEEP copy:    " + original.members);

        section("6. getClass() vs instanceof");
        Object o = new Employee(9, "X");
        System.out.println("getClass(): " + o.getClass().getName());
        System.out.println("instanceof Object? " + (o instanceof Object));
    }

    static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}

class NoOverrides {
    int id;

    NoOverrides(int id) {
        this.id = id;
    }
}

class OnlyEquals {
    int id;

    OnlyEquals(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof OnlyEquals other && other.id == id;
    }
    // hashCode NOT overridden -> contract violated
}

class Employee {
    private final int id;
    private final String name;

    Employee(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;                       // 1. same reference
        if (!(o instanceof Employee e)) return false;     // 2. type check (also handles null)
        return id == e.id && Objects.equals(name, e.name); // 3. compare significant fields
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);                    // SAME fields as equals
    }

    @Override
    public String toString() {
        return "Employee{id=" + id + ", name='" + name + "'}";
    }
}

class MutableKey {
    String value;

    MutableKey(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof MutableKey m && Objects.equals(m.value, value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}

class Team implements Cloneable {
    String name;
    List<String> members;

    Team(String name, List<String> members) {
        this.name = name;
        this.members = members;
    }

    Team shallowClone() throws CloneNotSupportedException {
        return (Team) super.clone();                      // copies the reference to the SAME list
    }

    Team deepCopy() {                                     // copy-constructor style: preferred over clone()
        return new Team(name, new ArrayList<>(members));
    }
}
