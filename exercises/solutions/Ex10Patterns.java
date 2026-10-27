import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/*
 * SOLUTIONS - Exercises section 10: Design patterns
 * RUN: java exercises/solutions/Ex10Patterns.java
 */
public class Ex10Patterns {

    public static void main(String[] args) throws Exception {
        // 10.1 Builder
        HttpRequestSpec req = HttpRequestSpec.builder("https://api.bank.com/accounts")
                .method("POST").header("Auth", "token").timeout(Duration.ofSeconds(5)).build();
        check("10.1 builder", "POST https://api.bank.com/accounts {Auth=token} PT5S", req.toString());
        check("10.1 defaults", "GET http://x.io {} PT30S", HttpRequestSpec.builder("http://x.io").build().toString());
        check("10.1 validation", "IllegalStateException", attempt(() -> HttpRequestSpec.builder("ftp://x").build()));

        // 10.2 Strategy
        Cart cart = new Cart(500, 3);
        check("10.2 standard", 50.0, cart.shipping(new StandardShipping()));
        check("10.2 express", 150.0, cart.shipping(new ExpressShipping()));
        check("10.2 free (lambda)", 0.0, new Cart(1200, 3).shipping(c -> c.value() > 999 ? 0 : 50));

        // 10.3 Observer
        StockTicker ticker = new StockTicker();
        AlertListener alerts = new AlertListener();
        ticker.subscribe(alerts);
        ticker.update("INFY", 1500);
        ticker.update("INFY", 1480);     // -1.3%, no alert
        ticker.update("INFY", 1380);     // -6.8%, alert
        check("10.3 alerts", List.of("INFY dropped 6.8% to 1380.0"), alerts.messages);

        // 10.4 Decorator
        Text t = new Bold(new Italic(new Plain("java")));
        check("10.4 bold(italic)", "**_java_**", t.render());
        check("10.4 upper(bold)", "**JAVA**", new Upper(new Bold(new Plain("java"))).render());

        // 10.5 Singleton
        Set<Long> ids = ConcurrentHashMap.newKeySet();
        try (ExecutorService pool = Executors.newFixedThreadPool(16)) {
            for (int i = 0; i < 1000; i++) pool.submit(() -> ids.add(IdGenerator.getInstance().nextId()));
        }
        check("10.5 1000 unique ids", 1000, ids.size());
        check("10.5 same instance", true, IdGenerator.getInstance() == IdGenerator.getInstance());
        summary();
    }

    // ============ 10.1 Builder ============
    static final class HttpRequestSpec {
        private final String url, method;
        private final Map<String, String> headers;
        private final Duration timeout;

        private HttpRequestSpec(Builder b) {
            url = b.url;
            method = b.method;
            headers = Map.copyOf(b.headers);
            timeout = b.timeout;
        }

        static Builder builder(String url) {
            return new Builder(url);
        }

        @Override
        public String toString() {
            return method + " " + url + " " + headers + " " + timeout;
        }

        static final class Builder {
            private final String url;
            private String method = "GET";
            private final Map<String, String> headers = new LinkedHashMap<>();
            private Duration timeout = Duration.ofSeconds(30);

            private Builder(String url) { this.url = url; }

            Builder method(String m) { method = m; return this; }

            Builder header(String k, String v) { headers.put(k, v); return this; }

            Builder timeout(Duration d) { timeout = d; return this; }

            HttpRequestSpec build() {
                if (url == null || !url.startsWith("http")) throw new IllegalStateException("url must start with http");
                return new HttpRequestSpec(this);
            }
        }
    }

    // ============ 10.2 Strategy ============
    record Cart(double value, double weightKg) {
        double shipping(ShippingStrategy s) { return s.cost(this); }
    }

    @FunctionalInterface
    interface ShippingStrategy { double cost(Cart cart); }

    static class StandardShipping implements ShippingStrategy {
        public double cost(Cart c) { return 50; }
    }

    static class ExpressShipping implements ShippingStrategy {
        public double cost(Cart c) { return 120 + 10 * c.weightKg(); }
    }

    // ============ 10.3 Observer ============
    interface PriceListener { void onPrice(String symbol, double oldPrice, double newPrice); }

    static class StockTicker {
        private final List<PriceListener> listeners = new ArrayList<>();
        private final Map<String, Double> last = new ConcurrentHashMap<>();

        void subscribe(PriceListener l) { listeners.add(l); }

        void update(String symbol, double price) {
            Double old = last.put(symbol, price);
            if (old != null && old != price) listeners.forEach(l -> l.onPrice(symbol, old, price));
        }
    }

    static class AlertListener implements PriceListener {
        final List<String> messages = new ArrayList<>();

        public void onPrice(String symbol, double oldPrice, double newPrice) {
            double change = (newPrice - oldPrice) / oldPrice * 100;
            if (change < -5) messages.add(String.format("%s dropped %.1f%% to %.1f", symbol, -change, newPrice));
        }
    }

    // ============ 10.4 Decorator ============
    interface Text { String render(); }

    record Plain(String s) implements Text {
        public String render() { return s; }
    }

    record Bold(Text inner) implements Text {
        public String render() { return "**" + inner.render() + "**"; }
    }

    record Italic(Text inner) implements Text {
        public String render() { return "_" + inner.render() + "_"; }
    }

    record Upper(Text inner) implements Text {
        public String render() { return inner.render().toUpperCase(); }
    }

    // ============ 10.5 Singleton (holder idiom) ============
    static final class IdGenerator {
        private final AtomicLong counter = new AtomicLong();

        private IdGenerator() { }

        private static final class Holder {
            static final IdGenerator INSTANCE = new IdGenerator();   // created lazily, thread-safe by class init
        }

        static IdGenerator getInstance() { return Holder.INSTANCE; }

        long nextId() { return counter.incrementAndGet(); }
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
