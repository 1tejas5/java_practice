package com.minibank.sim;

import com.minibank.model.Account;
import com.minibank.service.BankService;
import com.minibank.util.Money;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CONCEPTS: ExecutorService (platform pool OR virtual threads), AtomicInteger counters,
 * ThreadLocalRandom, try-with-resources on an ExecutorService (Java 19+: close() waits for tasks),
 * and an INVARIANT check: concurrent transfers must never create or destroy money.
 */
public final class ConcurrencySimulation {

    public record Result(int transfers, int succeeded, int rejected, BigDecimal totalBefore,
                         BigDecimal totalAfter, long millis, boolean virtualThreads) {

        public boolean moneyConserved() {
            return totalBefore.compareTo(totalAfter) == 0;
        }

        @Override
        public String toString() {
            return String.format("%,d transfers on %s threads in %d ms -> %,d succeeded, %,d rejected (insufficient funds)%n"
                            + "   total before %s | total after %s | money conserved: %s",
                    transfers, virtualThreads ? "VIRTUAL" : "PLATFORM (pool of 8)", millis, succeeded, rejected,
                    Money.format(totalBefore), Money.format(totalAfter), moneyConserved() ? "YES" : "NO !!!");
        }
    }

    private ConcurrencySimulation() { }

    public static Result run(BankService bank, int transfers, boolean useVirtualThreads) {
        List<String> ids = bank.allAccounts().stream().map(Account::id).toList();
        if (ids.size() < 2) throw new IllegalStateException("need at least 2 accounts");
        BigDecimal before = bank.totalMoney();
        AtomicInteger ok = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();

        long start = System.currentTimeMillis();
        try (ExecutorService pool = useVirtualThreads
                ? Executors.newVirtualThreadPerTaskExecutor()
                : Executors.newFixedThreadPool(8)) {
            for (int i = 0; i < transfers; i++) {
                pool.submit(() -> {
                    ThreadLocalRandom rnd = ThreadLocalRandom.current();
                    int a = rnd.nextInt(ids.size());
                    int b = (a + 1 + rnd.nextInt(ids.size() - 1)) % ids.size();   // always a different account
                    String from = ids.get(a), to = ids.get(b);
                    BigDecimal amount = Money.of(1 + rnd.nextInt(2_000));
                    if (bank.tryTransfer(from, to, amount)) ok.incrementAndGet();
                    else rejected.incrementAndGet();
                });
            }
        }                                                                // close() waits for every task
        long millis = System.currentTimeMillis() - start;
        return new Result(transfers, ok.get(), rejected.get(), before, bank.totalMoney(), millis, useVirtualThreads);
    }
}
