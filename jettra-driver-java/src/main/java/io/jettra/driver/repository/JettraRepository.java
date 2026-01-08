package io.jettra.driver.repository;

import io.jettra.driver.JettraClient;
import io.jettra.driver.annotations.Entity;
import io.jettra.driver.annotations.Id;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JettraRepository<T, K> implements Repository<T, K> {
    private static final Logger LOG = Logger.getLogger(JettraRepository.class.getName());
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    
    private final JettraClient client;
    private final Class<T> entityClass;
    private final String collection;

    public JettraRepository(JettraClient client, Class<T> entityClass) {
        this.client = client;
        this.entityClass = entityClass;
        this.collection = resolveCollectionName(entityClass);
    }

    private String resolveCollectionName(Class<T> clazz) {
        if (clazz.isAnnotationPresent(Entity.class)) {
            Entity entity = clazz.getAnnotation(Entity.class);
            if (!entity.collection().isEmpty()) {
                return entity.collection();
            }
        }
        return clazz.getSimpleName().toLowerCase();
    }

    @Override
    public T save(T entity) {
        LOG.info("Saving entity to " + collection);
        return client.save(collection, entity)
                .onItem().transform(v -> entity)
                .await().atMost(TIMEOUT);
    }

    @Override
    public T update(T entity) {
        LOG.info("Updating entity in " + collection);
        return client.save(collection, entity)
                .onItem().transform(v -> entity)
                .await().atMost(TIMEOUT);
    }

    @Override
    public Optional<T> findById(K id) {
        return client.findById(collection, id.toString())
                .onItem().transform(obj -> {
                    if (obj == null) return Optional.<T>empty();
                    return Optional.ofNullable((T) obj);
                })
                .await().atMost(TIMEOUT);
    }

    @Override
    public List<T> findAll() {
        LOG.warning("findAll not fully implemented in JettraClient yet");
        return List.of();
    }

    @Override
    public void delete(T entity) {
        K id = getIdValue(entity);
        if (id != null) {
            deleteById(id);
        }
    }

    @Override
    public void deleteById(K id) {
        LOG.info("Deleting " + id + " from " + collection);
        // Implement synchronous deleteById if needed
    }

    @Override
    public Long count() {
        LOG.info("Counting records in " + collection);
        return 0L;
    }

    protected K getIdValue(T entity) {
        if (entityClass.isRecord()) {
            for (java.lang.reflect.RecordComponent component : entityClass.getRecordComponents()) {
                if (component.isAnnotationPresent(Id.class) || component.getAccessor().isAnnotationPresent(Id.class)) {
                    try {
                        return (K) component.getAccessor().invoke(entity);
                    } catch (Exception e) {
                        LOG.log(Level.SEVERE, "Could not access ID from record component", e);
                    }
                }
            }
        }
        
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                try {
                    field.setAccessible(true);
                    return (K) field.get(entity);
                } catch (IllegalAccessException e) {
                    LOG.log(Level.SEVERE, "Could not access ID field", e);
                }
            }
        }
        return null;
    }
}
