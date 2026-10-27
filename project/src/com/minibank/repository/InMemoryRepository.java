package com.minibank.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * CONCEPTS: generic class implementing a generic interface, ConcurrentHashMap (thread-safe map),
 * Function<T, ID> as a STRATEGY for extracting ids (passed as a method reference, e.g. Account::id).
 */
public class InMemoryRepository<T, ID> implements Repository<T, ID> {

    private final Map<ID, T> store = new ConcurrentHashMap<>();
    private final Function<? super T, ? extends ID> idOf;

    public InMemoryRepository(Function<? super T, ? extends ID> idOf) {
        this.idOf = Objects.requireNonNull(idOf);
    }

    @Override
    public T save(T entity) {
        store.put(idOf.apply(entity), entity);
        return entity;
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public boolean deleteById(ID id) {
        return store.remove(id) != null;
    }

    @Override
    public long count() {
        return store.size();          // overrides the default method with an O(1) version
    }
}
