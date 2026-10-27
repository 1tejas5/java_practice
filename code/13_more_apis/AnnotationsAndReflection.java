import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * TOPIC: Custom annotations + reflection, by building 3 mini-frameworks:
 *   1. a JUnit-like test runner          (@Test)
 *   2. a bean validator                   (@NotBlank, @Min)
 *   3. a Spring-like dependency injector  (@Component, @Inject)
 *   plus class inspection, private field/method access.
 * RUN:   java code/13_more_apis/AnnotationsAndReflection.java
 */
public class AnnotationsAndReflection {

    public static void main(String[] args) throws Exception {
        section("1. Inspecting a class with reflection");
        Class<?> clazz = Employee.class;
        System.out.println("class      : " + clazz.getSimpleName() + " extends " + clazz.getSuperclass().getSimpleName());
        System.out.println("modifiers  : " + Modifier.toString(clazz.getModifiers()));
        for (Field f : clazz.getDeclaredFields()) {
            System.out.println("field      : " + Modifier.toString(f.getModifiers()) + " " + f.getType().getSimpleName() + " " + f.getName());
        }
        Arrays.stream(clazz.getDeclaredMethods()).sorted(Comparator.comparing(Method::getName))
                .forEach(m -> System.out.println("method     : " + Modifier.toString(m.getModifiers()) + " " + m.getName()
                        + Arrays.toString(m.getParameterTypes())));
        System.out.println("Class.forName(\"java.util.ArrayList\") -> " + Class.forName("java.util.ArrayList").getName());

        section("2. Creating objects and breaking encapsulation (use responsibly!)");
        Constructor<Employee> ctor = Employee.class.getDeclaredConstructor(String.class, double.class);
        Employee emp = ctor.newInstance("Asha", 50_000);
        System.out.println("created via reflection: " + emp);
        Field salary = Employee.class.getDeclaredField("salary");
        salary.setAccessible(true);                       // bypass private
        salary.set(emp, 99_000.0);
        System.out.println("private field changed : " + emp);
        Method bonus = Employee.class.getDeclaredMethod("secretBonus", int.class);
        bonus.setAccessible(true);
        System.out.println("private method result: " + bonus.invoke(emp, 10));

        section("3. Mini JUnit: run every method annotated with @Test");
        runTests(CalculatorTests.class);

        section("4. Mini validator: @NotBlank and @Min on fields");
        System.out.println("valid user   -> " + validate(new SignupForm("asha", "asha@mail.com", 25)));
        System.out.println("invalid user -> " + validate(new SignupForm("  ", "x@y.z", 15)));

        section("5. Mini dependency injection container (how @Autowired works)");
        Container container = new Container(List.of(ConsoleLogger.class, UserService.class, UserController.class));
        UserController controller = container.get(UserController.class);
        controller.register("ravi");
    }

    // ---------- 3. test runner ----------
    static void runTests(Class<?> testClass) throws Exception {
        Object instance = testClass.getDeclaredConstructor().newInstance();
        int passed = 0, failed = 0;
        List<Method> methods = new ArrayList<>(Arrays.asList(testClass.getDeclaredMethods()));
        methods.sort(Comparator.comparing(Method::getName));
        for (Method m : methods) {
            Test t = m.getAnnotation(Test.class);
            if (t == null) continue;                      // not a test
            String name = t.value().isEmpty() ? m.getName() : t.value();
            try {
                m.invoke(instance);
                passed++;
                System.out.println("  PASS  " + name);
            } catch (InvocationTargetException e) {
                failed++;
                System.out.println("  FAIL  " + name + " -> " + e.getCause().getMessage());
            }
        }
        System.out.println("  " + passed + " passed, " + failed + " failed");
    }

    // ---------- 4. validator ----------
    static List<String> validate(Object obj) throws IllegalAccessException {
        List<String> errors = new ArrayList<>();
        for (Field f : obj.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            Object value = f.get(obj);
            NotBlank nb = f.getAnnotation(NotBlank.class);
            if (nb != null && (value == null || value.toString().isBlank())) errors.add(f.getName() + ": " + nb.message());
            Min min = f.getAnnotation(Min.class);
            if (min != null && ((Integer) value) < min.value()) errors.add(f.getName() + ": must be >= " + min.value());
        }
        return errors.isEmpty() ? List.of("OK") : errors;
    }

    static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}

// ================= annotations =================
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Test {
    String value() default "";               // 'value' can be passed without a name: @Test("...")
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface NotBlank {
    String message() default "must not be blank";
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Min {
    int value();
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface Component { }

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Inject { }

// ================= sample classes =================
class Person { }

final class Employee extends Person {
    private final String name;
    private double salary;

    Employee(String name, double salary) {
        this.name = name;
        this.salary = salary;
    }

    private double secretBonus(int percent) {
        return salary * percent / 100;
    }

    @Override
    public String toString() {
        return "Employee[" + name + ", " + salary + "]";
    }
}

class CalculatorTests {
    @Test("2 + 2 equals 4")
    void addition() {
        check(2 + 2 == 4, "math is broken");
    }

    @Test
    void divisionByZeroThrows() {
        try {
            int x = 1 / 0;
            check(false, "expected ArithmeticException");
        } catch (ArithmeticException expected) {
        }
    }

    @Test("deliberately failing test")
    void failing() {
        check("java".length() == 5, "expected length 5 but was " + "java".length());
    }

    void helperNotATest() {
        throw new IllegalStateException("should never run");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}

class SignupForm {
    @NotBlank
    private final String username;
    @NotBlank(message = "email is required")
    private final String email;
    @Min(18)
    private final int age;

    SignupForm(String username, String email, int age) {
        this.username = username;
        this.email = email;
        this.age = age;
    }
}

// ================= mini DI container =================
interface Logger {
    void log(String msg);
}

@Component
class ConsoleLogger implements Logger {
    public void log(String msg) {
        System.out.println("  [LOG] " + msg);
    }
}

@Component
class UserService {
    @Inject
    private Logger logger;                   // interface type: the container finds the implementation

    void create(String user) {
        logger.log("user '" + user + "' created");
    }
}

@Component
class UserController {
    @Inject
    private UserService service;

    void register(String user) {
        System.out.println("  controller received register(" + user + ")");
        service.create(user);
    }
}

class Container {
    private final Map<Class<?>, Object> beans = new HashMap<>();

    Container(List<Class<?>> classes) throws Exception {
        for (Class<?> c : classes) {                          // 1. instantiate every @Component
            if (c.isAnnotationPresent(Component.class)) {
                Constructor<?> ctor = c.getDeclaredConstructor();
                ctor.setAccessible(true);
                beans.put(c, ctor.newInstance());
                System.out.println("  created bean " + c.getSimpleName());
            }
        }
        for (Object bean : beans.values()) {                  // 2. inject @Inject fields by type
            for (Field f : bean.getClass().getDeclaredFields()) {
                if (f.isAnnotationPresent(Inject.class)) {
                    Object dependency = findByType(f.getType());
                    f.setAccessible(true);
                    f.set(bean, dependency);
                    System.out.println("  injected " + dependency.getClass().getSimpleName() + " into "
                            + bean.getClass().getSimpleName() + "." + f.getName());
                }
            }
        }
    }

    private Object findByType(Class<?> type) {
        return beans.values().stream().filter(type::isInstance).findFirst()
                .orElseThrow(() -> new IllegalStateException("no bean of type " + type.getSimpleName()));
    }

    <T> T get(Class<T> type) {
        return type.cast(findByType(type));
    }
}
