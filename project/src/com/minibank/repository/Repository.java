package com.minibank.repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * CONCEPTS: GENERIC interface with two type parameters, Optional return type,
 * a DEFAULT method built on the abstract ones, PECS (Predicate<? super T>).
 *
 * Programming to this interface (Dependency Inversion) means BankService works with an in-memory
 * map today and could work with a JDBC/JPA implementation tomorrow without changes.
 */
public interface Repository<T, ID> {

    T save(T entity);

    Optional<T> findById(ID id);

    List<T> findAll();

    boolean deleteById(ID id);

    default long count() {
        return findAll().size();
    }

    default List<T> findWhere(Predicate<? super T> condition) {
        return findAll().stream().filter(condition).toList();
    }
}
