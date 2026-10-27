package com.minibank.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * CONCEPTS: SINGLETON via enum (thread-safe, serialization-safe, reflection-safe),
 * ConcurrentHashMap.computeIfAbsent (atomic per key), AtomicLong (lock-free counter).
 *
 * Usage: IdGenerator.INSTANCE.next("ACC") -> "ACC-1001", "ACC-1002", ...
 */
public enum IdGenerator {
    INSTANCE;

    private static final long START = 1000;
    private final Map<String, AtomicLong> counters = new ConcurrentHashMap<>();

    public String next(String prefix) {
        long n = counters.computeIfAbsent(prefix, p -> new AtomicLong(START)).incrementAndGet();
        return prefix + "-" + n;
    }
}
