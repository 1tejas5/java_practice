package com.minibank.event;

import com.minibank.model.Account;
import com.minibank.model.Transaction;
import com.minibank.util.Money;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * CONCEPTS: an Observer implementation; CopyOnWriteArrayList because notifications may arrive
 * from many threads at once (see the concurrency simulation).
 */
public class FraudDetector implements TransactionListener {

    private final BigDecimal threshold;
    private final List<String> alerts = new CopyOnWriteArrayList<>();
    private final boolean printAlerts;

    public FraudDetector(BigDecimal threshold, boolean printAlerts) {
        this.threshold = threshold;
        this.printAlerts = printAlerts;
    }

    @Override
    public void onTransaction(Account account, Transaction tx) {
        if (tx.amount().compareTo(threshold) >= 0) {
            String alert = "[FRAUD-CHECK] " + tx.type() + " of " + Money.format(tx.amount()) + " on " + account.id()
                    + " (" + account.owner().name() + ")";
            alerts.add(alert);
            if (printAlerts) System.out.println("   " + alert);
        }
    }

    public List<String> alerts() {
        return List.copyOf(alerts);
    }
}
