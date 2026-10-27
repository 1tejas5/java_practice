import java.util.List;
import java.util.Map;
import java.util.Optional;

/*
 * TOPIC: Optional - creating, transforming, unwrapping, orElse vs orElseGet, anti-patterns
 * RUN:   java code/07_functional/OptionalDemo.java
 */
public class OptionalDemo {

    record Address(String city) { }

    record User(int id, String name, Address address) {
        Optional<Address> addressOpt() {
            return Optional.ofNullable(address);
        }
    }

    static final Map<Integer, User> DB = Map.of(
            1, new User(1, "Asha", new Address("Pune")),
            2, new User(2, "Ravi", null));

    static Optional<User> findUser(int id) {           // Optional as a RETURN type (the right use)
        return Optional.ofNullable(DB.get(id));
    }

    public static void main(String[] args) {
        section("1. Creating");
        System.out.println("of(\"x\")         = " + Optional.of("x"));
        System.out.println("ofNullable(null)= " + Optional.ofNullable(null));
        System.out.println("empty()         = " + Optional.empty());
        try {
            Optional.of(null);
        } catch (NullPointerException e) {
            System.out.println("of(null)        -> NullPointerException");
        }

        section("2. Transforming safely: map / flatMap / filter");
        for (int id : new int[]{1, 2, 3}) {
            String city = findUser(id)
                    .flatMap(User::addressOpt)           // flatMap: the function returns an Optional
                    .map(Address::city)                  // map: the function returns a plain value
                    .map(String::toUpperCase)
                    .orElse("UNKNOWN");
            System.out.println("user " + id + " city -> " + city);
        }
        System.out.println("filter(name starts with A) on user 2 -> "
                + findUser(2).filter(u -> u.name().startsWith("A")).map(User::name).orElse("filtered out"));

        section("3. Consuming");
        findUser(1).ifPresent(u -> System.out.println("ifPresent: found " + u.name()));
        findUser(9).ifPresentOrElse(
                u -> System.out.println("found " + u.name()),
                () -> System.out.println("ifPresentOrElse: user 9 not found"));

        section("4. Unwrapping");
        System.out.println("orElse            -> " + findUser(9).map(User::name).orElse("guest"));
        System.out.println("orElseGet         -> " + findUser(9).map(User::name).orElseGet(() -> "generated-guest"));
        try {
            findUser(9).orElseThrow(() -> new IllegalArgumentException("no user with id 9"));
        } catch (IllegalArgumentException e) {
            System.out.println("orElseThrow       -> " + e.getMessage());
        }
        try {
            findUser(9).get();
        } catch (java.util.NoSuchElementException e) {
            System.out.println("get() on empty    -> NoSuchElementException (" + e.getMessage() + ")");
        }
        System.out.println("or() alternative  -> " + findUser(9).or(() -> findUser(1)).map(User::name).orElse("-"));
        System.out.println("stream()          -> " + List.of(1, 9, 2).stream().map(OptionalDemo::findUser)
                .flatMap(Optional::stream).map(User::name).toList());

        section("5. TRAP: orElse is EAGER, orElseGet is LAZY");
        Optional<String> present = Optional.of("cached-value");
        System.out.println("orElse    with a value present:");
        String r1 = present.orElse(expensiveDefault());
        System.out.println("   result = " + r1 + "  (but the expensive call STILL happened!)");
        System.out.println("orElseGet with a value present:");
        String r2 = present.orElseGet(OptionalDemo::expensiveDefault);
        System.out.println("   result = " + r2 + "  (no expensive call)");

        section("6. Anti-patterns vs idioms");
        Optional<User> u = findUser(1);
        if (u.isPresent()) {                                       // works but clunky
            System.out.println("anti-pattern isPresent/get: " + u.get().name());
        }
        System.out.println("idiomatic map/orElse     : " + u.map(User::name).orElse("none"));
        System.out.println("Don't: Optional fields/params, Optional<List> (return an empty list), returning null Optionals");
    }

    static String expensiveDefault() {
        System.out.println("   ...expensiveDefault() called (imagine a DB query)");
        return "default";
    }

    static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}
