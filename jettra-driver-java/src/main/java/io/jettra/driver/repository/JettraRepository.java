package io.jettra.driver.repository;

import java.util.List;

import io.smallrye.mutiny.Uni;

public interface JettraRepository<T, ID> {
    Uni<T> save(T entity);
    Uni<T> update(T entity);
    Uni<Void> delete(T entity);
    Uni<Void> deleteById(ID id);
    Uni<T> findById(ID id);
    Uni<List<T>> findAll();
    Uni<List<T>> findAll(int offset, int limit);
    Uni<Void> saveMany(List<T> entities);
    Uni<Void> updateMany(List<T> entities);
    Uni<Void> deleteMany(List<T> entities);
    
    // Aggregations
    Uni<Long> count();
    Uni<Long> count(String query);
    Uni<Double> sum(String field);
    Uni<Double> sum(String field, String query);
    Uni<Double> avg(String field);
    Uni<Double> avg(String field, String query);
    Uni<Double> min(String field);
    Uni<Double> max(String field);
}
