import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/*
 * TOPIC: Inter-thread communication
 *   1. Producer-consumer with wait()/notifyAll()   (the classic, low-level way)
 *   2. Producer-consumer with BlockingQueue         (the modern way)
 *   3. Interview classic: print odd/even numbers alternately with two threads
 * RUN:   java code/08_concurrency/ProducerConsumer.java
 */
public class ProducerConsumer {

    public static void main(String[] args) throws InterruptedException {
        section("1. wait/notify bounded buffer (capacity 3)");
        Buffer buffer = new Buffer(3);
        Thread producer = new Thread(() -> {
            for (int i = 1; i <= 6; i++) buffer.produce(i);
        }, "producer");
        Thread consumer = new Thread(() -> {
            for (int i = 1; i <= 6; i++) {
                buffer.consume();
                pause(60);                     // slow consumer -> producer will have to wait
            }
        }, "consumer");
        producer.start();
        consumer.start();
        producer.join();
        consumer.join();

        section("2. BlockingQueue: put() blocks when full, take() blocks when empty");
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(2);
        Thread p = new Thread(() -> {
            try {
                for (String order : new String[]{"order-1", "order-2", "order-3", "order-4", "STOP"}) {
                    queue.put(order);
                    System.out.println("  [producer] put " + order + "  (queue size " + queue.size() + ")");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        Thread c = new Thread(() -> {
            try {
                while (true) {
                    String order = queue.take();
                    if (order.equals("STOP")) break;  // "poison pill" ends the consumer
                    System.out.println("  [consumer] processed " + order);
                    pause(50);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        p.start();
        c.start();
        p.join();
        c.join();

        section("3. Odd/even printer with two threads (1..10)");
        OddEvenPrinter printer = new OddEvenPrinter(10);
        Thread odd = new Thread(() -> printer.print(true), "ODD ");
        Thread even = new Thread(() -> printer.print(false), "EVEN");
        odd.start();
        even.start();
        odd.join();
        even.join();
    }

    static void pause(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}

class Buffer {
    private final Queue<Integer> items = new LinkedList<>();
    private final int capacity;

    Buffer(int capacity) {
        this.capacity = capacity;
    }

    synchronized void produce(int item) {
        try {
            while (items.size() == capacity) {           // WHILE, not if
                System.out.println("  [producer] buffer full, waiting...");
                wait();                                  // releases the lock
            }
            items.add(item);
            System.out.println("  [producer] produced " + item + " -> " + items);
            notifyAll();                                 // wake consumers
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    synchronized int consume() {
        try {
            while (items.isEmpty()) wait();
            int item = items.poll();
            System.out.println("  [consumer] consumed " + item + " -> " + items);
            notifyAll();                                 // wake producers
            return item;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return -1;
        }
    }
}

class OddEvenPrinter {
    private final int max;
    private int current = 1;

    OddEvenPrinter(int max) {
        this.max = max;
    }

    synchronized void print(boolean oddTurn) {
        while (current <= max) {
            boolean isOdd = current % 2 == 1;
            if (isOdd != oddTurn) {                      // not my turn
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                continue;                                // re-check the condition after waking
            }
            System.out.println("  " + Thread.currentThread().getName() + " -> " + current++);
            notifyAll();
        }
    }
}
