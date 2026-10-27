import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/*
 * TOPIC: Generic classes, interfaces, methods, bounded types, wildcards (PECS), invariance, type erasure
 * RUN:   java code/05_generics/GenericsDemo.java
 */
public class GenericsDemo {

    public static void main(String[] args) {
        section("1. Generic class");
        Box<String> stringBox = new Box<>("Java");
        Box<Integer> intBox = new Box<>(21);
        String s = stringBox.get();                 // no cast
        int n = intBox.get();
        System.out.println(stringBox + " and " + intBox + " -> " + s + ", " + n);
        // stringBox.set(42);                        // compile error: type safety

        section("2. Multiple type parameters + generic interface");
        Pair<String, Integer> p = new Pair<>("age", 30);
        System.out.println(p.key() + " = " + p.value());
        Repository<Product, Integer> repo = new InMemoryRepository<>();
        repo.save(1, new Product("Laptop", 55000));
        repo.save(2, new Product("Mouse", 700));
        System.out.println("findById(2) = " + repo.findById(2).map(Product::name).orElse("none"));
        System.out.println("findById(9) = " + repo.findById(9).map(Product::name).orElse("none"));

        section("3. Generic methods (type inferred)");
        System.out.println("max(3, 9, 2)        = " + max(List.of(3, 9, 2)));
        System.out.println("max(pear, apple)    = " + max(List.of("pear", "apple", "fig")));
        System.out.println("swap                = " + swap(new Pair<>("left", 1)));
        Integer[] arr = {1, 2, 3};
        System.out.println("firstOf(Integer[])  = " + firstOf(arr));

        section("4. Bounded type: <N extends Number>");
        System.out.println("sum(ints)    = " + sum(List.of(1, 2, 3)));
        System.out.println("sum(doubles) = " + sum(List.of(1.5, 2.5)));
        // sum(List.of("a"));                        // compile error: String is not a Number

        section("5. Invariance vs array covariance");
        Integer[] ints = {1, 2};
        Number[] numsArr = ints;                     // arrays are covariant (compiles)
        try {
            numsArr[0] = 3.14;
        } catch (ArrayStoreException e) {
            System.out.println("arrays: ArrayStoreException at RUNTIME");
        }
        List<Integer> intList = new ArrayList<>(List.of(1, 2));
        // List<Number> numList = intList;           // compile error: generics are invariant
        System.out.println("generics: List<Integer> -> List<Number> is a COMPILE error (safer)");

        section("6. Wildcards - PECS");
        List<Integer> integers = List.of(1, 2, 3);
        List<Double> doubles = List.of(0.5, 0.25);
        System.out.println("total(? extends Number) of ints    = " + total(integers));
        System.out.println("total(? extends Number) of doubles = " + total(doubles));
        List<Number> numberSink = new ArrayList<>();
        List<Object> objectSink = new ArrayList<>();
        fillWithInts(numberSink);
        fillWithInts(objectSink);
        System.out.println("fillWithInts(? super Integer) into List<Number> = " + numberSink);
        System.out.println("fillWithInts(? super Integer) into List<Object> = " + objectSink);
        List<Number> dest = new ArrayList<>();
        copy(dest, integers);
        copy(dest, doubles);
        System.out.println("copy(dest super, src extends) = " + dest);
        printAny(List.of("x", "y"));
        printAny(List.of(true));

        section("7. Type erasure");
        List<String> a = new ArrayList<>();
        List<Integer> b = new ArrayList<>();
        System.out.println("List<String>.class == List<Integer>.class ? " + (a.getClass() == b.getClass()));
        Object obj = a;
        System.out.println("obj instanceof List<?> ? " + (obj instanceof List<?>) + " (List<String> not allowed)");
        // Creating T: impossible with new T(), so pass a Supplier
        List<String> created = create(ArrayList::new);
        created.add("made by Supplier");
        System.out.println(created);
        Map<String, Integer> map = create(HashMap::new);
        System.out.println("created " + map.getClass().getSimpleName());

        section("8. Raw types (avoid)");
        rawTypeDemo();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static void rawTypeDemo() {
        List raw = new ArrayList<String>();
        raw.add(42);                                 // compiles (only a warning): no type checking
        System.out.println("raw list happily holds: " + raw);
    }

    // ---------- generic methods ----------
    static <T extends Comparable<T>> T max(List<T> list) {
        T best = list.get(0);
        for (T t : list) if (t.compareTo(best) > 0) best = t;
        return best;
    }

    static <K, V> Pair<V, K> swap(Pair<K, V> p) {
        return new Pair<>(p.value(), p.key());
    }

    static <T> T firstOf(T[] array) {
        return array[0];
    }

    static <N extends Number> double sum(List<N> nums) {
        double total = 0;
        for (N num : nums) total += num.doubleValue();
        return total;
    }

    static double total(List<? extends Number> producer) {     // READ only
        double sum = 0;
        for (Number num : producer) sum += num.doubleValue();
        // producer.add(1);                                    // compile error
        return sum;
    }

    static void fillWithInts(List<? super Integer> consumer) { // WRITE Integers
        for (int i = 1; i <= 3; i++) consumer.add(i);
        Object first = consumer.get(0);                        // reading gives only Object
    }

    static <T> void copy(List<? super T> dest, List<? extends T> src) {
        for (T item : src) dest.add(item);
    }

    static void printAny(List<?> list) {                       // unknown type: read as Object
        System.out.println("List<?> of size " + list.size() + ": " + list);
    }

    static <T> T create(Supplier<T> factory) {
        return factory.get();
    }

    static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}

class Box<T> {
    private T value;

    Box(T value) {
        this.value = value;
    }

    T get() {
        return value;
    }

    void set(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Box(" + value + ")";
    }
}

record Pair<K, V>(K key, V value) { }                           // generic record

record Product(String name, double price) { }

interface Repository<T, ID> {
    void save(ID id, T entity);

    Optional<T> findById(ID id);
}

class InMemoryRepository<T, ID> implements Repository<T, ID> {
    private final Map<ID, T> store = new HashMap<>();

    @Override
    public void save(ID id, T entity) {
        store.put(id, entity);
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(store.get(id));
    }
}
