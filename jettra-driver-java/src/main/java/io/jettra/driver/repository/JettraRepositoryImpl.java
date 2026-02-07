package io.jettra.driver.repository;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

import io.jettra.driver.JettraReactiveClient;
import io.jettra.driver.annotation.Entity;
import io.jettra.driver.annotation.Id;
import io.smallrye.mutiny.Uni;

public class JettraRepositoryImpl<T, ID> implements JettraRepository<T, ID> {
    protected final JettraReactiveClient client;
    protected final Class<T> entityClass;
    protected final String collection;

    public JettraRepositoryImpl(JettraReactiveClient client, Class<T> entityClass) {
        this.client = client;
        this.entityClass = entityClass;
        Entity entityAnn = entityClass.getAnnotation(Entity.class);
        this.collection = (entityAnn != null && !entityAnn.collection().isEmpty()) 
            ? entityAnn.collection() 
            : entityClass.getSimpleName().toLowerCase();
    }

    @Override
    public Uni<T> save(T entity) {
        return client.save(collection, entity).onItem().transform(v -> entity);
    }

    @Override
    public Uni<T> update(T entity) {
        return client.save(collection, getEntityId(entity), entity).onItem().transform(v -> entity);
    }

    @Override
    public Uni<Void> delete(T entity) {
        return client.delete(collection, getEntityId(entity));
    }

    @Override
    public Uni<Void> deleteById(ID id) {
        return client.delete(collection, id.toString());
    }

    @Override
    public Uni<T> findById(ID id) {
        return client.findById(collection, id.toString()).onItem().transform(obj -> {
            // In a real impl, we'd map JSON back to T
            return (T) obj; 
        });
    }

    @Override
    public Uni<List<T>> findAll() {
        return findAll(0, 100);
    }

    @Override
    public Uni<List<T>> findAll(int offset, int limit) {
        return client.find(collection, "{}", offset, limit).onItem().transform(list -> (List<T>) list);
    }

    @Override
    public Uni<Void> saveMany(List<T> entities) {
        return client.insertMany(collection, (List<Object>) entities);
    }

    @Override
    public Uni<Void> updateMany(List<T> entities) {
        List<Uni<Void>> unis = entities.stream().map(this::delete).collect(Collectors.toList());
        return Uni.combine().all().unis(unis).discardItems().onItem().transformToUni(v -> saveMany(entities));
    }

    @Override
    public Uni<Void> deleteMany(List<T> entities) {
        List<Uni<Void>> unis = entities.stream().map(this::delete).collect(Collectors.toList());
        return Uni.combine().all().unis(unis).discardItems();
    }

    @Override
    public Uni<Long> count() {
        return client.count(collection);
    }

    @Override
    public Uni<Long> count(String query) {
        return client.count(collection, query);
    }

    @Override
    public Uni<Double> sum(String field) {
        return sum(field, "{}");
    }

    @Override
    public Uni<Double> sum(String field, String query) {
        return client.sum(collection, field, query);
    }

    @Override
    public Uni<Double> avg(String field) {
        return avg(field, "{}");
    }

    @Override
    public Uni<Double> avg(String field, String query) {
        return client.avg(collection, field, query);
    }

    @Override
    public Uni<Double> min(String field) {
        return client.min(collection, field, "{}");
    }

    @Override
    public Uni<Double> max(String field) {
        return client.max(collection, field, "{}");
    }

    private String getEntityId(T entity) {
        try {
            for (Field field : entityClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class)) {
                    field.setAccessible(true);
                    Object val = field.get(entity);
                    return val != null ? val.toString() : null;
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
