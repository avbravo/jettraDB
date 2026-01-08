package io.jettra.driver.repository;

import java.util.List;
import java.util.Optional;

public interface Repository<T, K> {
    T save(T entity);
    T update(T entity);
    Optional<T> findById(K id);
    List<T> findAll();
    void delete(T entity);
    void deleteById(K id);
    Long count();
}
