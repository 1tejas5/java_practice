import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/*
 * SOLUTIONS - Exercises section 2: OOP
 * RUN: java exercises/solutions/Ex02Oop.java
 */
public class Ex02Oop {

    public static void main(String[] args) {
        // 2.1 Shapes
        List<Shape> shapes = List.of(new Circle(1), new Rectangle(2, 3), new Triangle(3, 4, 5));
        double total = 0;
        for (Shape s : shapes) total += s.area();
        check("2.1 total area", "15.14", String.format("%.2f", total));
        check("2.1 triangle perimeter", 12.0, new Triangle(3, 4, 5).perimeter());

        // 2.2 Accounts + polymorphism
        Account savings = new SavingsAccount("S1", 5000);
        Account current = new CurrentAccount("C1", 1000);
        check("2.2 savings keeps min 1000", "rejected", tryWithdraw(savings, 4500));
        check("2.2 savings ok", "ok", tryWithdraw(savings, 4000));
        check("2.2 current overdraft ok", "ok", tryWithdraw(current, 5500));
        check("2.2 current overdraft limit", "rejected", tryWithdraw(current, 1000));
        check("2.2 balances", "1000.0,-4500.0", savings.getBalance() + "," + current.getBalance());

        // 2.3 equals/hashCode
        Set<Employee> set = new HashSet<>(List.of(new Employee(1, "Asha"), new Employee(1, "Asha K"), new Employee(2, "Ravi")));
        check("2.3 HashSet dedupes by id", 2, set.size());

        // 2.4 Immutable Money
        Money a = Money.of("10.50", "INR"), b = Money.of("4.25", "INR");
        Money sum = a.add(b);
        check("2.4 add", "14.75 INR", sum.toString());
        check("2.4 original unchanged", "10.50 INR", a.toString());
        check("2.4 currency mismatch", "IllegalArgumentException", attempt(() -> a.add(Money.of("1", "USD"))));

        // 2.5 Payable
        List<Payable> bills = List.of(new Invoice("Laptop", 2, 40_000), new Salary("Asha", 85_000));
        check("2.5 total payable", 165_000.0, bills.stream().mapToDouble(Payable::amountDue).sum());

        // 2.6 Parking lot
        ParkingLot lot = new ParkingLot(Map.of(SpotSize.SMALL, 1, SpotSize.MEDIUM, 1, SpotSize.LARGE, 1));
        Spot bikeSpot = lot.park(new Bike("MH12-B1"));
        Spot carSpot = lot.park(new Car("MH12-C1"));
        Spot car2Spot = lot.park(new Car("MH12-C2"));          // medium is taken -> uses LARGE
        check("2.6 bike -> SMALL", SpotSize.SMALL, bikeSpot.size());
        check("2.6 car -> MEDIUM", SpotSize.MEDIUM, carSpot.size());
        check("2.6 2nd car -> LARGE", SpotSize.LARGE, car2Spot.size());
        check("2.6 truck when full", "IllegalStateException", attempt(() -> lot.park(new Truck("MH12-T1"))));
        lot.leave(car2Spot);
        check("2.6 truck after leave", SpotSize.LARGE, lot.park(new Truck("MH12-T1")).size());
        summary();
    }

    static String tryWithdraw(Account acc, double amt) {
        try {
            acc.withdraw(amt);
            return "ok";
        } catch (IllegalStateException e) {
            return "rejected";
        }
    }

    // ============ 2.1 ============
    abstract static class Shape {
        abstract double area();

        abstract double perimeter();
    }

    static class Circle extends Shape {
        private final double r;

        Circle(double r) { this.r = r; }

        double area() { return Math.PI * r * r; }

        double perimeter() { return 2 * Math.PI * r; }
    }

    static class Rectangle extends Shape {
        private final double w, h;

        Rectangle(double w, double h) { this.w = w; this.h = h; }

        double area() { return w * h; }

        double perimeter() { return 2 * (w + h); }
    }

    static class Triangle extends Shape {
        private final double a, b, c;

        Triangle(double a, double b, double c) {
            if (a + b <= c || a + c <= b || b + c <= a) throw new IllegalArgumentException("not a triangle");
            this.a = a; this.b = b; this.c = c;
        }

        double area() {                               // Heron's formula
            double s = perimeter() / 2;
            return Math.sqrt(s * (s - a) * (s - b) * (s - c));
        }

        double perimeter() { return a + b + c; }
    }

    // ============ 2.2 ============
    static class Account {
        private final String id;
        protected double balance;

        Account(String id, double opening) {
            this.id = id;
            this.balance = opening;
        }

        double getBalance() { return balance; }

        void deposit(double amt) {
            if (amt <= 0) throw new IllegalArgumentException("deposit must be positive");
            balance += amt;
        }

        void withdraw(double amt) {
            if (amt <= 0) throw new IllegalArgumentException("withdrawal must be positive");
            if (amt > balance) throw new IllegalStateException("insufficient funds in " + id);
            balance -= amt;
        }
    }

    static class SavingsAccount extends Account {
        static final double MIN_BALANCE = 1000;

        SavingsAccount(String id, double opening) { super(id, opening); }

        @Override
        void withdraw(double amt) {
            if (balance - amt < MIN_BALANCE) throw new IllegalStateException("minimum balance " + MIN_BALANCE + " required");
            super.withdraw(amt);
        }
    }

    static class CurrentAccount extends Account {
        static final double OVERDRAFT = 5000;

        CurrentAccount(String id, double opening) { super(id, opening); }

        @Override
        void withdraw(double amt) {
            if (amt <= 0) throw new IllegalArgumentException("withdrawal must be positive");
            if (balance - amt < -OVERDRAFT) throw new IllegalStateException("overdraft limit exceeded");
            balance -= amt;
        }
    }

    // ============ 2.3 ============
    static final class Employee {
        private final int id;
        private final String name;

        Employee(int id, String name) { this.id = id; this.name = name; }

        @Override
        public boolean equals(Object o) {
            return this == o || (o instanceof Employee e && e.id == id);
        }

        @Override
        public int hashCode() { return Integer.hashCode(id); }   // same field as equals

        @Override
        public String toString() { return "Employee(" + id + ", " + name + ")"; }
    }

    // ============ 2.4 ============
    static final class Money {
        private final BigDecimal amount;
        private final String currency;

        private Money(BigDecimal amount, String currency) {
            this.amount = Objects.requireNonNull(amount);
            this.currency = Objects.requireNonNull(currency);
        }

        static Money of(String amount, String currency) { return new Money(new BigDecimal(amount), currency); }

        Money add(Money other) {
            if (!currency.equals(other.currency)) throw new IllegalArgumentException("currency mismatch");
            return new Money(amount.add(other.amount), currency);        // NEW object
        }

        @Override
        public String toString() { return amount.toPlainString() + " " + currency; }
    }

    // ============ 2.5 ============
    interface Payable {
        double amountDue();
    }

    record Invoice(String item, int qty, double unitPrice) implements Payable {
        public double amountDue() { return qty * unitPrice; }
    }

    record Salary(String employee, double monthly) implements Payable {
        public double amountDue() { return monthly; }
    }

    // ============ 2.6 ============
    enum SpotSize { SMALL, MEDIUM, LARGE }

    abstract static class Vehicle {
        private final String plate;

        Vehicle(String plate) { this.plate = plate; }

        abstract SpotSize requiredSize();

        String plate() { return plate; }
    }

    static class Bike extends Vehicle {
        Bike(String p) { super(p); }

        SpotSize requiredSize() { return SpotSize.SMALL; }
    }

    static class Car extends Vehicle {
        Car(String p) { super(p); }

        SpotSize requiredSize() { return SpotSize.MEDIUM; }
    }

    static class Truck extends Vehicle {
        Truck(String p) { super(p); }

        SpotSize requiredSize() { return SpotSize.LARGE; }
    }

    static final class Spot {
        private final int number;
        private final SpotSize size;
        private Vehicle parked;

        Spot(int number, SpotSize size) { this.number = number; this.size = size; }

        SpotSize size() { return size; }

        boolean isFree() { return parked == null; }
    }

    static class ParkingLot {
        private final Map<SpotSize, List<Spot>> spots = new EnumMap<>(SpotSize.class);

        ParkingLot(Map<SpotSize, Integer> capacity) {
            int n = 1;
            for (SpotSize size : SpotSize.values()) {
                List<Spot> list = new ArrayList<>();
                for (int i = 0; i < capacity.getOrDefault(size, 0); i++) list.add(new Spot(n++, size));
                spots.put(size, list);
            }
        }

        Spot park(Vehicle v) {
            for (SpotSize size : SpotSize.values()) {
                if (size.compareTo(v.requiredSize()) < 0) continue;        // too small
                for (Spot s : spots.get(size)) {
                    if (s.isFree()) {
                        s.parked = v;
                        return s;
                    }
                }
            }
            throw new IllegalStateException("no spot for " + v.plate());
        }

        void leave(Spot s) { s.parked = null; }
    }

    // ---------------- test helpers ----------------
    interface Risky { Object run() throws Exception; }

    static String attempt(Risky r) {
        try {
            return String.valueOf(r.run());
        } catch (Exception e) {
            return e.getClass().getSimpleName();
        }
    }

    private static int passed, failed;

    static void check(String name, Object expected, Object actual) {
        boolean ok = Objects.deepEquals(expected, actual);
        if (ok) passed++;
        else failed++;
        System.out.printf("%s %-28s %s%n", ok ? "PASS" : "FAIL", name, ok ? "" : "expected=" + expected + " actual=" + actual);
    }

    static void summary() {
        System.out.printf("%n%d passed, %d failed%n", passed, failed);
    }
}
